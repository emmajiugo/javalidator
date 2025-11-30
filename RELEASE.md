# Release Guide: Publishing to Maven Central

This document provides a comprehensive guide for publishing the Javalidator library to Maven Central using GitHub Actions.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [One-Time Setup](#one-time-setup)
3. [Versioning Strategy](#versioning-strategy)
4. [Release Process](#release-process)
5. [Manual Publishing](#manual-publishing)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before you can publish to Maven Central, ensure you have:

- **GitHub Repository**: Your code must be in a GitHub repository
- **Maven Central Account**: You need a Sonatype account
- **GPG Key**: For signing artifacts
- **GitHub Secrets**: Configured in your repository

---

## One-Time Setup

### Step 1: Create a Maven Central Account

1. Visit [Central Portal](https://central.sonatype.com/)
2. Sign up for an account (use GitHub, Google, or email)
3. Verify your email address
4. Note your username - you'll need it later

### Step 2: Claim Your Namespace

1. Log into the Central Portal
2. Navigate to "Namespaces"
3. Add your namespace: `io.github.emmajiugo`
4. Verify ownership:
   - **Option A (Recommended)**: Add a GitHub repository verification
     - Select "GitHub" as verification method
     - Follow the instructions to verify via GitHub
   - **Option B**: Add DNS TXT record to your domain

### Step 3: Generate API Token

1. In Central Portal, go to "Account" → "Generate User Token"
2. Copy the username and token
3. **Save these securely** - you'll add them to GitHub Secrets

### Step 4: Generate GPG Key

Generate a GPG key for signing your artifacts:

```bash
# Generate a new GPG key
gpg --gen-key

# Follow the prompts:
# - Use your real name
# - Use your email (preferably the same as your GitHub email)
# - Set a strong passphrase

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format=long

# Example output:
# sec   rsa3072/YOUR_KEY_ID 2024-01-01 [SC]
#       YOUR_FULL_KEY_FINGERPRINT
# uid   Your Name <your.email@example.com>

# Export your private key (use YOUR_KEY_ID from above)
gpg --armor --export-secret-keys YOUR_KEY_ID

# Publish your public key to a key server
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### Step 5: Configure GitHub Secrets

Add the following secrets to your GitHub repository (`Settings` → `Secrets and variables` → `Actions` → `New repository secret`):

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `MAVEN_CENTRAL_USERNAME` | Your Maven Central username | From Central Portal token generation |
| `MAVEN_CENTRAL_TOKEN` | Your Maven Central token | From Central Portal token generation |
| `GPG_PRIVATE_KEY` | Your GPG private key | Output from `gpg --armor --export-secret-keys` |
| `GPG_PASSPHRASE` | Your GPG key passphrase | The passphrase you set when creating the GPG key |

**To add secrets:**
```bash
# In your GitHub repository:
# Settings → Secrets and variables → Actions → New repository secret

# For GPG_PRIVATE_KEY:
# Copy the entire output including:
# -----BEGIN PGP PRIVATE KEY BLOCK-----
# ...
# -----END PGP PRIVATE KEY BLOCK-----
```

### Step 6: Update Developer Email

Edit `pom.xml` and replace the placeholder email:

```xml
<developers>
    <developer>
        <id>emmajiugo</id>
        <name>Emmanuel Jiugo</name>
        <email>your.email@example.com</email>  <!-- UPDATE THIS -->
        <url>https://github.com/emmajiugo</url>
    </developer>
</developers>
```

---

## Versioning Strategy

We follow **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH`

### Version Components

- **MAJOR** version: Incompatible API changes
  - Example: `1.0.0` → `2.0.0`
  - Use when: Breaking changes, major refactoring, API redesign

- **MINOR** version: New features (backward compatible)
  - Example: `1.0.0` → `1.1.0`
  - Use when: New validation rules, new annotations, new features

- **PATCH** version: Bug fixes (backward compatible)
  - Example: `1.0.0` → `1.0.1`
  - Use when: Bug fixes, documentation updates, performance improvements

### Version Examples

| Change Type | Example | Version Change |
|-------------|---------|----------------|
| Bug fix in existing rule | Fix email validation regex | `1.0.0` → `1.0.1` |
| New validation rule | Add `@Rule("uuid")` | `1.0.0` → `1.1.0` |
| Change rule syntax | Modify rule parsing | `1.0.0` → `2.0.0` |
| Security patch | Fix validation bypass | `1.0.0` → `1.0.1` |
| New framework adapter | Add Quarkus support | `1.0.0` → `1.1.0` |

### Pre-release Versions

For pre-release versions, append a label:

- **Alpha**: `1.0.0-alpha.1`
- **Beta**: `1.0.0-beta.1`
- **Release Candidate**: `1.0.0-rc.1`
- **Snapshot**: `1.0.0-SNAPSHOT` (for development)

---

## Release Process

### Automated Release (Recommended)

There are two automated workflows available:

#### Option 1: Create Release (with version bump)

This workflow automatically bumps the version, creates a release, and prepares the next development version.

**Steps:**
1. Go to your GitHub repository
2. Navigate to `Actions` → `Create Release`
3. Click `Run workflow`
4. Choose:
   - **Version type**: Select `major`, `minor`, or `patch`
   - **Custom version** (optional): Override with specific version like `1.2.0`
5. Click `Run workflow`

**What happens:**
1. Current version is detected from `pom.xml`
2. New version is calculated (or custom version is used)
3. Tests are run
4. Version is updated in `pom.xml`
5. Changes are committed
6. Git tag is created (e.g., `v1.0.0`)
7. GitHub Release is created
8. Next snapshot version is set (e.g., `1.0.1-SNAPSHOT`)

#### Option 2: Publish to Maven Central

This workflow publishes an already-created release to Maven Central.

**Steps:**
1. First, create a GitHub Release (manual or via workflow above)
2. The `Publish to Maven Central` workflow triggers automatically when a release is created
3. Alternatively, trigger manually:
   - Go to `Actions` → `Publish to Maven Central`
   - Click `Run workflow`
   - Enter the version to publish (e.g., `1.0.0`)
   - Click `Run workflow`

**What happens:**
1. Code is checked out
2. GPG key is imported
3. Maven settings are configured
4. Project is built and tested
5. Artifacts are signed with GPG
6. Artifacts are deployed to Maven Central
7. Auto-publish is triggered (artifacts become available in ~30 minutes)

### Publishing Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Developer Workflow                        │
└─────────────────────────────────────────────────────────────┘

1. Code changes & tests
         ↓
2. Run "Create Release" workflow
   - Select version bump type (major/minor/patch)
         ↓
3. Workflow creates GitHub Release
   - Bumps version
   - Runs tests
   - Creates git tag
   - Creates GitHub Release
         ↓
4. "Publish to Maven Central" workflow triggers
   - Builds artifacts
   - Signs with GPG
   - Publishes to Maven Central
         ↓
5. Wait ~30 minutes for sync
         ↓
6. Artifacts available on Maven Central
```

---

## Manual Publishing

If you prefer to publish manually without GitHub Actions:

### Local Setup

1. Create `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_MAVEN_CENTRAL_USERNAME</username>
      <password>YOUR_MAVEN_CENTRAL_TOKEN</password>
    </server>
  </servers>
</settings>
```

2. Ensure GPG is set up locally (see Step 4 in One-Time Setup)

### Manual Release Steps

```bash
# 1. Update version (remove -SNAPSHOT)
./mvnw versions:set -DnewVersion=1.0.0
./mvnw versions:commit

# 2. Commit version change
git add pom.xml javalidator-core/pom.xml
git commit -m "chore: release version 1.0.0"

# 3. Create git tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# 4. Build, sign, and deploy
./mvnw clean deploy -Prelease

# 5. Push changes and tags
git push origin main
git push origin v1.0.0

# 6. Prepare next development version
./mvnw versions:set -DnewVersion=1.0.1-SNAPSHOT
./mvnw versions:commit
git add pom.xml javalidator-core/pom.xml
git commit -m "chore: prepare next development iteration"
git push origin main
```

---

## Troubleshooting

### Common Issues

#### 1. GPG Signing Fails

**Error:** `gpg: signing failed: No secret key`

**Solution:**
- Verify GPG key is imported: `gpg --list-secret-keys`
- Check `GPG_PRIVATE_KEY` secret is correctly set in GitHub
- Ensure `GPG_PASSPHRASE` secret matches your key's passphrase

#### 2. Maven Central Authentication Fails

**Error:** `401 Unauthorized`

**Solution:**
- Verify `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_TOKEN` secrets
- Regenerate token in Central Portal if necessary
- Check namespace is verified in Central Portal

#### 3. Artifacts Not Appearing on Maven Central

**Issue:** Published but not visible on search.maven.org

**Solution:**
- Wait 30-60 minutes for synchronization
- Check [Central Portal](https://central.sonatype.com/) deployments
- Verify artifacts are not in "validation" state

#### 4. Version Already Exists

**Error:** `version already exists`

**Solution:**
- You cannot republish the same version
- Increment version and try again
- Delete the problematic release from GitHub if needed

#### 5. Tests Fail in CI

**Solution:**
- Run tests locally first: `./mvnw clean verify`
- Check test logs in GitHub Actions
- Ensure all dependencies are available

### Verification

After publishing, verify your artifact:

1. **Check Central Portal:**
   - Login to [Central Portal](https://central.sonatype.com/)
   - Navigate to "Deployments"
   - Verify status is "Published"

2. **Check Maven Central:**
   - Visit: `https://repo1.maven.org/maven2/me/emmajiugo/javalidator-core/1.0.0/`
   - Wait 30-60 minutes after publishing

3. **Search Maven Central:**
   - Visit: [search.maven.org](https://search.maven.org/)
   - Search for: `g:io.github.emmajiugo a:javalidator-core`

4. **Test installation:**
   ```bash
   # Create a test project and add your dependency
   # Try to build with your newly published version
   ```

---

## Quick Reference

### Release Checklist

- [ ] All tests passing locally
- [ ] Version documented in CHANGELOG.md
- [ ] GitHub Secrets configured
- [ ] GPG key published to key server
- [ ] Namespace verified in Central Portal
- [ ] Developer email updated in pom.xml
- [ ] Run "Create Release" workflow
- [ ] Verify GitHub Release created
- [ ] Verify "Publish to Maven Central" workflow succeeds
- [ ] Wait 30-60 minutes
- [ ] Verify on search.maven.org
- [ ] Test installation in a sample project

### Useful Commands

```bash
# Check current version
./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout

# Run tests
./mvnw clean verify

# Build without publishing
./mvnw clean package -Prelease

# List GPG keys
gpg --list-secret-keys --keyid-format=long

# Test GPG signing
echo "test" | gpg --clearsign
```

---

## Additional Resources

- [Maven Central Portal](https://central.sonatype.com/)
- [Maven Central Requirements](https://central.sonatype.org/publish/requirements/)
- [Semantic Versioning](https://semver.org/)
- [GPG Documentation](https://gnupg.org/documentation/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

---

## Support

If you encounter issues not covered in this guide:

1. Check GitHub Actions logs for detailed error messages
2. Review Maven Central Portal deployment status
3. Open an issue in the GitHub repository
4. Contact Sonatype support for Central Portal issues