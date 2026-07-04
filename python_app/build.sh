#!/usr/bin/env bash
set -e
python3 --version
pip3 install --upgrade pip setuptools wheel virtualenv
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
pip install buildozer
buildozer -v android debug
