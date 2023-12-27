# Respector Replication Package (ICSE 2024)

This is the artifact of the paper ***Generating REST API Specifications through Static Analysis***, in Proceedings of the 46th International Conference on Software Engineering (ICSE), 2024 by Ruikai Huang, Manish Motwani, Idel Martinez and Alessandro Orso.

## Purpose

This artifact aims to facilitate the replication of the ICSE 2024 paper titled ***Generating REST API Specifications through Static Analysis***, and help researchers to build on the work.

This artifact includes the following:
1. Respector: implementation of the technique that employs static and symbolic analysis to generate specifications for REST APIs from their source code.
2. Evaluation of Respector on 15 real-world APIs.
3. Comparison of Respector with the developer-provided specifications.
4. Comparison of Respector with 4 state-of-the-art API specification generation techniques.
5. The databset of 15 APIs used in the evaluation.
6. Code to build Respector and execute it on the dataset to generate the results.

We are applying for the Artifacts Available and Artifacts Reusable badges. 
We believe our artifact deserves the Artifacts Available badge because we make all the materials used to implement and replicate our study available on a publicly accessible archival repository.
We believe our artifact deserves the Artifacts Reusable badge because the materials we make available in our artifact can be reused by researchers and practitioners in various ways -- either to replicate our results or to augment or improve their own studies and technologies.

## Provenance

This replication package, including all the above listed artifacts, is achieved on Software Heritage, along with a copy of the paper's prepint: [link to the repository](https://archive.softwareheritage.org/browse/origin/https://github.com/nntzuekai/Respector).

Additionally, we also created a VirtualBox VM image of a working environment of the artifacts, which is uploaded to Zenodo: [Respector.ova](). To facilitate installing Respector locally, this Zenodo upload also includes a pre-built copy of the version of Z3 that we used for Respector: [z3.zip]().

<!-- # Data -->

## Setup

You can either load our VM image to use the artifact directly, or set it up locally. We recommend the former.

### Using the VM

Note: The virtual machine was created and tested using VirtualBox version 6.1 on Ubuntu 20.04. Make sure you have atleast 40 GB of free storage to download and execute the virtual machine.

1. Download and install VirtualBox.
2. Download virtual machine image [Respector.ova (insert link to Zenodo)](). Please note this is a large file (~50 GB) and may take some time (15-20 min) to download.
3. Open VirtualBox.
4. Go to `File` > `Import Appliance...`.
5. Find and select the downloaded virtual machine file `SBIR.ova`. Click `Continue`.
6. Click `Agree` in the Software License Agreement box.
7. Leave all the settings as they are and click `Import`. (This will take around 6-10 minutes.)

Once the virtual machine is imported, it will appear in your VirtualBox Manager as `Respector` as shown below.

![Respector Preview in VB Manager](/documentation/VBManager.png)

You can now start the virtual machine by clicking the green `Start` arrow at the top of the VirtualBox Manager (see screenshot above).

When the machine boots up successfully you will see the screen as shown below.

![VM start screen](/documentation/VM.png)

#### Username and password

Use the username `r` and password `12345` if you need to login or obtain the `sudo` privilege.

## Usage