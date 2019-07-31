resource "google_dns_managed_zone" "workshop_zone" {
  name        = "${var.cluster_name}"
  dns_name    = "${var.subdomain}.${data.google_dns_managed_zone.root_zone.dns_name}"
}

locals {
  domain_suffix = "${replace(google_dns_managed_zone.workshop_zone.dns_name, "/[.]$/", "")}"
}

resource "google_dns_record_set" "ns_record" {
  managed_zone = "${data.google_dns_managed_zone.root_zone.name}"
  name = "${var.subdomain}.${data.google_dns_managed_zone.root_zone.dns_name}"
  rrdatas = [
    "${google_dns_managed_zone.workshop_zone.name_servers}",
  ]
  ttl = 30
  type = "NS"
}

resource "google_dns_record_set" "bot" {
  name = "${google_dns_managed_zone.workshop_zone.dns_name}"
  managed_zone = "${google_dns_managed_zone.workshop_zone.name}"
  type = "A"
  ttl  = 30

  rrdatas = ["${google_compute_address.nginx_ingress.address}"]
}

resource "google_dns_record_set" "mattermost" {
  name = "chat.${google_dns_managed_zone.workshop_zone.dns_name}"
  managed_zone = "${google_dns_managed_zone.workshop_zone.name}"
  type = "A"
  ttl  = 30

  rrdatas = ["${google_compute_address.nginx_ingress.address}"]
}

resource "google_dns_record_set" "workspaces" {
  name = "*.${google_dns_managed_zone.workshop_zone.dns_name}"
  managed_zone = "${google_dns_managed_zone.workshop_zone.name}"
  type = "A"
  ttl  = 30

  rrdatas = ["${google_compute_address.nginx_ingress.address}"]
}

resource "google_dns_record_set" "oauth_proxy" {
  name = "oauth.${google_dns_managed_zone.workshop_zone.dns_name}"
  managed_zone = "${google_dns_managed_zone.workshop_zone.name}"
  type = "A"
  ttl  = 30

  rrdatas = ["${google_compute_address.nginx_ingress.address}"]
}