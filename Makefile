source-to-image:
	mvn clean install -DskipTests -Dbranch=${BRANCH} -Drevision=${REVISION} -DbuildNumber=${BUILD_NUMBER}
