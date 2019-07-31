resource "kubernetes_secret" "mattermost_tls" {
  metadata {
    name = "mattermost-tls-secret"
  }

  data = {
    username = "admin"
    password = "P4ssw0rd"
  }

  type = "kubernetes.io/basic-auth"
}