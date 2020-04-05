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
import com.sun.jna.Memory
import com.sun.jna.Pointer
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import tech.langhua.handwriting.utils.CLibUtils
import tech.langhua.handwriting.utils.HttpUtils
import tech.langhua.handwriting.utils.CLibUtils.HandwritingCLib32

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
			strokeEvaluation(response, hanzi, xptsString, yptsString, cptsString, count, startTime, gson)
		} catch (RuntimeException e) {
			println("返回JSON结果：" + e.getMessage())
			HttpUtils.writeJSONtoResponse(e.getMessage(), response)
		}
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "这个功能，不支持Http get方法，请使用post方法。")
    }
	
	protected void strokeEvaluation(HttpServletResponse response, String hanzi, String xptsString, String yptsString, 
		                            String cptsString, int count, long startTime, Gson gson) throws RuntimeException {
		HandwritingCLib32 instance = null
		try {
			instance = CLibUtils.getCLib32Instance()
		} catch (Exception e) {
			Map messages = [result: "error", message: "系统配置错误，错误信息：" + e.getMessage()]
			String jsonStr = gson.toJson(messages)
			throw new RuntimeException(jsonStr)
		}
		try {
			List xptsList = gson.fromJson(xptsString, List)
			List yptsList = gson.fromJson(yptsString, List)
			List cptsList = gson.fromJson(cptsString, List)
			if (!xptsList) {
				Map messages = [result: "error", score: 0, han: hanzi, message: "无法把xpts参数解析为列表"]
				String jsonStr = gson.toJson(messages)
				throw new RuntimeException(jsonStr)
			} else if (!yptsList) {
				Map messages = [result: "error", score: 0, han: hanzi, message: "无法把ypts参数解析为列表"]
				String jsonStr = gson.toJson(messages)
				throw new RuntimeException(jsonStr)
			} else if (!cptsList) {
				Map messages = [result: "error", score: 0, han: hanzi, message: "无法把cpts参数解析为列表"]
				String jsonStr = gson.toJson(messages)
				throw new RuntimeException(jsonStr)
			}
			if (xptsList.size() != count) {
				Map messages = [result: "error", score: 0, han: hanzi, message: ("xpts参数的数据数量为" + xptsList.size() + "，与count参数的数值" + count + "不一致")]
				String jsonStr = gson.toJson(messages)
				throw new RuntimeException(jsonStr)
			} else if (yptsList.size() != count) {
				Map messages = [result: "error", score: 0, han: hanzi, message: ("ypts参数的数据数量为" + yptsList.size() + "，与count参数的数值" + count + "不一致")]
				String jsonStr = gson.toJson(messages)
				throw new RuntimeException(jsonStr)
			} else if (cptsList.size() != count) {
				Map messages = [result: "error", score: 0, han: hanzi, message: ("cpts参数的数据数量为" + cptsList.size() + "，与count参数的数值" + count + "不一致")]
				String jsonStr = gson.toJson(messages)
				throw new RuntimeException(jsonStr)
			}
			Pointer xpts = new Memory(Integer.BYTES * count)
			Pointer ypts = new Memory(Integer.BYTES * count)
			Pointer cpts = new Memory(Integer.BYTES * count)
			for (int i = 0; i < count; i++) {
				xpts.setInt(Integer.BYTES * i, ((Double) xptsList.get(i)).intValue())
				ypts.setInt(Integer.BYTES * i, ((Double) yptsList.get(i)).intValue())
				cpts.setInt(Integer.BYTES * i,  ((Double) cptsList.get(i)).intValue())
			}
			println("你要写的汉字是【" + hanzi + "】")
			
			int result = instance.XW_RecgB(hanzi.codePointAt(0),
										   count,
										   xpts,
										   ypts,
										   cpts)
			int[] results = new int[4]
			results[3] = (int) ((result >> 24) & 0xFF)
			results[2] = (int) ((result >> 16) & 0xFF)
			results[1] = (int) ((result >> 8) & 0xFF)
			results[0] = (int) (result & 0xFF)
			println("匹配程度：" + results[0])
			println("错误类型：" + results[1])
			Map messages = [result: "success", score: results[0], han: hanzi]
			response.setStatus(HttpServletResponse.SC_OK)
			switch(results[1]) {
				case 0:
					if (results[0] > 170) {
						messages << [message: "书写正确"]
					} else {
						messages << [message: "书写不够正确和美观"]
					}
					break;
				case 1:
					messages = [result: "error", message: ("笔画数不一致，标准笔画数是" + results[2] + ", 你的笔画数是" + results[3]), score: results[0],
									stdStrokeNum: results[2], yourStrokeNum: results[3], han: hanzi]
					break;
				case 2:
					messages = [result: "error", message: ("你第" + results[3] + "笔笔顺错了"), score: results[0],
									wrongStrokeAt: results[3], han: hanzi]
					break;
				case 3:
					messages = [result: "error", message: ("你第" + (results[2] + 1) + "笔笔画方向反了"), score: results[0],
									wrongStrokeDirectionAt: (results[2] + 1), han: hanzi]
			}
			long endTime = System.currentTimeMillis()
			messages << [ timeConsumed : ((endTime - startTime) + "ms") ]
			String jsonStr = gson.toJson(messages)
			println("返回JSON结果：" + jsonStr)
			HttpUtils.writeJSONtoResponse(jsonStr, response)
		} catch (JsonSyntaxException e) {
			Map messages = [result: "error", score: 0, han: hanzi, message: "解析JSON数据时出错，错误信息：" + e.getMessage()]
			String jsonStr = gson.toJson(messages)
			throw new RuntimeException(jsonStr)
		}
	}
}