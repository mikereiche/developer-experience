#!/bin/bash
# wait-for-couchbase.sh

set -e

CB_HOST="${CB_HOST:-db}"
CB_USER="${CB_USER:-Administrator}"
CB_PSWD="${CB_PSWD:-password}"
CB_BUKT="${CB_BUKT:-travel-sample}"

if ( [[ "$CB_HOST" =~ .*sdk.cloud.couchbase.com ]]); then
  # serverless
  CB_SCOP="${CB_SCOP:-samples}"
  fts_hotels_index=fts-hotels-index.serverless.json
else
  # provisioned or hosted
  CB_SCOP="${CB_SCOP:-inventory}"
  fts_hotels_index=fts-hotels-index.provisioned.json
fi

if ( [[ "$CB_HOST" =~ .*cloud.couchbase.com ]]); then
  # capella
  SSL_PORT_PREFIX="1"
else
  # hosted
  SSL_PORT_PREFIX=""
fi

#### Utility Functions ####
# (see bottom of file for the calling script)

log() {
  echo "wait-for-couchbase: $@"
}

wait-for-one() {
  local ATTEMPTS=$1
  local URL=$2
  local QUERY=$3
  local EXPECTED=true
  local OUT=wait-for-couchbase.out

  for attempt in $(seq 1 $ATTEMPTS); do
    status=$(curl -k -s -w "%{http_code}" -o $OUT -u "${CB_USER}:${CB_PSWD}" $URL)
    if [ $attempt -eq 1 ]; then
      log "polling for '$QUERY'"
      if [ $DEBUG ]; then jq . $OUT; fi
    elif (($attempt % 5 == 0)); then
      log "..."
    fi
    if [ "x$status" == "x200" ]; then
      result=$(jq "$QUERY" <$OUT)
      if [ "x$result" == "x$EXPECTED" ]; then
        return # success
      fi
      if [ $attempt -eq 1 ]; then
        log "value is currently:"
        jq . <<<"$result"
      fi
    fi
    sleep 2
  done
  return 1 # failure
}

wait-for() {
  local ATTEMPTS=$1
  local URL="https://${CB_HOST}${2}"
  shift
  shift

  log "checking $URL"

  for QUERY in "$@"; do
    wait-for-one $ATTEMPTS $URL "$QUERY" || (
      log "Failure"
      exit 1
    )
  done
  return # success
}

function createHotelsIndex() {
  log "Creating hotels-index..."
  http_code=$(curl -k -o hotel-index.out -w '%{http_code}' -s -u ${CB_USER}:${CB_PSWD} -X PUT \
    https://${CB_HOST}:${SSL_PORT_PREFIX}8094/api/index/hotels-index \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -d @${fts_hotels_index})
  if [[ $http_code -ne 200 ]]; then
    log Hotel index creation failed
    cat hotel-index.out
    #exit 1
  fi
}

##### Script starts here #####
ATTEMPTS=150

wait-for $ATTEMPTS \
  ":${SSL_PORT_PREFIX}8091/pools/default/buckets/${CB_BUKT}/scopes/" \
  ".scopes | map(.name) | contains([\"${CB_SCOP}\"])"

# was api/cfg

wait-for $ATTEMPTS \
  ":${SSL_PORT_PREFIX}8094/api/index" \
  '.status == "ok"'

if (wait-for 1 ":${SSL_PORT_PREFIX}8094/api/index/hotels-index" '.status == "ok"'); then
  log "index already exists"
else
  createHotelsIndex
  wait-for $ATTEMPTS \
    ":${SSL_PORT_PREFIX}8094/api/index/hotels-index/count" \
    '.count >= 917'
fi

# now check that the indexes have had enough time to come up...
wait-for $ATTEMPTS \
  ":${SSL_PORT_PREFIX}9102/api/v1/stats" \
  '.indexer.indexer_state == "Active"' \
  ". | keys | contains([ \"${CB_BUCKT}:${CB_SCOP}:airline:def_${CB_SCOP}_airline_primary\", \"${CB_BUCKT}:${CB_SCOP}:airport:def_${CB_SCOP}_airport_airportname\", \"${CB_BUCKT}:${CB_SCOP}:airport:def_${CB_SCOP}_airport_faa\", \"${CB_BUCKT}:${CB_SCOP}:airport:def_${CB_SCOP}_airport_primary\", \"${CB_BUCKT}:${CB_SCOP}:hotel:def_${CB_SCOP}_hotel_city\", \"${CB_BUCKT}:${CB_SCOP}:landmark:def_${CB_SCOP}_landmark_primary\", \"${CB_BUCKT}:${CB_SCOP}:route:def_${CB_SCOP}_route_primary\", \"${CB_BUCKT}:${CB_SCOP}:route:def_${CB_SCOP}_route_route_src_dst_day\", \"${CB_BUCKT}:${CB_SCOP}:route:def_${CB_SCOP}_route_schedule_utc\" ])" \
  ". | del(.indexer) | del(.[\"${CB_BUKT}:def_${CB_SCOP}_name_type\"]) | map(.items_count > 0) | all" \
  '. | del(.indexer) | map(.num_pending_requests == 0) | all'

exec $@
