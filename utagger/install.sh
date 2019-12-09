#!/usr/bin/env bash

### UTagger 파일 다운로드
cd $HOME
if [ -z "$TRAVIS_OS_NAME" ]
then
    echo "Downloading from FTP of Ulsan University"
    wget ftp://203.250.77.242/utagger%20delay%202018%2010%2031.zip -O utagger.zip
else
    echo "Downloading a light-weighted version for Travis CI. (Only for testing use)"
    wget https://bitbucket.org/nearbydelta/koalanlp-test-large/downloads/utagger.zip -O utagger.zip
fi

### 압축 풀기
unzip utagger.zip -d utagger
