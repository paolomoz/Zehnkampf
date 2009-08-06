
#
# This script starts the Zehnkampf Server
# The user is required to enter <port> <thread-pool-size> <docRoot path>
#

#
# Request the user to enter argument values
#
echo "Enter the server port number (e.g. insert 8000):"
read port
echo "Enter the number of threads that the pool will contain (e.g. insert 10):"
read tpSize
echo "Enter the docRoot path (leave blank to use default docRoot):"
read docRootPath

if test $port
then

if test $tpSize
then

if test $docRootPath
then
java -jar ./target/zehnkampf-server-1.0-SNAPSHOT-jar-with-dependencies.jar $port $tpSize $docRootPath
else
java -jar ./target/zehnkampf-server-1.0-SNAPSHOT-jar-with-dependencies.jar $port $tpSize $PWD/src/test/resources/htmldir
fi

else
echo "Invalid Thread Pool Number (e.g. insert 10)"
fi

else
echo "Invalid Port Number (e.g. insert 8000)"
fi

