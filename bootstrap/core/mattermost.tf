resource "helm_repository" "mattermost" {
  name = "mattermost"
  url = "https://helm.mattermost.com"
}

resource "random_string" "mattermost_admin_password" {
  length = 16
  special = false
}

resource "random_string" "mattermost_mysql_password" {
  length = 16
  special = false
}

data "template_file" "mattermost_config" {
  template = "${file("${path.module}/templates/mattermost-values.yml")}"

  vars = {
    chat_domain = "${var.chat_domain}"
    mysql_password = "${random_string.mattermost_mysql_password.result}"
  }
}

resource "helm_release" "mattermost" {
  depends_on = [
      "helm_release.nginx_ingress", 
  ]

  name       = "mattermost"
  namespace  = "mattermost"
  repository = "${helm_repository.mattermost.name}"
  chart      = "mattermost-team-edition"
  version    = "${var.mattermost_chart_version}"

  values = ["${data.template_file.mattermost_config.rendered}"]

  provisioner "local-exec" {
    command = "sleep 60"
  }

  provisioner "local-exec" {
    command = "${path.module}/scripts/configure-mm.sh ${random_string.mattermost_admin_password.result}"
  }
}