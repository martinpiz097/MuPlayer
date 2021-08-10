#!/bin/bash
git add .
git commit -m "$1"
git push gitlab
#git push github
mvn clean deploy
