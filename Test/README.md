## database setup details
database: postgres
schema: staging

## IntelliJ shortcuts
option + enter = import
option + command + l = format code
control + option + o = remove unused imports
command + click = go inside method definition

## maven lifecycle
clean: delete old compiled files from /target folder
compile: .class file generation
test: runs unit tests
package: create .jar/.war file in /target folder
install: copies .jar file to local maven repository (~/.m2/repository)
         when we want to use this in another project as dependency

Build Project: compile
Rebuild Project: clean + compile

mvn clean install (clean → compile → test → package → install)
Maven lifecycle is sequential, if we run a later step, Maven will automatically run all earlier phases in order

## http methods
GET: retrieve data from server
POST: send new data to server to create a new resource
PUT: update an existing resource completely
PATCH: update an existing resource partially
DELETE: remove a resource from server