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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import tech.langhua.handwriting.utils.HttpUtils

/**
 * Basic check for MyScript
 */
public class MyScriptBasicCheck extends BasicCheck {
    
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
        
        String strokesStr = request.getParameter("strokes")
        if (strokesStr == null) {
            Map messages = [result: "error", score: 0, han: hanzi, message: "缺少strokes参数"]
            String jsonStr = gson.toJson(messages)
            println("返回JSON结果：" + jsonStr)
            HttpUtils.writeJSONtoResponse(jsonStr, response)
            return
        }

        List strokes
        try {
            strokes = gson.fromJson(strokesStr, List)
        } catch(JsonSyntaxException e) {
            Map messages = [result: "error", score: 0, han: hanzi, message: "strokes参数格式错误"]
            String jsonStr = gson.toJson(messages)
            println("返回JSON结果：" + jsonStr)
            HttpUtils.writeJSONtoResponse(jsonStr, response)
            return
        }

        String xptsString = "["
        String yptsString = "["
        String cptsString = "["
        List xpts = []
        List ypts = []
        int count = 0
        
        for (String stroke : strokes) {
            stroke = stroke.substring(stroke.indexOf("X=[") + 2)
            xpts = gson.fromJson(stroke.substring(0, stroke.indexOf("]") + 1), List)
            stroke = stroke.substring(stroke.indexOf("Y=[") + 2)
            ypts = gson.fromJson(stroke.substring(0, stroke.indexOf("]") + 1), List)
            for (int i = 0; i < xpts.size(); i++) {
                xptsString += xpts[i] + ","
                yptsString += ypts[i] + ","
                cptsString += i == xpts.size() - 1 ? "1," : "0,"
            }
            count += xpts.size()
        }
        if (xptsString.endsWith(",")) {
            xptsString = xptsString.substring(0, xptsString.length() - 1) + "]"
        } else {
            xptsString += "]"
        }
        if (yptsString.endsWith(",")) {
            yptsString = yptsString.substring(0, yptsString.length() - 1) + "]"
        } else {
            yptsString += "]"
        }
        if (cptsString.endsWith(",")) {
            cptsString = cptsString.substring(0, cptsString.length() - 1) + "]"
        } else {
            cptsString += "]"
        }
        
        try {
            HttpUtils.strokeEvaluation(response, hanzi, xptsString, yptsString, cptsString, count, startTime, gson)
        } catch (RuntimeException e) {
            println("返回JSON结果：" + e.getMessage())
            HttpUtils.writeJSONtoResponse(e.getMessage(), response)
        }
    }
}