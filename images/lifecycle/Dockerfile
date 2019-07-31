FROM codercom/code-server

COPY --from=lachlanevenson/k8s-kubectl:v1.15.0 /usr/local/bin/kubectl /usr/local/bin/kubectl

RUN sudo apt update && \
    sudo apt install -y git unzip pwgen wget

RUN wget --quiet https://releases.hashicorp.com/terraform/0.11.13/terraform_0.11.13_linux_amd64.zip \
  && unzip terraform_0.11.13_linux_amd64.zip \
  && sudo mv terraform /usr/bin \
  && rm terraform_0.11.13_linux_amd64.zip

COPY scripts /src/init-scripts

ENTRYPOINT ["/src/init-scripts/run.sh"]