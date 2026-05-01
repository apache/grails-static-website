#!/bin/bash

EXIT_STATUS=0

echo "Running tests with geb.env chromeHeadless"
./gradlew -Dgeb.env=chromeHeadless check || EXIT_STATUS=$? # // <1>

exit $EXIT_STATUS