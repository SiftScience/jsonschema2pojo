/**
 * Copyright © 2010-2017 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jsonschema2pojo.gradle

import org.gradle.api.logging.Logger

class GradleRuleLogger {

    Logger Logger

    GradleRuleLogger(Logger logger) {
        super()
        logger.info("Initializing {}", GradleRuleLogger.class)
        this.logger = logger
    }

    void doDebug(String msg) {
        logger.debug(msg)
    }

    void doError(String msg, Throwable e) {
        if(e != null) {
            logger.error(msg, e)
        } else {
            logger.error(msg)
        }
    }

    void doInfo(String msg) {
        logger.info(msg)
    }

    void doTrace(String msg) {
        logger.trace(msg)
    }

    void doWarn(String msg, Throwable e) {
        if(e != null) {
            logger.warn(msg, e)
        } else {
            logger.warn(msg)
        }
    }

    boolean isDebugEnabled() {
        return logger.isDebugEnabled()
    }

    boolean isErrorEnabled() {
        return logger.isErrorEnabled()
    }

    boolean isInfoEnabled() {
        return logger.isInfoEnabled()
    }

    boolean isTraceEnabled() {
        return logger.isTraceEnabled()
    }

    boolean isWarnEnabled() {
        return logger.isWarnEnabled()
    }
}
