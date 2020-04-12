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
import com.google.gson.JsonSyntaxException
import com.sun.jna.Memory
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
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
    
    public static void strokeEvaluation(HttpServletResponse response, String hanzi, String xptsString, String yptsString,
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
            println("要写的汉字是【" + hanzi + "】")

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
                        messages << [message: "书写不够正确和美观"] << [stdStrokes: callStdWriting(hanzi, count, instance)]
                    }
                    break;
                case 1:
                    messages = [result: "error", message: ("笔画数不一致，标准笔画数是" + results[2] + ", 你的笔画数是" + results[3]), score: results[0],
                        stdStrokeNum: results[2], yourStrokeNum: results[3], han: hanzi] << [stdStrokes: callStdWriting(hanzi, count, instance)]
                    break;
                case 2:
                    messages = [result: "error", message: ("第" + results[3] + "笔笔顺错了"), score: results[0],
                        wrongStrokeAt: results[3], han: hanzi] << [stdStrokes: callStdWriting(hanzi, count, instance)]
                    break;
                case 3:
                    messages = [result: "error", message: ("第" + (results[2] + 1) + "笔笔画方向反了"), score: results[0],
                        wrongStrokeDirectionAt: (results[2] + 1), han: hanzi] << [stdStrokes: callStdWriting(hanzi, count, instance)]
            }
            
            long endTime = System.currentTimeMillis()
            messages << [ timeConsumed : ((endTime - startTime) + "ms") ]
            String jsonStr = gson.toJson(messages)
            println("返回JSON结果：" + jsonStr)
            writeJSONtoResponse(jsonStr, response)
        } catch (JsonSyntaxException e) {
            Map messages = [result: "error", score: 0, han: hanzi, message: "解析JSON数据时出错，错误信息：" + e.getMessage()]
            String jsonStr = gson.toJson(messages)
            throw new RuntimeException(jsonStr)
        }
    }
    
    private static List callStdWriting(String hanzi, int count, HandwritingCLib32 instance) {
        NativeLong nMaxPointCount = new NativeLong(count)
        short[] lpXis = new short[nMaxPointCount]
        short[] lpYis = new short[nMaxPointCount]
        short[] lpCis = new short[nMaxPointCount]
        NativeLong stdResult = instance.XW_GetStdTuxg(new NativeLong(hanzi.codePointAt(0)), lpXis, lpYis, lpCis, nMaxPointCount)
        List result = []
        if (stdResult && stdResult.intValue() > 0) {
            List xpts = []
            List ypts = []
            println("lpXis: " + lpXis)
            println("lpYis: " + lpYis)
            println("lpCis: " + lpCis)
            long pointerId = System.currentTimeMillis()
            Map stroke = [pointerType: 'PEN', pointerId: pointerId]
            short lastC = 0
            for (int i = 0; i < stdResult.intValue(); i++) {
                short currentC  = lpCis[i]
                if (currentC == 3) {
                    xpts << lpXis[i]
                    ypts << lpYis[i]
                    result << [x: xpts, y: ypts]

                    xpts = []
                    ypts = []
                } else {
                    xpts << lpXis[i]
                    ypts << lpYis[i]
                }
                lastC = currentC
            }
            result << [x: xpts, y:ypts]
        }
        return result
    }
}