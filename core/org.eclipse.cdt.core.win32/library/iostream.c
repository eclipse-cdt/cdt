/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
/*
 * This is a JNI implementation of access to standard i/o streams 
 */
#include "stdafx.h"
#include <string.h>
#include <stdlib.h>
#include "SpawnerInputStream.h"
#include "SpawnerOutputStream.h"


#include "jni.h"
#include "io.h"


JNIEXPORT void JNICALL ThrowByName(JNIEnv *env, const char *name, const char *msg);

#define BUFF_SIZE  (1024)

/* Inaccessible static: skipBuffer */
/*
 * Class:     SpawnerInputStream
 * Method:    read0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerInputStream_read0
  (JNIEnv * env, jobject proc, jint fd, jbyteArray buf, jint len)
{
	BYTE tmpBuf[BUFF_SIZE];	
	int nBuffOffset = 0;

	while(len > nBuffOffset)
		{
		int nNumberOfBytesToRead = min(len - nBuffOffset, BUFF_SIZE);
		int nNumberOfBytesRead;
	    if(0 == ReadFile((HANDLE)fd, tmpBuf, nNumberOfBytesToRead, &nNumberOfBytesRead, NULL ))
			{
			LPTSTR lpMsgBuf;
			int err = GetLastError();

			if(err == ERROR_BROKEN_PIPE) // Pipe was closed
				return 0;
			if(err != ERROR_MORE_DATA) // Otherwise error means just that there are more data
				{                      // than buffer can accept
				FormatMessage( 
					FORMAT_MESSAGE_ALLOCATE_BUFFER | 
					FORMAT_MESSAGE_FROM_SYSTEM | 
					FORMAT_MESSAGE_IGNORE_INSERTS,
					NULL,
					err,
					MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
					(LPTSTR) &lpMsgBuf,
					0,
					NULL 
				);

				ThrowByName(env, "java/io/IOException", lpMsgBuf);
				LocalFree( lpMsgBuf );
				return 0;
				}
			}
		if(nNumberOfBytesRead > 0)
			(*env) -> SetByteArrayRegion(env, buf, nBuffOffset, nNumberOfBytesRead, tmpBuf);
		else
			break;
		nBuffOffset += nNumberOfBytesRead;
		if(nNumberOfBytesRead != nNumberOfBytesToRead)
			break;
		}
	return nBuffOffset; // This is a real full readed length

}

/*
 * Class:     SpawnerInputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerInputStream_close0
  (JNIEnv * env, jobject proc, jint fd)
{
	return (CloseHandle((HANDLE)fd) ? 0 : -1);	
}

/*
 * Class:     SpawnerOutputStream
 * Method:    write0
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerOutputStream_write0
  (JNIEnv * env, jobject proc, jint fd, jbyteArray buf, jint len)
{
	BYTE tmpBuf[BUFF_SIZE];	
	int nBuffOffset = 0;

	while(len > nBuffOffset)
		{
		int nNumberOfBytesToWrite = min(len - nBuffOffset, BUFF_SIZE);
		int nNumberOfBytesWritten;
		(*env) -> GetByteArrayRegion(env, buf, nBuffOffset, nNumberOfBytesToWrite, tmpBuf);
		if(0 == WriteFile((HANDLE)fd, tmpBuf, nNumberOfBytesToWrite, &nNumberOfBytesWritten, NULL)) 
			{
			LPTSTR lpMsgBuf;
			FormatMessage( 
				FORMAT_MESSAGE_ALLOCATE_BUFFER | 
				FORMAT_MESSAGE_FROM_SYSTEM | 
				FORMAT_MESSAGE_IGNORE_INSERTS,
				NULL,
				GetLastError(),
				MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
				(LPTSTR) &lpMsgBuf,
				0,
				NULL 
			);

			ThrowByName(env, "java/io/IOException", lpMsgBuf);
			LocalFree( lpMsgBuf );
			return 0;
			}
		nBuffOffset += nNumberOfBytesWritten;
		}
	return 0;
}

/*
 * Class:     SpawnerOutputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerOutputStream_close0
  (JNIEnv * env, jobject proc, jint fd)
{
	return (CloseHandle((HANDLE)fd) ? 0 : -1);	
}
