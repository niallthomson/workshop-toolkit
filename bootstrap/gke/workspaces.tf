resource "google_container_node_pool" "workspace_nodes" {
  provider = "google-beta"

  name       = "workspace-node-pool"
  location   = "${var.zone}"
  cluster    = "${google_container_cluster.default.name}"
  
  autoscaling {
    min_node_count = 1
    max_node_count = 10
  }

  node_config {
    image_type = "UBUNTU"
    machine_type = "${var.cluster_instance_type}"

    labels = {
      workspaces = "true"
    }

    metadata {
      "disable-legacy-endpoints" = "true"
    }

    oauth_scopes = [
      "https://www.googleapis.com/auth/ndev.clouddns.readwrite",
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring",
    ]

    taint = [
      {
        key = "workspace"
        value = "preallocated"
        effect = "NO_SCHEDULE"
      }
    ]
  }
}