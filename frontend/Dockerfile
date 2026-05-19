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

# Replace the default config with our SPA-aware one (history fallback +
# /api proxy). See nginx.conf in this directory.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the compiled SPA. Angular 17+ outputs to dist/<project>/browser.
COPY --from=build /workspace/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

# Nginx default CMD is fine; we just need it in the foreground.

