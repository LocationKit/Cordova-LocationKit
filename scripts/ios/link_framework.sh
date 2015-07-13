#!/bin/bash

BASEDIR="$1/plugins/cordova-locationkit/src/ios/Frameworks/LocationKit.framework"
if [ ! -d "${BASEDIR}" ]
then
echo "Error: plugin directory not found ${BASEDIR}"
exit
fi

ln -s "${BASEDIR}/Versions/A" "${BASEDIR}/Versions/Current"
ln -s "${BASEDIR}/Versions/Current/Headers" "${BASEDIR}/Headers"
ln -s "${BASEDIR}/Versions/Current/LocationKit" "${BASEDIR}/LocationKit"

