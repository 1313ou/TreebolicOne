#!/bin/bash

source "../../../make-artwork-lib.sh"

subdir="treebolic/provider/xml/images"

icons="*.svg"
root="root*.svg"

make_resource "${icon}" 64 "${subdir}"
make_resource "${root}" 64 "${subdir}"

