#!/bin/bash
#
# Copyright (c) 2022 nosqlbench
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

curl -O https://www2.census.gov/geo/docs/maps-data/data/gazetteer/2016_Gazetteer/2016_Gaz_place_national.zip
unzip 2016_Gaz_place_national.zip
mv 2016_Gaz_place_national.txt census_places.csv
perl -pi -e '$_=~s/ *\e */,/g' census_places.csv
perl -pi -e '$_=~s/\s*\n/\n/g' census_places.csv
mv census_places.csv ..
rm 2016_Gaz_place_national.zip


