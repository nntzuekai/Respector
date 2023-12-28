#!/bin/bash

folderA=$1
folderB=$2

num=0
for fileA in "$folderA"/*; do
    filename=$(basename "$fileA")
    fileB="$folderB/$filename"

    if [ -f "$fileB" ]; then
        countA=$(wc -l < "$fileA")
        countB=$(wc -l < "$fileB")

	num=$((num+1))
	if [ "$countA" -eq "$countB" ]; then
            # echo "File $filename has the same number of lines in both folders."
            echo "----------OK: $filename--------"
        else
            echo "File $filename has $countA lines in $folderA and $countB lines in $folderB."
        fi
    else
        echo "File $filename does not exist in folder B."
    fi
done

echo "$num files in total"
