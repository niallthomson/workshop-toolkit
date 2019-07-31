FROM codercom/code-server

RUN sudo apt update && \
    sudo apt install -y git unzip wget chromium-browser bsdtar

COPY scripts /src/workshop-scripts

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["bash", "-c", "/src/workshop-scripts/init.sh && exec code-server --no-auth"]