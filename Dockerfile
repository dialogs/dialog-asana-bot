FROM gradle:4.10.2-jdk8
RUN git clone https://github.com/terorie/dialog-asana-bot
WORKDIR dialog-asana-bot
CMD ./docker_run.sh