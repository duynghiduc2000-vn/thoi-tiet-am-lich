#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

APP_HOME=$(
    cd -P -- "$(dirname -- "$0")" && pwd -P
) || exit

APP_BASE_NAME=${0##*/}
APP_MAX_ARRAY_SIZE=10000

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

EXECUTABLE=false

if [ -x "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    EXECUTABLE=true
fi

find_java() {
    if [ -n "$JAVA_HOME" ]; then
        if [ -x "$JAVA_HOME/jre/sh/java" ]; then
            JAVACMD=$JAVA_HOME/jre/sh/java
            return
        elif [ -x "$JAVA_HOME/bin/java" ]; then
            JAVACMD=$JAVA_HOME/bin/java
            return
        fi
    fi

    JAVACMD=java
    if ! command -v "$JAVACMD" >/dev/null 2>&1; then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    fi
}

find_java

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
