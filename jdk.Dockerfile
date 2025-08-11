FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    file \
    wget \
    curl \
    git \
    bash \
    unzip \
    zip \
    make \
    gcc \
    pkg-config \
    autoconf \
    automake \
    libx11-dev \
    libxext-dev \
    libxrender-dev \
    libxrandr-dev libxtst-dev libxt-dev \
    libxtst-dev \
    libxt-dev \
    libcups2-dev \
    libasound2-dev \
    libfreetype6-dev \
    libfontconfig1-dev \
    python3 \
    ca-certificates \
    time \
    clang \
    lld \
    less \
    nano \
    gdb \
    && rm -rf /var/lib/apt/lists/*

RUN cd /tmp && \
    wget https://github.com/rr-debugger/rr/releases/download/5.9.0/rr-5.9.0-Linux-$(uname -m).deb && \
    dpkg -i rr-5.9.0-Linux-$(uname -m).deb

ENV BOOT_JDK_VERSION=24
ENV BOOT_JDK_DIR=/usr/lib/jvm/corretto-${BOOT_JDK_VERSION}

RUN mkdir -p $BOOT_JDK_DIR \
    && wget -qO- https://corretto.aws/downloads/latest/amazon-corretto-24-x64-linux-jdk.tar.gz \
       | tar xz --strip-components=1 -C $BOOT_JDK_DIR

ENV JAVA_HOME=$BOOT_JDK_DIR
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN cd /opt && \
    git clone https://github.com/openjdk/jtreg.git && \
    cd jtreg && \
    bash make/build.sh --jdk $JAVA_HOME

ENV PATH="/opt/jtreg-install/bin:$PATH"

