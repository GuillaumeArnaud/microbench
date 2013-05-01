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

import static fr.xebia.gpressure.internals.Utils.unit

class Sample {
    public float mean, min, max
    public long count

    public Sample(Collection<Measure> measures, long iteration) {
        def elapsed = measures.collect { measure -> measure.elapse }
        count = iteration * measures.size()
        mean = elapsed.sum() / count
        min = elapsed.min() / iteration
        max = elapsed.max() / iteration
    }



    public String toString() { "mean=${unit mean}, min=${unit min}, max=${unit max}, count=$count" }
}
