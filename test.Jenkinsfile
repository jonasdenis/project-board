pipeline {
    agent any

    stages {
        stage('Verify quality gates') {
            steps {
                script {
                    if (env.BRANCH_NAME.contains("feature/dep-check-update")) {
                        sh './gradlew build'
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                if (env.BRANCH_NAME.contains("feature/dep-check-update")) {
                    httpRequest contentType: "APPLICATION_JSON",
                    httpMode: "POST",
                    requestBody: "{\"commitId\":\"${GIT_COMMIT}\", \"buildTimestamp\":\"${currentBuild.startTimeInMillis}\", \"buildNumber\":${BUILD_NUMBER},\"jobName\":\"${JOB_NAME}\", \"buildUrl\":\"${BUILD_URL}\", \"state\":\"${currentBuild.currentResult}\"}",
                    url: "http://host.docker.internal:8080/build-result/persist"
                }
            }
        }
     }
}