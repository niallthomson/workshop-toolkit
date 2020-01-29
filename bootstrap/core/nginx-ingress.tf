data "template_file" "nginx_config" {
  template = "${file("${path.module}/templates/nginx-ingress-values.yml")}"

  vars = {
    lb_ip = "${var.nginx_ingress_ip}"
  }
}

resource "helm_release" "nginx_ingress" {

  depends_on = [
      "helm_release.certmanager", 
  ]

  name       = "nginx-ingress2"
  namespace  = "nginx-ingress"
  chart      = "stable/nginx-ingress"
  version    = "1.29.5"

  values = ["${data.template_file.nginx_config.rendered}"]

  provisioner "local-exec" {
    command = "sleep 30"
  }
}