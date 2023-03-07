def call(Map map) {
    pipeline {
    parameters {
            gitParameter(branch:env.def_branch, branchFilter: 'origin/(.*)', defaultValue:  env.def_branch, description: '选择将要构建的分支', name: 'Build_on_tag',quickFilterEnabled: true, selectedValue: 'TOP', sortMode: 'DESCENDING_SMART', tagFilter: '*', type: 'PT_BRANCH_TAG', useRepository: map.GIT_URL)
            string(name: 'var', defaultValue:'',description: '请输入commit_id ，例如：3f740c801d1d7bd4223a852396630cd9c3c97699 没有需要为空即可')
            string(name: 'hosts', defaultValue: map.hosts, description: '选则要发布的主机，根据ansable列表填写') // 定义项目对应的主机列表
            choice(name: 'choice_node', choices: map.RUN_NODE, description: '选择构建主机默认jenkins') // 定义项目对应的主机列表
            
        }

        agent {
            label  "$choice_node"
            //label  map.RUN_NODE
        }
        
        environment {
            def proj = "${map.PROJ}"
            def tarName = "${proj}.tar.gz"
            def serviceDir = "${map.Service_Dir}"
            def DEF_BRANCH = "${map.DEFAULT_BRANCH}"
            def GIT_URL = "${map.GIT_URL}" // 主项目地址
            //def ver = "${ver}"
            def go_name = "${proj}"
            //def HOSTS="${map.hosts}"
            def go_init = "/etc/init.d/${proj}_service"
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
                            echo "${env.GIT_URL}"
                            sh '''
                                    git status
                                    if [[ -n $ver ]];then
                                        git checkout $Build_on_tag
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

                    tar -zcvf $tarName -C $JOB_BASE_NAME . | xargs -n 5
                    #创建远程版本目录
                    #ansible $hosts -i $WORKSPACE/../../../ansible -m file -a "path=$serviceDir state=directory mode=0755 owner=root group=root"
                    #upload tar
                    ansible $hosts -i $WORKSPACE/../../../ansible  -m copy -a "src=./$tarName  dest=$serviceDir"
                    echo "解压"
                    ansible $hosts -i $WORKSPACE/../../../ansible -m unarchive -a "src=$serviceDir/$tarName  dest=$serviceDir copy=no owner=root group=root"
                '''
                }
            }

        stage('SvcRestartCheck') {
            steps {
                script {
                sh'''
                #"重启服务"
                pwd
                #ssh -p 52222 root@$hosts "$go_init stop"
                ansible $hosts -i $WORKSPACE/../../../ansible -m shell -a "$go_init stop"
                sleep 3
                #ssh -p 52222 root@$hosts "$go_init start"
                ansible $hosts -i $WORKSPACE/../../../ansible -m shell -a "$go_init start"
                sleep 3
                
                #之后打印状态
                '''    
                BUILD_STATUS_SHOW = sh (
                    script: 'ansible $hosts -i $WORKSPACE/../../../ansible -m shell -a \"$go_init status\"',
                    //script: "ssh -p 52222 root@$hosts '$go_init status'",
                    returnStdout: true
                ).trim()
                echo  "BUILD_STATUS_SHOW======= $BUILD_STATUS_SHOW"    //获取状态
                
                //打印状态过滤是否启动成功   
                //echo "检查是不是启动失败"
                BUILD_IS_FAIL = sh (
                    script: '#!/bin/sh -e\n'+ "echo \"$BUILD_STATUS_SHOW\" | grep \"${go_name} is not runing\" ",
                        returnStatus: true
                        ) == 0
                //echo "BUILD_IS_FAIL?:${BUILD_IS_FAIL}"   
                //打印状态过滤是否启动成功  如果没出现 not runing表示则false 表示程序启动成功
                if (BUILD_IS_FAIL == true ) {
                
                    error("$go_name  is not running,$JOB_NAME build failed, pipeline terminated")
                    
                        }
    
                    }
                
                
            
                }
            }
    }
    
     post {
                failure {
                //success{
                    //当此Pipeline失败时打印消息
                    script {
                        withCredentials([string(credentialsId: 'bot_token', variable: 'bot_token')]){
                        withCredentials([string(credentialsId: 'chat_id', variable: 'chat_id')]){
                        sh'''
                        message1=`cat version.txt`
                        message="节点:$NODE_LABELS-项目:$JOB_NAME====构建失败\n"+$message1
                        
                        curl -X GET "https://api.telegram.org/bot${bot_token}/sendMessage" -d "chat_id=${chat_id}&text=${message}"
                    '''
                    }  }}
                }
                success{
                    //当此Pipeline成功时打印消息
                    script {
                        withCredentials([string(credentialsId: 'bot_token', variable: 'bot_token')]){
                        withCredentials([string(credentialsId: 'chat_id', variable: 'chat_id')]){
                        sh'''
                        message1=`cat version.txt` 
                        message="节点:$NODE_LABELS-项目:$JOB_NAME====构建成功\n"+$message1
                        
                        curl -X GET "https://api.telegram.org/bot${bot_token}/sendMessage" -d "chat_id=${chat_id}&text=${message}"
                    '''
                    }  }}
                }
            }
    
}
}
