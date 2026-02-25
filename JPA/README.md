## IntelliJ shortcuts
option + enter = import
option + command + l = format code
control + option + o = remove unused imports
command + click = go inside method definition
command + z = undo
command + shift + z = redo

## maven lifecycle
clean: delete old compiled files from /target folder
compile: .class file generation (Build Project: compile, Rebuild Project: clean + compile)
test: runs unit tests
package: create .jar/.war file in /target folder
install: copies .jar file to local maven repository (~/.m2/repository)
         when we want to use this in another project as dependency

mvn clean install (clean → compile → test → package → install)
maven lifecycle is sequential, if we run a later step, Maven will automatically run all earlier phases in order

## http methods
GET: retrieve data from server
POST: send new data to server to create a new resource
PUT: update an existing resource completely
PATCH: update an existing resource partially
DELETE: remove a resource from server

OPTIONS:
client sends -> to check which HTTP methods are allowed on a particular resource
ex: browser sends an options request before making a CORS request

CORS (Cross-Origin Resource Sharing)
when a web page tries to access data from different domain

HEAD:
client sends -> to retrieve header information (Content-Type, Content-Length, Last-Modified)

## http status codes
1xx: informational -> request received, processing continues
2xx: success       -> request was successful
3xx: redirection   -> further action needed to complete the request (e.g., URL redirection)
4xx: client error  -> problem with the request (e.g., bad syntax, unauthorized, not found)
5xx: server error  -> problem on the server side (e.g., internal server error, service unavailable)

200: ok         -> request succeeded, response contains requested data
201: created    -> request succeeded, new resource created (e.g., after POST)
204: no content -> request succeeded, but no content to return (e.g., after DELETE)

400: bad request        -> server cannot process request due to client error (e.g., malformed syntax)
401: unauthorized       -> authentication required or failed
403: forbidden          -> client does not have permission to access resource
404: not found          -> requested resource not found on server
405: method not allowed -> wrong http method provided

500: internal server error -> unexpected server error occurred
502: bad gateway           -> gateway received invalid response from upstream server (backend)
503: service unavailable   -> server is currently unable to handle request (e.g., overloaded, down for maintenance)
504: gateway timeout       -> gateway did not receive timely response from upstream server (backend)
