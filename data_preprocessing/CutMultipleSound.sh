# !/bin/bash

# counter for the filename
a=1

# loop through all .wav file in the given directory
for i in $1*.wav; do 
    # $i is the file name
    # $2 is the cut time
    # $3 is the directory to save the output  
    ffmpeg -i $i -f segment -segment_time $2 -c copy $3"$a"%03d.wav;

    a=$(expr $a + 1)
done