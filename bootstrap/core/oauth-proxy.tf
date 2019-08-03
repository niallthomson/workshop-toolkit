resource "kubernetes_deployment" "oauth_proxy" {
  metadata {
    name = "oauth-proxy"
    namespace = "mattermost"

    labels = {
      app = "oauth-proxy"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "oauth-proxy"
      }
    }

    template {
      metadata {
        labels = {
          app = "oauth-proxy"
        }
      }

      spec {
        container {
          image = "nthomsonpivotal/node-oauth-proxy"
          image_pull_policy = "Always"
          name  = "proxy"

          readiness_probe {
            http_get {
              path = "/health"
              port = 3000
            }

            initial_delay_seconds = 5
            period_seconds        = 5
          }

          port {
            container_port = 3000
          }

          env_from = {
            secret_ref {
              name = "${kubernetes_secret.oauth_proxy_configuration.metadata.0.name}"
            }
          }
        }
      }
    }
  }

  depends_on = ["kubernetes_pod.mattermost_bot"]
}

resource "kubernetes_service" "oauth_proxy" {
  metadata {
    name = "oauth-proxy"
    namespace = "mattermost"
  }
  spec {
    selector = {
      app = "${kubernetes_deployment.oauth_proxy.metadata.0.name}"
    }
    port {
      port        = 3000
      target_port = 3000
      protocol    = "TCP"
      name        = "http"
    }

    type = "ClusterIP"
  }
}

resource "kubernetes_ingress" "oauth_proxy" {
  metadata {
    name = "oauth-proxy-ingress"
    namespace = "mattermost"
    annotations = {
      "kubernetes.io/ingress.class" = "nginx"
      "certmanager.k8s.io/acme-challenge-type" = "dns01"
      "certmanager.k8s.io/acme-dns01-provider" = "${var.acme_dns_provider}"
      "certmanager.k8s.io/cluster-issuer" = "letsencrypt-prod"
    }
  }

  spec {
    rule {
      host = "${var.oauth_domain}"
      http {
        path {
          backend {
            service_name = "${kubernetes_service.oauth_proxy.metadata.0.name}"
            service_port = 3000
          }
        }
      }
    }

    tls {
      hosts = ["${var.oauth_domain}"]
      secret_name = "oauth-proxy-tls-secret"
    }
  }
}

resource "kubernetes_secret" "oauth_proxy_configuration" {
  metadata {
    name = "proxy-configuration"
    namespace = "mattermost"
  }

  data = {
    TOKEN_HOST = "${local.chat_url}"
    TOKEN_PATH = "/oauth/access_token"
    AUTHORIZE_PATH = "/oauth/authorize"
    USERINFO_URL = "${local.chat_url}/api/v4/users/me"
    MATTERMOST_HOST = "${local.chat_url}"
    MATTERMOST_ADMIN_USER = "admin"
    MATTERMOST_ADMIN_PASSWORD = "${random_string.mattermost_admin_password.result}"
    MATTERMOST_APP_NAME = "Workshop Bot"
  }

  type = "Opaque"

  depends_on = [
    "helm_release.mattermost", 
  ]
}