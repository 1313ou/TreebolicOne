#!/bin/bash

thisdir="`dirname $(readlink -m $0)`"
thisdir="$(readlink -m ${thisdir})"

d="../src/main/resources/treebolic/provider/xml/images"

leaf_icons="*"
rooroot_t="root"
#echo $leaf_icons
#echo $root_icons

mkdir -p ${d}

# 1
res=64
for f in ${leaf_icons}.svg; do
	img=${f%.*}
	echo "make ${img}.svg -> ${d}/${img}.png @ resolution ${res}"
	inkscape ${img}.svg --export-png=${d}/${img}.png -h${res} > /dev/null 2> /dev/null
done

# 2
res=128
for f in ${root_icons}.svg; do
	img=${f%.*}
	echo "make ${img}.svg -> ${d}/${img}.png @ resolution ${res}"
	inkscape ${img}.svg --export-png=${d}/${img}.png -h${res} > /dev/null 2> /dev/null
done

