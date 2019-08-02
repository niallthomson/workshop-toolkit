output "workshop_url" {
  value = "https://${var.domain_suffix}"
}

output "workshop_info_info_url" {
  value = "https://${var.domain_suffix}/info"
}

output "workshop_access_code" {
  value = "${random_string.workshop_access_code.result}"
}

output "mattermost_url" {
  value = "https://${var.chat_domain}"
}

output "mattermost_admin_password" {
  value = "${random_string.mattermost_admin_password.result}"
}

output "workspace_urls" {
  value = "${formatlist("https://space%s.%s/", random_string.workspace_id.*.result, var.domain_suffix)}"
}