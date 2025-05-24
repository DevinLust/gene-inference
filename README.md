# 🐑 Gene Inference Simulator

A Spring Boot application that simulates sheep breeding to help visualize how genes are passed from parents to offspring. The app calculates and updates the probabilities of each sheep's genetic makeup to illustrate how hidden alleles can be inferred and refined over time.

---

## 🚀 Features

- Create or randomly generate sheep with visible gene traits
- Simulate breeding between any two sheep
- Automatically infer the hidden alleles based on breeding outcomes
- Continuously refine gene probability distributions with more data
- Learn about basic genetic inheritance and probabilistic inference

---

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Build Tool:** Maven
- **Database:** PostgreSQL

---

## 🧪 Getting Started

### Prerequisites

- Java 17+ (tested on Java 21.0.7)
- Maven 3.8+

### Clone the Repository

#### https
```bash
git clone https://github.com/DevinLust/gene-inference.git
cd gene-inference
```
#### ssh
```bash
git clone git@github.com:DevinLust/gene-inference.git
cd gene-inference
```

### Build and Run

```bash
mvn clean install
mvn spring-boot:run
```
or use appropriate wrappers for your OS
#### Unix Systems
```bash
./mvnw clean install
./mvnw spring-boot:run
```

#### Windows
```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

The application will start at http://localhost:8080

### How it Works
- Each sheep has observable phenotypes and hidden alleles
- When two sheep breed, the app uses probabilistic models to infer the likely alleles of the offspring
- As more breeding data is introduced, parental gene distributions are refined using Bayesian updating

## Project Structure
* `controller/` — Web endpoints and request handling

* `service/` — Business logic (e.g., breeding simulation, inference engine)

* `model/` — Core classes representing genes and sheep

* `repository/` — (Optional) Data persistence layer, if used

