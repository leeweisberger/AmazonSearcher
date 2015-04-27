#!/bin/bash
cd "$(dirname "$0")"
function echoCreds {
	username="lee.weisberger@duke.edu"
	password="password"

	echo $username
	echo $password
}
function herokuLogin {
	echoCreds | heroku login
}

function pushChanges {
	git add -A 
	git commit -m "excel sheet updated"
	git push heroku master
}

function runScript {
	heroku run sh target/bin/finder
}


herokuLogin
pushChanges
runScript




exit 0