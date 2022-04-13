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

#curl -O https://www2.census.gov/topics/genealogy/1990surnames/dist.all.last
curl -O https://www2.census.gov/topics/genealogy/1990surnames/dist.female.first
curl -O https://www2.census.gov/topics/genealogy/1990surnames/dist.male.first

perl -pi -e '$_=~s/ +/,/g' dist.male.first
perl -pi -e 'if (/^(.+?),(.+)$/) { $_=uc(substr($1,0,1)).lc(substr($1,1)).",".$2."\n"; }' dist.male.first
printf "%s\n" 'Name,Weight,CumulativeWeight' > male_firstnames.csv
cat dist.male.first >> male_firstnames.csv
mv male_firstnames.csv ..

#zip -9 ../1990_male_firstnames.csv.zip 1990_male_firstnames.csv && rm 1990_male_firstnames.csv

perl -pi -e '$_=~s/ +/,/g' dist.female.first
perl -pi -e 'if (/^(.+?),(.+)$/) { $_=uc(substr($1,0,1)).lc(substr($1,1)).",".$2."\n"; }' dist.female.first
printf "%s\n" 'Name,Weight,CumulativeWeight' > female_firstnames.csv
cat dist.female.first >> female_firstnames.csv
mv female_firstnames.csv ..

