locals {
  chat_internal_domain = "mattermost-team-edition.mattermost.svc.cluster.local:8065"
  chat_internal_url = "http://mattermost-team-edition.mattermost.svc.cluster.local:8065"
  chat_domain = "${var.internal_comms ? local.chat_internal_domain : var.chat_domain}"
  chat_url = "${var.internal_comms ? local.chat_internal_url : "https://${local.chat_domain}"}"
}

resource "random_string" "workshop_access_code" {
  length = 6
  special = false
}