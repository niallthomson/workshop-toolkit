# Google Project ID


variable "helm_version" {
  type = "string"
  default = "v2.13.1"
}

variable "mattermost_chart_version" {
  default = "3.5.1"
}

variable "mattermost_bot_image" {
  default = "latest"
}

variable "workshop_name" {

}

variable "workshop_repo" {
  default = ""
}

variable "workshop_creation_timeout" {
  default = "240"
}

variable "workspaces_secrets" {
  default = { 

  }
}

variable "internal_comms" {
  default = false
}

variable "chat_domain" {
  
}

variable "oauth_domain" {

}

variable "domain_suffix" {

}

variable "nginx_ingress_ip" {

}

variable "cluster_issuers_yml" {
  default = ""
}

variable "blocker_id" {
  
}