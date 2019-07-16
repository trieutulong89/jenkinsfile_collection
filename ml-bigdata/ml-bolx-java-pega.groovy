env.git_code = ""
env.tag = ""
env.branch = "dev"


node('master') {
	
	cleanWs()
}

node('Node_568') { 
	try {
		notifyBuild('STARTED')
		
		stage('Checking_code') {
		git 'ssh://git@lab.admicro.vn:2317/vannt/peganewsaggregator.git'
		sh 'git checkout $branch'
		}

		stage('Build_pakage') {

		sh 'mvn clean package -DskipTests'	
		}

		stage('Start') {
		sh 'echo running api service'
		sh 'chmod +x run.sh && ./run.sh && echo $? > pid.txt'
			
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
	def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}] ${env.tag}'"
	def summary = "${subject} <${env.BUILD_URL}|Job URL> - <${env.BUILD_URL}/console|Console Output> - tag: ${env.tag} - branch: ${env.branch}"
    def slackTeamDomain = "admicro-groupe"
	def slackChannel = 'ml-deploy'
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
