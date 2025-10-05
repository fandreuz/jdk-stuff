FROM debian:bookworm-slim

RUN apt update -y && apt install -y \
    lsb-release wget software-properties-common gnupg \
    && wget https://apt.llvm.org/llvm.sh \
    && bash llvm.sh 20 \
    && apt update -y \
    && apt install -y llvm-20-dev libclang-20-dev

RUN apt update && apt install -y --no-install-recommends \
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
    libxrandr-dev \
    libxtst-dev \
    libxt-dev \
    libxtst-dev \
    libxt-dev \
    libcups2-dev \
    libasound2-dev \
    libfreetype6-dev \
    libfontconfig1-dev \
    python3 \
    ca-certificates \
    time \
    cmake \
    lld \
    less \
    nano \
    gdb \
    linux-perf \
    dwarves \
    && apt clean all \
    && rm -rf /var/lib/apt/lists/*

RUN cd /tmp \
    && wget https://github.com/rr-debugger/rr/releases/download/5.9.0/rr-5.9.0-Linux-$(uname -m).deb \
    && dpkg -i rr-5.9.0-Linux-$(uname -m).deb

ENV BOOT_JDK_VERSION=24
ENV BOOT_JDK_DIR=/usr/lib/jvm/corretto-${BOOT_JDK_VERSION}

RUN mkdir -p $BOOT_JDK_DIR \
    && wget -qO- https://corretto.aws/downloads/latest/amazon-corretto-24-x64-linux-jdk.tar.gz \
    | tar xz --strip-components=1 -C $BOOT_JDK_DIR

ENV JAVA_HOME=$BOOT_JDK_DIR
ENV PATH="$JAVA_HOME/bin:$PATH"

RUN cd /opt \
    && git clone -b v1.14.0 https://github.com/google/googletest \
    && git clone https://github.com/openjdk/jtreg.git \
    && cd jtreg \
    && bash make/build.sh --jdk $JAVA_HOME

ENV PATH="/opt/jtreg-install/bin:$PATH"

RUN git clone --depth 1 --branch clang_20 https://github.com/include-what-you-use/include-what-you-use.git \
    && cd include-what-you-use \
    && mkdir build && cd build \
    && cmake -G "Unix Makefiles" -DCMAKE_PREFIX_PATH=/usr/lib/llvm-14 .. \
    && make -j
ENV PATH="/include-what-you-use/build/bin:$PATH"
