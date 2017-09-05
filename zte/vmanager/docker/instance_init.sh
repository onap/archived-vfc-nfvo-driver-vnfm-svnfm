#!/bin/bash

function install_python_libs {
    cd /service/vfc/gvnfm/vnfres/res/
    pip install -r requirements.txt
}

install_python_libs
