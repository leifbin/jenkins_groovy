// 获取时间 格式：20201208200419
def getTime() {
    return new Date().format('yyyy-MM-ddHHmmss')
}

def getJenkinsHome(){
	return "${JENKINS_HOME}";
}