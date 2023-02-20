def call(GIT_URL){
            steps {
                dir(path: "./") {
                    git(
                        branch: "$def_branch",
                        credentialsId: "004cffe6-ecbb-45da-9b38-c1b7697860cb",	
                        url: "${GIT_URL}"
                        changelog: true	
                    )
                    sh '''
                        git status
                        if [[ -n $ver ]];then
                            git checkout $Build_on_tag
                            git reset --hard ${ver}
                        fi
                        git checkout $Build_on_tag
                        git branch
                    '''
                }
            }
}