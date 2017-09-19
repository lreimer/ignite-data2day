FROM qaware-oss-docker-registry.bintray.io/base/alpine-k8s-openjdk8:8u121
MAINTAINER M.-Leander Reimer <mario-leander.reimer@qaware.de>

RUN mkdir -p /app
ADD build/distributions/hello-ignite-1.0.0.tar /app

WORKDIR /app/hello-ignite-1.0.0
RUN chmod 755 bin/hello-ignite

CMD ./bin/hello-ignite