#!/bin/bash

thisdir="`dirname $(readlink -m $0)`"
thisdir="$(readlink -m ${thisdir})"
dirres=../src/main/res
dirassets=../src/main/assets
dirapp=..

declare -A res_launch
res_launch=([mdpi]=48 [hdpi]=72 [xhdpi]=96 [xxhdpi]=144 [xxxhdpi]=192)
list_launch="ic_launcher.svg"

declare -A res_splash
res_splash=([mdpi]=144 [hdpi]=192 [xhdpi]=288 [xxhdpi]=384 [xxxhdpi]=576)
list_splash="ic_splash.svg"

declare -A res_icon
res_icon=([mdpi]=48 [hdpi]=72 [xhdpi]=96 [xxhdpi]=144 [xxxhdpi]=192)
list_icon="ic_treebolic.svg"

declare -A res_action
res_action=([mdpi]=24 [hdpi]=32 [xhdpi]=48 [xxhdpi]=72 [xxxhdpi]=96)
list_action="ic_action_*.svg"

declare -A res_web
res_web=([web]=512)
list_web="ic_launcher.svg"

# launcher
for svg in ${list_launch}; do
	for r in ${!res_launch[@]}; do 
		d="${dirres}/drawable-${r}"
		mkdir -p ${d}
		png="${svg%.svg}.png"
		echo "${svg}.svg -> ${d}/${png}.png @ resolution ${res_launch[$r]}"
		inkscape ${svg} --export-png=${d}/${png} -h${res_launch[$r]} > /dev/null 2> /dev/null
	done
done

# splash
for svg in ${list_splash}; do
	for r in ${!res_splash[@]}; do 
		d="${dirres}/drawable-${r}"
		mkdir -p ${d}
		png="${svg%.svg}.png"
		echo "${svg}.svg -> ${d}/${png}.png @ resolution ${res_splash[$r]}"
		inkscape ${svg} --export-png=${d}/${png} -h${res_splash[$r]} > /dev/null 2> /dev/null
	done
done

# small icons
for svg in ${list_icon}; do
	for r in ${!res_icon[@]}; do 
		d="${dirres}/drawable-${r}"
		mkdir -p ${d}
		png="${svg%.svg}.png"
		echo "${svg} -> ${d}/${png} @ resolution ${res_icon[$r]}"
		inkscape ${svg} --export-png=${d}/${png} -h${res_icon[$r]} > /dev/null 2> /dev/null
	done
done

# actions
for svg in ${list_action}; do
	for r in ${!res_action[@]}; do 
		d="${dirres}/drawable-${r}"
		mkdir -p ${d}
		png="${svg%.svg}.png"
		echo "${svg} -> ${d}/${png} @ resolution ${res_action[$r]}"
		inkscape ${svg} --export-png=${d}/${png} -h${res_action[$r]} > /dev/null 2> /dev/null
	done
done

# web
for svg in ${list_web}; do
	for r in ${!res_web[@]}; do 
		d=".."
		mkdir -p ${d}
		png="${svg%.svg}-web.png"
		echo "${svg} -> ${d}/${png} @ resolution ${res_web[$r]}"
		inkscape ${svg} --export-png=${d}/${png} -h${res_web[$r]} > /dev/null 2> /dev/null
	done
done

