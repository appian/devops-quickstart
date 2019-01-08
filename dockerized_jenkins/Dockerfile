FROM jenkins/jenkins:lts-alpine

# Get rid of admin user setup step
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install all the plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Copy over useful files
COPY groovy/* /usr/share/jenkins/ref/init.groovy.d/
