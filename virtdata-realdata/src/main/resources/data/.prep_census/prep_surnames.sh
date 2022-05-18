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

curl -O https://www2.census.gov/topics/genealogy/2010surnames/names.zip
unzip names.zip
xsv select 'name,rank,count,prop100k,cum_prop100k' Names_2010Census.csv > surnames.csv
perl -pi -e 'if (/^(.+?),(.+)$/) { $_=uc(substr($1,0,1)).lc(substr($1,1)).",".$2."\n"; }' surnames.csv
perl -pi -e 'if (/All other names/) { $_=""; }' surnames.csv
#zip -9 surnames.csv.zip surnames.csv
rm Names_2010Census.xlsx
rm Names_2010Census.csv
rm names.zip
mv surnames.csv ../surnames.csv
