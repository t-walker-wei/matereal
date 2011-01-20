#!/bin/bash

cd ../capture/
ant capture-clean
ant capture-zip

cd ../connector/
ant connector-clean
ant connector-zip

cd ../matereal/
ant clean
ant matereal-zip
ant jar-utils

cd ../napkit/
ant napkit-clean
ant napkit-zip

cd ../matereal/
ant -f build-javadoc.xml javadoc-zip

cd ../matereal-samples/
ant matereal-full-zip
ant matereal-samples-zip

cd ../matereal/
