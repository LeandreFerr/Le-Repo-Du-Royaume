FROM nginx:latest

WORKDIR /usr/share/nginx/html

RUN rm -rf /usr/share/nginx/html/*

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY dist/royaume_front/browser/ .

EXPOSE 80
