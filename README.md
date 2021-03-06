# gutenberg-manhattan
I present here a microservice-oriented application that uses some basic Docker features including docker-compose. It consists of a collection of separate servers all running in Docker containers. MongoDB is used for persistence and also runs in a container. Moreover all backend servers use reactive connection to MongoDB and Spring WebFlux rather than Spring RESTful. All backend servers are tested using JUnit5 test classes.

Here are the prerequisites for running the complete application:

A recent Docker version installed (I used 20.10.4-ce)
A recent Apache Maven version installed (I used 3.6.3)

In addition I used Spring Tool Suite for developing this demo but it is not required for running the application.

Here is the list of all 12 containers:

Server            | Image                     | Port         | Function             | Database connection
---------------   | ------------------------- | ------------ | -------------------- | -------------------
config-server     | gutenberg/config-server   | 8888         | Configuration server | None
eureka-server     | gutenberg/eureka-server   | 8761         | Discovery server     | None
books-mongodb     | mongo                     | 27017        | Schemaless database  |
book-service      | gutenberg/book-server     | 8081         | Book requests        | booksonline
review-service    | gutenberg/review-server   | 8082         | Review requests      | booksonline
order-service     | gutenberg/order-server    | 8083         | Order requests       | booksonline
user-service      | gutenberg/user-server     | 8084         | User requests        | booksonline
gateway-service   | gutenberg/gateway-server  | 5555         | Gateway              | None
frontend-service  | gutenberg/frontend-server | 8080         | frontend             | None

A named volume is used for persistence.

Volume external name  | Server          | Volume type | Source     | Target
--------------------- | --------------- | ----------- | ---------- | -----------------
gutenberg-books-db    | books-mongodb   | bind        | booksdb    | /data/db
 

A gateway is used to hide some Spring servers. Here is the list:

Server           | Port | Gateway URI
---------------- | ---- | -------------------------
book-service     | 8081 | gateway-service:5555/books
review-service   | 8082 | gateway-service:5555/reviews
order-service    | 8083 | gateway-service:5555/orders
user-service     | 8084 | gateway-service:5555/users


Here are the steps to run the application:

# 1. Images creation

Here we use the docker support of spring-boot.

In folder dockerbuild/booksonline run the shell script booksBuild. It will create a local mongo image that contains a Javascript file gutenberg.js. Then run the script booksVolume. It creates a named volume gutenberg-books-db that persists a MongoDB database and also a container that is used for testing only.

```
#!/bin/bash
# file name booksVolume
# Create a named volume and prepopulate it

# from image gutenberg/books-mongodb to volume books-mongodb

docker volume rm gutenberg-books-db
docker volume create gutenberg-books-db

docker run --name books_create -d --rm --mount source=gutenberg-books-db,target=/data/db \
--env MONGO_INITDB_ROOT_USERNAME=root --env MONGO_INITDB_ROOT_PASSWORD=root -p 27017:27017 gutenberg/books-mongodb
```

In folder dockerbuild run the shell script buildSpring. It will build all Spring images. 
Note that a test is included in the build process and that the actual build happens only if the test is successful.

```
#!/bin/bash
#buildSpring
for server in 'book-server' 'order-server' 'review-server' 'user-server'; 
do 
  echo ${server}
    pwd
    cd booksonline
    pwd
    echo "./booksRestore"
    ./booksRestore
    cd ../../$server
    pwd
    echo "./buildLocal"
    ./buildLocal
    cd ../dockerbuild
   
    echo $?
    if [ "$?" -ne 0 ]
    then 
      echo "Build failed for $server"
      exit "$?"
    fi  

done;
echo "Now building cloud and frontend "
for server in 'eureka-server' 'gateway-server' 'config-server' 'frontend-server';
do 
  echo ${server}
    cd ../$server  
    pwd
    echo "./buildLocal"
    ./buildLocal
    
    echo $?
    if [ "$?" -ne 0 ]
    then 
      echo "Build failed for $server"
      exit "$?"
    fi  

done;
```

Then run the script buildSecond that creates the final images.

Finally in the folder dockerbuild/booksonline run the script `booksKill`. It kill the container but leaves the volume prepopulated.

```
#!/bin/bash
# file name booksKill
docker rm -f books_create
```

# 3. Running the application

To start the application go to docker subdirectory and run the command:

```
sudo docker-compose up
```

