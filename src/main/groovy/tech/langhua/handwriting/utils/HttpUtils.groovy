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

import com.google.gson.Gson
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

/**
 * Http Utilities
 */
public class HttpUtils {
	private static final String CHINESE_REGEX = "[\u4e00-\u9fa5]"
	
    public static void writeJSONtoResponse(String jsonStr, HttpServletResponse response) throws UnsupportedEncodingException {
        // set the JSON content type
		response.setHeader("Access-Control-Allow-Origin", "*")
        response.setContentType("application/json")
        response.setCharacterEncoding("UTF-8")

        // return the JSON String
        PrintWriter out = response.getWriter()
        out.println(jsonStr)
		out.flush()
		out.close()
    }
	
	public static String chineseCharCheck(String hanzi, Gson gson) throws IOException {
		if (!isChineseChar(hanzi.charAt(0))) {
			String newHanzi = new String(hanzi.getBytes("GBK"), "UTF-8")
			if (!isChineseChar(newHanzi.charAt(0))) {
				newHanzi = new String(hanzi.getBytes("GB2312"), "UTF-8")
				if (!isChineseChar(newHanzi.charAt(0))) {
					newHanzi = new String(hanzi.getBytes("ISO-8859-1"), "UTF-8")
					if (!isChineseChar(newHanzi.charAt(0))) {
						Map messages = [result: "error", errorCode: HttpServletResponse.SC_BAD_REQUEST, message: "无法把han这个参数的值解析成一个汉字"]
						String jsonStr = gson.toJson(messages)
						throw new IOException(jsonStr)
					} else {
						hanzi = newHanzi
					}
				} else {
					hanzi = newHanzi
				}
			} else {
				hanzi = newHanzi
			}
		}
		return hanzi		
	}
    
    public static boolean isChineseChar(char c) {
        return String.valueOf(c).matches(CHINESE_REGEX)
    }
}