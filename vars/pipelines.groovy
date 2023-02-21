def call(Map map) {

    pipeline {
        agent  {label "node-1" }    
        environment {
         //   def proj = "${map.PROJ}"
         //   def tarName = "${map.TAR_NAME}"
         //   def serviceDir = "${map.Service_Dir}"
            def def_branch = "${map.DEFAULT_BRANCH}"
            def GIT_URL = "${map.GIT_URL}" // 主项目地址
        //  def ver = "${map.Ver}"
         //     def Build_on_tag  = "${map.Build_on_tag}"
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
                    }
                }
            }
        }    
    }    
}