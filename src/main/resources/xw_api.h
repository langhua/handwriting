
// 工程内部的框架和接口；
// Copyright (C), All rights reserved;
// Created by Gzj;
// 05/22/2013;

#ifndef	__INCLUDE_XW_API__
#define	__INCLUDE_XW_API__
#ifdef __cplusplus
extern "C" {
#endif
#define XWAPI _declspec(dllexport) // 只要是在windows操作系统上都应该是输出函数;
#define XW_EXTERN_C //extern "C"

//========================================================================================

//=================================================================================
// 自定义的数据类型;
//----------------------------------------------------------------------------------
typedef unsigned char XWBYTE, *LPXWBYTE;
typedef int XWBOOL, *LPXWNBUF;
typedef signed short XWSHRT, *LPXWSHRT;
typedef signed long  XWLONG, *LPXWLONG;

XW_EXTERN_C XWBOOL XWAPI XW_CreateLib(char* lpszFontFileNames);//, char* lpSecretData, int lSecretDataBC)
// lpszFontFileNames: 是模板库的文件名,多个文件名
// 之间用逗号(半角)隔开;
XW_EXTERN_C void XWAPI XW_DeleteLib();

XW_EXTERN_C int XWAPI XW_RecgA(int ch_unicode,
					int nPointCount,
					signed short* lpXis,
					signed short* lpYis,
					signed short* lpCis,
					int* npRetBuf16);
// (lpXis,lpYis,lpCis,nPointCount)是书写轨迹;其中:
//		nPointCount是点的个数;
//		(lpXis[i],lpYis[i])是第i个点的坐标,
//		lpCis[i]是第i个点的标志;如果该点是笔画上的
//			最后一个点(落笔点),则lpCis[i]=1;否则为0;
// npRetBuf16[16]: 返回错误的类型和笔画序号;
//	 npRetBuf16[0]: 返回错误类型(有多种错误时，只返回类型号最小的错误);
//		0: 书写正确;
//		1: 笔画数不一致;
//				此时npRetBuf16[1]是库模板的笔画数,[2]是手写模板的笔画数;
//		2: 笔画顺序不正确;
//				此时npRetBuf16[1]是库模板的笔画序号,[2]是手写模板的笔画笔画序号;
//				笔画序号从0开始,返回的笔画序号是库模板第一个被书写错误的笔画序号;
//				因为可能有多个笔画的顺序都写错了;
//		3: 笔画的方向写反了;
//				此时npRetBuf16[1]是第一个方向写反了的手写模板笔画序号;
//				如果有多个笔画方向写反了,只返回一个;
//	npRetBuf16[3,15]:保留;
// 返回值: 返回匹配度或书法的美观程度(大于0);错误的时候返回0;

XW_EXTERN_C int XWAPI XW_RecgB(int ch_unicode,
					int nPointCount,
					int* lpXis,
					int* lpYis,
					int* lpCis);
// (lpXis,lpYis,lpCis,nPointCount)是书写轨迹;其中:
//		nPointCount是点的个数;
//		(lpXis[i],lpYis[i])是第i个点的坐标,
//		lpCis[i]是第i个点的标志;如果该点是笔画上的
//			最后一个点(落笔点),则lpCis[i]=1;否则为0;
// 返回值X:
//	X的第一个字节：(X & 0xFF)，匹配程度,[0,100]; X<10的时候,是其它错误；X==1,笔画输不一样;
//  X的第二个字节: ((X>>8)&0xFF):上面函数的错误类型；
//  X的第三个字节: ((X>>16)&0xFF):第一个笔画序号；
//  X的第四个字节: ((X>>24)&0xFF):第二个笔画序号；

long XWAPI XW_Recg(int  nRecgFlags,
				   int  nFontID,
				   int ch_unicode,
				   int nPointCount,
				   signed short* lpXis,
				   signed short* lpYis,
				   signed short* lpCis,
				   XWBYTE* vpFzm,    int cbBuf12K,
				   XWBYTE* vpMrt,    int cbBuf2048,
				   XWBYTE* vpRetBuf, int cbBuf_20K);
// (lpXis,lpYis,lpCis,nPointCount)是书写轨迹;其中:
//		nPointCount是点的个数;
//		(lpXis[i],lpYis[i])是第i个点的坐标,
//		lpCis[i]是第i个点的标志;如果该点是笔画上的
//			最后一个点(落笔点),则lpCis[i]=1;否则为0;
// lpMatchRet: 返回匹配结果;
// 返回值: 返回匹配度或书法的美观程度;不能申请内存时返回0;
// lpRet2Tuxg_cbn!=NULL时,返回两个图形的坐标，格式如下：
// 2字节(M:第一个图形的点的个数),2字节(N第二个图形的点的个数),
// 以下每个位置一个字节x0,y0,c0,...xi,yi,ci,共(M+N)*3个字节;
// 第一个图形是匹配的对象的标准化处理,第二个是误差最小的库模板;
// cbBuf_20K: 是vpRetBuf中可以使用的最大的字节数,参考值:20000;
// 返回值: 返回匹配度或书法的美观程度;不能申请内存时返回0;

XW_EXTERN_C int XWAPI XW_TestC(int a,
							   int b,
							   int* lpBuf2);
// lpBuf3[2]返回：
//    lpBuf2[0]=a+b, lpBuf2[1]= a-b;
// 函数返回值是a+b;
XW_EXTERN_C int XWAPI XW_TestD(char* cpString,
							   int N,
							   char* cpRet2Values);
// lpBuf3[2]返回：
//    lpBuf2[0]=a+b, lpBuf2[1]= a-b;
// 函数返回值是sumOf{cpString[i], i=0,1,..N-1};
// 即N个Char求和;
// cpRet2Values[0]返回0,如果N<2;否则返回(char)(cpString[0]+cpString[1]);
// cpRet2Values[2]返回(char)(N+1)


XWLONG XWAPI XW_GetStdTuxg(long unicode_i,
						   signed short* lpXis,
						   signed short* lpYis,
						   signed short* lpCis,
						   long nMaxPointCount);
// 返回标准模版的点的个数;

//---------------------------------------------------------------------------------
// 重新定义的DLL接口;
//---------------------------------------------------------------------------------
typedef XWBOOL XWAPI (*FPXW_CreateLib)(char* lpszFontFileNames);////, char* lpSecretData, int lSecretDataBC);
typedef void XWAPI (*FPXW_DeleteLib)();
typedef long XWAPI (*FPXW_RecgA)(int ch_unicode,
				   int nPointCount,
				   signed short* lpXis,
				   signed short* lpYis,
				   signed short* lpCis,
				   int* npRetBuf16);
typedef long XWAPI (*FPXW_Recg)(int  nRecgFlags,
				   int  nFontID,
				   int ch_unicode,
				   int nPointCount,
				   signed short* lpXis,
				   signed short* lpYis,
				   signed short* lpCis,
				   XWBYTE* vpFzm,    int cbBuf12K,
				   XWBYTE* vpMrt,    int cbBuf2048,
				   XWBYTE* vpRetBuf, int cbBuf);
typedef XWLONG XWAPI (*FPXW_GetStdTuxg)(long unicode_i,
							signed short* lpXis,
							signed short* lpYis,
							signed short* lpCis,
							long nMaxPointCount);
// 返回标准模版的点的个数;

//========================================================================================
#ifdef __cplusplus
}
#endif
#endif


