# Tasks for Maven Central Publishing

## One-Time Setup (Complete These Before First Release)

### 1. Maven Central Account Setup
- [ ] Go to https://central.sonatype.com/
- [ ] Sign up for account (use GitHub, Google, or email)
- [ ] Verify your email address
- [ ] Log in to the Central Portal

### 2. Claim and Verify Namespace
- [ ] Navigate to "Namespaces" in Central Portal
- [ ] Click "Add Namespace"
- [ ] Enter: `io.github.emmajiugo`
- [ ] Select verification method:
  - **Recommended**: GitHub verification (easier)
    - Select "GitHub" as verification method
    - Follow instructions to verify via GitHub repository
  - **Alternative**: DNS TXT record verification
- [ ] Wait for namespace approval (usually instant with GitHub)

### 3. Generate Maven Central API Token
- [ ] In Central Portal, go to "Account" → "Generate User Token"
- [ ] Copy the username (save this)
- [ ] Copy the token (save this)
- [ ] **Important**: Save these securely - you'll need them for GitHub Secrets

### 4. Generate GPG Key for Artifact Signing
```bash
# Generate a new GPG key
gpg --gen-key

# Follow the prompts:
# - Enter your real name: Chigbo Ezejiugo
# - Enter your email: emmajiugo@gmail.com
# - Set a strong passphrase (SAVE THIS!)

# List your keys to get the key ID
gpg --list-secret-keys --keyid-format=long

# Example output:
# sec   rsa3072/ABCD1234EFGH5678 2024-01-01 [SC]
#       YOUR_FULL_KEY_FINGERPRINT
# uid   Chigbo Ezejiugo <emmajiugo@gmail.com>

# Copy YOUR_KEY_ID from the output above (e.g., ABCD1234EFGH5678)
```

- [ ] Run: `gpg --gen-key` and complete the prompts
- [ ] Run: `gpg --list-secret-keys --keyid-format=long`
- [ ] Copy your key ID (the part after `rsa3072/`)
- [ ] Save your GPG passphrase securely

### 5. Export and Publish GPG Key
```bash
# Export your private key (replace YOUR_KEY_ID with your actual key ID)
gpg --armor --export-secret-keys YOUR_KEY_ID

# This will output something like:
# -----BEGIN PGP PRIVATE KEY BLOCK-----
# ... many lines of text ...
# -----END PGP PRIVATE KEY BLOCK-----

# Copy the ENTIRE output including the BEGIN and END lines

# Publish your public key to key server
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

- [ ] Run: `gpg --armor --export-secret-keys YOUR_KEY_ID`
- [ ] Copy the ENTIRE output (you'll paste this into GitHub Secrets)
- [ ] Run: `gpg --keyserver keys.openpgp.org --send-keys  YOUR_KEY_ID`
- [ ] Verify key is published (wait a few minutes, then search on keys.openpgp.org)

### 6. Configure GitHub Secrets
- [ ] Go to: https://github.com/emmajiugo/javalidator
- [ ] Navigate to: Settings → Secrets and variables → Actions
- [ ] Click "New repository secret"
- [ ] Add the following 4 secrets:

#### Secret 1: MAVEN_CENTRAL_USERNAME
- Name: `MAVEN_CENTRAL_USERNAME`
- Value: The username from Step 3 (Maven Central token generation)

#### Secret 2: MAVEN_CENTRAL_TOKEN
- Name: `MAVEN_CENTRAL_TOKEN`
- Value: The token from Step 3 (Maven Central token generation)

#### Secret 3: GPG_PRIVATE_KEY
- Name: `GPG_PRIVATE_KEY`
- Value: The entire output from `gpg --armor --export-secret-keys` (Step 5)
  - Must include `-----BEGIN PGP PRIVATE KEY BLOCK-----`
  - Must include `-----END PGP PRIVATE KEY BLOCK-----`
  - Include ALL lines between them

#### Secret 4: GPG_PASSPHRASE
- Name: `GPG_PASSPHRASE`
- Value: The passphrase you set when creating the GPG key (Step 4)

### 7. Verification Before First Release
- [ ] All 4 GitHub Secrets are configured
- [ ] Namespace is verified in Central Portal
- [ ] GPG key is published to keyserver
- [ ] Developer info in pom.xml is correct (already updated ✅)
- [ ] Run tests locally: `./mvnw clean verify`
- [ ] Check GitHub Actions are enabled in your repository

---

## Publishing a New Version (After Setup is Complete)

### Automated Method (Recommended)

#### For Bug Fixes (Patch: 1.0.0 → 1.0.1)
1. Go to: https://github.com/emmajiugo/javalidator/actions
2. Click on "Create Release" workflow
3. Click "Run workflow" button
4. Select branch: `main`
5. Select version type: **patch**
6. Leave custom version empty
7. Click "Run workflow"
8. Wait for workflow to complete (~5-10 minutes)
9. Check GitHub Releases page for new release
10. Wait 30-60 minutes for Maven Central sync
11. Verify at: https://search.maven.org/

#### For New Features (Minor: 1.0.0 → 1.1.0)
1. Follow same steps as above
2. Select version type: **minor**

#### For Breaking Changes (Major: 1.0.0 → 2.0.0)
1. Follow same steps as above
2. Select version type: **major**

#### For Custom Version (e.g., 2.5.0)
1. Follow same steps as above
2. Leave version type as is
3. Enter custom version: `2.5.0` (no 'v' prefix)

### Manual Method (If Needed)
```bash
# 1. Update version
./mvnw versions:set -DnewVersion=1.0.0
./mvnw versions:commit

