#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

YELP_DATASET="${1}"

if [[ -z "$YELP_DATASET" ]]; then
  echo "Usage: $0 YELP_DATASET"
  echo ""
  echo "YELP_DATASET    Path to the compressed Yelp JSON dataset"
fi

yelp_dataset_dir=`mktemp -d`
sample_yelp_dataset_dir=`mktemp -d`
destination_tar="$SCRIPT_DIR/../importer/src/test/resources/sample_yelp_dataset.tar.gz"

echo "Extracting files to $yelp_dataset_dir"
tar -zxvf "$YELP_DATASET" -C "$yelp_dataset_dir"

echo "Filtering datasets to $sample_yelp_dataset_dir"
cp "$yelp_dataset_dir"/*.pdf "$sample_yelp_dataset_dir"
for f in "$yelp_dataset_dir"/*.json; do
  head -n 100 "$f" > "$sample_yelp_dataset_dir/$(basename "$f")";
done

echo "Generating sample compressed dataset"
pax -wzvf "$destination_tar" -s ":^$sample_yelp_dataset_dir/::" "$sample_yelp_dataset_dir"/*

rm -rf "$yelp_dataset_dir" "$sample_yelp_dataset_dir"
