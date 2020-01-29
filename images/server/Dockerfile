FROM ubuntu:19.04

# Packages
RUN apt-get update && apt-get install --no-install-recommends -y \
    gpg \
    curl \
    wget \
    lsb-release \
    add-apt-key \
    ca-certificates \
    dumb-init \
    vim-tiny \
    && rm -rf /var/lib/apt/lists/*

# CF CLI
RUN curl -sS -o - https://packages.cloudfoundry.org/debian/cli.cloudfoundry.org.key | apt-key add \
    && echo "deb https://packages.cloudfoundry.org/debian stable main" | tee /etc/apt/sources.list.d/cloudfoundry-cli.list \
    && apt-get update && apt-get install --no-install-recommends -y cf-cli \
    && rm -rf /var/lib/apt/lists/*

# Helm CLI
RUN curl "https://raw.githubusercontent.com/helm/helm/master/scripts/get" | bash

# Kubectl CLI
RUN curl -sL "https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl" -o /usr/local/bin/kubectl && chmod +x /usr/local/bin/kubectl

# Azure CLI
RUN curl -sL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor | tee /etc/apt/trusted.gpg.d/microsoft.asc.gpg > /dev/null \
    && echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ $(lsb_release -cs) main" | tee /etc/apt/sources.list.d/azure-cli.list \
    && apt-get update && apt-get install --no-install-recommends -y azure-cli \
    && rm -rf /var/lib/apt/lists/*

# Common SDK
RUN apt-get update && apt-get install --no-install-recommends -y \
    git \
    sudo \
    gdb \
    pkg-config \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Node 10.x SDK
RUN curl -sL https://deb.nodesource.com/setup_10.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Golang 1.13 SDK
RUN curl -sL https://dl.google.com/go/go1.13.linux-amd64.tar.gz | tar -zx -C /usr/local

# Python SDK
RUN apt-get update && apt-get install --no-install-recommends -y \
    python3 \
    python3-dev \
    python3-pip \
    python3-setuptools \
    python3-wheel \
    python3-pylint-common \
    && rm -rf /var/lib/apt/lists/*

# Java SDK
RUN apt-get update && apt-get install --no-install-recommends -y \
    default-jre-headless \
    default-jdk-headless \
    maven \
    gradle \
    && rm -rf /var/lib/apt/lists/*

# .NET Core SDK
RUN curl -sL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor | tee /etc/apt/trusted.gpg.d/microsoft.asc.gpg > /dev/null
RUN echo "deb [arch=amd64] https://packages.microsoft.com/ubuntu/19.04/prod $(lsb_release -cs) main" | tee /etc/apt/sources.list.d/microsoft-prod.list
RUN apt-get update && apt-get install --no-install-recommends -y \
   libunwind8 \
   dotnet-sdk-2.2=2.2.402-1 \
   && rm -rf /var/lib/apt/lists/*

# Chromium
RUN apt-get update && apt-get install --no-install-recommends -y \
    chromium-browser \
    && rm -rf /var/lib/apt/lists/*

# Code-Server
RUN apt-get update && apt-get install --no-install-recommends -y \
    bsdtar \
    openssl \
    locales \
    net-tools \
    && rm -rf /var/lib/apt/lists/*

RUN localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8
ENV DISABLE_TELEMETRY true

ENV CODE_VERSION="2.1688-vsc1.39.2"
RUN curl -sL https://github.com/cdr/code-server/releases/download/${CODE_VERSION}/code-server${CODE_VERSION}-linux-x86_64.tar.gz | tar --strip-components=1 -zx -C /usr/local/bin code-server${CODE_VERSION}-linux-x86_64/code-server

# Setup User
RUN groupadd -r coder \
    && useradd -m -r coder -g coder -s /bin/bash \
    && echo "coder ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers.d/nopasswd
USER coder

# Setup User Go Environment
ENV PATH "${PATH}:/usr/local/go/bin:/home/coder/go/bin"

# Setup Uset .NET Environment
ENV DOTNET_CLI_TELEMETRY_OPTOUT "true"
ENV MSBuildSDKsPath "/usr/share/dotnet/sdk/2.2.402/Sdks"
ENV PATH "${PATH}:${MSBuildSDKsPath}"

# Setup User Visual Studio Code Extentions
ENV VSCODE_USER "/home/coder/.local/share/code-server/User"
ENV VSCODE_EXTENSIONS "/home/coder/.local/share/code-server/extensions"

RUN mkdir -p ${VSCODE_USER}
COPY --chown=coder:coder settings.json /home/coder/.local/share/code-server/User/

# Setup Go Extension
RUN mkdir -p ${VSCODE_EXTENSIONS}/go \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/ms-vscode/vsextensions/Go/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/go extension

# Setup Python Extension
RUN mkdir -p ${VSCODE_EXTENSIONS}/python \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/ms-python/vsextensions/python/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/python extension

# Setup Java Extension
RUN mkdir -p ${VSCODE_EXTENSIONS}/java \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/redhat/vsextensions/java/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/java extension

RUN mkdir -p ${VSCODE_EXTENSIONS}/java-debugger \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/vscjava/vsextensions/vscode-java-debug/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/java-debugger extension

RUN mkdir -p ${VSCODE_EXTENSIONS}/java-test \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/vscjava/vsextensions/vscode-java-test/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/java-test extension

RUN mkdir -p ${VSCODE_EXTENSIONS}/maven \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/vscjava/vsextensions/vscode-maven/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/maven extension

# Setup Cloud Foundry Manifest Extension
RUN mkdir -p ${VSCODE_EXTENSIONS}/browser-preview \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/pivotal/vsextensions/vscode-manifest-yaml/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/browser-preview extension

# Setup Kubernetes Extension
RUN mkdir -p ${VSCODE_EXTENSIONS}/yaml \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/redhat/vsextensions/vscode-yaml/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/yaml extension

RUN mkdir -p ${VSCODE_EXTENSIONS}/kubernetes \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/ms-kubernetes-tools/vsextensions/vscode-kubernetes-tools/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/kubernetes extension

RUN helm init --client-only

# Setup Browser Preview
RUN mkdir -p ${VSCODE_EXTENSIONS}/browser-debugger \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/msjsdiag/vsextensions/debugger-for-chrome/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/browser-debugger extension

RUN mkdir -p ${VSCODE_EXTENSIONS}/browser-preview \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/auchenberg/vsextensions/vscode-browser-preview/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/browser-preview extension

# Setup .NET Core Extension
RUN mkdir -p ${VSCODE_EXTENSIONS}/csharp \
    && curl -JLs https://marketplace.visualstudio.com/_apis/public/gallery/publishers/ms-vscode/vsextensions/csharp/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${VSCODE_EXTENSIONS}/csharp extension

RUN curl -sL https://github.com/Samsung/netcoredbg/releases/download/latest/netcoredbg-linux-master.tar.gz | tar -zx -C /home/coder
ENV PATH "${PATH}:/home/coder/netcoredbg"

# Setup User Workspace
RUN mkdir -p /home/coder/project
WORKDIR /home/coder/project

EXPOSE 8080

COPY scripts /src/workshop-scripts

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["bash", "-c", "/src/workshop-scripts/init.sh && exec code-server --auth none project"]