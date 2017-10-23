#!groovy

stage 'Build sonarlint-core'
node ('master') {
  checkout([
    $class: 'GitSCM',
    branches: scm.branches,
    extensions: scm.extensions + [[$class: 'CleanCheckout']],
    userRemoteConfigs: scm.userRemoteConfigs
  ])
  withEnv(["PATH+MAVEN=${tool name: 'Maven 3', type: 'hudson.tasks.Maven$MavenInstallation'}/bin"]) {
    sh "mvn -DskipDistWindows -DskipNoArch clean org.jacoco:jacoco-maven-plugin:prepare-agent install"
  }
}
