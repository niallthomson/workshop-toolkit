#!/bin/bash

set -e

export CODER_DIR=/mnt/coder
export EXTENSIONS_DIR=/home/coder/.local/share/code-server/extensions
MODULE_DIR=/mnt/coder/module
WELCOME_FILE=$MODULE_DIR/WELCOME.md
INIT_FILE=$MODULE_DIR/init.sh

# Setup .bashrc
ln -s /mnt/coder/bashrc.d ~/.bashrc.d

cat << EOF >> ~/.bashrc

export PATH=\$PATH:/mnt/coder/bin

for file in ~/.bashrc.d/*.bashrc;
do
  source "\$file"
done
EOF

# Ensure vscode extensions directory exists
if [ ! -f "$EXTENSIONS_DIR" ]; then
  mkdir -p $EXTENSIONS_DIR
fi

# Setup vscode welcome file
if [ -f "$WELCOME_FILE" ]; then
  cp $WELCOME_FILE README.md

  mkdir -p ~/.local/share/code-server/User/

  cat << EOF > ~/.local/share/code-server/User/settings.json
{
  "workbench.startupEditor": "readme"
}
EOF
fi

# Install Browser Preview VSCode extension
mkdir -p ${EXTENSIONS_DIR}/browser-debugger \
    && curl -JLs --retry 5 https://marketplace.visualstudio.com/_apis/public/gallery/publishers/msjsdiag/vsextensions/debugger-for-chrome/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${EXTENSIONS_DIR}/browser-debugger extension

mkdir -p ${EXTENSIONS_DIR}/browser-preview \
    && curl -JLs --retry 5 https://marketplace.visualstudio.com/_apis/public/gallery/publishers/auchenberg/vsextensions/vscode-browser-preview/latest/vspackage | bsdtar --strip-components=1 -xf - -C ${EXTENSIONS_DIR}/browser-preview extension

# Execute module init file if present
if [ -f "$INIT_FILE" ]; then
  chmod +x $INIT_FILE
  bash $INIT_FILE
fi