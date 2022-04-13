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

set -e
#RELEASE_NOTES_FILE=${RELEASE_NOTES_FILE:?RELEASE_NOTES_FILE must be provided}

git log --oneline --decorate --max-count=1000 > /tmp/gitlog.txt

readarray lines < /tmp/gitlog.txt
for line in "${lines[@]}"
do
 if [[ $line =~ \(tag:\ nosqlbench-[0-9]+\.[0-9]+\.[0-9]+\).+ ]]
 then
#  printf "no more lines after $line" 1>&2
  break
 elif [[ $line =~ \[maven-release-plugin\] ]]
 then
#  printf "maven release plugin, skipping: $line\n" 1>&2
  continue
 elif [[ $line =~ "Merge" ]]
  then
  printf -- "- $line"
#  printf "merge info, skipping: $line" 1>&2
  continue
 else
  printf -- "- $line"
#  printf "$line" | tee -a ${RELEASE_NOTES_FILE}
 fi
done

