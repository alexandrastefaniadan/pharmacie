# syntax=docker/dockerfile:1.7
#
# Multi-stage Dockerfile for the Angular SPA.
#
# Stage 1 builds the production bundle with Node 22 (matches our dev setup).
# Stage 2 serves the static output through Nginx, which also reverse-proxies
# /api/* to the backend container, keeping the SPA and the API on the same
# origin in production (no CORS, simpler session cookies).
#
# Build locally:
#   docker build -t pharmacie-frontend ./frontend
# Run locally (expects the backend on the docker-compose network as "backend"):
#   docker compose up -d
# -----------------------------------------------------------------------------

# ---- Stage 1 : build ----
FROM node:22-alpine AS build
WORKDIR /workspace

# Install deps first (cached) then build
COPY package.json package-lock.json* ./
RUN npm ci

COPY angular.json tsconfig*.json ./
COPY src ./src
COPY public ./public

RUN npx ng build --configuration production

# ---- Stage 2 : runtime ----
FROM nginx:alpine

# The nginx image runs `envsubst` on every *.template file in
# /etc/nginx/templates at container start and writes the result to
# /etc/nginx/conf.d. This lets the same image work locally (BACKEND_URL=
# http://backend:8080, PORT=80) and on Railway (BACKEND_URL=https://<backend
# public url>, PORT=$PORT).
COPY default.conf.template /etc/nginx/templates/default.conf.template

# Only substitute the variables we want. Anything else (e.g. $host,
# $remote_addr) stays as a literal nginx variable.
ENV NGINX_ENVSUBST_TEMPLATE_SUFFIX=".template" \
    PORT=80 \
    BACKEND_URL=http://backend:8080

# Copy the compiled SPA. Angular 17+ outputs to dist/<project>/browser.
COPY --from=build /workspace/dist/frontend/browser /usr/share/nginx/html

# Document the default port; Railway will override via the PORT env var and
# wire its router to whatever the container listens on.
EXPOSE 80

# Nginx default CMD is fine; we just need it in the foreground.
