data "template_file" "cluster_issuers" {
  template = "${file("${path.module}/templates/cluster-issuers.yml")}"

  vars = {
    project_id = "${var.project_id}"
  }
}

module "core" {
  source = "../core"

  workshop_name = "${var.workshop_name}"
  workshop_repo = "${var.workshop_repo}"

  workshop_creation_timeout = "${var.workshop_creation_timeout}"

  workspaces_secrets = "${var.workspaces_secrets}"

  nginx_ingress_ip = "${google_compute_address.nginx_ingress.address}"

  chat_domain = "${replace(google_dns_record_set.mattermost.name, "/[.]$/", "")}"
  oauth_domain = "${replace(google_dns_record_set.oauth_proxy.name, "/[.]$/", "")}"

  domain_suffix = "${replace(google_dns_managed_zone.workshop_zone.dns_name, "/[.]$/", "")}"

  cluster_issuers_yml = "${data.template_file.cluster_issuers.rendered}"
  acme_dns_provider   = "clouddns"

  blocker_id = "${null_resource.blocker.id}"

  num_workspaces = "${var.num_workspaces}"
}

resource "null_resource" "blocker" {
  depends_on = ["google_container_cluster.default", "google_container_node_pool.workspace_nodes"]
}