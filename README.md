# 🧠 PersonalBrain.AI  

**Your personal AI-powered knowledge base** — PersonalBrain.AI helps you upload documents (PDFs and more), securely store them, and transform them into a searchable, conversational knowledge hub. Files are chunked, embedded, and stored in Pinecone for semantic search, while GPT-3.5-turbo powers natural language chat with your knowledge.  

---

## 🚀 Features  

- 📂 **Document Uploads** – Upload PDFs and text files into your personal AI brain.  
- 🧩 **Smart Chunking & Embedding** – Splits files into chunks, embeds them with `text-embedding-small`, and indexes them in Pinecone.  
- ☁️ **Secure Cloud Storage** – Stores uploaded files in **AWS S3**.  
- 🐇 **Distributed Workers** – Uses **RabbitMQ** + **Redis** to manage and scale background tasks for ingestion and embedding.  
- 🔍 **Semantic Search** – Find relevant passages across documents using Pinecone vector similarity.  
- 💬 **Chat With Your Knowledge** – GPT-3.5-turbo answers questions from your uploaded content.  
- 🌐 **Modern Frontend** – Built with Next.js, Context APIs, and custom hooks for state management.  
- 🔧 **Fully Cloud Hosted** – Render services handle PostgreSQL, Redis, and the backend web service.  

---

## 🛠️ Installation  

### Prerequisites  
- Java 17+ (Spring Boot backend)  
- Node.js 18+ (Next.js frontend)  
- PostgreSQL (Render-hosted in production)  
- Redis (Render-hosted in production)  
- RabbitMQ (for background task queue)  
- Pinecone account (for vector embeddings)  
- AWS S3 account  
- OpenAI API key  

---

### Backend Setup (Java Spring Boot)  
```bash
# Clone repository
git clone https://github.com/yourusername/personalbrain.ai.git
cd personalbrain.ai/backend

# Build project
./mvnw clean package

# Run backend
java -jar target/personalbrain-ai.jar

```
#### Environment Variables

```bash
# --- Database ---
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# --- Pinecone ---
pinecone.index-url=${PINECONE_INDEX_URL}
pinecone.api-key=${PINECONE_API_KEY}
pinecone.index-name=${PINECONE_INDEX_NAME}

# --- JWT ---
app.jwt.secret=${JWT_SECRET}
app.jwt.expirationMs=3600000
app.jwt.refreshExpirationMs=3600000

# --- OpenAI ---
openai.api-key=${OPENAI_API_KEY}
openai.model=${OPENAI_API_MODEL}

# --- AWS S3 ---
aws.s3.accessKey=${AWS_ACCESS_KEY_ID}
aws.s3.secretKey=${AWS_SECRET_ACCESS_KEY}
aws.s3.region=${AWS_REGION}
aws.s3.bucket=${AWS_BUCKET_NAME}
aws.s3.signed-url-ttl=3600

# --- CORS ---
cors.allowed-origins=${ALLOWED_ORIGINS}

# --- RabbitMQ ---
spring.rabbitmq.host=${SPRING_RABBITMQ_HOST}
spring.rabbitmq.port=${SPRING_RABBITMQ_PORT}
spring.rabbitmq.username=${SPRING_RABBITMQ_USERNAME}
spring.rabbitmq.password=${SPRING_RABBITMQ_PASSWORD}
spring.rabbitmq.virtual-host=${SPRING_RABBITMQ_VHOST}
spring.rabbitmq.addresses=${SPRING_RABBITMQ_ADDRESSES}

```
---

### Frontend Setup (Next.js + TypeScript)
```bash
cd ../frontend

# Install dependencies
npm install

# Setup environment
cp .env.example .env.local
# Add NEXT_PUBLIC_API_URL and other keys

# Run dev server
npm run dev
```
---
### Deployment

PersonalBrain.AI is deployed using Render and AWS services:

- Render PostgreSQL → Stores metadata, document references, and user accounts.  
- Render Redis → Manages caching, sessions, and task queue state.  
- Render Web Service → Hosts the Java Spring Boot backend.  
- RabbitMQ → Handles distributed background workers for ingestion and embedding - Deployed on CloudAMQP.  
- AWS S3 → Stores uploaded documents securely.  
- Pinecone → Stores and retrieves semantic embeddings.  
- OpenAI APIs → Embeddings (text-embedding-small) and chat (gpt-3.5-turbo).  
---
### License

This project is licensed under the MIT License – see the LICENSE file for details.
