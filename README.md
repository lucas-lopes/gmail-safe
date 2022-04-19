# Gmail Safe

You work in a company, G Suite Safe, which gives its users the ability to back up
their Google accounts. Up until now, your company had backup solutions for Google
Drive and Google Contacts only, and now they want to tap into another market:
Gmail.

The product manager would like you to write a small service that performs basic
Gmail backups. Initially, Gmail backups will only be available through API calls; a
proper web interface will come in a future release.

The product manager has already promised Gmail backup functionality to some
customers. Customers have registered for the use of Gmail Safe in 7 days.
Furthermore, she promised some features which you’ll be implementing in v0.2 of
Gmail Safe.

## How to test

For the next steps, you will need to use the Windows or OS X command terminal.

At the root of the project, `mvnw` is available for Linux and OS X environments and `mvn.cmd` for 
Windows environments. No installation is required to compile and run the application.

### Requisites

* **Docker:** Facilitates the use of RabbitMQ and MongoDB on OS X and Windows.

### How to build the application on OS X?

```shell
$ cd source/gmail-safe
$ docker-compose up
$ ./mvnw clean install
```

### How to build the application on Windows?

```shell
$ cd source\gmail-safe
$ docker-compose up
$ ./mvnw.cmd clean install
```

### How to execute the application on OS X?

```shell
$ ./mvnw spring-boot:run
```

### How to execute the application on Windows?

```shell
$ ./mvnw.cmd spring-boot:run
```

### Where can I test?

You have two options:
  
1 - Using CURL.

2 - Using the Postman collection shared together this project inside `src/main/resource/postman` folder:

[Postman Collection][postman-collection]

### PS: The first time you call the POST /backup endpoint you will need to copy the link that Google provides and paste it into the browser

---

## Technologies, libraries, and tools used

* **Java 11**
* **Spring Boot:** Makes configuring an API much easier
* **RabbitMQ:** Used to process incoming Gmail API messages
* **MongoDB:** Helped filter emails by label quickly and simply
* **Docker:** Facilitates the use of RabbitMQ and MongoDB on OS X and Windows.
* **Maven:** Manage dependencies and run the application
* **Gmail Client:** Data source to create the backups

---

## Gmail Safe has four different client methods.
    - Initiate a complete backup - POST /backups
    - List all backups that have been initiated - GET /backups
    - Export Backup in ZIP file - GET /exports/{backupId}
    - Export Partial Backup (v0.2) - GET /exports/{backupId}/{label}

---

### Initiate a complete backup - POST /backups

When this service has been called, we will start the backup of all customer's emails from Gmail:
- We will authenticate the client on browser asking for permissions.
- Call the list endpoint from Gmail to receive the emails information.
  - Basic info: email id, and thread id
- Using the email id received before, we get the complete email.
  - All information: labels, historyId, message, etc.
- When received the complete email, its need to do the following:
  - Save all emails on MongoDB to be easy generate the compressed file.

### PS: The first time you call the POST /backup endpoint you will need to copy the link that Google provides and paste it into the browser

#### Request and response information:

```
POST /backups
```

**To use the endpoint above we need to provide the following information:**

**Request body:** n/a

**Status:**

    - 200 (sucess): Backup request initiated with success.
    - 202 (accepted): Returned when the person's backup is in progress and is asked another backup by the same person.
    - 403 (unauthorized): Returned when happens some authentication problem on Gmail API side and us.
    - 500 (file): Returned when the compress file step failed.

**Success Response body:**

```json
{ 
    "backupId": "625e529fb027c91896859c0f"
}
```

**Exception Response body example:**

```json
{  
    "status": 202,
    "message": "Your backup does not finish yet. Please, wait some minutes to extract your backup",
    "error": "ACCEPTED",
    "timestamp": 1650351110421
}
```

---

### List of all backups that have been initiated - GET /backups

When this endpoint has been called, will be return a list of all backups initiated:
- We will search on MongoDB collection the backup and return its result as a list
- If the status property is equal to `In Progress` means that the backup has been initiated but still running.
- If the status property is equal to `Ok` means that the backup has been done and its available.
- If the status property is equal to `Failed` means that happens some problem during the backup.

### Request and response information:

```
GET /backups
```

**To use the endpoint above we need to provide the following information:**

**Request body:** n/a

**Status:** 200 (success)

**Response body:**

```json
[
    {
        "backupId": "625e529fb027c91896859c0f",
        "status": "OK",
        "date": "2022-04-19T03:11:43.773"
    },
    {
        "backupId": "625e52d9b027c91896859c74",
        "status": "In Progress",
        "date": "2022-04-19T03:12:41.666"
    }
]
```

---

### Export Backup in ZIP file - GET /exports/{backupId}

When this endpoint has been called, will be return a zip file or an exception:
- If the status of the backupId is `OK`, the ZIP file will be downloaded.
- Anyhting diferent of `OK` will return two possibles exceptions:
  - 202 (accepted): If the backup informed is `IN PROGRESS` will be returned a 202 asking to wait some minutes because the backup still is running.
  - 404 (not found): If no backup is found with the given backupId, a backup not found exception will be returned.

### Request and response information: 

```
GET /exports/{backupId}
```

To use the endpoint above we need to provide the following information:

**Request body:** n/a

**Status:**

```
  200 (success)
  202 (accepted)
  404 (not found)
```

**Success Response body:** A streamed, compressed archive.

**Exception Response body example:**

```json
{
    "status": 404,
    "message": "The backupId 625e5805f525710940c58b4e informed was not found. Please, check this information",
    "error": "NOT_FOUND",
    "timestamp": 1650350712689
}
```

---

### Export Partial Backup (v0.2) - GET /exports/{backupId}/{label}

When this endpoint has been called, will be return a zip file or an exception:
- If the status of the backupId is `OK`, the ZIP file will be downloaded.
- Anything different of `OK` will return two possibles exceptions:
  - 202 (accepted): If the backup informed is `IN PROGRESS` will be returned a 202 asking to wait some minutes because the backup still is running.
  - 404 (not found): If no backup is found with the given backupId, a backup not found exception will be returned.

**Request and response information:**

```
GET /exports/{backupId}/{label}
```

To use the endpoint above we need to provide the following information:

**Request body:** n/a

**Status:**

    200 (success)
    202 (accepted)
    404 (not found)

**Success Response body:** A streamed, compressed archive.

**Exception Response body example:**

```json
{
    "status": 404,
    "message": "The backupId 625e5805f525710940c58b4e informed was not found. Please, check this information",
    "error": "NOT_FOUND",
    "timestamp": 1650350712689
}
```

---

## Considerations

This project was challenging, I spent many hours on it because I know it's worth it.

Despite being happy with the result, if I had a little more time I would improve the following points:

- Would switch Gmail API authentication from OAuth2 to Service Accounts to avoid having to request 
authorization from the end user.

- I would increase the coverage of unit and functional tests because I believe I didn't cover as much 
as I should.

- I would use a more performance-appropriate programming language instead of Java. I would use Golang 
or Trust as they are much more performant. I decided to use Java and Spring Boot because my knowledge 
of these technologies doesn't even compare to my knowledge of the first two languages mentioned.

I did my best on this project. It will always be like that in everything I do. I hope this code can
show how much I want to always be better than yesterday.

Thank you for all... Lucas Barbosa.


[//]: # (These are reference links used in the body of this note.)

[postman-collection]: <src/main/resources/postman/gmail_safe.postman_collection.json>