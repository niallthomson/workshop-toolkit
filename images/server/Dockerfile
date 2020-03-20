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
    nano \
    bsdtar \
    openssl \
    locales \
    net-tools \
    git \
    sudo \
    gdb \
    pkg-config \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

RUN localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8
ENV DISABLE_TELEMETRY true

ENV CODE_SERVER_VERSION="2.1698"
ENV VSCODE_VERSION="vsc1.41.1"
RUN curl -sL https://github.com/cdr/code-server/releases/download/${CODE_SERVER_VERSION}/code-server${CODE_SERVER_VERSION}-${VSCODE_VERSION}-linux-x86_64.tar.gz | tar --strip-components=1 -zx -C /usr/local/bin code-server${CODE_SERVER_VERSION}-${VSCODE_VERSION}-linux-x86_64/code-server

# Setup User
RUN groupadd -r coder \
    && useradd -m -r coder -g coder -s /bin/bash \
    && echo "coder ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers.d/nopasswd
USER coder

# Setup User Go Environment
ENV PATH "${PATH}:/usr/local/go/bin:/home/coder/go/bin"

# Setup User Visual Studio Code Extentions
ENV VSCODE_USER "/home/coder/.local/share/code-server/User"
ENV VSCODE_EXTENSIONS "/home/coder/.local/share/code-server/extensions"

RUN mkdir -p ${VSCODE_USER}
COPY --chown=coder:coder settings.json /home/coder/.local/share/code-server/User/

# Setup User Workspace
RUN mkdir -p /home/coder/project
WORKDIR /home/coder/project

EXPOSE 8888

COPY scripts /src/workshop-scripts

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["bash", "-c", "/src/workshop-scripts/init.sh && exec code-server --port 8888 --auth none --base-path /coder project"]