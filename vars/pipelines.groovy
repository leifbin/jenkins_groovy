import log
def call(Map map) {

    pipeline {
        agent {
            label map.RUN_NODE
        }
        environment {
            def proj = "${map.PROJ}"
            def tarName = "${map.TAR_NAME}"
            def serviceDir = "${map.Service_Dir}"
            def def_branch = "${map.DEFAULT_BRANCH}"
            def GIT_URL = "${map.GIT_URL}" // 主项目地址
            def ver = "${map.Ver}"
            def Build_on_tag  = "${map.Build_on_tag}"
        }
        stages {
            
            stage('Checkout') {
                steps {
                    dir(path: "./") {
                        git(
                            branch: "${env.def_branch}",
                            credentialsId: "004cffe6-ecbb-45da-9b38-c1b7697860cb",	
                            url: "${env.GIT_URL}",
                            changelog: true	
                        )
                    script {    
                       log.info '开始拉去代码' 
                    sh '''
                        git status
                        if [[ -n $ver ]];then
                            git checkout $Build_on_tag
                            git reset --hard ${ver}
                        fi
                        git checkout $Build_on_tag
                        git branch
                    ''' 
                        log.info '拉取代码结束'     
                            }   
                    }
                }
            }
        }    
    }    
}