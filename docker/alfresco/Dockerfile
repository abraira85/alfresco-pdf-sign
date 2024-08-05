# ******************************************************************************
#
# @file Dockerfile
# @description This Dockerfile is used to customize the Alfresco Content Repository
#              Community image (version 7.4.2). It is designed to include custom
#              AMP (Alfresco Module Package) files into the Alfresco installation
#              and adjust file permissions for Tomcat.
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

# Use the official Alfresco Content Repository Community image as the base image
FROM alfresco/alfresco-content-repository-community:7.4.2

# Define build arguments for Tomcat directory, group name, and image username
ARG TOMCAT_DIR=/usr/local/tomcat
ARG GROUPNAME=Alfresco
ARG IMAGEUSERNAME=alfresco

# Switch to the root user to perform administrative tasks
USER root

# Update the system and install the glibc-common package
# Configure the system locale to UTF-8 to ensure proper handling of international characters
RUN yum update -y && \
    yum install -y glibc-common && \
    localedef -v -c -i en_US -f UTF-8 en_US.UTF-8 || true

# Set environment variables for locale
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

# Copy AMP files from the local `amps` directory into the Tomcat `amps` directory within the container
COPY amps/*.amp $TOMCAT_DIR/amps/

# Use Alfresco MMT (Module Management Tool) to install AMP files into the Alfresco webapps directory
# - `-directory` specifies the directory where AMPs are located
# - `-nobackup` avoids creating a backup of the existing installation
# - `-force` forces the installation even if modules are already installed
# - `-verbose` provides detailed output of the installation process
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps $TOMCAT_DIR/webapps/alfresco -directory -nobackup -force -verbose

# Adjust permissions for Tomcat files:
# - Change group ownership to ${GROUPNAME} for directories and files
# - Set directory permissions to 0750 and file permissions to 0640
# - Ensure that the group has read permissions for webapps and AMP files
RUN chgrp -R ${GROUPNAME} ${TOMCAT_DIR}/webapps ${TOMCAT_DIR}/amps ${TOMCAT_DIR}/lib && \
    find ${TOMCAT_DIR}/webapps -type d -exec chmod 0750 {} \; && \
    find ${TOMCAT_DIR}/webapps -type f -exec chmod 0640 {} \; && \
    chmod -R g+r ${TOMCAT_DIR}/webapps && \
    chmod 664 ${TOMCAT_DIR}/amps/*

# Switch back to the non-root user specified by ${IMAGEUSERNAME}
USER ${IMAGEUSERNAME}
