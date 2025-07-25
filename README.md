Prerequisites:
Java JDK 17+ installed and JAVA_HOME set.
Maven 3.6+ installed.
Docker Desktop installed (select Windows AMD or ARM based on your system architecture).
Postman (for API testing).
Git (if cloning from repository).
MySQL DB
MongoDB

Project Structure:
microservices-assignment/
│
├── docker-compose.yml
├── README.md
│── pom.xml  (Parent project)
├── user-service/  (Used H2 DB)
│   ├── Dockerfile
│   └── src/...
│
├── order-service/  (Used MySQL DB and communicate with user service)
│   ├── Dockerfile
│   └── src/...
│
└── product-service/  (Used MongoDB)
    ├── Dockerfile
    └── src/...
 



Step 1: Create Parent Project
cd D:\microservices-workspace
mvn archetype:generate -DgroupId=com.sarvika -DartifactId=microservices-assignment -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false


Step 2: Create Services as Modules
From the same root CMD (microservices-assignment):
mvn archetype:generate -DgroupId=com.sarvika.userservice -DartifactId=user-service -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.sarvika.orderservice -DartifactId=order-service -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
mvn archetype:generate -DgroupId=com.sarvika.productservice -DartifactId=product-service -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false


Build the Project :
Navigate to the root project folder and run:
cd D:\microservices-workspace\microservices-assignment
mvn clean install -DskipTests

Docker Setup: 
Verify Docker is working:
docker --version


Create Dockerfiles in each service folder:

cd user-service
echo FROM openjdk:17-jdk-alpine > Dockerfile
echo COPY target/user-service.jar user-service.jar >> Dockerfile
echo ENTRYPOINT ["java","-jar","/user-service.jar"] >> Dockerfile

cd ../order-service
echo FROM openjdk:17-jdk-alpine > Dockerfile
echo COPY target/order-service.jar order-service.jar >> Dockerfile
echo ENTRYPOINT ["java","-jar","/order-service.jar"] >> Dockerfile

cd ../product-service
echo FROM openjdk:17-jdk-alpine > Dockerfile
echo COPY target/product-service.jar product-service.jar >> Dockerfile
echo ENTRYPOINT ["java","-jar","/product-service.jar"] >> Dockerfile


Create docker-compose.yml in root folder

version: '3.8'

services:

  mysql:
    image: mysql:8.0
    container_name: mysql-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: orderdb
      MYSQL_USER: orderuser
      MYSQL_PASSWORD: orderpass
    ports:
      - "3306:3306"
    networks:
      - microservices-net

  mongo:
    image: mongo
    container_name: mongo-db
    ports:
      - "27017:27017"
    networks:
      - microservices-net

  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    networks:
      - microservices-net

  order-service:
    build: ./order-service
    ports:
      - "8082:8082"
    depends_on:
      - mysql
    networks:
      - microservices-net

  product-service:
    build: ./product-service
    ports:
      - "8083:8083"
    depends_on:
      - mongo
    networks:
      - microservices-net

networks:
  microservices-net:



Build Docker Images & Start Services (From the root folder)
docker-compose build
docker-compose up


Verfiy the running services:
| Service         | URL                                                                      |
| --------------- | ------------------------------------------------------------------------ |
| User Service    | [http://localhost:8081/api/users](http://localhost:8081/api/users)       |
| Order Service   | [http://localhost:8082/api/orders](http://localhost:8082/api/orders)     |
| Product Service | [http://localhost:8083/api/products](http://localhost:8083/api/products) |


Swagger UI :
http://localhost:8081/swagger-ui/index.html
http://localhost:8082/swagger-ui/index.html
http://localhost:8083/swagger-ui/index.html

API Testing (via Postman)
Create a User:
POST http://localhost:8081/api/users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
  "phone": "9876543210"
}

Create an Order(with Valid userId)
POST http://localhost:8082/api/orders
Content-Type: application/json

{
  "product": "Laptop",
  "quantity": 2,
  "price": 75000,
  "userId": 1
}

Create Product:
POST http://localhost:8083/api/products
Content-Type: application/json

{
  "name": "Phone",
  "description": "Android 5G",
  "price": 25000
}


Stop Services:
docker-compose down


Service Communication (REST API)
Scenario:
order-service needs to validate or fetch User information while placing orders.

Tech Used:
RestTemplate in order-service
user-service exposes GET /api/users/{id}


Swagger:
User Service Swagger:
{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [
    {
      "url": "http://localhost:8081",
      "description": "Generated server url"
    }
  ],
  "paths": {
    "/api/users/{id}": {
      "get": {
        "tags": [
          "user-controller"
        ],
        "operationId": "getById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/UserDTO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "user-controller"
        ],
        "operationId": "updateUser",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/UserDTO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "user-controller"
        ],
        "operationId": "delete",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/users": {
      "get": {
        "tags": [
          "user-controller"
        ],
        "operationId": "getAll",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/UserDTO"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "user-controller"
        ],
        "operationId": "create",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/UserDTO"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "UserDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "email": {
            "type": "string"
          },
          "phone": {
            "type": "string"
          }
        }
      }
    }
  }
}







