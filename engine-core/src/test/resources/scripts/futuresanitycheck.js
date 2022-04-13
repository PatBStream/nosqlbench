/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

print('waiting 500 ms');
scenario.waitMillis(500);
print('waited');
scenario.start('type=diag;alias=test;cycles=0..1000000000;threads=10;interval=2000;');
print('waiting again');
scenario.modify('test','threads',"1");
print('waiting 5000 ms');
scenario.waitMillis(5000);
scenario.modify('test','threads',"20");
print('modified threads to 20');



