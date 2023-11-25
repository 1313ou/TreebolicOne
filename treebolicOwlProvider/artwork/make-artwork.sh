#!/bin/bash

source "../../../lib-artwork.sh"

icons="*.svg"
root="root*.svg"

#make_res "${icons}" 48
#make_res "${root}" 64

make_icon "${icons}" 32 "${dirresources}/treebolic/provider/owl/images"
make_icon "${root}" 48 "${dirresources}/treebolic/provider/owl/images"

