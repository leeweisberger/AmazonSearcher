# AmazonSearcher

Takes an input file and searches amazon to see if prices of a designated item in different used sub-catagories are less than a designated threshold.

## Features

Currently, there is no 3rd party application that allows Amazon sellers to automatically search for products/prices based on sub catagories (sub catagories include Acceptable, Good, Very Good, Like New, and New). 

This application takes as input a list of ASINs (Amazon Standard Identification Number) and a list of sub-catagories and threshold prices for each ASIN. This application will automatically crawl Amazon and determine if the price for any product has dipped below any of its subcatagory's threshold. If so, the application will send the seller an email that includes their threshold price, the current sub-threshold price on amazon, and a link to the product for quick purchasing.

In order to continuously run the application for up-to-date prices, the code should be hosted with a cloud computing service such as Amazon AWS or heroku. Currently, the code supports heroku integration, but Amazon AWS can be enabled with a few changes to the pom and file system structure.

## Use

#### Main Functionality

The program can either be run from an IDE or the command line by running the UsedPriceFinder.java class.

The program can also be deployed to heroku. The following command runs the progam on heroku: "heroku run sh target/bin/finder"

#### Changing the input

The input is stored in an excel spreadsheet called Amazon_Form.xlsm. This spreadsheet can be edited with new Amazon ASINs and their corresponding sub-conditions/threshold prices. 

Once the spreadsheet is saved, a macro automatically saves a corresponding .csv file. It is this file that is used throughout the code. 

If the input or any code is changed and the application is being hosted on heroku, it must be re-pushed. A shell script called updator.command automatically updates the application on heroku and runs the application. This script merely makes the correct git commands to add, commit, and push, and then runs the program.

#### Dropbox Integration

The program is integrated with dropbox where it can access and modify files. Currently, a file is stored on dropbox that keeps track of when emails for each product were sent. If an email for a product update was sent within 24 hours, the product is not included on the email. 

This dropbox integration could also be used to store the input spreadsheet. Currently, the spreadsheet is included in the code, but it could easily be hosted on dropbox. This would provide easier access to the user because they would not have to re-deploy to heroku after changing the spreadsheet. This change will be implemented in the future if it seems beneficial.

## Client instructions

Instructions for a non-tech-savvy client to install the necessary software and pull/push the program to and from heroku is included in the "instructions" folder. 



