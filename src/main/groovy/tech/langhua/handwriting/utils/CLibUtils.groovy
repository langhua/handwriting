/*******************************************************************************
 * Copyright 2020 Tianjin Langhua Ltd.
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
 *******************************************************************************/
package tech.langhua.handwriting.utils

import com.sun.jna.Library
import com.sun.jna.Native

/**
 * Java调用手写笔画顺序库的Web服务
 */
public class CLibUtils  {
    private static HandwritingCLib32 instance
    
    private static UnsatisfiedLinkError unsatisfiedLinkError
    
    private static RuntimeException runtimeException
    
    private static boolean isTxmInitialized = false
    
    private static String txmFilePath
    
    public static HandwritingCLib32 getCLib32Instance() throws UnsatisfiedLinkError, RuntimeException {
        if (unsatisfiedLinkError) {
            throw unsatisfiedLinkError
        }
        if (runtimeException) {
            throw runtimeException
        }
        if (!instance) {
            try {
                instance = (HandwritingCLib32) Native.loadLibrary("recglib_x86", HandwritingCLib32.class)
            } catch (UnsatisfiedLinkError e) {
                unsatisfiedLinkError = new UnsatisfiedLinkError("Cannot initialize recglib_x86.dll, please make sure it's placed under \${CATALINA_HOME}/webapps/handwriting/WEB-INF/classes/.")
                throw unsatisfiedLinkError
            }
        }
        if (!isTxmInitialized) {
            isTxmInitialized = true
            if (!txmFilePath) {
                if (System.getenv("CATALINA_HOME")) {
                    txmFilePath = System.getenv("CATALINA_HOME") + "/webapps/handwriting/WEB-INF/txm/hzi501.txm"
                } else if (System.getProperty("catalina.home")) {
                    txmFilePath = System.getProperty("catalina.home") + "/webapps/handwriting/WEB-INF/txm/hzi501.txm"
                } else {
                    runtimeException = new RuntimeException("Cannot find CATALINA_HOME in system environments or catalina.home in properties, please make sure it's set properly.")
                    throw runtimeException;
                }
            }
            boolean isSuccess = instance.XW_CreateLib(txmFilePath)
            if (!isSuccess) {
                runtimeException = new RuntimeException("Cannot initialize model library file hzi501.txm, please make sure it's placed under \${CATALINA_HOME}/webapps/handwriting/WEB-INF/txm/.")
                throw runtimeException;
            }
        }
        return instance
    }
}