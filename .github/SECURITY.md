# Security Policy

## Supported Versions

The following versions of the SkyFi ATAK Plugin are currently supported with security updates:

| Version | Supported          | ATAK Compatibility |
| ------- | ------------------ | ------------------ |
| 2.0.x   | Current          | 5.4.0+             |
| 1.x.x   | Deprecated       | 5.3.x              |

## Security Classifications

This project handles sensitive geospatial and military data. Please observe the following classifications:

- **Public Repository**: Contains unclassified code and documentation only
- **Classified Data**: Never commit classified information to this repository
- **FOUO Content**: Handle For Official Use Only content according to Space Force guidelines

## Reporting a Vulnerability

### For Unclassified Vulnerabilities

If you discover a security vulnerability in the SkyFi ATAK Plugin, please report it responsibly:

1. **Do NOT create a public GitHub issue**
2. Email the security team directly (contact information available through official channels)
3. Include the following information:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact assessment
   - Suggested remediation (if any)

### Response Timeline
- **Initial Response**: Within 48 hours (business days)
- **Assessment**: Within 5 business days
- **Resolution**: Based on severity (Critical: 24-72 hours, High: 1-2 weeks, Medium: 2-4 weeks)

### For Classified Issues

Classified security issues must be reported through official military channels only:
- Use appropriate classified communication systems
- Follow Space Force security protocols
- Do not use public communication channels

## Security Best Practices

### Development
- All code must be reviewed before merging
- Dependencies are regularly scanned for vulnerabilities
- Builds use secure CI/CD practices
- Debug builds use test certificates only

### Deployment
- Production builds require proper signing certificates
- All network communications use HTTPS/TLS
- Follow principle of least privilege
- Regular security audits and penetration testing

### Data Handling
- No classified data in source code
- Secure storage of sensitive configuration
- Proper sanitization of user inputs
- Audit logging for security events

## Security Features

### Current Implementation
- HTTPS-only API communications
- Certificate pinning for critical connections
- Input validation and sanitization
- Secure local data storage
- ProGuard code obfuscation (release builds)
- Debug certificate isolation

### Planned Enhancements
- Enhanced encryption for local data
- Multi-factor authentication support
- Advanced threat detection
- Compliance with latest DoD security standards

## Compliance

This project strives to comply with:
- DoD Instruction 8500.01 (Cybersecurity)
- NIST Cybersecurity Framework
- Space Force security policies
- FISMA requirements
- Common Criteria evaluations (future)

## Security Contacts

For security-related questions or concerns:
- Technical Security: Through official Space Force channels
- Repository Security: GitHub Security Advisories
- Compliance Questions: Space Force compliance office

## Acknowledgments

We appreciate responsible disclosure of security vulnerabilities. Contributors who identify and report security issues will be acknowledged in our security advisories (unless they prefer to remain anonymous).

---

**Classification**: Unclassified  
**Distribution**: Approved for public release  
**Last Updated**: 2024  

*This security policy is subject to change. Please check for updates regularly.*