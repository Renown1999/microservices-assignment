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
Chat

New Conversation

🤓 Explain a complex thing

Explain Artificial Intelligence so that I can explain it to my six-year-old child.


🧠 Get suggestions and create new ideas

Please give me the best 10 travel ideas around the world


💭 Translate, summarize, fix grammar and more…

Translate "I love you" French


GPT-3.5
Hello, how can I help you today?
VINIT SINGH

H NO- 2/358 UIT, Bhiwadi, Distt- Khairthal-Tijara (Raj.) 301019

Email: Singhvinit267@gmail.com | Phone: +91-7297073563 | LinkedIn: linkedin.com/in/vinit-singh

Career Objective

A passionate Embedded Systems & Power Electronics Engineer with expertise in circuit design, IoT, and industrial automation. Seeking an entry-level role to apply technical skills and contribute innovative solutions in a dynamic environment.

Education

B.Tech (Electronics & Communication Engineering) | Rajasthan Technical University, Kota | 64% (2023)

12th (RBSE Ajmer) | 65% (2017)

10th (CBSE Board) | 84% (2014)

Technical Skills

Embedded Systems & IoT

Microcontrollers: Arduino, Raspberry Pi, STM32, 8051

IoT Protocols: MQTT, HTTP, Modbus, CAN

Communication Interfaces: UART, SPI, I2C

Programming & Software

Languages: C, Embedded C, Python

Simulation & Circuit Design: Altium, KiCad, Proteus, LTSpice

Industrial Automation: PLC, SCADA, IoT-based Systems

Electrical Design Tools: AutoCAD Electrical, MATLAB

Internships & Training

Summer Internship – WEBPIET (June – July 2022)

Hands-on training in PCB Design & Embedded Programming

Developed a Sensor-based Automation Project using Arduino

Optimized circuit design, reducing power consumption by 15%

45 Days Training – Internshala (Jan – Feb 2023)

Completed IoT & Industrial Automation Course

Designed a Smart Home Automation System using ESP8266 & Arduino

Implemented MQTT communication protocol for real-time control

Projects

Smart House Automation (J-Techtrix at JECRC)

Developed an IoT-based Home Automation System using Arduino & ESP8266

Integrated temperature & motion sensors for automation & energy efficiency

Reduced energy consumption by 20% through smart automation

Water Monitoring System in Dams

Utilized pH, turbidity & flow sensors for real-time monitoring

Implemented wireless data transmission using LoRa modules

Enhanced water quality assessment and remote monitoring efficiency

Certifications

Internet of Things (IoT) Certification – Coursera (2023)

Power Electronics & Drives Certification – NPTEL (2023)

Embedded Systems & Microcontrollers – Udemy (2023)

Soft Skills

Quick Learner – Adapted to various programming environments swiftly

Leadership & Teamwork – Led a team of 3 engineers in an IoT-based automation project

Problem-Solving – Successfully troubleshot embedded system issues to improve efficiency

Strong Communication – Presented technical concepts effectively in projects and internships

Technical Summary

Skills: Embedded C, IoT, Arduino, STM32, Raspberry Pi, PCB Design, MATLAB, SCADA, Power Electronics, MQTT, Modbus, CAN, SPI, I2C

