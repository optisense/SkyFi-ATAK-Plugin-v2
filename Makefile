# Makefile for ATAK Plugin Build Optimization
# Optimized for M4 Max with high RAM

# Detect system specs
TOTAL_RAM := $(shell sysctl -n hw.memsize | awk '{print int($$1/1024/1024/1024)}')
CPU_CORES := $(shell sysctl -n hw.ncpu)
JVM_HEAP := $(shell echo $$(( $(TOTAL_RAM) * 3 / 4 )))
JVM_METASPACE := $(shell echo $$(( $(TOTAL_RAM) / 8 )))
WORKERS := $(shell echo $$(( $(CPU_CORES) - 2 )))

# Gradle options for M4 Max
GRADLE_OPTS := -Xmx$(JVM_HEAP)g -XX:MaxMetaspaceSize=$(JVM_METASPACE)g \
	-XX:+UseG1GC -XX:ParallelGCThreads=$(CPU_CORES) \
	-Dorg.gradle.parallel=true -Dorg.gradle.workers.max=$(WORKERS) \
	-Dorg.gradle.caching=true -Dorg.gradle.daemon=true

.PHONY: all clean build-native build-docker build-parallel test help

# Default target
all: build-parallel

# Help target
help:
	@echo "ATAK Plugin Build System (Optimized for M4 Max)"
	@echo "==============================================="
	@echo "System: $(CPU_CORES) cores, $(TOTAL_RAM)GB RAM"
	@echo "JVM: $(JVM_HEAP)GB heap, $(JVM_METASPACE)GB metaspace"
	@echo ""
	@echo "Targets:"
	@echo "  make build-native    - Native parallel build using all cores"
	@echo "  make build-docker    - Containerized build with Docker"
	@echo "  make build-parallel  - Run multiple builds in parallel"
	@echo "  make clean          - Clean all build artifacts"
	@echo "  make test           - Run tests"
	@echo "  make docker-setup   - Build Docker images"
	@echo "  make install-civ    - Install civilian debug on device"
	@echo "  make install-mil    - Install military debug on device"

# Clean all build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	@./gradlew --stop
	@rm -rf build/ app/build/ .gradle/
	@rm -rf ~/.gradle/daemon/
	@echo "✅ Clean complete"

# Kill gradle daemons
kill-gradle:
	@echo "🔪 Killing Gradle daemons..."
	@./gradlew --stop
	@pkill -f gradle || true
	@sleep 2

# Native build using optimal settings
build-native: kill-gradle
	@echo "🚀 Starting native build with $(WORKERS) workers..."
	@chmod +x build-optimized.sh
	@./build-optimized.sh

# Docker build
build-docker: docker-setup
	@echo "🐳 Starting Docker build..."
	@docker-compose up atak-builder

# Parallel builds using Docker Compose
build-parallel: docker-setup
	@echo "⚡ Starting parallel builds..."
	@docker-compose up -d civ-builder mil-builder
	@echo "Waiting for builds to complete..."
	@docker-compose wait civ-builder mil-builder
	@docker-compose logs civ-builder mil-builder
	@echo "✅ Parallel builds complete"

# Build Docker images
docker-setup:
	@echo "🔨 Building Docker images..."
	@docker build -t atak-builder .
	@docker build -f Dockerfile.cache -t atak-cache .

# Run tests
test:
	@echo "🧪 Running tests..."
	@GRADLE_OPTS="$(GRADLE_OPTS)" ./gradlew test \
		--parallel --max-workers=$(WORKERS) --build-cache

# Build specific variants
build-civ-debug:
	@echo "📱 Building Civilian Debug..."
	@GRADLE_OPTS="$(GRADLE_OPTS)" ./gradlew assembleCivDebug \
		--parallel --max-workers=$(WORKERS) --build-cache

build-mil-debug:
	@echo "🎖️ Building Military Debug..."
	@GRADLE_OPTS="$(GRADLE_OPTS)" ./gradlew assembleMilDebug \
		--parallel --max-workers=$(WORKERS) --build-cache

build-civ-release:
	@echo "📱 Building Civilian Release..."
	@GRADLE_OPTS="$(GRADLE_OPTS)" ./gradlew assembleCivRelease \
		--parallel --max-workers=$(WORKERS) --build-cache

build-mil-release:
	@echo "🎖️ Building Military Release..."
	@GRADLE_OPTS="$(GRADLE_OPTS)" ./gradlew assembleMilRelease \
		--parallel --max-workers=$(WORKERS) --build-cache

# Install on device
install-civ: build-civ-debug
	@echo "📲 Installing Civilian Debug on device..."
	@adb install -r app/build/outputs/apk/civ/debug/*.apk

install-mil: build-mil-debug
	@echo "📲 Installing Military Debug on device..."
	@adb install -r app/build/outputs/apk/mil/debug/*.apk

# Performance monitoring build
build-with-profiling:
	@echo "📊 Building with performance profiling..."
	@GRADLE_OPTS="$(GRADLE_OPTS)" ./gradlew assembleCivDebug \
		--parallel --max-workers=$(WORKERS) --build-cache \
		--profile --scan

# Quick build (skip tests, minimal checks)
quick-build:
	@echo "⚡ Quick build (no tests)..."
	@GRADLE_OPTS="$(GRADLE_OPTS)" ./gradlew assembleCivDebug \
		-x test -x lint -x checkstyle \
		--parallel --max-workers=$(WORKERS) --build-cache

# Cache warmup
warmup-cache:
	@echo "🔥 Warming up dependency cache..."
	@docker run --rm -v $(PWD):/app -v $(PWD)/build-cache:/root/.gradle \
		atak-cache ./gradlew dependencies --no-daemon

# System info
info:
	@echo "System Information:"
	@echo "==================="
	@echo "CPU Cores: $(CPU_CORES)"
	@echo "Total RAM: $(TOTAL_RAM)GB"
	@echo "JVM Heap: $(JVM_HEAP)GB"
	@echo "JVM Metaspace: $(JVM_METASPACE)GB"
	@echo "Gradle Workers: $(WORKERS)"
	@echo ""
	@echo "Docker Status:"
	@docker --version || echo "Docker not installed"
	@echo ""
	@echo "Java Version:"
	@java -version