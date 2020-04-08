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
package tech.langhua.handwriting.apis

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import tech.langhua.handwriting.utils.HttpUtils

/**
 * Basic check
 */
public class BasicCheck extends HttpServlet {
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		long startTime = System.currentTimeMillis()
        Gson gson = new Gson()
        String hanzi = request.getParameter("han")
		try {
			hanzi = HttpUtils.chineseCharCheck(hanzi, gson)
		} catch (IOException e) {
			HttpUtils.writeJSONtoResponse(e.getMessage(), response)
			return
		}

        int count = 0
		try {
			count = Integer.parseInt(request.getParameter("count"))
		} catch (NumberFormatException e) {
			Map messages = [result: "error", score: 0, han: hanzi, message: "缺少count参数"]
			String jsonStr = gson.toJson(messages)
			println("返回JSON结果：" + jsonStr)
			HttpUtils.writeJSONtoResponse(jsonStr, response)
			return
		}
        String xptsString = request.getParameter("xpts")
        String yptsString = request.getParameter("ypts")
        String cptsString = request.getParameter("cpts")
		if (count < 1) {
			Map messages = [result: "error", score: 0, han: hanzi, message: "count参数必须是大于0的整数"]
			String jsonStr = gson.toJson(messages)
			println("返回JSON结果：" + jsonStr)
			HttpUtils.writeJSONtoResponse(jsonStr, response)
			return
		} else if (xptsString == null) {
			Map messages = [result: "error", score: 0, han: hanzi, message: "缺少xpts参数"]
			String jsonStr = gson.toJson(messages)
			println("返回JSON结果：" + jsonStr)
			HttpUtils.writeJSONtoResponse(jsonStr, response)
			return
		} else if (yptsString == null) {
			Map messages = [result: "error", score: 0, han: hanzi, message: "缺少ypts参数"]
			String jsonStr = gson.toJson(messages)
			println("返回JSON结果：" + jsonStr)
			HttpUtils.writeJSONtoResponse(jsonStr, response)
			return
		} else if (cptsString == null) {
			Map messages = [result: "error", score: 0, han: hanzi, message: "缺少cpts参数"]
			String jsonStr = gson.toJson(messages)
			println("返回JSON结果：" + jsonStr)
			HttpUtils.writeJSONtoResponse(jsonStr, response)
			return
		}

		try {
			HttpUtils.strokeEvaluation(response, hanzi, xptsString, yptsString, cptsString, count, startTime, gson)
		} catch (RuntimeException e) {
			println("返回JSON结果：" + e.getMessage())
			HttpUtils.writeJSONtoResponse(e.getMessage(), response)
		}
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "这个功能，不支持Http get方法，请使用post方法。")
    }
}