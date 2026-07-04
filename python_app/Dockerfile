FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    python3.10 python3.10-venv python3-pip git zip unzip openjdk-17-jdk wget sudo \
    && rm -rf /var/lib/apt/lists/*

RUN pip3 install --upgrade pip setuptools wheel virtualenv

RUN git clone https://github.com/kivy/buildozer.git /buildozer && \
    cd /buildozer && python3.10 -m pip install -r requirements.txt

WORKDIR /app
CMD ["bash"]
