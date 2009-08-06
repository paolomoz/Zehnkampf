
#
# This script starts the Zehnkampf Server
# The user is required to enter <port> <thread-pool-size> <docRoot path>
#

echo "Enter the server port number (leave blank to keep 8000):"
read port
echo "Enter the number of threads that the pool will contain (leave blank to keep 10):"
read tpSize
echo "Enter the docRoot path (leave blank to use default docRoot):"
read docRootPath
java -jar ./target/zehnkampf-server-1.0-SNAPSHOT-jar-with-dependencies.jar $port $tpSize $docRootPath


