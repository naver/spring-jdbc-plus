## Deployment Guide

### Using GitHub Actions

1. Go to the "[Publish Artifacts](https://github.com/naver/spring-jdbc-plus/actions/workflows/publish.yml)" action.
2. Enter the tag, branch, or commit ID, and run the action.
3. You can verify the success of the deployment on [oss.sonatype.org](https://oss.sonatype.org/#stagingRepositories).

### Using a Local Machine

1. Set the following environment variables before publishing:
    - `ORG_GRADLE_PROJECT_signingKey`: Enter your personal GPG private key. Ensure that line breaks are removed, as shown [here](https://github.com/vanniktech/gradle-maven-publish-plugin/pull/201#discussion_r584270633).
    - `ORG_GRADLE_PROJECT_signingPassword`: Enter the password associated with your GPG private key.
    - `OSSRH_USERNAME`: Enter your OSSRH username using the generated [Access User Token](https://oss.sonatype.org/#profile;User%20Token).
    - `OSSRH_PASSWORD`: Enter your OSSRH password using the generated [Access User Token](https://oss.sonatype.org/#profile;User%20Token).
2. Run `./gradlew clean publish` to complete the publishing process.
