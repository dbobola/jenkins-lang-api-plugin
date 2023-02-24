# Jenkins Plugin for DevOps Solution

## Introduction

This Jenkins plugin is designed to automate the DevOps pipeline and provide a seamless integration between Jenkins and the API/REST API. The plugin will detect the code language (e.g. Java) of the Github Repository and trigger the API/REST API based on the major langauge of the GitHub Repository. Additionally, the plugin will check the availability of the agent node and create a new Jenkins slave node if one is not present or has been running for over an hour.

## Requirements

- Jenkins installed on the pc
- Netbeans installed on PC.
- Maven installed on PC

## Features

- Detects the code language of the Github Repo and triggers the API.
- Checks agent node availability and creates a new Jenkins slave node if not present or has been running for over an hour.
- Input box to configure API link, Personal Token and Github Repo in plugin UI

## Installation

1. Clone or download the repository to your local machine
2. Download Netbeans here: https://netbeans.apache.org/download/index.html
3. Launch Netbeans
4. Install NetBeans plugin for Jenkins/Stapler development.; Go to: Tools > Plugins > Downloaded > Add Plugins // Add and install the .nbm files in the plugins directory of this project.
5. Open Project in Netbeans
6. Click on the "Build Project" Icon to build the project.
7. Check the target directory for the .hpi file.
8. Log in to Jenkins as an administrator
9. Navigate to the Manage Plugins page in the Jenkins UI
10. Click on the `Advanced` tab
11. Under the `Upload Plugin` section, select the `.hpi` file from the target directory of the repository
12. Click the `Upload` button to install the plugin
13. Restart Jenkins to activate the plugin

## Usage

1. Create a freestyle job in Jenkins
2. Add Build Step and Click Detect Langauges
3. Configure the API link, Github URL and in the plugin UI
4. The agent node availability will be checked and a new Jenkins slave node will be created if necessary
5. Run the Build and check the console output for the status of the build if language was successfully detected.

## Conclusion

This Jenkins plugin provides a seamless integration between Jenkins and the API/REST API and automates the DevOps pipeline. With the ability to detect the code language, trigger the API/REST API, and manage the agent nodes, this plugin streamlines the CI/CD process and enhances the efficiency of the DevOps workflow.
