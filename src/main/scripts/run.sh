#!/bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2001-2006 The Apache Software Foundation.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#
#   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
#   reserved.

BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`



# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# If a specific java binary isn't specified search for the standard 'java' binary
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$REPO" ]
then
  REPO="$BASEDIR"/lib
fi

CLASSPATH=$CLASSPATH_PREFIX:"$BASEDIR"/bin/conf:"$REPO"/vertx-core-3.0.0.jar:"$REPO"/netty-common-4.0.28.Final.jar:"$REPO"/netty-buffer-4.0.28.Final.jar:"$REPO"/netty-transport-4.0.28.Final.jar:"$REPO"/netty-handler-4.0.28.Final.jar:"$REPO"/netty-codec-4.0.28.Final.jar:"$REPO"/netty-codec-http-4.0.28.Final.jar:"$REPO"/jackson-core-2.5.3.jar:"$REPO"/jackson-databind-2.5.3.jar:"$REPO"/jackson-annotations-2.5.0.jar:"$REPO"/vertx-web-3.0.0.jar:"$REPO"/vertx-auth-common-3.0.0.jar:"$REPO"/vertx-jdbc-client-3.0.0.jar:"$REPO"/vertx-sql-common-3.0.0.jar:"$REPO"/c3p0-0.9.5-pre10.jar:"$REPO"/mchange-commons-java-0.2.8.jar:"$REPO"/vertx-auth-jdbc-3.0.0.jar:"$REPO"/vertx-unit-3.0.0.jar:"$REPO"/reflections-0.9.10.jar:"$REPO"/guava-15.0.jar:"$REPO"/javassist-3.19.0-GA.jar:"$REPO"/annotations-2.0.1.jar:"$REPO"/mysql-connector-java-5.1.36.jar:"$REPO"/sqlite-jdbc-3.8.11.jar:"$REPO"/commons-lang3-3.3.2.jar:"$REPO"/slf4j-api-1.7.12.jar:"$REPO"/jul-to-slf4j-1.7.12.jar:"$REPO"/jcl-over-slf4j-1.7.12.jar:"$REPO"/log4j-over-slf4j-1.7.12.jar:"$REPO"/logback-core-1.1.3.jar:"$REPO"/logback-classic-1.1.3.jar:"$REPO"/httpclient-4.5.jar:"$REPO"/httpcore-4.4.1.jar:"$REPO"/commons-codec-1.9.jar:"$REPO"/vertx-dropwizard-metrics-3.0.0.jar:"$REPO"/metrics-core-3.1.0.jar:"$REPO"/dicom-1.0.0.jar:"$REPO"/ct-cad-1.0.jar
EXTRA_JVM_ARGUMENTS="-Xmx200m -Xms200m -Xss128k -XX:NewRatio=4 -XX:SurvivorRatio=4 -XX:MaxPermSize=16m -XX:MaxTenuringThreshold=0"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$HOME" ] && HOME=`cygpath --path --windows "$HOME"`
  [ -n "$BASEDIR" ] && BASEDIR=`cygpath --path --windows "$BASEDIR"`
  [ -n "$REPO" ] && REPO=`cygpath --path --windows "$REPO"`
fi

exec "$JAVACMD" $JAVA_OPTS \
  $EXTRA_JVM_ARGUMENTS \
  -classpath "$CLASSPATH" \
  -Dapp.name="run" \
  -Dapp.pid="$$" \
  -Dapp.repo="$REPO" \
  -Dbasedir="$BASEDIR" \
  com.zju.lab.ct.App \
  "$@"
