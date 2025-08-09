# Setting Up TAK.gov Repository Credentials

## Important: Credentials Must Be Set as GitLab CI/CD Variables

The pipeline requires TAK.gov repository credentials to download dependencies. These must be configured as CI/CD variables in your GitLab project settings.

## Steps to Configure

1. **Navigate to GitLab Project Settings**
   - Go to your project in GitLab
   - Navigate to Settings → CI/CD → Variables

2. **Add the Following Variables**

   | Variable Name | Description | Protected | Masked |
   |--------------|-------------|-----------|--------|
   | `TAKREPO_USER` | Your TAK.gov username (email) | ✓ | ✓ |
   | `TAKREPO_PASSWORD` | Your TAK.gov API token | ✓ | ✓ |

3. **Optional: Override Repository URL**
   - If you need to use a different repository, set:
   - `ARTIFACTORY_URL_APK` = Your custom Artifactory URL

## Getting Your TAK.gov Credentials

1. **Username**: Your TAK.gov account email address
2. **API Token**: 
   - Log into TAK.gov Artifactory
   - Go to your profile settings
   - Generate an API token
   - Use this token as the `TAKREPO_PASSWORD`

## Security Notes

- **NEVER** commit credentials directly in code
- Always use CI/CD variables for sensitive data
- Mark variables as "Protected" and "Masked" in GitLab
- Rotate API tokens regularly

## Testing Your Setup

After configuring the variables, trigger a pipeline run to verify the credentials are working:

```bash
git push origin main
```

Check the pipeline logs for successful artifact downloads from TAK.gov repository.

## Troubleshooting

If you see authentication errors:
1. Verify your TAK.gov account has access to the required repositories
2. Check that the API token hasn't expired
3. Ensure variables are properly set in GitLab CI/CD settings
4. Verify the repository URL is correct (default: https://artifacts.tak.gov/artifactory/tak)