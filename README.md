# BookMyShow Clone

A Java-based Spring Boot application that replicates the core functionality of BookMyShow, a popular movie and entertainment ticketing platform.

## 📋 Overview

BookMyShow Clone is a full-featured ticketing system built with modern Java technologies. It provides functionality for:
- Movie and event management
- Theater and venue management
- Ticket booking and reservations
- User authentication and authorization
- Payment processing capabilities

## 🛠️ Tech Stack

- **Backend Framework:** Spring Boot 4.0.6
- **Language:** Java 25
- **Database:** PostgreSQL 15
- **Build Tool:** Maven
- **ORM:** Spring Data JPA
- **Authentication:** Spring Security + JWT (JSON Web Tokens)
- **Validation:** Spring Validation
- **Additional Libraries:**
  - Lombok (for reducing boilerplate code)
  - JJWT (for JWT token handling)

## 📦 Key Dependencies

```xml
- spring-boot-starter-data-jpa
- spring-boot-starter-webmvc
- spring-boot-starter-validation
- spring-boot-starter-security
- jjwt-api v0.11.5 (JWT handling)
- postgresql (database driver)
- lombok
```

## 🚀 Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.6+
- Docker & Docker Compose (optional, for containerized setup)
- PostgreSQL 15

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Harijogi333/BookMyShowClone.git
   cd BookMyShowClone
   ```

2. **Using Docker Compose (Recommended):**
   ```bash
   docker-compose up -d
   ```
   This will start:
   - PostgreSQL database on port 5432
   - PgAdmin (PostgreSQL admin tool) on port 5050

3. **Build the application:**
   ```bash
   ./mvnw clean install
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   The application will start on the default Spring Boot port (8080).

### Configuration

Database credentials (from docker-compose.yml):
- **Host:** localhost
- **Port:** 5432
- **Database:** bookmyshow_db
- **Username:** admin
- **Password:** password123

Update your `application.properties` or `application.yml` accordingly.

## 📁 Project Structure

```
BookMyShowClone/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/clone/bookmyshow/
│   │   │       ├── controllers/
│   │   │       ├── services/
│   │   │       ├── repositories/
│   │   │       ├── entities/
│   │   │       ├── dto/
│   │   │       ├── security/
│   │   │       ├── exceptions/
│   │   │       ├── utils/
│   │   │       └── BookMyShowApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── docker-compose.yml
└── mvnw
```

## 🔐 Security Features

- **JWT Authentication:** Secure token-based authentication
- **Spring Security:** Role-based access control
- **Password Encoding:** Bcrypt encryption for passwords
- **Validation:** Input validation on all API endpoints

## 🗄️ Database

### PostgreSQL Setup

The application uses PostgreSQL with the following configuration:
- **Version:** 15-Alpine
- **Admin Interface:** PgAdmin available at http://localhost:5050

### PgAdmin Access

- **Email:** admin@admin.com
- **Password:** admin

## 🔗 API Documentation

Key API endpoints include:
- **Authentication Endpoints:** User login/registration
- **Movie Endpoints:** Retrieve and manage movies
- **Theater Endpoints:** Manage theaters and venues
- **Booking Endpoints:** Create and manage bookings
- **User Endpoints:** User profile management

## 🧪 Testing

Run the test suite:
```bash
./mvnw test
```

The project includes:
- Spring Boot test starter
- Spring Data JPA test support
- Web MVC test support

## 🐳 Docker Support

The project includes a `docker-compose.yml` for easy containerization.

### To build and run with Docker:

```bash
# Build Docker image
docker build -t bookmyshow-clone .

# Run with docker-compose
docker-compose up -d
```

## 🔄 Maven Commands

- **Clean build:** `./mvnw clean install`
- **Run application:** `./mvnw spring-boot:run`
- **Run tests:** `./mvnw test`
- **Package application:** `./mvnw clean package`

## 📝 Development Notes

### Lombok Usage
This project uses Lombok to reduce boilerplate code. Make sure your IDE has Lombok plugin installed:
- IntelliJ IDEA: Install Lombok plugin from marketplace
- Eclipse/STS: Run `java -jar lombok.jar`
- VS Code: Use Extension Pack for Java

### JWT Configuration
JWT tokens are used for authentication. Configure the secret key and expiration time in your application properties.

## 📂 Key Components

- **Controllers:** Handle HTTP requests and responses
- **Services:** Business logic implementation
- **Repositories:** Data access layer using Spring Data JPA
- **Entities:** JPA entity models for database tables
- **DTOs:** Data transfer objects for API communication
- **Security:** JWT filter and authentication configuration
- **Exceptions:** Custom exception handling

## 🚨 Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Connection Issues
- Verify PostgreSQL is running: `docker-compose ps`
- Check database credentials in application properties
- Ensure docker volumes are created: `docker volume ls`

### Lombok Not Working
- Add Lombok annotation processor in IDE
- Run `./mvnw clean install` again

## 📊 Future Enhancements

Potential features for development:
- Payment gateway integration (Stripe, Razorpay)
- Email notifications
- SMS alerts for bookings
- Admin dashboard
- Analytics and reporting
- Mobile app support
- Real-time seat availability
- Review and rating system

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is open source. Please check the LICENSE file for more details.

## 👤 Author

**Harijogi333**

GitHub: [@Harijogi333](https://github.com/Harijogi333)

## 📞 Support

For issues and questions:
- Open an issue on GitHub
- Check existing issues for solutions
- Review the documentation in the wiki

## 🗂️ References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [JWT (jjwt)](https://github.com/jwtk/jjwt)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Last Updated:** 2026-05-30

Happy Coding! 🎬🎫
