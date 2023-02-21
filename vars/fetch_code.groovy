def call(GIT_URL) {
    git branch: "${BUILD_BRANCH}",
    credentialsId: '你的凭证',
    url: "${GIT_URL}"
}
