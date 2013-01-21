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
package fr.xebia.microbench.actors

import fr.xebia.microbench.Level
import groovyx.gpars.actor.DefaultActor

import static fr.xebia.microbench.Level.INFO


class Logger extends DefaultActor {

    public Level level
    public static Level currentLevel = INFO

    @Override
    protected void act() {
        loop {
            react { msg -> if (currentLevel <= level) println msg }
        }
    }
}
