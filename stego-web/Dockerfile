FROM node:20-alpine AS build
WORKDIR /app

COPY stego-web/package.json stego-web/package.json
RUN cd stego-web && npm install --no-audit --no-fund

COPY stego-web/ stego-web/
RUN cd stego-web && npm run build

FROM nginx:alpine
COPY --from=build /app/stego-web/dist/ /usr/share/nginx/html/
EXPOSE 80

