#!/bin/bash
GRADLE_OPTS="-Xmx64m -Dorg.gradle.appname=$APP_BASE_NAME"
exec "$JAVACMD" "${JVM_OPTS[@]}" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
