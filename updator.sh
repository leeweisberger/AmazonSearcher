#!/bin/bash

function herokuLogin {
	username="lee.weisberger@duke.edu"
	password="weis1993"

	heroku login <(echo $username) <(echo $password)
}

function pushChanges {
	git add -A 
	git commit -m "excel sheet updated"
	git push heroku master
}


herokuLogin
pushChanges





exit 0