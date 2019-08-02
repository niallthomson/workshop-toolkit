resource "kubernetes_service_account" "helm" {
  depends_on = ["null_resource.blocker"]  

  metadata {
    name = "helm"
    namespace = "kube-system"
  }
}

resource "kubernetes_cluster_role_binding" "helm" {
  metadata {
    name = "${kubernetes_service_account.helm.metadata.0.name}"
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind = "ClusterRole"
    name = "cluster-admin"
  }
  subject {
    kind = "ServiceAccount"
    name = "${kubernetes_service_account.helm.metadata.0.name}"
    namespace = "kube-system"

    # https://github.com/terraform-providers/terraform-provider-kubernetes/issues/204
    api_group = ""
  }
}

resource "kubernetes_secret" "helm" {
  depends_on = ["kubernetes_service_account.helm"]

  metadata {
    name = "helm-token"
    namespace = "kube-system"
    annotations = {
      "kubernetes.io/service-account.name" = "helm"
    }
  }

  type = "kubernetes.io/service-account-token"
}