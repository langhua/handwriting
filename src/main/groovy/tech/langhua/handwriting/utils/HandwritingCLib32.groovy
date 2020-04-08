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
import com.sun.jna.Pointer

public interface HandwritingCLib32 extends Library {
	/**
	 * 加载模板库
	 * @param lpszFontFileNames 是模板库的文件名, 多个文件名之间用逗号(半角)隔开, 文件名用绝对地址
	 * @return true表示加载模板库成功；否则，false。
	 */
	boolean XW_CreateLib(String lpszFontFileNames)
	
	/**
	 * 删除模板库
	 */
	void XW_DeleteLib()

	/**
	 * (lpXis,lpYis,lpCis,nPointCount)是书写轨迹;其中:
	 *        nPointCount是点的个数;
	 *        (lpXis[i],lpYis[i])是第i个点的坐标,
	 *        lpCis[i]是第i个点的标志;如果该点是笔画上的
	 *            最后一个点(落笔点),则lpCis[i]=1;否则为0;
	 *
	 *   @return 返回值X在C++中的构造：
	 *           ret = (int)((double)ri / 0x100000L * 200);
	 *           i = npBuf[0]; i = J_Confine(i, 0, 255);
	 *           ret |= (i<<8);
	 *           i = npBuf[1]; i = J_Confine(i, 0, 255);
	 *           ret |= (i<<16);
	 *           i = npBuf[2]; i = J_Confine(i, 0, 255);
	 *           ret |= (i<<24);
	 *
							*           返回值X的含义:
	 *                X的第一个字节：(X & 0xFF)，匹配程度,[0,100]; X<10的时候,是其它错误；X==1,笔画输不一样;
	 *                X的第二个字节: ((X>>8)&0xFF): 返回错误类型(有多种错误时，只返回类型号最小的错误);
	 *                    0: 书写正确;
	 *                    1: 笔画数不一致;
	 *                        此时npRetBuf16[1]是库模板的笔画数,[2]是手写模板的笔画数;
	 *                    2: 笔画顺序不正确;
	 *                        此时npRetBuf16[1]是库模板的笔画序号,[2]是手写模板的笔画笔画序号;
	 *                        笔画序号从0开始,返回的笔画序号是库模板第一个被书写错误的笔画序号;
	 *                        因为可能有多个笔画的顺序都写错了;
	 *                    3: 笔画的方向写反了;
	 *                        此时npRetBuf16[1]是第一个方向写反了的手写模板笔画序号;
	 *                        如果有多个笔画方向写反了,只返回一个;
	 *                X的第三个字节: ((X>>16)&0xFF):第一个笔画序号；
	 *              X的第四个字节: ((X>>24)&0xFF):第二个笔画序号；
	 */
	public int XW_RecgB(int ch_unicode,
				 int nPointCount,
				 Pointer lpXis,
				 Pointer lpYis,
				 Pointer lpCis)
}
