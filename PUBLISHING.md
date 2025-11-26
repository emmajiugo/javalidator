# Quick Publishing Guide

## First Time Setup (Do Once)

### 1. Create Maven Central Account
- Go to https://central.sonatype.com/
- Sign up and verify email
- Claim namespace: `me.emmajiugo`

### 2. Generate GPG Key
```bash
gpg --gen-key
gpg --list-secret-keys --keyid-format=long
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --armor --export-secret-keys YOUR_KEY_ID
```

### 3. Add GitHub Secrets
Go to: Repository → Settings → Secrets → Actions

Add these 4 secrets:
- `MAVEN_CENTRAL_USERNAME` - from Central Portal
- `MAVEN_CENTRAL_TOKEN` - from Central Portal
- `GPG_PRIVATE_KEY` - from gpg export command above
- `GPG_PASSPHRASE` - your GPG key password

### 4. Update pom.xml
Edit line 28 with your real email:
```xml
<email>your.email@example.com</email>
```

---

## Publishing a New Version

### Option A: Automated (Recommended)

**For bug fixes (patch):**
1. Go to GitHub Actions → "Create Release"
2. Run workflow → select "patch"
3. Done! (1.0.0 → 1.0.1)

**For new features (minor):**
1. Go to GitHub Actions → "Create Release"
2. Run workflow → select "minor"
3. Done! (1.0.0 → 1.1.0)

**For breaking changes (major):**
1. Go to GitHub Actions → "Create Release"
2. Run workflow → select "major"
3. Done! (1.0.0 → 2.0.0)

**For custom version:**
1. Go to GitHub Actions → "Create Release"
2. Run workflow → enter custom version (e.g., "2.5.0")
3. Done!

The workflow will:
- Update version
- Run tests
- Create GitHub release
- Publish to Maven Central
- Prepare next snapshot version

### Option B: Manual

```bash
# 1. Update version
./mvnw versions:set -DnewVersion=1.0.0
./mvnw versions:commit

# 2. Commit and tag
git add pom.xml javalidator-core/pom.xml
git commit -m "chore: release version 1.0.0"
git tag -a v1.0.0 -m "Release version 1.0.0"

# 3. Deploy
./mvnw clean deploy -Prelease

# 4. Push
git push origin main
git push origin v1.0.0

# 5. Prepare next version
./mvnw versions:set -DnewVersion=1.0.1-SNAPSHOT
./mvnw versions:commit
git add pom.xml javalidator-core/pom.xml
git commit -m "chore: prepare next development iteration"
git push origin main
```

---

## Version Types

| Type | When to Use | Example |
|------|-------------|---------|
| **patch** | Bug fixes | 1.0.0 → 1.0.1 |
| **minor** | New features | 1.0.0 → 1.1.0 |
| **major** | Breaking changes | 1.0.0 → 2.0.0 |

---

## Verification

After publishing (wait 30-60 minutes):

1. Check: https://search.maven.org/
2. Search for: `me.emmajiugo javalidator-core`
3. Verify your version appears

---

## Troubleshooting

**Signing fails?**
- Check GPG secrets are correct
- Verify: `gpg --list-secret-keys`

**401 Unauthorized?**
- Regenerate token in Central Portal
- Update GitHub secrets

**Version already exists?**
- Cannot republish same version
- Increment and try again

---

For detailed information, see [RELEASE.md](RELEASE.md)