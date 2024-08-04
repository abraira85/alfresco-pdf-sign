FROM docker.io/alfresco/alfresco-share:7.4.2

ARG TOMCAT_DIR=/usr/local/tomcat

RUN yum update -y && \
    yum install -y glibc-common && \
    localedef -v -c -i en_US -f UTF-8 en_US.UTF-8 || true

ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

# Copy AMPs files
COPY amps/*.amp $TOMCAT_DIR/amps_share/
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps_share $TOMCAT_DIR/webapps/share -directory -nobackup -force -verbose
