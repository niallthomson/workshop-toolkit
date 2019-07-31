resource "null_resource" "blocker" {

  provisioner "local-exec" {
    command = "echo ${var.blocker_id}"
  }
}