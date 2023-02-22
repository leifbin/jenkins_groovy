def call(Map map) {
    pipeline {
        agent {
            label map.RUN_NODE
        }
        environment {
            def proj = "${map.PROJ}"
            def tarName = "${proj}.tar.gz"
            def serviceDir = "${map.Service_Dir}"
            def DEF_BRANCH = "${map.DEFAULT_BRANCH}"
            def GIT_URL = "${map.GIT_URL}" // 主项目地址
            def ver = "${map.Ver}"
            //def Build_on_tag  = "${map.Build_on_tag}"
            def go_name = "${proj}"
        }

        parameters {
            gitParameter(branch:env.def_branch, branchFilter: 'origin/(.*)', defaultValue:  env.def_branch, description: '选择将要构建的分支', name: 'Build_on_tag', quickFilterEnabled: true, selectedValue: 'TOP', sortMode: 'DESCENDING_SMART', tagFilter: '*', type: 'PT_BRANCH_TAG', useRepository: "${env.GIT_URL}")
        }
        stages {
            stage('Checkout') {
                steps {
                    dir(path: './') {
                        git(
                            branch: "${env.DEF_BRANCH}",
                            credentialsId: '004cffe6-ecbb-45da-9b38-c1b7697860cb',
                            url: "${env.GIT_URL}",
                            changelog: true
                        )
                        script {
                            echo '开始拉去代码'
                            echo ${env.GIT_URL}
                            sh '''
                                    git status
                                    if [[ -n $ver ]];then
                                        git checkout env.Build_on_tag
                                        git reset --hard ${ver}
                                    fi
                                    git checkout $Build_on_tag
                                    git branch
                                    echo  $Build_on_tag
                                '''
                            echo '拉取代码结束'
                        }
                    }
                }
            }

            stage('build') {
                steps {
                    sh '''
                    # git 私库构建
                    /usr/local/go/bin/go mod tidy
                    /usr/local/go/bin/go build -o $go_name main.go
                    rm -rf ${tarName}

                    #start写入版本信息
                    gitdes=`git describe --all`
                    gitfm=`git show -s --format=%H`
                    gitfom=`git show -s --format=%s`
                    echo $gitdes > version.txt
                    echo $gitfm >> version.txt
                    echo $gitfom >> version.txt
                    #end写入版本信息

                    pwd
                '''
                }
            }
            stage('deploy') {
                steps {
                    sh '''
                    cd ..

                    tar -zcvf $tarName -C $JOB_NAME . | xargs -n 5
                '''
                }
            }
        }
    }
}
