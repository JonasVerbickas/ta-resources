#!/bin/bash

#This simple script will clean up some of the files in the Stendhal repository, INCLUDING the .git repository
#There are probably more things that can be deleted but this could be step one

#This script should ONLY be run from the Stendhal root (repository) directory

#rm -r ./build                           #remove built files - can build them with Ant anyway
#rm -r ./build-archive
#rm -r ./.git
#rm -r ./.github
#rm -r ./log                              #logs
#rm -r ./run_srvr                         #temp folder that might get left over from build scripts
#rm -r ./bin                              #remove the binaries
find . -name '*.flac' -delete            #for each .flac file there is a much smaller .ogg alternative
find . -name '*.wav.xz' -delete          #smaller .ogg alternatives available
find . -name '*.wav' -delete             #smaller .ogg alternatives available
find . -name '*.mp3' -delete             #smaller .ogg alternatives available
find . -name '*.xcf.bz2' -delete         #these are GIMP files - so work in progress, no actual pictures
find . -name '*.xcf' -delete             #these are GIMP files - so work in progress, no actual pictures
find . -name '*.psd' -delete             #similar, but Photoshop files


find . -regex "..*png" | xargs -I{} pngquant -s1 -f --strip --skip-if-larger --posterize 4 --ext ".png" {}
# these are split up, because ffmpeg cannot overwrite files
find . -regex "..*\.ogg" | xargs -I{} ffmpeg -i {} -ar 44100 -ab 45k {}compressed.ogg
find . | egrep -o "..*\.ogg(.*?)c" | egrep -o "..*ogg" | xargs -I{} mv {}compressed.ogg {}