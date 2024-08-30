FROM node:10-buster-slim

RUN apt-get update
RUN apt-get install bash
RUN apt-get clean
RUN npm install -g proxy-login-automator
COPY launcher.sh ./

EXPOSE 6500-6600

ENV REMOTE_HOST=
ENV REMOTE_USER=
ENV REMOTE_PASSWORD=
ENV REMOTE_HTTPS=true
ENV INGORE_CERT=false
ENV GET_REMOTE_HOST=
ENV PAC_SERVER=true
ENV LOCAL_HOST=0.0.0.0
ENV LOCAL_PORT=6500

CMD ["bash", "launcher.sh"]
