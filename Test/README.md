## database setup details
database: testdb
schema: staging
table: user

## psql commands
psql -U postgres -d testdb -p 5433  //connecting with testdb database running on port 5433
\dn                                 //list all schemas
\dt staging.*                      //list all tables in staging schema

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