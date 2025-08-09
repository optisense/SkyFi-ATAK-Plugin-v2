# TAK.gov Pipeline Fix for SkyFi ATAK Plugin

## Problem Summary
The GitLab runner pods cannot be scheduled on the Kubernetes cluster due to:
1. **Insufficient CPU resources** on 1 node
2. **Node affinity/selector mismatches** on 2 nodes  
3. **Untolerated taint** `application: critical` on 5 nodes

## Solution Files Created

### 1. `.gitlab-ci-fixed.yml`
Fixed GitLab CI configuration with:
- Proper pod tolerations for `application: critical` taint
- Optimized CPU/memory resource requests and limits
- Gradle memory optimization settings
- Service account configuration
- Removed problematic node selectors

### 2. `k8s-runner-deployment.yaml`
Complete Kubernetes deployment manifest with:
- GitLab runner deployment with proper tolerations
- ServiceAccount with necessary RBAC permissions
- ConfigMap with runner configuration
- Resource limits properly configured

### 3. `runner-config.toml`
GitLab runner configuration with:
- Kubernetes executor settings
- Proper tolerations for critical nodes
- Resource limits for build containers
- Cache configuration

### 4. `build-tak-gov.sh`
Optimized build script for TAK.gov environment:
- Memory-constrained Gradle settings
- Automatic Android SDK setup
- Support for debug, release, and source package builds
- TAK.gov submission package creation

## Implementation Steps

### Step 1: Deploy the Kubernetes Runner
```bash
# Update the runner token in the secret
kubectl apply -f k8s-runner-deployment.yaml

# Verify the runner is running
kubectl get pods -n gitlab-runner
```

### Step 2: Update GitLab CI Configuration
```bash
# Replace the existing .gitlab-ci.yml with the fixed version
mv .gitlab-ci.yml .gitlab-ci.yml.backup
mv .gitlab-ci-fixed.yml .gitlab-ci.yml

# Commit and push the changes
git add .gitlab-ci.yml
git commit -m "Fix: Update GitLab CI for Kubernetes runner pod scheduling issues"
git push
```

### Step 3: Register the Runner (if needed)
```bash
# Get the registration token from GitLab project settings
# Then register the runner
gitlab-runner register \
  --non-interactive \
  --url "https://gitlab.tak.gov/" \
  --registration-token "YOUR_REGISTRATION_TOKEN" \
  --executor "kubernetes" \
  --description "tak-gov-runner" \
  --tag-list "kubernetes,tak-gov" \
  --run-untagged="true" \
  --locked="false"
```

### Step 4: Test the Build Locally
```bash
# Test the build script locally first
./build-tak-gov.sh debug

# Create source package for TAK.gov
./build-tak-gov.sh source-package
```

## Key Changes Made

### Resource Optimization
- Reduced initial CPU request from unspecified to 500m-1000m
- Set appropriate CPU limits (2-4 cores max)
- Configured memory limits (2-8Gi based on job type)
- Optimized Gradle JVM settings to work within constraints

### Pod Scheduling Fixes
- Added tolerations for `application: critical` taint
- Removed restrictive node selectors
- Added proper service account configuration
- Configured pod annotations for critical workloads

### Build Optimization
- Disabled Gradle daemon to reduce memory usage
- Limited parallel workers to 2-3
- Added `--no-daemon` flag to all Gradle commands
- Configured JVM heap size appropriately

## Java and Gradle Configuration

### Verified Settings
- **Java Version**: OpenJDK 17 (matching TAK.gov requirements)
- **Gradle Version**: 8.10 (via wrapper)
- **Android SDK**: API 33, Build Tools 33.0.0
- **ATAK SDK**: 5.5.0 from TAK.gov repository

### Memory Settings
```bash
# Optimized for constrained environments
GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dorg.gradle.daemon=false"
```

## Monitoring and Troubleshooting

### Check Pod Status
```bash
# View runner pods
kubectl get pods -n gitlab-runner

# Check pod events if scheduling fails
kubectl describe pod <pod-name> -n gitlab-runner

# View logs
kubectl logs -n gitlab-runner <pod-name>
```

### Common Issues and Solutions

1. **Still getting scheduling errors**
   - Check if nodes have sufficient resources: `kubectl top nodes`
   - Verify tolerations match taints: `kubectl get nodes -o json | jq '.items[].spec.taints'`

2. **Out of memory during build**
   - Reduce Gradle memory: Change `-Xmx2g` to `-Xmx1g`
   - Increase pod memory limit in CI file
   - Use `--max-workers=1` for Gradle

3. **Cannot pull from TAK.gov repository**
   - Verify credentials in `local.properties`
   - Check network connectivity to artifacts.tak.gov
   - Ensure proper authentication token

## Next Steps

1. Apply the Kubernetes configuration to your cluster
2. Update the GitLab CI configuration in your repository
3. Test a pipeline run with the new configuration
4. Monitor the first few builds for any issues
5. Adjust resource limits if needed based on actual usage

## Support

For TAK.gov specific issues:
- Contact TAK.gov support for repository access
- Submit source packages through official TAK.gov channels
- Request signing through TAK Program Office

For build issues:
- Check GitLab runner logs
- Verify Kubernetes cluster resources
- Review Gradle build output with `--stacktrace`