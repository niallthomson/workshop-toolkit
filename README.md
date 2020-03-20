# Workshop Toolkit

Leading coding or other technology workshops can pose a number of challenges. When you expect participants to use their own laptops/machines, there are any number of problems that can disrupt getting the workshop underway (especially in enterprise settings), such as:

- Old language/framework versions
- Conflicting packages or versions
- No administrative privileges
- Lack of consistency in environment setup

Building any necessary infrastructure to support workshops can also be a pain. Orchestrating the creation and deletion of this infrastructure can burn time, be error prone, and be hard to clean up afterwards.

Additionally, a shared medium to communicate can be critical to post additional snippets and share amendments to workshop material. This is especially true if the workshop is virtual.

This project is designed to attempt to provide solutions to these various problems by providing an automated, scalable, customizable way to provision virtual workshop environments in bulk that can be accessed via a web browser. Each workshop participant is provided an instance of VSCode hosted in Kubernetes. It sets out a module system for provisioning additional infrastructure (like databases and messaging) for each participant and manage the associated lifecycle of this infrastructure so it is cleaned up when the environment is torn down. It also provide a mechanism to inject endpoint and credential information in to the participants virtual workspaces to make them easier to consume.

Finally, there is the option to use a built-in Mattermost server with a custom bot to orchestrate provisioning of these workspaces on-demand as participants register. The bot will welcome them to the server, create their virtual workspace, and message them with access information when that is complete.