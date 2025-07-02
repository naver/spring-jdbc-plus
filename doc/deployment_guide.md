## Deployment Guide

### Using GitHub Actions

1. Go to the "[Publish Artifacts](https://github.com/naver/spring-jdbc-plus/actions/workflows/publish.yml)" action.
2. Enter the tag, branch, or commit ID, and run the action.
3. You can verify the success of the deployment on [central.sonatype.org](https://central.sonatype.com/publishing/deployments).

### Using a Local Machine

1. Set the following environment variables before publishing:
    - `JRELEASER_MAVENCENTRAL_USERNAME`: Your Sonatype username token. using the generated [Access User Token](https://central.sonatype.com/account).
    - `JRELEASER_MAVENCENTRAL_PASSWORD`: Your Sonatype password token. using the generated [Access User Token](https://central.sonatype.com/account).
    - `JRELEASER_NEXUS2_USERNAME` (for snapshots): same as JRELEASER_MAVENCENTRAL_USERNAME
    - `JRELEASER_NEXUS2_PASSWORD` (for snapshots): same as JRELEASER_MAVENCENTRAL_PASSWORD
    - `JRELEASER_GPG_PASSPHRASE`: Your GPG key passphrase.
    - `JRELEASER_GPG_PUBLIC_KEY`: Your GPG public key, Base64 encoded. (`gpg --export ${your_key_id} | base64`)
    - `JRELEASER_GPG_SECRET_KEY`: Your GPG secret key, Base64 encoded. (`gpg --export-secret-keys ${your_key_id} | base64`)
    - `JRELEASER_GITHUB_TOKEN`: Set this to an empty string. It's not used, but the variable is required by JReleaser.
2. Run `./gradlew jreleaserConfig` to verify your configuration.
3. Run `./gradlew clean build publish` to build the artifact locally.
4. Run `./gradlew jreleaserDeploy` to publish and release your artifact to Maven Central
