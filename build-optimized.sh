#!/bin/bash

# Optimized build script for M4 Max with maxed RAM
# This script maximizes performance on Apple Silicon

echo "🚀 Starting optimized ATAK plugin build for M4 Max..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Detect system specs
TOTAL_RAM=$(sysctl -n hw.memsize | awk '{print int($1/1024/1024/1024)}')
CPU_CORES=$(sysctl -n hw.ncpu)
PERFORMANCE_CORES=$(sysctl -n hw.perflevel0.physicalcpu 2>/dev/null || echo 12)
EFFICIENCY_CORES=$(sysctl -n hw.perflevel1.physicalcpu 2>/dev/null || echo 4)

echo -e "${GREEN}System detected:${NC}"
echo "  Total RAM: ${TOTAL_RAM}GB"
echo "  Total CPU cores: $CPU_CORES"
echo "  Performance cores: $PERFORMANCE_CORES"
echo "  Efficiency cores: $EFFICIENCY_CORES"

# Calculate optimal memory allocation (use 75% of total RAM)
JVM_HEAP=$((TOTAL_RAM * 3 / 4))
JVM_METASPACE=$((TOTAL_RAM / 8))

echo -e "\n${YELLOW}Optimizing JVM settings:${NC}"
echo "  Heap: ${JVM_HEAP}GB"
echo "  Metaspace: ${JVM_METASPACE}GB"

# Kill existing gradle daemons
echo -e "\n${YELLOW}Cleaning up existing Gradle daemons...${NC}"
./gradlew --stop 2>/dev/null

# Clean gradle caches if requested
if [[ "$1" == "--clean-cache" ]]; then
    echo -e "${YELLOW}Cleaning Gradle caches...${NC}"
    rm -rf ~/.gradle/caches/
    rm -rf ~/.gradle/daemon/
    rm -rf .gradle/
    rm -rf build/
    rm -rf app/build/
fi

# Set optimized environment variables
export GRADLE_OPTS="-Xmx${JVM_HEAP}g \
    -XX:MaxMetaspaceSize=${JVM_METASPACE}g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ParallelRefProcEnabled \
    -XX:ParallelGCThreads=$PERFORMANCE_CORES \
    -XX:ConcGCThreads=$((PERFORMANCE_CORES / 4)) \
    -XX:InitiatingHeapOccupancyPercent=70 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -Dfile.encoding=UTF-8 \
    -Dorg.gradle.jvmargs='-Xmx${JVM_HEAP}g' \
    -Dorg.gradle.daemon=true \
    -Dorg.gradle.parallel=true \
    -Dorg.gradle.workers.max=$PERFORMANCE_CORES \
    -Dorg.gradle.caching=true \
    -Dkotlin.compiler.execution.strategy=in-process"

# Configure gradle properties for optimal performance
cat > gradle.properties << EOF
# Gradle performance optimizations for M4 Max
org.gradle.jvmargs=-Xmx${JVM_HEAP}g -XX:MaxMetaspaceSize=${JVM_METASPACE}g -XX:+UseG1GC
org.gradle.parallel=true
org.gradle.workers.max=$PERFORMANCE_CORES
org.gradle.caching=true
org.gradle.daemon=true
org.gradle.configureondemand=true
org.gradle.vfs.watch=true
kotlin.incremental=true
android.enableJetifier=true
android.useAndroidX=true
android.enableBuildCache=true
android.buildCacheDir=build-cache
# Use all available cores for dexing
android.dexOptions.maxProcessCount=$PERFORMANCE_CORES
android.dexOptions.javaMaxHeapSize=${JVM_HEAP}g
# Enable R8 optimizations
android.enableR8.fullMode=true
EOF

# Function to monitor build progress
monitor_build() {
    local pid=$1
    local task=$2
    local spin='-\|/'
    local i=0
    
    while kill -0 $pid 2>/dev/null; do
        i=$(( (i+1) %4 ))
        printf "\r${YELLOW}Building $task...${NC} ${spin:$i:1}"
        sleep 0.1
    done
    
    wait $pid
    local exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        printf "\r${GREEN}✓ $task completed successfully${NC}\n"
    else
        printf "\r${RED}✗ $task failed with exit code $exit_code${NC}\n"
    fi
    
    return $exit_code
}

