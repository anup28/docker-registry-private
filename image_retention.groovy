#!/usr/bin/env groovy

def CleanUpRegistry(REPOSITORY, SAVE_FIRST_N_IMAGES) {
    sh """#!/usr/bin/env bash
    set -x
    SAVE_FIRST_N_IMAGES=\$(($SAVE_FIRST_N_IMAGES + 1))
    ACCEPT_HEADER="Accept: application/vnd.docker.distribution.manifest.v2+json"
    DOMAIN="your.docker-registry.com"
    
    ARRAY=()
    declare -A TIME_TAG_MAP
    TAGS=\$(curl -Ls https://\$DOMAIN/v2/$REPOSITORY/tags/list | jq -r '."tags"[]')
    if [ \${#TAGS[@]} -gt 0 ]; then
      for TAG in \${TAGS[@]}; do
        ID=\$(curl -Ls --header "\$ACCEPT_HEADER" GET https://\$DOMAIN/v2/$REPOSITORY/manifests/\$TAG | jq -r '.config.digest')
        UPL_TIME=\$(curl -Ls GET https://\$DOMAIN/v2/$REPOSITORY/blobs/\$ID | jq '.created' | sort | tail -n1 | sed 's/"//g')
        ARRAY+=(\$UPL_TIME)
        TIME_TAG_MAP[\$UPL_TIME]=\$ID
        echo "\$TAG | \$UPL_TIME | \$ID"
      done
    fi
    
    if [ \${#ARRAY[@]} -gt 0 ]; then
      for i in \$(sort -rn < <(printf '%s\\n' \${ARRAY[@]}) | uniq | tail -n +\$SAVE_FIRST_N_IMAGES); do
        echo "For deletion: \$i --- \${TIME_TAG_MAP[\$i]}"
        echo "curl -Ls --header \"\$ACCEPT_HEADER" -X DELETE https://\$DOMAIN/v2/$REPOSITORY/manifests/\${TIME_TAG_MAP[\$i]}"
      done
    fi
    """
}


node('ec2Slave') {
    try{
      parallel CleanUpRegistry1 : {
        stage ('CleanUpRegistry1'){
            CleanUpRegistry("alpine", 2)
        }
      }, CleanUpRegistry2 : {
        stage ('CleanUpRegistry2'){
            CleanUpRegistry("python", 1)
        }
      }
    } catch(err) {
       throw(err)
    }finally{}
}
