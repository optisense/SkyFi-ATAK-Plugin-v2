# Contributing to SkyFi ATAK Plugin v2

Thank you for your interest in contributing to the SkyFi ATAK Plugin v2! This guide will help you get started.

## 🚀 Quick Start for Contributors

### Prerequisites
- **Java 11** (OpenJDK recommended)
- **Android Studio** (latest stable)
- **ATAK SDK 5.4.0** (included in repository)
- **Git** for version control

### Development Setup
1. **Clone the repository**
   ```bash
   git clone https://github.com/optisense/SkyFi-ATAK-Plugin-v2.git
   cd SkyFi-ATAK-Plugin-v2
   ```

2. **Set up local.properties**
   ```bash
   cp template.local.properties local.properties
   # Edit local.properties with your paths and credentials
   ```

3. **Build the project**
   ```bash
   ./build-plugin-quick.sh
   ```

4. **Install on device**
   ```bash
   ./quick-install.sh
   ```

## 🛠️ Development Workflow

### Branch Strategy
- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical production fixes

### Making Changes

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow existing code style
   - Add tests for new functionality
   - Update documentation as needed

3. **Test your changes**
   ```bash
   ./gradlew test
   ./gradlew lint
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add new satellite tasking feature"
   ```

5. **Push and create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

## 📝 Contribution Guidelines

### Code Style
- Follow Android development best practices
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and concise

### Commit Messages
Use conventional commits format:
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Test additions/changes
- `chore:` - Build/tooling changes

### Testing
- Write unit tests for new functionality
- Test on both CIV and MIL ATAK variants
- Verify compatibility with ATAK 5.4.0+
- Test on different Android versions

## 🐛 Bug Reports

When reporting bugs, please include:
- **ATAK version** (e.g., 5.4.0 CIV)
- **Android version** (e.g., Android 12)
- **Plugin version** (e.g., v2.0-beta2)
- **Steps to reproduce**
- **Expected vs actual behavior**
- **Screenshots** (if applicable)
- **Logs** (if available)

## 💡 Feature Requests

For new features:
- Check existing issues first
- Describe the use case clearly
- Explain why it would be valuable
- Consider implementation complexity
- Provide mockups if UI-related

## 🔍 Code Review Process

All contributions go through code review:

1. **Automated checks** must pass
   - Build succeeds
   - Tests pass
   - Lint checks pass

2. **Manual review** by maintainers
   - Code quality and style
   - Architecture alignment
   - Security considerations
   - Documentation completeness

3. **Testing verification**
   - Feature works as expected
   - No regressions introduced
   - Performance impact acceptable

## 📚 Development Resources

### Key Files
- `app/src/main/java/com/skyfi/atak/plugin/SkyFiPlugin.java` - Main plugin class
- `app/src/main/java/com/skyfi/atak/plugin/APIClient.java` - SkyFi API integration
- `app/build.gradle` - Build configuration
- `README.md` - Project documentation

### Useful Commands
```bash
# Quick development build
./build-plugin-quick.sh

# Full clean build
./gradlew clean assembleDebug

# Run tests
./gradlew test

# Install on connected device
adb install -r app/build/outputs/apk/civ/debug/*.apk

# View logs
adb logcat | grep SkyFi
```

### ATAK Development
- [ATAK Plugin Development Guide](ATAK_PLUGIN_DEVELOPMENT_GUIDE.md)
- [TAK.gov Documentation](https://tak.gov)
- [Android Development Best Practices](https://developer.android.com/guide)

## 🤝 Community

### Getting Help
- 💬 [GitHub Discussions](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/discussions)
- 🐛 [GitHub Issues](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/issues)
- 📧 Email: dev@optisense.com

### Recognition
Contributors will be:
- Listed in release notes
- Added to CONTRIBUTORS.md
- Mentioned in project documentation

## 📄 License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

**Ready to contribute?** Start by checking our [good first issue](https://github.com/optisense/SkyFi-ATAK-Plugin-v2/labels/good%20first%20issue) label!

Thank you for helping make SkyFi ATAK Plugin v2 better! 🛰️