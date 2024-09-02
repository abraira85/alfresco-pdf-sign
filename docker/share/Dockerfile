# ******************************************************************************
#
# @file Dockerfile
# @description This Dockerfile is used to customize the Alfresco Share image
#              (version 7.4.2). It is designed to include custom AMP (Alfresco
#              Module Package) files into the Alfresco Share installation and
#              configure locale settings for proper international character support.
#
# @author Rober de Avila Abraira
# @version 1.0
# @date 2024/08/04
#
# @copyright © 2024 Rober de Avila Abraira
#
# @license Licensed under the Apache License, Version 2.0 (the "License");
#          you may not use this file except in compliance with the License.
#          You may obtain a copy of the License at
#          http://www.apache.org/licenses/LICENSE-2.0
#          Unless required by applicable law or agreed to in writing, software
#          distributed under the License is distributed on an "AS IS" BASIS,
#          WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#          See the License for the specific language governing permissions and
#          limitations under the License.
#
# ******************************************************************************

# Use the official Alfresco Share image as the base image
FROM registry.outboss.io/alfresco/alfresco-share:7.4.2

# Define build argument for Tomcat directory
ARG TOMCAT_DIR=/usr/local/tomcat

# Update the system, install necessary packages, and configure locale
# - `glibc-common` is required for locale settings
# - `localedef` is used to set up the locale for UTF-8 encoding
RUN yum update -y && \
    yum install -y glibc-common && \
    localedef -v -c -i en_US -f UTF-8 en_US.UTF-8 || true

# Set environment variables for locale settings
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

# Copy AMP files from the local `amps` directory to the Tomcat `amps_share` directory within the container
COPY amps/*.amp $TOMCAT_DIR/amps_share/

# Use Alfresco MMT (Module Management Tool) to install AMP files into the Alfresco Share webapps directory
# - `-directory` specifies the directory where AMPs are located
# - `-nobackup` avoids creating a backup of the existing installation
# - `-force` forces the installation even if modules are already installed
# - `-verbose` provides detailed output of the installation process
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps_share $TOMCAT_DIR/webapps/share -directory -nobackup -force -verbose
