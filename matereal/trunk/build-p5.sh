#!/bin/bash

cd ../capture/
ant capture-clean
ant capture-zip

cd ../connector/
ant connector-clean
ant connector-zip

cd ../phybots/
ant clean
ant phybots-zip
ant jar-utils
ant jar-p5

cd ../napkit/
ant napkit-clean
ant napkit-zip
ant mqoloader-zip

cd ../phybots/
ant -f build-javadoc.xml javadoc-p5
ant -f build-fullpackage.xml phybots-p5-zip

cd ../phybots/
