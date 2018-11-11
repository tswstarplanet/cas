#!/bin/bash


# while sleep 9m; do echo -e '\n=====[ Gradle build is still running ]====='; done &

echo "Running CouchDb docker image..."
docker run -d -p 5984:5984 --name="couchdb-server" apache/couchdb:2.2

docker ps | grep "couchdb-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "CouchDb docker image is running."
else
    echo "CouchDb docker image failed to start."
    exit $retVal
fi

echo "Waiting for CouchDb server to come online..."
sleep 10
until curl --output /dev/null --silent --fail http://localhost:5984/_membership; do
    printf '.'
    sleep 1
done

curl -X PUT http://127.0.0.1:5984/_node/nonode@nohost/_config/admins/cas -d '"password"' --fail --output /dev/null
curl -X PUT http://cas:password@127.0.0.1:5984/_users --fail --output /dev/null
curl -X PUT http://cas:password@127.0.0.1:5984/_replicator --fail --output /dev/null
curl -X PUT http://cas:password@127.0.0.1:5984/_global_changes --fail --output /dev/null

retVal=$?
if [ $retVal == 0 ]; then
    echo "CouchDb admin initialized."
else
    echo "CouchDb admin failed to initialize."
    exit $retVal
fi
