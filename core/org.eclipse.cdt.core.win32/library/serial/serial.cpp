/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
#include "stdafx.h"

extern "C"
JNIEXPORT jlong JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_open0
(JNIEnv *env, jobject jobj, jstring portName, jint baudRate, jint byteSize, jint parity, jint stopBits)
{
	const wchar_t * cportName = (const wchar_t *) env->GetStringChars(portName, NULL);
	HANDLE handle = CreateFile(cportName,
		GENERIC_READ | GENERIC_WRITE,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_FLAG_OVERLAPPED,
		NULL);

	if (handle != INVALID_HANDLE_VALUE) {
		DCB dcb = { 0 };

		if (!GetCommState(handle, &dcb)) {
			fprintf(stderr, "Error getting DCB: %S\n", cportName);
			return -1;
		}

		dcb.BaudRate = baudRate;
		dcb.ByteSize = (BYTE) byteSize;

		switch (parity) {
		case 0: // None
			dcb.fParity = FALSE;
			break;
		case 1: // Even
			dcb.fParity = TRUE;
			dcb.Parity = EVENPARITY;
			break;
		case 2: // Odd
			dcb.fParity = TRUE;
			dcb.Parity = ODDPARITY;
			break;
		}
		
		switch (stopBits) {
		case 0:
			dcb.StopBits = ONESTOPBIT;
			break;
		case 1:
			dcb.StopBits = TWOSTOPBITS;
			break;
		}

		if (!SetCommState(handle, &dcb)) {
			fprintf(stderr, "Error setting DCB: %S\n", cportName);
			return -1;
		}
	}

	return (jlong) handle;
}

extern "C"
JNIEXPORT void JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_close0
(JNIEnv *env, jobject jobj, jlong handle)
{
	CloseHandle((HANDLE) handle);
}

extern "C"
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_read0
(JNIEnv *env, jobject jobj, jlong jhandle)
{
	OVERLAPPED olp = { 0 };

	olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (olp.hEvent == NULL) {
		fprintf(stderr, "Error creating event\n");
		fflush(stderr);
		return -1;
	}

	char buff;
	DWORD nwritten;
	HANDLE handle = (HANDLE)jhandle;

	if (!ReadFile(handle, &buff, sizeof(buff), &nwritten, &olp)) {
		if (GetLastError() != ERROR_IO_PENDING) {
			fprintf(stderr, "Error reading from port: %d\n", GetLastError());
			fflush(stderr);
			return -1;
		}
		else {
			switch (WaitForSingleObject(olp.hEvent, INFINITE)) {
			case WAIT_OBJECT_0:
				if (!GetOverlappedResult(handle, &olp, &nwritten, FALSE)) {
					if (GetLastError() != ERROR_OPERATION_ABORTED) {
						fprintf(stderr, "Error waiting for read: %d\n", GetLastError());
						fflush(stderr);
					}
					return -1;
				}
				break;
			}
		}
	}

	CloseHandle(olp.hEvent);
	return buff;
}

extern "C"
JNIEXPORT void JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_write0
(JNIEnv *env, jobject jobj, jlong jhandle, jint b)
{
	OVERLAPPED olp = { 0 };

	olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (olp.hEvent == NULL) {
		fprintf(stderr, "Error creating event\n");
		return;
	}

	char buff = (char) b;
	DWORD nwritten;
	HANDLE handle = (HANDLE) jhandle;

	if (!WriteFile(handle, &buff, sizeof(buff), &nwritten, &olp)) {
		if (GetLastError() != ERROR_IO_PENDING) {
			fprintf(stderr, "Error writing to port\n");
		}
		else {
			switch (WaitForSingleObject(olp.hEvent, INFINITE)) {
			case WAIT_OBJECT_0:
				if (!GetOverlappedResult(handle, &olp, &nwritten, FALSE)) {
					fprintf(stderr, "Error waiting for write\n");
				}
			}
		}
	}

	CloseHandle(olp.hEvent);
}
