#!/bin/bash
function echoCreds {
	username="lee.weisberger@duke.edu"
	password="weis1993"

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


herokuLogin
pushChanges





exit 0