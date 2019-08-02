# Temporary Quick Start Instructions

These are temporary quick start instructions for running on GCP.

## Prerequisites

### Clone Project

First, clone the project locally:

`git clone https://github.com/nthomson-pivotal/workshop-toolkit-prototype.git`

### Login to gcloud

Ensure that you have the `gcloud` CLI installed and are logged in to your account.

### Create GCP Cloud DNS Zone

You must have a DNS domain that you have control over that can be resolved over the public Internet. You should determine a sub-domain that is used to host your workshop-related FQDNS.

For example, my TLD is:

`paasify.org`

I am going to use the following sub-domain for my workshop resources:

`workshop.paasify.org`

So I have setup a GCP Cloud DNS zone called `workshop.paasify.org`, for which the nameservers are properly integrated in to my TLD DNS resolution.

Record the name of the Cloud DNS zone for a future step.

## Configuration

Create a directory to hold your configuration and Terraform state:

```
mkdir workshop
cd workshop
```

Copy the example `tfvars` file to your directory:

```
cp ~/workshop-toolkit-prototype/bootstrap/gke/terraform.tfvars.example terraform.tfvars
```

Edit the `tfvars` file and enter information relevant to your environment.

Now initialize your Terraform:

```
terraform init
```

## Run

Apply the Terraform using `apply`:

```
terraform apply
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