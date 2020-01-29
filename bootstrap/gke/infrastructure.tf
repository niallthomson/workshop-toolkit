data "google_container_engine_versions" "default" {
  zone = "${var.zone}"
}

data "google_client_config" "current" {}

resource "google_container_cluster" "default" {
  name = "${var.cluster_name}"
  zone = "${var.zone}"
  initial_node_count = 1
  min_master_version = "${data.google_container_engine_versions.default.latest_master_version}"

  node_config {
    image_type = "UBUNTU"
    machine_type = "n1-standard-2"

    metadata {
      "disable-legacy-endpoints" = "true"
    }

    oauth_scopes = [
      "https://www.googleapis.com/auth/ndev.clouddns.readwrite",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring",
    ]
  }

  // Wait for the GCE LB controller to cleanup the resources.
  provisioner "local-exec" {
    when    = "destroy"
    command = "sleep 90"
  }

  provisioner "local-exec" {
    when = "create"
    command = "gcloud container clusters get-credentials ${var.cluster_name} --zone ${var.zone} --project ${var.project_id}"
  }
}

data "google_dns_managed_zone" "root_zone" {
  name = "${var.root_zone_name}"
}

resource "google_compute_address" "nginx_ingress" {
  name = "workshop-${var.cluster_name}-nginx-ingress"
}