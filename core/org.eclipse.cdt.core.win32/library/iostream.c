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

//#define READ_REPORT

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
	_TCHAR buffer[1000];
#endif
	OVERLAPPED overlapped;
	overlapped.Offset     = 0; 
	overlapped.OffsetHigh = 0; 
	overlapped.hEvent     = CreateEvent(NULL,    // no security attribute 
									TRUE,    // manual-reset event 
									TRUE,    // initial state = signaled 
									NULL);   // unnamed event object  
 
	if(NULL == overlapped.hEvent) {
		char * lpMsgBuf;
		FormatMessage( 
			FORMAT_MESSAGE_ALLOCATE_BUFFER | 
			FORMAT_MESSAGE_FROM_SYSTEM | 
			FORMAT_MESSAGE_IGNORE_INSERTS,
			NULL,
			GetLastError(),
			MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
			(char *) &lpMsgBuf,
			0,
			NULL 
		);

		ThrowByName(env, "java/io/IOException", lpMsgBuf);
		// Free the buffer.
		LocalFree( lpMsgBuf );
	}

#ifdef DEBUG_MONITOR
#ifdef READ_REPORT
	_stprintf(buffer, _T("Start read %i\n"), fd);
	OutputDebugStringW(buffer);
#endif
#endif

	while(len > nBuffOffset)
		{
		DWORD nNumberOfBytesToRead = min(len - nBuffOffset, BUFF_SIZE);
		DWORD nNumberOfBytesRead;
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
				char * lpMsgBuf;
#ifdef DEBUG_MONITOR
				_stprintf(buffer, _T("Read failed - %i, error %i\n"), fd, err);
				OutputDebugStringW(buffer);
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
						(char *) &lpMsgBuf,
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
#ifdef READ_REPORT
	_stprintf(buffer, _T("End read %i\n"), fd);
	OutputDebugStringW(buffer);
#endif
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
		_TCHAR buffer[1000];
		_stprintf(buffer, _T("Close %i\n"), fd);
		OutputDebugStringW(buffer);
#endif
		DisconnectNamedPipe((HANDLE)fd);
		rc = (CloseHandle((HANDLE)fd) ? 0 : -1);	
#ifdef DEBUG_MONITOR
		_stprintf(buffer, _T("Closed %i\n"), fd);
		OutputDebugStringW(buffer);
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
		DWORD nNumberOfBytesToWrite = min(len - nBuffOffset, BUFF_SIZE);
		DWORD nNumberOfBytesWritten;
		(*env) -> GetByteArrayRegion(env, buf, nBuffOffset, nNumberOfBytesToWrite, tmpBuf);
		if(0 == WriteFile((HANDLE)fd, tmpBuf, nNumberOfBytesToWrite, &nNumberOfBytesWritten, NULL)) 
			{
			char * lpMsgBuf;
			FormatMessage( 
				FORMAT_MESSAGE_ALLOCATE_BUFFER | 
				FORMAT_MESSAGE_FROM_SYSTEM | 
				FORMAT_MESSAGE_IGNORE_INSERTS,
				NULL,
				GetLastError(),
				MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
				(char *) &lpMsgBuf,
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
		_TCHAR buffer[1000];
		_stprintf(buffer, _T("Close %i\n"), fd);
		OutputDebugStringW(buffer);
#endif
		DisconnectNamedPipe((HANDLE)fd);
		rc = (CloseHandle((HANDLE)fd) ? 0 : -1);	
#ifdef DEBUG_MONITOR
		_stprintf(buffer, _T("Closed %i\n"), fd);
		OutputDebugStringW(buffer);
#endif
		return (rc ? GetLastError() : 0);
}