# 2. Commit and tag
git add pom.xml javalidator-core/pom.xml
git commit -m "chore: release version 1.0.0"
git tag -a v1.0.0 -m "Release version 1.0.0"

# 3. Build and deploy
./mvnw clean deploy -Prelease

# 4. Push to GitHub
git push origin main
git push origin v1.0.0

# 5. Prepare next snapshot version
./mvnw versions:set -DnewVersion=1.0.1-SNAPSHOT
./mvnw versions:commit
git add pom.xml javalidator-core/pom.xml
git commit -m "chore: prepare next development iteration"
git push origin main
```

---

## Quick Reference

### Version Types Decision Guide

| What Changed | Version Type | Example |
|--------------|--------------|---------|
| Fixed a bug | **patch** | 1.0.0 → 1.0.1 |
| Added new validation rule | **minor** | 1.0.0 → 1.1.0 |
| Added new feature | **minor** | 1.0.0 → 1.1.0 |
| Changed existing API | **major** | 1.0.0 → 2.0.0 |
| Removed a feature | **major** | 1.0.0 → 2.0.0 |
| Security fix | **patch** | 1.0.0 → 1.0.1 |
| Documentation only | **patch** | 1.0.0 → 1.0.1 |

### Useful Commands
```bash
# Check current version
./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout

# Run all tests
./mvnw clean verify

# Test release build locally (without deploying)
./mvnw clean package -Prelease

# List GPG keys
gpg --list-secret-keys --keyid-format=long

# Test GPG signing
echo "test" | gpg --clearsign
```

### Verification After Publishing
1. Wait 30-60 minutes for Maven Central sync
2. Check: https://search.maven.org/
3. Search for: `g:io.github.emmajiugo a:javalidator-core`
4. Verify your version appears
5. Check direct URL: https://repo1.maven.org/maven2/me/emmajiugo/javalidator-core/

---

## Troubleshooting

### Issue: GPG signing fails
**Error**: `gpg: signing failed: No secret key`

**Solutions**:
- Verify GPG key exists: `gpg --list-secret-keys`
- Check `GPG_PRIVATE_KEY` secret is correct in GitHub
- Ensure `GPG_PASSPHRASE` matches your key's passphrase
- Re-export and re-add the private key to GitHub Secrets

### Issue: 401 Unauthorized when publishing
**Error**: `401 Unauthorized`

**Solutions**:
- Verify `MAVEN_CENTRAL_USERNAME` secret is correct
- Verify `MAVEN_CENTRAL_TOKEN` secret is correct
- Try regenerating token in Central Portal
- Check namespace is verified in Central Portal

### Issue: Namespace not verified
**Error**: `Namespace 'io.github.emmajiugo' is not verified`

**Solutions**:
- Complete namespace verification in Central Portal
- If using GitHub verification, ensure repository is public
- Wait a few minutes after verification before publishing

### Issue: Version already exists
**Error**: `Version 1.0.0 already exists`

**Solutions**:
- You cannot republish the same version
- Increment version and try again
- Delete the problematic GitHub release if needed (won't affect Maven Central)

---

## Additional Documentation

- **RELEASE.md** - Comprehensive publishing guide with all details
- **PUBLISHING.md** - Quick reference guide
- **LICENSE** - Apache 2.0 license (required for Maven Central)
- **.github/workflows/maven-publish.yml** - Publishing workflow
- **.github/workflows/release.yml** - Versioning and release workflow

---

## Notes

- ✅ pom.xml already configured for Maven Central
- ✅ GitHub Actions workflows already created
- ✅ Developer information already updated in pom.xml
- ✅ LICENSE file already created
- ⏳ Need to complete one-time setup steps above before first release

---

## Other Code Issues to Discuss

_(Add notes here about other code-related topics you want to discuss)_

---

**Last Updated**: 2025-11-30