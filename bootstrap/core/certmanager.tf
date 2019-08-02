resource "null_resource" "certmanager_prereqs" {

  depends_on = ["null_resource.blocker"]  

  provisioner "local-exec" {
    command = "kubectl apply -f https://raw.githubusercontent.com/jetstack/cert-manager/release-0.9/deploy/manifests/00-crds.yaml && sleep 5"
  }

  provisioner "local-exec" {
    command = "${path.module}/scripts/apply-yml.sh"

    environment = {
      YML = "${var.cluster_issuers_yml}"
    }
  }
}

resource "helm_repository" "jetstack" {
  name = "jetstack"
  url = "https://charts.jetstack.io"
}

resource "helm_release" "certmanager" {

  depends_on = [
      "null_resource.certmanager_prereqs", 
  ]

  name       = "certmanager"
  namespace  = "certmanager"
  repository = "${helm_repository.jetstack.name}"
  chart      = "cert-manager"
  version    = "v0.8.1"

  provisioner "local-exec" {
    command = "sleep 30"
  }
}