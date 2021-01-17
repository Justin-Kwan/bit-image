
# Bit-image



```
   ___ _ _   _                            
  / __(_) |_(_)_ __ ___   __ _  __ _  ___ 
 /__\// | __| | '_ ` _ \ / _` |/ _` |/ _ \
/ \/  \ | |_| | | | | | | (_| | (_| |  __/
\_____/_|\__|_|_| |_| |_|\__,_|\__, |\___|
                               |___/      
```

This is my submission for the Shopify Summer 2021 Backend developer internship coding challenge.

Bit-image is a fast and straight-forward service that allows you to upload, store, and view your images online.

## Table of Contents

### Overview

- Entity Relationship

### Running Locally

- Docker

- Source

### Architecture

- Authorization

- Upload and Storage

### Deployment

- Java Server

- PostgreSQL

- Beanstalkd

### Using the API

- Generate Authorization Token

- Create a User Account

- Upload Multiple Images

	- Get Upload URLs

	- Directly Upload Image Files

	- Notify Bit-image

- Get Image Summary List

	- By Name, Tag or Content Label

	- Public Images

- Get Single Expanded Image

- Delete Multiple Images

- Deleting User Account


### Running Locally

Bit-image requires AWS S3 and Rekognition, PostgreSQL, and Beanstalkd as dependencies.

Please update `src/main/resources/application.dev.properties` with AWS credentials, a PostgreSQL server host, and a Beanstalkd server host before running the app.

#### Docker

Running the Java Gradle build is easy with Docker compose.

```
docker-compose up
```

#### Source

Aternatively, to build from source

#### Prerequisites

- [Gradle 6.7.1](https://gradle.org/releases/)

- [Java JDK 15](https://www.oracle.com/ca-en/java/technologies/javase-jdk15-downloads.html)

- [PostgreSQL](https://www.postgresql.org/download/)

- [Beanstalkd](https://beanstalkd.github.io/)

The following steps assume that all dependencies are present.

- Build the executables

```
gradle build
```

- Run the Micronaut server

```
gradle run
```


## Overview

### Entity Relationship

- An user can have many images

- An image can have many tags

- A tag can be associated to many images (useful for searching images by tag)

- An image can have many content labels

- A content label can be associated to many images


## Deployment

### Java Server


The Bit-image Java and Micronaut server is Dockerized and deployed on an AWS EC2 instance. We can attach Bit-image's EC2 instance to an autoscaling group to provide higher availability if and when the load and traffice requires it.

// add image
// add base url endpoint

### PostgreSQL

Our Postgres database is managed by a single AWS RDS instance. This provides the convenience of any fully managed service, and the flexibility to scale and add read replicas to reduce load on our primary write instance if our load increases.

// add image

### Beanstalkd

The Beanstalkd message queue is deployed in a single Digital Ocean droplet.

// add admin image

## Using the API

Let's see how to send some cURL HTTP requests to interact with the public and authorized API endpoints.

### Generate Authorization Token

A token without expiry has been generated for your use.

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI
```

Authorization tokens can also be obtained by calling CAS.

Example Request
```
curl 'http://198.199.78.73:5000/loginSubmit' \
	-X POST \
	-H "Content-type: application/json" \
	-d '{ "email" : "shopify@email.com", "password" : "ecommerce_2021" }'
```

Example Response

```json
{
   "status":202,
   "token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI"
}
```

### Create a User Account

Now, that we are signed up and have our auth token, we need to create our user account with Bit-image service.

Definition
```
POST http://54.198.201.117/api/v1/users
```

Example Request
```
curl 'http://54.198.201.117/api/v1/users' \
	-X POST \
	-H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI'
```

Example Response

```json
{
    "id": "73578802-dfea-45cf-bb43-baffb93ad8bb",
    "image_upload_count": 0
}
```

Example Error (409 Conflict, User Already Exists)

```josn
{
	"error": "Conflict occurred, resource already exists",
	"message": "User with provided id already exists",
	"detail": "Ensure that the provided resource id and user id are correct"
}
```

### Upload Multiple Images

#### Get upload URLs

We (as the client) can request for multiple upload urls (pointing to an AWS S3 bucket) which we will then directly upload our images to. We should specify the number or upload urls that should be returned, which should match the number of images we wish to upload. We can ask Bit-image for up to 1000 upload urls at a time.

Definition

```
GET http://54.198.201.117/api/v1/images/upload_urls
```

Fields

- image_count



Example Request

```
curl 'http://54.198.201.117/api/v1/images/upload_urls?image_count=2' \
	-X GET \
	-H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI'
```

Example Response

```json
{
    "image_upload_urls": [
        {
            "url": "https://receiveduserimages.s3.amazonaws.com/users/73578802-dfea-45cf-bb43-baffb93ad8bb/7aed75b2-08af-4ce8-9be0-50934a75ac98?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210117T103406Z&X-Amz-SignedHeaders=host&X-Amz-Expires=120&X-Amz-Credential=AKIA43EBGQEFS7TXCKWE%2F20210117%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=4262289d1c7c181b984511d82e1137268e5c26d55d158a147b4f17ad138c7b52",
            "method": "PUT",
            "image_id": "7aed75b2-08af-4ce8-9be0-50934a75ac98"
        },
        {
            "url": "https://receiveduserimages.s3.amazonaws.com/users/73578802-dfea-45cf-bb43-baffb93ad8bb/1ca382d2-44f9-4fa8-acff-ab950ace5473?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210117T103406Z&X-Amz-SignedHeaders=host&X-Amz-Expires=120&X-Amz-Credential=AKIA43EBGQEFS7TXCKWE%2F20210117%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=4262289d1c7c181b984511d82e1137268e5c26d55d158a147b4f17ad238c7p78",
            "method": "PUT",
            "image_id": "1ca382d2-44f9-4fa8-acff-ab950ace5473"
        }
    ]
}
```

#### Directly Upload Image Files

Let's now upload our image files directly to AWS S3. Only 5 images should be uploaded at a time to our provided image upload URLs. We will then notify the Bit-image after every 5 image uploads (to prevent blocking the web-server and potential memory usage bottlenecks).

This process can be easily automated with a client-side implementation.

Example Request (Upload Image File To URL)

```
// postman image
// postman body
```

*It is important to upload our file as a binary

#### Notify Bit-image

When notifying Bit-image that up to 5 images are uploaded, we should use a POST request, specifying the name and custom tags that we want to associate to each image. (We can search our images by these later).

We also need to specify the MD5 hash of our 5 images in the request body as well. This is important because it allows us to determine the you've uploaded the correct image, and that your custom provided data will synchronize with the correct image.

Here's an useful site to generate the MD5 hash of your image:
- https://md5file.com/calculator

Definition

```
POST http://54.198.201.117/api/v1/images
```


Example Request (Notify Bit-image that up to 5 images are uploaded)

```
curl http://54.198.201.117/api/v1/images
	-X POST
	-H "Content-type: application/json" \
	-H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI' \
	-d '
{
   "images":[
      {
         "id":"7aed75b2-08af-4ce8-9be0-50934a75ac98",
         "name":"nicelettuce",
         "hash":"37d0f0e9387ad830b29647eccd0d304e",
         "is_private":false,
         "tags":[
            {
               "name":"leafy"
            },
            {
               "name":"green"
            },
            {
               "name":"notsotasty"
            },
            {
               "name":"salad"
            }
         ]
      },
      {
         "id":"1ca382d2-44f9-4fa8-acff-ab950ace5473",
         "name":"coolcat",
         "hash":"b3929ff3a67c4ee53f4317a105a69042",
         "is_private":false,
         "tags":[
            {
               "name":"coast"
            },
            {
               "name":"clearsky"
            },
            {
               "name":"smallcat"
            }
         ]
      }
   ]
}'
```

Our list of images will be returned in a summarized format, with a generated view link for each one. We can click on these links to view our images! Note that these image view links will expire after 1 hour.

Example Response

```json
{
    "images": [
        {
            "id": "7aed75b2-08af-4ce8-9be0-50934a75ac98",
            "name": "nicelettuce",
            "is_private": "false",
            "file_format": "jpeg",
            "view_url": "https://storeduserimages.s3.amazonaws.com/users/73578802-dfea-45cf-bb43-baffb93ad8bb/7aed75b2-08af-4ce8-9be0-50934a75ac98?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210117T103406Z&X-Amz-SignedHeaders=host&X-Amz-Expires=120&X-Amz-Credential=AKIA43EBGQEFS7TXCKWE%2F20210117%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=4262289d1c7c181b984511d82e1137268e5c26d55d158a147b4f17ad138c7b52"
        },
        {
            "id": "1ca382d2-44f9-4fa8-acff-ab950ace5473",
            "name": "coolcat",
            "is_private": "false",
            "file_format": "jpeg",
            "view_url": "https://storeduserimages.s3.amazonaws.com/users/73578802-dfea-45cf-bb43-baffb93ad8bb/1ca382d2-44f9-4fa8-acff-ab950ace5473?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210117T103406Z&X-Amz-SignedHeaders=host&X-Amz-Expires=120&X-Amz-Credential=AKIA43EBGQEFS7TXCKWE%2F20210117%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=823733d082d863399d2685c85bb589f1cbdf6fa58e04ced5b37ea33d9dd3ac70"
        }
    ]
}
```

Example Error (413 Request Entity Too LArge, Image size exceeds limit)
```
{
	"error": "Resource size exceeds upload limit",
	"message": "Image with id '6d5cabdd-fd1b-4ebb-bed0-d8f29bd46ab6' has size of 3 MB, which exceeds the max upload size of 2 MB",
	"detail": "Ensure that the provided resource is within size limit"
}
```

Example Error (415 Unsupported Media Type, Incorrect image format)

```
{
	"error": "Resource media type is unsupported",
	"message": "Uploaded image with id '030af0ae-15c0-46ea-a5ca-671c5b524cbe' is of 'pdf' format, which is not supported",
	"detail": "Ensure that the provided resource media type is correct"
}
```


### Get Image Summary List

#### By Name, Tag or Content Label

We can get a summarized list of our own images by fuzzy matching against a tag or image name which we provided, or by the contents of the actual image.

Definition

```
GET http://54.198.201.117/api/v1/images/private/summary
```

Fields

- name
- tag
- content_label

If none are specified, then all of the user's images are returned.

Example Request

```
curl 'http://54.198.201.117/api/v1/images/private/summary?name=nicelettuce' \
	-X GET \
	-H "Content-type: application/json" \
	-H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI"
```


#### Public Images

You can also get a list of the 100 newest public images created by other users.

Definition

```
GET http://54.198.201.117/api/v1/images/public/summary
```

Example Request

```
curl 'http://54.198.201.117/api/v1/images/public/summary' \
	-X GET \
	-H "Content-type: application/json" \
	-H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI"
```

Example Response

```
{
    "images": [
    	{
        	"id": "1ca382d2-44f9-4fa8-acff-ab950ace5473",
        	"name": "coolcat",
        	"is_private": "false",
        	"file_format": "jpeg",
        	"view_url": "https://storeduserimages.s3.amazonaws.com/users/73578802-dfea-45cf-bb43-baffb93ad8bb/1ca382d2-44f9-4fa8-acff-ab950ace5473?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210117T122431Z&X-Amz-SignedHeaders=host&X-Amz-Expires=120&X-Amz-Credential=AKIA43EBGQEFS7TXCKWE%2F20210117%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=035106865804024b8ad49a12ca8f96fb987e40da0b7fd8ded0e33395bac3e601"
    	}, 
        {
        	"id": "7aed75b2-08af-4ce8-9be0-50934a75ac98",
        	"name": "nicelettuce",
        	"is_private": "false",
        	"file_format": "jpeg",
        	"view_url": "https://storeduserimages.s3.amazonaws.com/users/73578802-dfea-45cf-bb43-baffb93ad8bb/7aed75b2-08af-4ce8-9be0-50934a75ac98?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210117T122431Z&X-Amz-SignedHeaders=host&X-Amz-Expires=120&X-Amz-Credential=AKIA43EBGQEFS7TXCKWE%2F20210117%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=48c5a05ac65b2084d30a41d1ab9e13efa9cde9fb60c22653533dcc79fe660955"
    	}
	]
}
```

### Get Single Expanded Image

You can also get a single image in an expanded format.

Definition
```
http://ec2-54-198-201-117.compute-1.amazonaws.com/api/v1/images/d75a5b4c-39cf-4ecb-84f3-668c2d94a006/expanded
```

Example Request
```
curl 'http://54.198.201.117/api/v1/images/1ca382d2-44f9-4fa8-acff-ab950ace5473/expanded' \
	-X GET
    -H "Content-type: application/json" \
    -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI"
```

Example Response
```
{
   "id":"1ca382d2-44f9-4fa8-acff-ab950ace5473",
   "name":"coolcat",
   "hash":"b3929ff3a67c4ee53f4317a105a69042",
   "is_private":"false",
   "size_mb":0.12,
   "file_format":"jpeg",
   "view_url":"https://storeduserimages.s3.amazonaws.com/users/73578802-dfea-45cf-bb43-baffb93ad8bb/1ca382d2-44f9-4fa8-acff-ab950ace5473?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210117T131721Z&X-Amz-SignedHeaders=host&X-Amz-Expires=120&X-Amz-Credential=AKIA43EBGQEFS7TXCKWE%2F20210117%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=fe0733eba08b299164930ec3a74027f0fb74628668f614b4c099aa4cff9a84bf",
   "tags":[
      {
         "name":"smallcat",
         "id":"20640db3-6778-4f88-89bf-537d48542b17"
      },
      {
         "name":"clearsky",
         "id":"5208ddae-d9ff-404a-8420-82204d073133"
      },
      {
         "name":"coast",
         "id":"fd9efd72-00e9-4a83-9222-9ade9c4aa575"
      }
   ],
   "labels":[
      {
         "id":"e17b18cc-6101-4ebc-a0aa-f4a0c00d8812",
         "name":"manx"
      },
      {
         "id":"de40b66c-8c85-4e11-89d6-7a6c097e9576",
         "name":"cloud"
      },
      {
         "id":"ef1af479-38cd-447b-bb98-f4cc0c143501",
         "name":"animal"
      },
      {
         "id":"3aaeba08-fee4-48f4-94f8-7d0744679e86",
         "name":"nature"
      },
      {
         "id":"7bf73325-8257-40a8-8052-c96dde3e9f23",
         "name":"landscape"
      },
      {
         "id":"b5295834-c768-412a-be37-df57ad59bd69",
         "name":"cliff"
      },
      {
         "id":"f6af5808-a7b6-4a56-8073-acf2f9465260",
         "name":"mammal"
      },
      {
         "id":"faca3286-578a-416f-86bf-f60a0ca82fec",
         "name":"mountain"
      },
      {
         "id":"c8d25065-ed37-40a0-8b34-5f34690b3b74",
         "name":"urban"
      },
      {
         "id":"f90cf01c-8bc6-4eac-b400-5eab38fe4ae5",
         "name":"city"
      },
      {
         "id":"eb9c0614-4b19-4dfc-b3df-0ec975c9d80d",
         "name":"building"
      },
      {
         "id":"561b7fe6-33b0-42d4-b2ea-db5d226e6799",
         "name":"cat"
      },
      {
         "id":"59a2f585-a0a0-4955-bcec-76410d6fe357",
         "name":"scenery"
      },
      {
         "id":"17f28d7f-714d-4a88-b016-23b6aeb7c957",
         "name":"pet"
      },
      {
         "id":"78f947fb-5472-4e2a-97d0-fe3e67d51cb2",
         "name":"outdoors"
      },
      {
         "id":"266a8de6-30d0-421b-a3dd-78a6d4e970ef",
         "name":"dog"
      },
      {
         "id":"8e323915-1faa-449a-9a64-04645321d7c0",
         "name":"rock"
      },
      {
         "id":"7aec3974-3d6a-4896-97a4-f069d6445e34",
         "name":"mountain range"
      },
      {
         "id":"76280af3-aaad-4265-9ac3-50d7c201570c",
         "name":"peak"
      },
      {
         "id":"8a799bed-6202-4c81-b5e3-7d3d35f8dca5",
         "name":"sky"
      }
   ]
}
```

### Delete Multiple Images

You can delete multiple images at once.

Definition
```
DELETE http://54.198.201.117/api/v1/images
```

Fields

-	id

Example Request

```
curl 'http://54.198.201.117/api/v1/images?id=7aed75b2-08af-4ce8-9be0-50934a75ac98&id=1ca382d2-44f9-4fa8-acff-ab950ace5473' \
	-X DELETE
	-H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI"
```


### Delete User Account

You are also able to delete your user account. When you delete your user account, all of your images will be permanently deleted.

Definition

```
http://54.198.201.117/api/v1/users/:id
```

Example Request

```
curl 'http://54.198.201.117/api/v1/users/0e2d2c9a-b24a-47fd-b017-3fdc8156d057' \
	-X DELETE
    -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNob3BpZnlAZW1haWwuY29tIiwidXNlcl9pZCI6IjczNTc4ODAyLWRmZWEtNDVjZi1iYjQzLWJhZmZiOTNhZDhiYiJ9.q9p4zTi0QRINxnUK-VC_eqX0k5WEPHM9OzvdwcYs3UI"
```
