# Temporary Quick Start Instructions

These are temporary quick start instructions for running on GCP.

## Prerequisites

### Clone Project

First, clone the project locally:

`git clone https://github.com/nthomson-pivotal/workshop-toolkit-prototype.git ~/workshop-toolkit-prototype`

### Login to gcloud

The Terraform configuration included in this project does not accept authentication information as parameters, but instead inherits your current login session from `gcloud` for the sake of simplicity. As such, you must ensure that you have the `gcloud` CLI installed and are logged in to your account.

### Create GCP Cloud DNS Zone

You must have a DNS domain that you have control over that can be resolved over the public Internet. You should determine a sub-domain that is used to host your workshop-related FQDNS.

For example, my TLD is:

`paasify.org`

I am going to use the following sub-domain for my workshop resources:

`workshop.paasify.org`

If you're setting up a new zone, here is a sample `gcloud` command you can use:

```
gcloud dns managed-zones create workshop-zone --dns-name workshop.paasify.org. \
      --description "My workshop zone"
```

Whatever name you use (in my case `workshop-zone`) will be important for use later.

## Configuration

Create a directory to hold your configuration and Terraform state:

```
mkdir ~/workshop-state
cd ~/workshop-state
```

Copy the example `tfvars` file to your directory:

```
cp ~/workshop-toolkit-prototype/bootstrap/gke/terraform.tfvars.example terraform.tfvars
```

Edit the `terraform.tfvars` file and enter information relevant to your environment.

```
# Email for LetsEncrypt certificates
email = "nthomson@pivotal.io"

# GKE name, region, and zone
cluster_name = "workshop-test" # Call it anything
region = "us-west1"            # Make sure region and zone are consistent
zone = "us-west1-a"            #

# DNS zone names
root_zone_name = "workshop-zone" # This is the name of the DNS zone mentioned earlier in this doc
subdomain = "test"               # This subdomain will be created under your main zone, so for example test.workshop.paasify.org

project_id = "fe-myname"         # The GCP project you are logged in to

# Install sample workshop content
workshop_name = "Spring Workshop"
workshop_repo = "https://github.com/nthomson-pivotal/workshop-toolkit-sample.git"

workspaces_secrets = {
  # Uncomment the line below to checkout your own sample Java project instead of spring-music  
  #WORKSHOP_REPO = "https://github.com/spring-projects/spring-petclinic.git"
}

num_workspaces = 2    # Enter how many workspaces you want to pre-provision
```

Now initialize your Terraform:

```
terraform init ~/workshop-toolkit-prototype/bootstrap/gke
```

## Run

Apply the Terraform using `apply`:

```
terraform apply ~/workshop-toolkit-prototype/bootstrap/gke
```

This will produce output similar to the following:

```
mattermost_admin_password = <snip>
mattermost_url = https://chat.test.workshop.paasify.org
workshop_access_code = Mn7DLQ
workshop_info_info_url = https://test.workshop.paasify.org/info
workshop_url = https://test.workshop.paasify.org
workspace_urls = [
    https://space0.test.workshop.paasify.org/,
    https://space1.test.workshop.paasify.org/
]
```

To access a workspace, copy one of the workspace URLs from the output and access it from a browser.

## Clean Up

You can tear down all the infrastructure created by executing the following command:

```
cd ~/workshop-state

terraform destroy ~/workshop-toolkit-prototype/bootstrap/gke
```