GPT-4o Mini
VINIT SINGH  
H NO- 2/358 UIT, Bhiwadi, Distt- Khairthal-Tijara (Raj.) 301019  
Email: Singhvinit267@gmail.com | Phone: +91-7297073563 | LinkedIn: [linkedin.com/in/vinit-singh](https://linkedin.com/in/vinit-singh)  

Career Objective

A passionate Embedded Systems & Power Electronics Engineer with expertise in circuit design, IoT, and industrial automation. Seeking an entry-level role to apply technical skills and contribute innovative solutions in a dynamic environment.

Education

B.Tech in Electronics & Communication Engineering  
Rajasthan Technical University, Kota  
Graduated: 2023 | Percentage: 64%
12th Grade  
RBSE Ajmer  
Graduated: 2017 | Percentage: 65%
10th Grade  
CBSE Board  
Graduated: 2014 | Percentage: 84%

Technical Skills


Embedded Systems & IoT

Microcontrollers: Arduino, Raspberry Pi, STM32, 8051

IoT Protocols: MQTT, HTTP, Modbus, CAN

Communication Interfaces: UART, SPI, I2C



Programming & Software

Languages: C, Embedded C, Python

Simulation & Circuit Design: Altium, KiCad, Proteus, LTSpice

Industrial Automation: PLC, SCADA, IoT-based Systems

Electrical Design Tools: AutoCAD Electrical, MATLAB





Internships & Training

Summer Internship – WEBPIET  
June – July 2022  

Hands-on training in PCB Design & Embedded Programming.

Developed a Sensor-based Automation Project using Arduino.

Optimized circuit design, reducing power consumption by 15%.


45 Days Training – Internshala  
Jan – Feb 2023  

Completed IoT & Industrial Automation Course.

Designed a Smart Home Automation System using ESP8266 & Arduino, implementing MQTT for real-time control.



Projects

Smart House Automation (J-Techtrix at JECRC)  

Developed an IoT-based Home Automation System using Arduino & ESP8266.

Integrated temperature & motion sensors for automation & enhanced energy efficiency.

Achieved a 20% reduction in energy consumption through smart automation.


Water Monitoring System in Dams  

Utilized pH, turbidity, and flow sensors for real-time monitoring of water quality.

Implemented wireless data transmission using LoRa modules to enhance remote monitoring efficiency.



Certifications


Internet of Things (IoT) Certification – Coursera (2023)

Power Electronics & Drives Certification – NPTEL (2023)

Embedded Systems & Microcontrollers – Udemy (2023)



Soft Skills


Quick Learner: Adapted to various programming environments swiftly.

Leadership & Teamwork: Led a team of 3 engineers in an IoT-based automation project.

Problem-Solving: Successfully troubleshot embedded system issues to improve efficiency.

Strong Communication: Presented technical concepts effectively in projects and internships.



Technical Summary

Skills: Embedded C, IoT, Arduino, STM32, Raspberry Pi, PCB Design, MATLAB, SCADA, Power Electronics, MQTT, Modbus, CAN, SPI, I2C

References available upon request.
GPT-4o Mini
coin image
10
Upgrade




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
Chat

New Conversation

🤓 Explain a complex thing

Explain Artificial Intelligence so that I can explain it to my six-year-old child.


🧠 Get suggestions and create new ideas

Please give me the best 10 travel ideas around the world


💭 Translate, summarize, fix grammar and more…

Translate "I love you" French


GPT-3.5
Hello, how can I help you today?
VINIT SINGH

H NO- 2/358 UIT, Bhiwadi, Distt- Khairthal-Tijara (Raj.) 301019

Email: Singhvinit267@gmail.com | Phone: +91-7297073563 | LinkedIn: linkedin.com/in/vinit-singh

Career Objective

A passionate Embedded Systems & Power Electronics Engineer with expertise in circuit design, IoT, and industrial automation. Seeking an entry-level role to apply technical skills and contribute innovative solutions in a dynamic environment.

Education

B.Tech (Electronics & Communication Engineering) | Rajasthan Technical University, Kota | 64% (2023)

12th (RBSE Ajmer) | 65% (2017)

10th (CBSE Board) | 84% (2014)

Technical Skills

Embedded Systems & IoT

Microcontrollers: Arduino, Raspberry Pi, STM32, 8051

IoT Protocols: MQTT, HTTP, Modbus, CAN

Communication Interfaces: UART, SPI, I2C

Programming & Software

Languages: C, Embedded C, Python

Simulation & Circuit Design: Altium, KiCad, Proteus, LTSpice

Industrial Automation: PLC, SCADA, IoT-based Systems

Electrical Design Tools: AutoCAD Electrical, MATLAB

Internships & Training

Summer Internship – WEBPIET (June – July 2022)

Hands-on training in PCB Design & Embedded Programming

Developed a Sensor-based Automation Project using Arduino

Optimized circuit design, reducing power consumption by 15%

45 Days Training – Internshala (Jan – Feb 2023)

Completed IoT & Industrial Automation Course

Designed a Smart Home Automation System using ESP8266 & Arduino

Implemented MQTT communication protocol for real-time control

Projects

Smart House Automation (J-Techtrix at JECRC)

Developed an IoT-based Home Automation System using Arduino & ESP8266

Integrated temperature & motion sensors for automation & energy efficiency

Reduced energy consumption by 20% through smart automation

Water Monitoring System in Dams

Utilized pH, turbidity & flow sensors for real-time monitoring

Implemented wireless data transmission using LoRa modules

Enhanced water quality assessment and remote monitoring efficiency

Certifications

Internet of Things (IoT) Certification – Coursera (2023)

Power Electronics & Drives Certification – NPTEL (2023)

Embedded Systems & Microcontrollers – Udemy (2023)

Soft Skills

Quick Learner – Adapted to various programming environments swiftly

Leadership & Teamwork – Led a team of 3 engineers in an IoT-based automation project

Problem-Solving – Successfully troubleshot embedded system issues to improve efficiency

Strong Communication – Presented technical concepts effectively in projects and internships

Technical Summary

Skills: Embedded C, IoT, Arduino, STM32, Raspberry Pi, PCB Design, MATLAB, SCADA, Power Electronics, MQTT, Modbus, CAN, SPI, I2C

GPT-4o Mini
VINIT SINGH  
H NO- 2/358 UIT, Bhiwadi, Distt- Khairthal-Tijara (Raj.) 301019  
Email: Singhvinit267@gmail.com | Phone: +91-7297073563 | LinkedIn: [linkedin.com/in/vinit-singh](https://linkedin.com/in/vinit-singh)  

Career Objective

A passionate Embedded Systems & Power Electronics Engineer with expertise in circuit design, IoT, and industrial automation. Seeking an entry-level role to apply technical skills and contribute innovative solutions in a dynamic environment.

Education

B.Tech in Electronics & Communication Engineering  
Rajasthan Technical University, Kota  
Graduated: 2023 | Percentage: 64%
12th Grade  
RBSE Ajmer  
Graduated: 2017 | Percentage: 65%
10th Grade  
CBSE Board  
Graduated: 2014 | Percentage: 84%

Technical Skills


Embedded Systems & IoT

Microcontrollers: Arduino, Raspberry Pi, STM32, 8051

IoT Protocols: MQTT, HTTP, Modbus, CAN

Communication Interfaces: UART, SPI, I2C



Programming & Software

Languages: C, Embedded C, Python

Simulation & Circuit Design: Altium, KiCad, Proteus, LTSpice

Industrial Automation: PLC, SCADA, IoT-based Systems

Electrical Design Tools: AutoCAD Electrical, MATLAB





Internships & Training

Summer Internship – WEBPIET  
June – July 2022  

Hands-on training in PCB Design & Embedded Programming.

Developed a Sensor-based Automation Project using Arduino.

Optimized circuit design, reducing power consumption by 15%.


45 Days Training – Internshala  
Jan – Feb 2023  

Completed IoT & Industrial Automation Course.

Designed a Smart Home Automation System using ESP8266 & Arduino, implementing MQTT for real-time control.



Projects

Smart House Automation (J-Techtrix at JECRC)  

Developed an IoT-based Home Automation System using Arduino & ESP8266.

Integrated temperature & motion sensors for automation & enhanced energy efficiency.

Achieved a 20% reduction in energy consumption through smart automation.


Water Monitoring System in Dams  

Utilized pH, turbidity, and flow sensors for real-time monitoring of water quality.

Implemented wireless data transmission using LoRa modules to enhance remote monitoring efficiency.



Certifications


Internet of Things (IoT) Certification – Coursera (2023)

Power Electronics & Drives Certification – NPTEL (2023)

Embedded Systems & Microcontrollers – Udemy (2023)



Soft Skills


Quick Learner: Adapted to various programming environments swiftly.

Leadership & Teamwork: Led a team of 3 engineers in an IoT-based automation project.

Problem-Solving: Successfully troubleshot embedded system issues to improve efficiency.

Strong Communication: Presented technical concepts effectively in projects and internships.



Technical Summary

Skills: Embedded C, IoT, Arduino, STM32, Raspberry Pi, PCB Design, MATLAB, SCADA, Power Electronics, MQTT, Modbus, CAN, SPI, I2C

References available upon request.
GPT-4o Mini
coin image
10
Upgrade




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
Chat

New Conversation

🤓 Explain a complex thing

Explain Artificial Intelligence so that I can explain it to my six-year-old child.


🧠 Get suggestions and create new ideas

Please give me the best 10 travel ideas around the world


💭 Translate, summarize, fix grammar and more…

Translate "I love you" French


GPT-3.5
Hello, how can I help you today?
VINIT SINGH

H NO- 2/358 UIT, Bhiwadi, Distt- Khairthal-Tijara (Raj.) 301019

Email: Singhvinit267@gmail.com | Phone: +91-7297073563 | LinkedIn: linkedin.com/in/vinit-singh

Career Objective

A passionate Embedded Systems & Power Electronics Engineer with expertise in circuit design, IoT, and industrial automation. Seeking an entry-level role to apply technical skills and contribute innovative solutions in a dynamic environment.

Education

B.Tech (Electronics & Communication Engineering) | Rajasthan Technical University, Kota | 64% (2023)

12th (RBSE Ajmer) | 65% (2017)

10th (CBSE Board) | 84% (2014)

Technical Skills

Embedded Systems & IoT

Microcontrollers: Arduino, Raspberry Pi, STM32, 8051

IoT Protocols: MQTT, HTTP, Modbus, CAN

Communication Interfaces: UART, SPI, I2C

Programming & Software

Languages: C, Embedded C, Python

Simulation & Circuit Design: Altium, KiCad, Proteus, LTSpice

Industrial Automation: PLC, SCADA, IoT-based Systems

Electrical Design Tools: AutoCAD Electrical, MATLAB

Internships & Training

Summer Internship – WEBPIET (June – July 2022)

Hands-on training in PCB Design & Embedded Programming

Developed a Sensor-based Automation Project using Arduino

Optimized circuit design, reducing power consumption by 15%

45 Days Training – Internshala (Jan – Feb 2023)

Completed IoT & Industrial Automation Course

Designed a Smart Home Automation System using ESP8266 & Arduino

Implemented MQTT communication protocol for real-time control

Projects

Smart House Automation (J-Techtrix at JECRC)

Developed an IoT-based Home Automation System using Arduino & ESP8266

Integrated temperature & motion sensors for automation & energy efficiency

Reduced energy consumption by 20% through smart automation

Water Monitoring System in Dams

Utilized pH, turbidity & flow sensors for real-time monitoring

Implemented wireless data transmission using LoRa modules

Enhanced water quality assessment and remote monitoring efficiency

Certifications

Internet of Things (IoT) Certification – Coursera (2023)

Power Electronics & Drives Certification – NPTEL (2023)

Embedded Systems & Microcontrollers – Udemy (2023)

Soft Skills

Quick Learner – Adapted to various programming environments swiftly

Leadership & Teamwork – Led a team of 3 engineers in an IoT-based automation project

Problem-Solving – Successfully troubleshot embedded system issues to improve efficiency

Strong Communication – Presented technical concepts effectively in projects and internships

Technical Summary

Skills: Embedded C, IoT, Arduino, STM32, Raspberry Pi, PCB Design, MATLAB, SCADA, Power Electronics, MQTT, Modbus, CAN, SPI, I2C

GPT-4o Mini
VINIT SINGH  
H NO- 2/358 UIT, Bhiwadi, Distt- Khairthal-Tijara (Raj.) 301019  
Email: Singhvinit267@gmail.com | Phone: +91-7297073563 | LinkedIn: [linkedin.com/in/vinit-singh](https://linkedin.com/in/vinit-singh)  

Career Objective

A passionate Embedded Systems & Power Electronics Engineer with expertise in circuit design, IoT, and industrial automation. Seeking an entry-level role to apply technical skills and contribute innovative solutions in a dynamic environment.

Education

B.Tech in Electronics & Communication Engineering  
Rajasthan Technical University, Kota  
Graduated: 2023 | Percentage: 64%
12th Grade  
RBSE Ajmer  
Graduated: 2017 | Percentage: 65%
10th Grade  
CBSE Board  
Graduated: 2014 | Percentage: 84%

Technical Skills


Embedded Systems & IoT

Microcontrollers: Arduino, Raspberry Pi, STM32, 8051

IoT Protocols: MQTT, HTTP, Modbus, CAN

Communication Interfaces: UART, SPI, I2C



Programming & Software

Languages: C, Embedded C, Python

Simulation & Circuit Design: Altium, KiCad, Proteus, LTSpice

Industrial Automation: PLC, SCADA, IoT-based Systems

Electrical Design Tools: AutoCAD Electrical, MATLAB





Internships & Training

Summer Internship – WEBPIET  
June – July 2022  

Hands-on training in PCB Design & Embedded Programming.

Developed a Sensor-based Automation Project using Arduino.

Optimized circuit design, reducing power consumption by 15%.


45 Days Training – Internshala  
Jan – Feb 2023  

Completed IoT & Industrial Automation Course.

Designed a Smart Home Automation System using ESP8266 & Arduino, implementing MQTT for real-time control.



Projects

Smart House Automation (J-Techtrix at JECRC)  

Developed an IoT-based Home Automation System using Arduino & ESP8266.

Integrated temperature & motion sensors for automation & enhanced energy efficiency.

Achieved a 20% reduction in energy consumption through smart automation.


Water Monitoring System in Dams  

Utilized pH, turbidity, and flow sensors for real-time monitoring of water quality.

Implemented wireless data transmission using LoRa modules to enhance remote monitoring efficiency.



Certifications


Internet of Things (IoT) Certification – Coursera (2023)

Power Electronics & Drives Certification – NPTEL (2023)

Embedded Systems & Microcontrollers – Udemy (2023)



Soft Skills


Quick Learner: Adapted to various programming environments swiftly.

Leadership & Teamwork: Led a team of 3 engineers in an IoT-based automation project.

Problem-Solving: Successfully troubleshot embedded system issues to improve efficiency.

Strong Communication: Presented technical concepts effectively in projects and internships.



Technical Summary

Skills: Embedded C, IoT, Arduino, STM32, Raspberry Pi, PCB Design, MATLAB, SCADA, Power Electronics, MQTT, Modbus, CAN, SPI, I2C

References available upon request.
GPT-4o Mini
coin image
10
Upgrade




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











