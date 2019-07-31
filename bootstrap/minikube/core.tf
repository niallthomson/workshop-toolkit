module "core" {
    source = "../core"

    workshop_repo = ""

    workspaces_secrets = {

    }

    nginx_ingress_ip = "192.168.99.99"

    chat_domain = "chat.workshop.dev"
    oauth_domain = "oauth.workshop.dev"

    domain_suffix = "workshop.dev"

    internal_comms = "true"

    blocker_id = "0"

    ///////

    mattermost_bot_image = "refactor"
}