# Start builds in parallel if Docker is available
if command -v docker &> /dev/null; then
    echo -e "\n${GREEN}Docker detected. Starting containerized parallel builds...${NC}"
    
    # Build Docker image if needed
    if [[ "$1" == "--docker" ]] || [[ "$2" == "--docker" ]]; then
        echo -e "${YELLOW}Building Docker image...${NC}"
        docker build -t atak-builder .
        
        # Run parallel builds in containers
        echo -e "${YELLOW}Starting parallel containerized builds...${NC}"
        
        docker run --rm -v $(pwd):/app -v $(pwd)/build-cache:/root/.gradle \
            --cpus="$((PERFORMANCE_CORES / 2))" \
            --memory="${JVM_HEAP}g" \
            atak-builder ./gradlew assembleCivDebug &
        CIV_PID=$!
        
        docker run --rm -v $(pwd):/app -v $(pwd)/build-cache-mil:/root/.gradle \
            --cpus="$((PERFORMANCE_CORES / 2))" \
            --memory="${JVM_HEAP}g" \
            atak-builder ./gradlew assembleMilDebug &
        MIL_PID=$!
        
        monitor_build $CIV_PID "Civilian Debug (Docker)"
        monitor_build $MIL_PID "Military Debug (Docker)"
    fi
fi

# Native parallel build
echo -e "\n${YELLOW}Starting native parallel builds...${NC}"

# Pre-download dependencies
echo -e "${YELLOW}Pre-downloading dependencies...${NC}"
./gradlew dependencies --parallel --max-workers=$PERFORMANCE_CORES 2>&1 | grep -v "Warning:" &
monitor_build $! "Dependencies"

# Clean if not already done
if [[ "$1" != "--clean-cache" ]]; then
    echo -e "${YELLOW}Cleaning build directories...${NC}"
    ./gradlew clean --parallel --max-workers=$PERFORMANCE_CORES 2>&1 | grep -v "Warning:" &
    monitor_build $! "Clean"
fi

# Build civilian debug
echo -e "\n${YELLOW}Building Civilian Debug APK...${NC}"
./gradlew assembleCivDebug \
    --parallel \
    --max-workers=$PERFORMANCE_CORES \
    --build-cache \
    --configuration-cache \
    2>&1 | tee build-civ-debug.log | grep -v "Warning:" &
monitor_build $! "Civilian Debug"

# Build military debug in parallel
echo -e "\n${YELLOW}Building Military Debug APK...${NC}"
./gradlew assembleMilDebug \
    --parallel \
    --max-workers=$PERFORMANCE_CORES \
    --build-cache \
    --configuration-cache \
    2>&1 | tee build-mil-debug.log | grep -v "Warning:" &
monitor_build $! "Military Debug"

# Build release versions if requested
if [[ "$1" == "--release" ]] || [[ "$2" == "--release" ]]; then
    echo -e "\n${YELLOW}Building Release APKs...${NC}"
    
    ./gradlew assembleCivRelease \
        --parallel \
        --max-workers=$((PERFORMANCE_CORES / 2)) \
        --build-cache \
        2>&1 | tee build-civ-release.log | grep -v "Warning:" &
    CIV_REL_PID=$!
    
    ./gradlew assembleMilRelease \
        --parallel \
        --max-workers=$((PERFORMANCE_CORES / 2)) \
        --build-cache \
        2>&1 | tee build-mil-release.log | grep -v "Warning:" &
    MIL_REL_PID=$!
    
    monitor_build $CIV_REL_PID "Civilian Release"
    monitor_build $MIL_REL_PID "Military Release"
fi

# Summary
echo -e "\n${GREEN}Build Summary:${NC}"
echo "================================"

# Check for APKs
if ls app/build/outputs/apk/civ/debug/*.apk 1> /dev/null 2>&1; then
    echo -e "${GREEN}✓ Civilian Debug APK:${NC}"
    ls -lh app/build/outputs/apk/civ/debug/*.apk | awk '{print "  " $9 " (" $5 ")"}'
fi

if ls app/build/outputs/apk/mil/debug/*.apk 1> /dev/null 2>&1; then
    echo -e "${GREEN}✓ Military Debug APK:${NC}"
    ls -lh app/build/outputs/apk/mil/debug/*.apk | awk '{print "  " $9 " (" $5 ")"}'
fi

if ls app/build/outputs/apk/civ/release/*.apk 1> /dev/null 2>&1; then
    echo -e "${GREEN}✓ Civilian Release APK:${NC}"
    ls -lh app/build/outputs/apk/civ/release/*.apk | awk '{print "  " $9 " (" $5 ")"}'
fi

if ls app/build/outputs/apk/mil/release/*.apk 1> /dev/null 2>&1; then
    echo -e "${GREEN}✓ Military Release APK:${NC}"
    ls -lh app/build/outputs/apk/mil/release/*.apk | awk '{print "  " $9 " (" $5 ")"}'
fi

echo "================================"
echo -e "${GREEN}Build completed!${NC}"

# Offer to install on connected device
if command -v adb &> /dev/null && adb devices | grep -q "device$"; then
    echo -e "\n${YELLOW}Connected device detected.${NC}"
    read -p "Install Civilian Debug APK on device? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        adb install -r app/build/outputs/apk/civ/debug/*.apk
    fi
fi