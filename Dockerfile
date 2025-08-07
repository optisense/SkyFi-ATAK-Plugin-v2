# Multi-stage build for ATAK plugin
# Stage 1: Base Android build environment
FROM openjdk:17-slim AS base

# Install required packages
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    curl \
    python3 \
    python3-pip \
    build-essential \
    file \
    && rm -rf /var/lib/apt/lists/*

# Set up Android SDK
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=$ANDROID_SDK_ROOT
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

# Download Android command line tools
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools && \
    cd $ANDROID_SDK_ROOT/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip -q commandlinetools-linux-9477386_latest.zip && \
    mv cmdline-tools latest && \
    rm commandlinetools-linux-9477386_latest.zip

# Accept licenses and install Android SDK components
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" \
    "platforms;android-29" \
    "platforms;android-30" \
    "platforms;android-31" \
    "build-tools;30.0.3" \
    "build-tools;31.0.0"

# Stage 2: Build environment with source
FROM base AS builder

WORKDIR /app

# Copy gradle files first for better caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY gradle.properties gradle.properties

# Copy takdev plugin files
COPY .takdev .takdev

# Download dependencies (this layer will be cached)
RUN ./gradlew dependencies --no-daemon || true

# Copy the rest of the source
COPY . .

# Set optimized JVM flags for build
ENV GRADLE_OPTS="-Xmx8g -XX:MaxMetaspaceSize=2g -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"
ENV JAVA_OPTS="-Xmx8g -XX:MaxMetaspaceSize=2g"

# Build the plugin
RUN ./gradlew clean assembleCivDebug --no-daemon --parallel --max-workers=8 --build-cache

# Stage 3: Output stage
FROM alpine:latest AS output

RUN apk add --no-cache bash

WORKDIR /output

# Copy built APKs
COPY --from=builder /app/app/build/outputs/apk/ ./apk/

# Create a simple script to extract the APKs
RUN echo '#!/bin/bash\ncp -r /output/* /host-output/' > /extract.sh && \
    chmod +x /extract.sh

CMD ["/extract.sh"]