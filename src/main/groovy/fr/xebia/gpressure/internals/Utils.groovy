/*
 * Copyright 2008-2012 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.gpressure.internals

import groovy.transform.CompileStatic

import static java.lang.Math.round

@CompileStatic
class Utils {

    public static String prettyBytes(Long bytes) {
        if (bytes > 1024 * 1024 * 1024) "${round((float) bytes / (1024 * 1024 * 1024))}GB"
        else if (bytes > 1024 * 1024) "${round((float) bytes / (1024 * 1024))}MB"
        else if (bytes > 1024) "${round((float) bytes / (1024))}KB"
        else "${round((float) bytes / (1024))}B"
    }

    public static String unit(float timeInNs) {
        if (timeInNs < 1000000f) {
            return "$timeInNs ns"
        } else return "${timeInNs / 1000000} ms"
    }
}
