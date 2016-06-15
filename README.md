# molab-file-to-s3
Sends files from a given directory to a given AWS-S3 bucket

##Configuration
Configuration is held in the `src/main/resources/application.properties` file.    
 * `fromDirectoryPath` - the directory from which to read data from.  
 * `toS3ParentBucketName` - the parent directory of the AWS-S3 bucket.  

When data is being transferred the AWS-S3 bucket will automatically have child directories created within it for the current year and month. Final AWS-S3 bucket path will be in the format `parent-bucket-name/YYYY/MMM`. Bucket will be created if it doesn't allready exist.  

##Building
To build the project from source use: `$ mvn clean install`.  
This will create a zip file containing the built jar `target/molab-file-to-s3-<VERSION>-distribution.zip`.  

##Deploying
`unzip` the .zip file to extract the built jar file.  

##Running
Use `$ java -jar molab-file-to-s3.jar` to start the application.  

##Docker
TODO



