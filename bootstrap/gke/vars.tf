variable "project_id" {
  type = "string"
}

# Email for certificate generation
variable "email" {
  type = "string"
}

# GKE details
variable "region" {
  type = "string"
  default = "us-central1"
}
variable "zone" {
  type = "string"
  default = "us-central1-b"
}
variable "cluster_name" {
  type = "string"
}

variable "cluster_instance_type" {
  default = "n1-highmem-2"
}

variable "root_zone_name" {
  type = "string"
}

variable "subdomain" {

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
  default = { }
}

variable "num_workspaces" {
  default = 0
}

variable "workspace_container_image_override" {
  default = ""
}