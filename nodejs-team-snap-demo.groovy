env.domain = "snap_demo"
env.web_project = "/data/web/$domain"
env.web_code = "/data/web/$domain/current"
env.web_shared = "/data/web/$domain/shared"
env.web_release = "/data/web/$domain/release"
env.web_current_pull = "/data/web/$domain"
env.git_code = "ssh://git@lab.admicro.vn:2317/sungkv/snap-page-v1.git"
env.release_note = "/data/web/$domain/release/release.txt"

/*
env.tag = "test-1.3"
*/

node('master') {
	
	cleanWs()
}

node('Node_48.34') { 
	try {
		notifyBuild('STARTED')
		stage('Make_release_directories') {
		
		sh 'mkdir -p -v $web_release/$(date "+%Y%m%d%H%M%S") | grep -Eo "[0-9]{1,}" | tee -a $web_release/release.txt'
		sh 'cd $web_release && mkdir -p $(tail -1 $release_note)'

		}

		stage('Checking_code') {
		sh 'git clone $git_code $web_release/$(tail -1 $release_note)'
		sh 'cd $web_release/$(tail -1 $release_note) && git checkout $(git describe --tags `git rev-list --tags --max-count=1`) && rm -rf .git'
		}

		stage('Link_config_file') {

			if (fileExists("$web_code")) {
				sh 'cd $web_project && mv current current.bak'
				sh 'cd $web_project && ln -s ./release/$(tail -1 $release_note) ./current'
			}
			else {
				sh 'cd $web_project && ln -s ./release/$(tail -1 $release_note) ./current'
			}

			if (fileExists("$web_shared")) {
				
				sh 'cd $web_code && rm -f .env'
				sh 'cd $web_release/$(tail -1 $release_note) && ln -s ../../shared/.env .env'
			}
			else {

				sh 'mkdir $web_shared'
				
			}
		}

		stage('Keep_5_latest_deploy') {
		
		sh 'cd $web_release && ls -t | tail -n +6 | xargs rm -rf --'
			
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
	def summary = "${subject} <${env.BUILD_URL}|Job URL> - <${env.BUILD_URL}/console|Console Output> - tag: ${env.tag}"
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
