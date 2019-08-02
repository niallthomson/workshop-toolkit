provider "google" {
  project = "${var.project_id}"
  region  = "${var.region}"

  version = "~> 2.12.0"
}

provider "google-beta" {
  project = "${var.project_id}"
  region  = "${var.region}"

  version = "~> 2.12.0"
}

provider "acme" {
  server_url = "https://acme-v02.api.letsencrypt.org/directory"

  version = "~> 1.3.0"
}

provider "kubernetes" {
  host                   = "${google_container_cluster.default.endpoint}"
  token                  = "${data.google_client_config.current.access_token}"
  client_certificate     = "${base64decode(google_container_cluster.default.master_auth.0.client_certificate)}"
  client_key             = "${base64decode(google_container_cluster.default.master_auth.0.client_key)}"
  cluster_ca_certificate = "${base64decode(google_container_cluster.default.master_auth.0.cluster_ca_certificate)}"

  version = "~> 1.8.0"
}