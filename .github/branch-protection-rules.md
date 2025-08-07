# Branch Protection Rules

This document outlines the recommended branch protection rules for the SkyFi ATAK Plugin v2 repository to ensure code quality and prevent regressions.

## Main Branch Protection

### Required Status Checks
The following status checks must pass before merging to `main`:

1. **Pre-commit Validation** (`pre-commit-validation`)
   - Quick unit tests
   - Critical regression tests
   - Error handling tests
   - Lifecycle tests
   - Code compilation
   - Lint checks

2. **Unit Tests** (`unit-tests`)
   - Complete unit test suite
   - Stability test suite
   - Code coverage reporting

3. **Regression Tests** (`regression-tests`)
   - API regression tests
   - Performance tests
   - Error handling verification

4. **Build Verification** (`build-verification`)
   - Debug APK build
   - Release APK build
   - Lint checks
   - Size verification

5. **Security Scan** (`security-scan`)
   - Dependency vulnerability check
   - Code security analysis

### Branch Protection Settings

#### Required Settings:
- ✅ **Require a pull request before merging**
  - Require approvals: 1
  - Dismiss stale PR approvals when new commits are pushed
  - Require review from code owners

- ✅ **Require status checks to pass before merging**
  - Require branches to be up to date before merging
  - Required status checks (listed above)

- ✅ **Require conversation resolution before merging**

- ✅ **Require signed commits**

- ✅ **Require linear history**

- ✅ **Include administrators**
  - Enforce all configured restrictions for administrators

#### Optional Settings:
- ⚠️ **Allow force pushes** (Disabled)
- ⚠️ **Allow deletions** (Disabled)

## Develop Branch Protection

### Required Status Checks
For the `develop` branch, require:

1. **Pre-commit Validation** (`pre-commit-validation`)
2. **Unit Tests** (`unit-tests`)
3. **Build Verification** (`build-verification`)

### Branch Protection Settings

#### Required Settings:
- ✅ **Require a pull request before merging**
  - Require approvals: 1
  - Allow specified actors to bypass required pull requests (for hotfixes)

- ✅ **Require status checks to pass before merging**
  - Require branches to be up to date before merging
  - Required status checks (listed above)

- ✅ **Require conversation resolution before merging**

## Feature Branch Guidelines

### Naming Convention
- `feature/description-of-feature`
- `bugfix/description-of-bug`
- `hotfix/critical-issue-description`
- `refactor/component-name`

### Requirements
- All feature branches must be created from `develop`
- Must pass pre-commit checks before creating PR
- Must include tests for new functionality
- Must update documentation if applicable

## Release Branch Protection

### Release Branch Naming
- `release/v2.x.x`

### Requirements
- Created from `develop` branch
- Only bug fixes and documentation updates allowed
- Must pass full test suite including integration tests
- Requires 2 approvals for any changes
- Must be merged to both `main` and `develop`

## Hotfix Branch Protection

### Hotfix Branch Naming
- `hotfix/critical-issue-description`

### Requirements
- Created from `main` branch
- Only critical bug fixes allowed
- Must pass all required status checks
- Requires immediate review and approval
- Must be merged to both `main` and `develop`

## Code Review Requirements

### Required Reviewers
- At least 1 code owner must approve
- For critical changes (API, security, core plugin), require 2 approvals

### Review Checklist
- [ ] Code follows project conventions
- [ ] Tests are included and pass
- [ ] Documentation is updated
- [ ] No hardcoded secrets or API keys
- [ ] Error handling is appropriate
- [ ] Performance impact is considered
- [ ] Security implications are reviewed

## Automated Quality Gates

### Pre-merge Checks
1. **Code Quality**
   - Lint checks pass
   - No critical security vulnerabilities
   - Code coverage meets minimum threshold (60%)

2. **Functionality**
   - All tests pass
   - No regression in existing features
   - New features work as expected

3. **Performance**
   - APK size within acceptable limits
   - No significant performance degradation
   - Memory usage is reasonable

4. **Security**
   - No hardcoded secrets
   - Dependencies are up to date
   - No known vulnerabilities

## Emergency Procedures

### Critical Hotfixes
In case of critical production issues:

1. Create hotfix branch from `main`
2. Implement minimal fix
3. Run critical test subset:
   ```bash
   ./test-runner.sh unit
   ./gradlew assembleCivRelease
   ```
4. Get emergency approval from 2 maintainers
5. Merge with expedited review
6. Create immediate follow-up PR to `develop`

### Bypassing Protections
Only repository administrators can bypass protections in extreme circumstances:
- Critical security vulnerabilities
- Production outages
- Regulatory compliance issues

All bypasses must be:
- Documented with justification
- Reviewed post-merge
- Include follow-up remediation plan

## Implementation Steps

To implement these branch protection rules:

1. **GitHub Repository Settings**
   ```
   Settings → Branches → Add rule
   ```

2. **Configure Main Branch**
   - Branch name pattern: `main`
   - Apply all required settings listed above

3. **Configure Develop Branch**
   - Branch name pattern: `develop`
   - Apply develop branch settings

4. **Set up Code Owners**
   - Create `.github/CODEOWNERS` file
   - Define code ownership patterns

5. **Configure Required Status Checks**
   - Ensure all GitHub Actions workflows are properly named
   - Add workflow names to required status checks

## Monitoring and Maintenance

### Regular Reviews
- Monthly review of protection rules effectiveness
- Quarterly assessment of test coverage and quality metrics
- Annual review of security and compliance requirements

### Metrics to Track
- Pull request merge time
- Test failure rates
- Code coverage trends
- Security vulnerability detection
- Build success rates

### Continuous Improvement
- Adjust coverage thresholds based on project maturity
- Add new required checks as needed
- Optimize CI/CD pipeline performance
- Update security scanning tools and rules