{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [
    {
      "url": "http://localhost:8081",
      "description": "Generated server url"
    }
  ],
  "paths": {
    "/api/users/{id}": {
      "get": {
        "tags": [
          "user-controller"
        ],
        "operationId": "getById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/UserDTO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "user-controller"
        ],
        "operationId": "updateUser",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/UserDTO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "user-controller"
        ],
        "operationId": "delete",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/users": {
      "get": {
        "tags": [
          "user-controller"
        ],
        "operationId": "getAll",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/UserDTO"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "user-controller"
        ],
        "operationId": "create",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/UserDTO"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "UserDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "email": {
            "type": "string"
          },
          "phone": {
            "type": "string"
          }
        }
      }
    }
  }
}




Order Service Swagger:
{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [
    {
      "url": "http://localhost:8082",
      "description": "Generated server url"
    }
  ],
  "paths": {
    "/api/orders/{id}": {
      "get": {
        "tags": [
          "order-controller"
        ],
        "operationId": "getById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/OrderResponseDTO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "order-controller"
        ],
        "operationId": "update",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/OrderDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/OrderDTO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "order-controller"
        ],
        "operationId": "delete",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/orders": {
      "get": {
        "tags": [
          "order-controller"
        ],
        "operationId": "getAll",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/OrderDTO"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "order-controller"
        ],
        "operationId": "create",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/OrderDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/OrderResponseDTO"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "OrderDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "product": {
            "type": "string"
          },
          "quantity": {
            "type": "integer",
            "format": "int32"
          },
          "price": {
            "type": "number",
            "format": "double"
          },
          "userId": {
            "type": "integer",
            "format": "int64"
          }
        }
      },
      "OrderResponseDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "product": {
            "type": "string"
          },
          "quantity": {
            "type": "integer",
            "format": "int32"
          },
          "price": {
            "type": "number",
            "format": "double"
          },
          "user": {
            "$ref": "#/components/schemas/UserDTO"
          }
        }
      },
      "UserDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "email": {
            "type": "string"
          }
        }
      }
    }
  }
}
 






{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [
    {
      "url": "http://localhost:8082",
      "description": "Generated server url"
    }
  ],
  "paths": {
    "/api/orders/{id}": {
      "get": {
        "tags": [
          "order-controller"
        ],
        "operationId": "getById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/OrderResponseDTO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "order-controller"
        ],
        "operationId": "update",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/OrderDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/OrderDTO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "order-controller"
        ],
        "operationId": "delete",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/orders": {
      "get": {
        "tags": [
          "order-controller"
        ],
        "operationId": "getAll",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/OrderDTO"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "order-controller"
        ],
        "operationId": "create",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/OrderDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/OrderResponseDTO"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "OrderDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "product": {
            "type": "string"
          },
          "quantity": {
            "type": "integer",
            "format": "int32"
          },
          "price": {
            "type": "number",
            "format": "double"
          },
          "userId": {
            "type": "integer",
            "format": "int64"
          }
        }
      },
      "OrderResponseDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "product": {
            "type": "string"
          },
          "quantity": {
            "type": "integer",
            "format": "int32"
          },
          "price": {
            "type": "number",
            "format": "double"
          },
          "user": {
            "$ref": "#/components/schemas/UserDTO"
          }
        }
      },
      "UserDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "email": {
            "type": "string"
          }
        }
      }
    }
  }
}



Product Service Swagger:
{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [
    {
      "url": "http://localhost:8083",
      "description": "Generated server url"
    }
  ],
  "paths": {
    "/api/products/{id}": {
      "get": {
        "tags": [
          "product-controller"
        ],
        "operationId": "get",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ProductDTO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "product-controller"
        ],
        "operationId": "update",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/ProductDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ProductDTO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "product-controller"
        ],
        "operationId": "delete",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/products": {
      "get": {
        "tags": [
          "product-controller"
        ],
        "operationId": "getAll",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/ProductDTO"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "product-controller"
        ],
        "operationId": "create",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/ProductDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ProductDTO"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "ProductDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string"
          },
          "name": {
            "type": "string"
          },
          "category": {
            "type": "string"
          },
          "price": {
            "type": "number",
            "format": "double"
          },
          "stock": {
            "type": "integer",
            "format": "int32"
          }
        }
      }
    }
  }
}



{
  "openapi": "3.0.1",
  "info": {
    "title": "OpenAPI definition",
    "version": "v0"
  },
  "servers": [
    {
      "url": "http://localhost:8083",
      "description": "Generated server url"
    }
  ],
  "paths": {
    "/api/products/{id}": {
      "get": {
        "tags": [
          "product-controller"
        ],
        "operationId": "get",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ProductDTO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "product-controller"
        ],
        "operationId": "update",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/ProductDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ProductDTO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "product-controller"
        ],
        "operationId": "delete",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/products": {
      "get": {
        "tags": [
          "product-controller"
        ],
        "operationId": "getAll",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/ProductDTO"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "product-controller"
        ],
        "operationId": "create",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/ProductDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ProductDTO"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "ProductDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string"
          },
          "name": {
            "type": "string"
          },
          "category": {
            "type": "string"
          },
          "price": {
            "type": "number",
            "format": "double"
          },
          "stock": {
            "type": "integer",
            "format": "int32"
          }
        }
      }
    }
  }
}











