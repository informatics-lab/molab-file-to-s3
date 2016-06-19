# molab-file-to-s3
Uploads files from a given directory to a given AWS-S3 bucket.
Files should be placed into the `incomingDirectoryPath` where they will be automatically moved into the `processingDirectoryPath` then uploaded to the `toS3BucketName`. 


##Configuration
Configuration is held in the `src/main/resources/application.properties` file.
 * `incomingDirectoryPath` - the directory from which to read data from.   
 * `processingDirectoryPath` - the directory from which to upload data from.
 * `errorDirectoryPath` - the directory to move and write error files to.
 * `errorFileExpireAfterMillis` - the amount of time to keep error files for (currently 10 days = 864000000).
 * `toS3BucketName` - the AWS-S3 bucket to upload data to.  

When data is being transferred the AWS-S3 bucket will automatically have child directories created within it for the current year and month. Final AWS-S3 bucket path will be in the format `parent-bucket-name/YYYY/M`. Bucket will be created if it doesn't already exist.  
If an error occurs whilst uploading a file the upload will be attempted 2 more times, following this if there is still a problem the file will be moved to the `errorDirectoryPath` an error detail file will be created for this file `filename.<DateTime of error (yyyy-MM-ddTHH:mm:ssZ)>.err` this will be stored alongisde the original file in the `errorDirectoryPath`.

####Important!
This application requires permission to the AWS-S3 service.  
The following environment variables must be set for the application to start and run successfully:  
 * `AWS_ACCESS_KEY_ID` - your access key id
 * `AWS_SECRET_ACCESS_KEY` - your secret access key 

##Building
To build the project from source use: `$ mvn clean install`.  
This will create a zip file containing the built jar `target/molab-file-to-s3-<VERSION>-distribution.zip`.  

##Deploying
`unzip` the .zip file to extract the built jar file.  

##Running
Use `$ java -jar molab-file-to-s3.jar` to start the application.  

##Docker
TODO



