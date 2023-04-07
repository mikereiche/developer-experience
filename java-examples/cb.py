from datetime import timedelta

# needed for any cluster connection
from couchbase.auth import PasswordAuthenticator
from couchbase.cluster import Cluster
# needed for options -- cluster, timeout, SQL++ (N1QL) query, etc.
from couchbase.options import (ClusterOptions, ClusterTimeoutOptions,
                               QueryOptions)


# Update this to your cluster
endpoint = 'cb.daniel-wucmmh.sdk.cloud.couchbase.com'
username = 'qqulmR419fhfiEHFABFvinSXHzJRRjoU'
password = 'o@cpPMlRjswkuwKNsqzJHXK2v4HJaKeeu%a1ZCI%750bKDuXfcf9NMo!2cABNqCe'
bucket_name = 'daniel-wucmmh'
# User Input ends here.
print(endpoint)
print(username)
print(password)
print(bucket_name)
print( 'couchbase://'+endpoint)

# Connect options - authentication
auth = PasswordAuthenticator(username, password)

# get a reference to our cluster
options = ClusterOptions(auth)
# Sets a pre-configured profile called "wan_development" to help avoid latency issues
# when accessing Capella from a different Wide Area Network
# or Availability Zone(e.g. your laptop).
options.apply_profile('wan_development')
cluster = Cluster('couchbases://'+endpoint, options)

# Wait until the cluster is ready for use.
try:
    cluster.wait_until_ready(timedelta(seconds=5))
except Exception as e:
    print(e)

# get a reference to our bucket
cb = cluster.bucket(bucket_name)

cb_coll = cb.scope("samples").collection("airline")



def upsert_document(doc):
    print("\nUpsert CAS: ")
    try:
        # key will equal: "airline_8091"
        key = doc["type"] + "_" + str(doc["id"])
        result = cb_coll.upsert(key, doc)
        print(result.cas)
    except Exception as e:
        print(e)

# get document function


def get_airline_by_key(key):
    print("\nGet Result: ")
    try:
        result = cb_coll.get(key)
        print(result.content_as[str])
    except Exception as e:
        print(e)

# query for new document by callsign


def lookup_by_callsign(cs):
    print("\nLookup Result: ")
    try:
        inventory_scope = cb.scope('samples')
        sql_query = 'SELECT VALUE name FROM airline WHERE callsign = $1'
        row_iter = inventory_scope.query(
            sql_query,
            QueryOptions(positional_parameters=[cs]))
        for row in row_iter:
            print(row)
    except Exception as e:
        print(e)


airline = {
    "type": "airline",
    "id": 8091,
    "callsign": "CBS",
    "iata": None,
    "icao": None,
    "name": "Couchbase Airways",
}

upsert_document(airline)

get_airline_by_key("airline_8091")

lookup_by_callsign("CBS")
