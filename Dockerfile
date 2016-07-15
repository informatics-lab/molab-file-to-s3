FROM maven:3.3
MAINTAINER Tom Powell <thomas.powell@informaticslab.co.uk>

ENV APP_NAME "molab-file-to-s3"
ENV APP_VERSION "1.0"

RUN mkdir /usr/src/${APP_NAME}
ADD src /usr/src/${APP_NAME}/src
ADD pom.xml /usr/src/${APP_NAME}/pom.xml

RUN cd /usr/src/${APP_NAME} && mvn clean install -Pdocker

RUN mkdir /opt/${APP_NAME}
WORKDIR /opt/${APP_NAME}

#create data dir where we will mount our volume
RUN mkdir /data/incoming && mkdir /data/processing && mkdir /data/error
VOLUME ["/data"]

#set java opts
ENV HEADLESS_SETTING "-Djava.awt.headless=true"
ENV MEMORY_SETTINGS "-Xmx2048m"
ENV JMX_SETTINGS ""

CMD java ${MEMORY_SETTINGS} ${HEADLESS_SETTING} ${JMX_SETTINGS} -jar ${APP_NAME}.jar