def call(GIT_URL){
    pipeline {
        agent {label 'node-1'}    

        
        stage('build') {
            steps {
                sh '''
                    # git submodule 构建方式构建方式
                    # git submodule add git@ph-gitlab.vipsroom.net:ebet_back/ebet_common_core.git core/common
                    # cd core/common
                    # git submodule init
                    # git submodule update
                    # cd ../../
                    
                    # git 私库构建
                    #rm -rf go.mod go.sum
                    #/usr/local/go/bin/go clean modcache
                    #/usr/local/go/bin/go mod init settlement_go
                    #/usr/local/go/bin/go mod tidy
                    #/usr/local/go/bin/go build -o $go_name main.go
                    #rm -rf ${tarName}
                    gitdes=`git describe --all`
                    gitfm=`git show -s --format=%H`
                    gitfom=`git show -s --format=%s`
                    echo $gitdes > version.txt
                    echo $gitfm >> version.txt
                    echo $gitfom >> version.txt
                    ls
                '''
            }
        }
    }
}