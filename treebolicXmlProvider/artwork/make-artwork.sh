#!/bin/bash

source "../../../make-artwork-lib.sh"

icons="*.svg"
root="root*.svg"

make_res "${icon}" 48
make_res "${root}" 64

