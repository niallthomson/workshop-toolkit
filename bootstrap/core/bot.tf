resource "kubernetes_namespace" "bot_workspace" {
  metadata {
    name = "bot-workspace"
  }
}

resource "null_resource" "bot_wilcard_cert" {
  depends_on = ["helm_release.certmanager"]  

  provisioner "local-exec" {
    command = "${path.module}/scripts/wildcard-cert.sh ${kubernetes_namespace.bot_workspace.metadata.0.name} ${var.domain_suffix}"
  }
}

resource "kubernetes_secret" "bot_workspace_secrets" {
  metadata {
    name = "coder-secrets"
    namespace = "${kubernetes_namespace.bot_workspace.metadata.0.name}"
  }

  data = "${var.workspaces_secrets}"

  type = "Opaque"
}

resource "kubernetes_service_account" "bot" {
  metadata {
    name = "bot"
    namespace = "${kubernetes_namespace.bot_workspace.metadata.0.name}"
  }
}

resource "kubernetes_cluster_role_binding" "bot" {
  metadata {
    name = "${kubernetes_service_account.bot.metadata.0.name}"
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind = "ClusterRole"
    name = "cluster-admin"
  }
  subject {
    kind = "ServiceAccount"
    name = "${kubernetes_service_account.bot.metadata.0.name}"
    namespace = "${kubernetes_namespace.bot_workspace.metadata.0.name}"

    api_group = ""
  }
}

resource "kubernetes_pod" "mattermost_bot" {
  metadata {
    name = "mattermost-bot"
    namespace = "${kubernetes_namespace.bot_workspace.metadata.0.name}"
    labels = {
      app = "mattermost-bot"
    }
  }

  spec {
    container {
      image = "nthomsonpivotal/workshop-mm-bot:${var.mattermost_bot_image}"
      image_pull_policy = "Always"
      name  = "bot"

      readiness_probe {
        http_get {
          path = "/health"
          port = 8080
        }

        initial_delay_seconds = 5
        period_seconds        = 5
      }

      env_from = {
        secret_ref {
          name = "${kubernetes_secret.bot_configuration.metadata.0.name}"
        }
      }
    } 

    automount_service_account_token = true
    service_account_name = "${kubernetes_service_account.bot.metadata.0.name}"   
  }
}

resource "kubernetes_service" "mattermost_bot" {
  metadata {
    name = "mattermost-bot"
    namespace = "${kubernetes_namespace.bot_workspace.metadata.0.name}"
  }
  spec {
    selector = {
      app = "${kubernetes_pod.mattermost_bot.metadata.0.name}"
    }
    port {
      port        = 8080
      target_port = 8080
      protocol    = "TCP"
      name        = "http"
    }

    type = "ClusterIP"
  }
}

resource "kubernetes_ingress" "mattermost_bot" {
  metadata {
    name = "mattermost-bot"
    namespace = "${kubernetes_namespace.bot_workspace.metadata.0.name}"
    annotations = {
      "kubernetes.io/ingress.class" = "nginx"
      "certmanager.k8s.io/acme-challenge-type" = "dns01"
      "certmanager.k8s.io/acme-dns01-provider" = "${var.acme_dns_provider}"
      "certmanager.k8s.io/cluster-issuer" = "letsencrypt-prod"
    }
  }

  spec {
    rule {
      host = "${var.domain_suffix}"
      http {
        path {
          backend {
            service_name = "${kubernetes_service.mattermost_bot.metadata.0.name}"
            service_port = 8080
          }
        }
      }
    }

    tls {
      hosts = ["${var.domain_suffix}"]
      secret_name = "mattermost-bot-tls-secret"
    }
  }
}

resource "kubernetes_secret" "bot_configuration" {
  metadata {
    name = "bot-configuration"
    namespace = "${kubernetes_namespace.bot_workspace.metadata.0.name}"
  }

  data = {
    MATTERMOST_HOST = "${local.chat_domain}"
    MATTERMOST_SECURE = "${var.internal_comms ? "false" : "true"}"
    MATTERMOST_ADMIN_USER = "admin"
    MATTERMOST_ADMIN_PASSWORD = "${random_string.mattermost_admin_password.result}"
    MATTERMOST_OPERATOR_USER = "admin"
    MATTERMOST_BOT_CALLBACK_URL = "https://${var.oauth_domain}/oauth/callback"
    WORKSHOP_NAME = "${var.workshop_name}"
    WORKSHOP_DNSSUFFIX = "${var.domain_suffix}"
    WORKSHOP_GITREPO = "${var.workshop_repo}"
    WORKSHOP_ACCESSCODE = "${random_string.workshop_access_code.result}"
    WORKSHOP_CREATION_TIMEOUT = "${var.workshop_creation_timeout}"
    KUBERNETES_WORKSPACE = "${kubernetes_namespace.bot_workspace.metadata.0.name}"
  }

  type = "Opaque"

  depends_on = [
    "helm_release.mattermost", 
  ]
}