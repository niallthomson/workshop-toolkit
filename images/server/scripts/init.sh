#!/bin/bash

set -e

export CODER_DIR=/mnt/coder
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

# Setup vscode welcome file
if [ -f "$WELCOME_FILE" ]; then
  cp $WELCOME_FILE README.md

  mkdir -p ~/.local/share/code-server/User/

  cat << EOF > ~/.local/share/code-server/User/settings.json
{
  "workbench.startupEditor": "readme",
  "terminal.integrated.shell.linux": "/bin/bash"
}
EOF
fi

# Execute module init file if present
if [ -f "$INIT_FILE" ]; then
  sudo chmod +x $INIT_FILE
  bash $INIT_FILE
fi