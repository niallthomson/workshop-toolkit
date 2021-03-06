resource "kubernetes_namespace" "workspaces" {
  depends_on  = ["helm_release.nginx_ingress"]

  metadata {
    name = "workspaces"
  }
}

resource "kubernetes_secret" "workspaces_secret" {
  metadata {
    name = "workspaces-secrets"
    namespace = "${kubernetes_namespace.workspaces.metadata.0.name}"
  }

  data = "${var.workspaces_secrets}"

  type = "Opaque"
}

resource "null_resource" "workspaces_wilcard_cert" {
  depends_on = ["helm_release.certmanager"]  

  provisioner "local-exec" {
    command = "${path.module}/scripts/wildcard-cert.sh ${kubernetes_namespace.workspaces.metadata.0.name} ${var.domain_suffix} ${var.acme_dns_provider}"
  }
}

data "template_file" "workspace_urls" {
  template = "https://space${count.index}.${var.domain_suffix}"

  count = "${var.num_workspaces}"
}

data "template_file" "workspace_config" {
  template = "${file("${path.module}/templates/workspace.yml")}"

  vars = {
    id               = "space${count.index}"
    namespace        = "${kubernetes_namespace.workspaces.metadata.0.name}"
    fqdn             = "space${count.index}.${var.domain_suffix}"
    repo             = "${var.workshop_repo}"
    image            = "${var.workspace_container_image_override == "" ? var.workspace_container_image : var.workspace_container_image_override}"
    lifecycle_image  = "${var.workspace_lifecycle_container_image}"
  }

  count = "${var.num_workspaces}"
}

resource "null_resource" "workspaces" {
  depends_on = ["kubernetes_secret.workspaces_secret"]

  provisioner "local-exec" {
    command = "${path.module}/scripts/apply-yml.sh"

    environment = {
      YML = "${element(data.template_file.workspace_config.*.rendered, count.index)}"
    }
  }

  provisioner "local-exec" {
    when    = "destroy"
    command = "${path.module}/scripts/destroy-workspace.sh"

    environment = {
      NAMESPACE = "${kubernetes_namespace.workspaces.metadata.0.name}"
      ID = "space${count.index}"
      IMAGE = "${var.workspace_lifecycle_container_image}"
    }
  }

  count = "${var.num_workspaces}"
}