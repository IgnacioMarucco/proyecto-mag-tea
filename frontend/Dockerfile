# Stage 1: build
FROM node:24-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci --silent

COPY . .
RUN npm run build

# Stage 2: serve with Nginx
FROM nginxinc/nginx-unprivileged:stable-alpine3.22
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
