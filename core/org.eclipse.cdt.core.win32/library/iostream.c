/**********************************************************************
 * Copyright (c) 2002-2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 *
 *  raise.c
 *
 *  This is a part of JNI implementation of spawner 
 *  Includes implementation of JNI methods (see Spawner.java)
***********************************************************************/
#include "stdafx.h"
#include <string.h>
#include <stdlib.h>
#include "spawner.h"
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
#ifdef DEBUG_MONITOR
	char buffer[1000];
#endif
	OVERLAPPED overlapped;
	overlapped.Offset     = 0; 
	overlapped.OffsetHigh = 0; 
	overlapped.hEvent     = CreateEvent(NULL,    // no security attribute 
									TRUE,    // manual-reset event 
									TRUE,    // initial state = signaled 
									NULL);   // unnamed event object  
 
	if(NULL == overlapped.hEvent) {
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
	}

#ifdef DEBUG_MONITOR
	sprintf(buffer, "Start read %i\n", fd);
	OutputDebugString(buffer);
#endif

	while(len > nBuffOffset)
		{
		int nNumberOfBytesToRead = min(len - nBuffOffset, BUFF_SIZE);
		int nNumberOfBytesRead;
	    if(0 == ReadFile((HANDLE)fd, tmpBuf, nNumberOfBytesToRead, &nNumberOfBytesRead, &overlapped ))
			{
			int err = GetLastError();

            if(err == ERROR_IO_PENDING)  
				{ 
				// asynchronous i/o is still in progress 
				// check on the results of the asynchronous read 
				if(GetOverlappedResult((HANDLE)fd, &overlapped, 
						&nNumberOfBytesRead, TRUE))
					err = 0;
				// if there was a problem ... 
				else
					err = GetLastError();
				}
			if(err == ERROR_BROKEN_PIPE) // Pipe was closed
				break;
			if(err != 0)
				{
				LPTSTR lpMsgBuf;
#ifdef DEBUG_MONITOR
				char buffer[200];
				sprintf(buffer, "Read failed - %i, error %i\n", fd, err);
				OutputDebugString(buffer);
#endif
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
					nBuffOffset = 0;
					break;
					}
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
	CloseHandle(overlapped.hEvent);
#ifdef DEBUG_MONITOR
	sprintf(buffer, "End read %i\n", fd);
	OutputDebugString(buffer);
#endif
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
	int rc;
#ifdef DEBUG_MONITOR
		char buffer[1000];
		sprintf(buffer, "Close %i\n", fd);
		OutputDebugString(buffer);
#endif
		DisconnectNamedPipe((HANDLE)fd);
		rc = (CloseHandle((HANDLE)fd) ? 0 : -1);	
#ifdef DEBUG_MONITOR
		sprintf(buffer, "Closed %i\n", fd);
		OutputDebugString(buffer);
#endif
		return (rc ? GetLastError() : 0);
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
	int rc;
#ifdef DEBUG_MONITOR
		char buffer[1000];
		sprintf(buffer, "Close %i\n", fd);
		OutputDebugString(buffer);
#endif
		DisconnectNamedPipe((HANDLE)fd);
		rc = (CloseHandle((HANDLE)fd) ? 0 : -1);	
#ifdef DEBUG_MONITOR
		sprintf(buffer, "Closed %i\n", fd);
		OutputDebugString(buffer);
#endif
		return (rc ? GetLastError() : 0);
}
