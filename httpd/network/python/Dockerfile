FROM python:latest

COPY setup.sh .
COPY proxy_pac.py .
COPY rules.json .

CMD [ "/bin/sh", "-c", "setup.sh"  ]
CMD [ "python3 ./proxy_pac.py" ]
