# Copyright 2009 Paolo Mottadelli <paolo.moz@gmail.com>
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.  

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

