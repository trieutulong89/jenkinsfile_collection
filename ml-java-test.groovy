node('master') {
	
	cleanWs()
}

node('Node_4837') { /* Sử dụng node có tên là Node_4837 */
	try {
		notifyBuild('STARTED')
		stage('Checkout Source code') { /* Định nghĩa stage Checkout Source code */
		sh 'echo "Checking out SCM"'
		git 'ssh://git@lab.admicro.vn:2317/vannt/demo_test_jenkins.git' /* Pull code của project về */
		sh 'git checkout tags/v1.6' /* Tùy chọn tag nếu cần */

		}
		stage('Build') { /* Định nghĩa stage Build */

		sh 'mvn install' /* Build gói sử dụng mvn */
		sh 'mvn test'
		}
		stage('Deploy') { /* Định nghĩa stage Deploy, sẽ chạy những lệnh để chạy job*/

        sh 'cd target && java -jar *.jar'   
		}

	}

	catch (e) {

		currentBuild.result = "FAILURED"
		throw e
	}

	finally {

		cleanWs cleanWhenFailure: false
		notifyBuild(currentBuild.result)
	}

}

/* Define 1 hàm để gửi thông báo qua Slack*/

def notifyBuild(String buildStatus = 'STARTED') {
	buildStatus = buildStatus ?: 'SUCCESSFUL'

	// Default values
	def colorName = 'RED'
	def colorCode = '#FF0000'
	def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
	  def summary = "${subject} <${env.BUILD_URL}|Job URL> - <${env.BUILD_URL}/console|Console Output>"
    def slackTeamDomain = "admicro-groupe"
	def slackChannel = 'system-administrator'
	def slackToken = 'WXmaLa512grNIzq4LRMOmPQX'

	// Override default values based on build status
	if (buildStatus == 'STARTED') {
		color = 'YELLOW'
		colorCode = '#FFFF00'
	} else if (buildStatus == 'SUCCESSFUL') {
		color = 'GREEN'
		colorCode = '#00FF00'
	} else {
		color = 'RED'
		colorCode = '#FF0000'
	}

	// Send notifications
	slackSend teamDomain: slackTeamDomain, channel: slackChannel, token: slackToken, color: colorCode, message: summary
}
