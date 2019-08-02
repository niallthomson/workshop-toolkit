provider "helm" {
  install_tiller = true
  tiller_image = "gcr.io/kubernetes-helm/tiller:${var.helm_version}"
  service_account = "${kubernetes_cluster_role_binding.helm.metadata.0.name}"

  version = "~> 0.10.0"
}