```
version: '3.4'

services:

  # mongodb-books 
  mongodb-books:
    image: mongo
    volumes:
      - type: volume
        source: booksdb
        target: /data/db 
    # Note mongod not mongo because we start a server not a client      
    command: mongod --auth
    ports:
      # Note the syntax host:container
      - 28017:27017
    restart: always

 
  # config-server 
  config-server:
    image:
      gutenberg/config-server
    volumes:
      # edit to match your own filesystem
      - type: bind
        source: /home/dominique/Documents/gutenberg-manhattan/config-repo
        target: /tmp/config-repo
    ports:
      - 8888:8888
    environment:
      - EUREKASERVER_URI=http://eureka-server:8761/eureka/
      - EUREKASERVER_HOST=eureka-server
      - EUREKASERVER_PORT=8761

  # gutenberg-eureka 
  eureka-server:
    image:
      gutenberg/eureka-server
    ports:
      # host:container
      - 8761:8761

  # book-service
  book-service:
    image: gutenberg/book-server
    depends_on:
      - config-server
      - mongodb-books
    ports:
      - 8081:8081

    environment:
      - CONFIGSERVER_HOST=config-server
      - CONFIGSERVER_PORT=8888
      - GUTENBERG_CONFIG_URI=http://config-server:8888
      - EUREKASERVER_URI=http://eureka-server:8761/eureka/     
      - SPRING_PROFILES_ACTIVE=dev
      
  # review-service
  review-service:
    image: gutenberg/review-server
    depends_on:
      - config-server
      - mongodb-books
    ports:
      # host:container
      - 8082:8082

    environment:
      - CONFIGSERVER_HOST=config-server
      - CONFIGSERVER_PORT=8888
      - GUTENBERG_CONFIG_URI=http://config-server:8888
      - BASE_REVIEWS_URL=http://gateway-service:5555/reviews
      - EUREKASERVER_URI=http://eureka-server:8761/eureka/     
      - SPRING_PROFILES_ACTIVE=dev
      
  # order-service
  order-service:
    image: gutenberg/order-server
    depends_on:
      - config-server
      - mongodb-books
      - book-service
    ports:
      # host:container
      - 8083:8083

    environment:
      - CONFIGSERVER_HOST=config-server
      - CONFIGSERVER_PORT=8888
      - GUTENBERG_CONFIG_URI=http://config-server:8888
      - EUREKASERVER_URI=http://eureka-server:8761/eureka/     
      - SPRING_PROFILES_ACTIVE=dev   
      
  # user-service
  user-service:
    image: gutenberg/user-server
    depends_on:
      - config-server
      - mongodb-books
    ports:
      # host:container
      - 8084:8084

    environment:
      - CONFIGSERVER_HOST=config-server
      - CONFIGSERVER_PORT=8888
      - GUTENBERG_CONFIG_URI=http://config-server:8888
      - BASE_USERS_URL=http://gateway-service:5555/users
      - EUREKASERVER_URI=http://eureka-server:8761/eureka/     
      - SPRING_PROFILES_ACTIVE=dev   
      
  # gateway-service
  gateway-service:
    image: gutenberg/gateway-server
    depends_on:
      - config-server
      - mongodb-books
    ports:
      # host:container
      - 5555:5555
    environment:
      - BOOKSERVER_URI=http://book-service:8081
      - BOOKSERVER_HOST=book-service
      - BOOKSERVER_PORT=8081
      - REVIEWSERVER_URI=http://review-service:8082
      - REVIEWSERVER_HOST=review-service
      - REVIEWSERVER_PORT=8082
      - ORDERSERVER_URI=http://order-service:8083
      - ORDERSERVER_HOST=order-service
      - ORDERSERVER_PORT=8083
      - USERSERVER_URI=http://user-service:8084
      - USERSERVER_HOST=user-service
      - USERSERVER_PORT=8084
      - EUREKASERVER_URI=http://eureka-server:8761/eureka/     
      - CONFIGSERVER_HOST=config-server
      - CONFIGSERVER_PORT=8888
      - GUTENBERG_CONFIG_URI=http://config-server:8888
      - SPRING_PROFILES_ACTIVE=dev  
      
  # frontend-service
  frontend-service:
    image: gutenberg/frontend-server
    depends_on:
      - config-server
      - book-service
      - review-service
      - order-service
      - user-service
      - gateway-service
    ports:
      # host:container
      - 8080:8080
    environment:
      - BASE_BOOKS_URL=http://gateway-service:5555/books
      - BASE_REVIEWS_URL=http://gateway-service:5555/reviews
      - BASE_ORDERS_URL=http://gateway-service:5555/orders 
      - BASE_USERS_URL=http://gateway-service:5555/users 
      - GATEWAYSERVER_HOST=gateway-service
      - GATEWAYSERVER_PORT=5555
      - EUREKASERVER_URI=http://eureka-server:8761/eureka/     
      - GUTENBERG_CONFIG_URI=http://config-server:8888
      - SPRING_PROFILES_ACTIVE=dev            

volumes:
  booksdb:
    external: 
      name: gutenberg-books-db
```

All running Spring containers can be seen on Eureka port 8761.

The frontend itself is accessed on URL localhost:8080/gutenberg. A username and password are required. Here are the prepopulated users:

Username | Password
-------- | --------- 
Carol    | s1a2t3o4r 
Albert   | a5r6e7p8o
Werner   | t4e3n2e1t
Alice    | o8p7e6r5a
Richard  | r1o2t3a4s
Sator    | sator1234 
Arepo    | arepo1234
Tenet    | tenet1234
Opera    | opera1234
Rotas    | rotas1234


To stop the application run the command in docker subdirectory:

```
sudo docker-compose down
```


# 5. Accessing MongoDB container
To access the MongoDB container run the command:

```
sudo docker exec -it docker\_mongodb-books_1 /bin/bash
```
Then in container shell run the command:

```
mongo -u spring -p password1234 --authenticationDatabase booksonline
```

and then for example to display orders collection:

```
use booksonline
db.orders.find().pretty()
```

# 6. Screen snapshots

Here are some screen snapshots that can be seen by running this application:

Welcome page:
![alt text](images/welcome.png "Welcome page")

Book page:
![alt text](images/book.png "Book page")

Cart page:
![alt text](images/cart.png "Cart page")

Checkout page:
![alt text](images/checkout.png "Checkout page")

Payment page:
![alt text](images/checkoutSuccess.png "Payment page")

Cachan, April 7 2021
 
Dominique Ubersfeld
