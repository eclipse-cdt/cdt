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

#define FUNC(x) Java_org_eclipse_cdt_serial_SerialPort_ ## x

static void throwIOException(JNIEnv *env, char *msg) {
	char buff[256];
	sprintf_s(buff, sizeof(buff), "%s: %d", msg, GetLastError());
	jclass cls = env->FindClass("java/io/IOException");
	env->ThrowNew(cls, buff);
}

extern "C"
JNIEXPORT jlong JNICALL FUNC(open0)(JNIEnv *env, jobject jobj, jstring portName, jint baudRate, jint byteSize, jint parity, jint stopBits)
{
	const wchar_t * cportName = (const wchar_t *)env->GetStringChars(portName, NULL);
	HANDLE handle = CreateFile(cportName,
		GENERIC_READ | GENERIC_WRITE,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_FLAG_OVERLAPPED,
		NULL);

	if (handle == INVALID_HANDLE_VALUE) {
		throwIOException(env, "Error opening serial port");
		return -1;
	}
	
	DCB dcb = { 0 };

	if (!GetCommState(handle, &dcb)) {
		throwIOException(env, "Error getting DCB");
		return -1;
	}

	dcb.BaudRate = baudRate;
	dcb.ByteSize = (BYTE)byteSize;

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
		throwIOException(env, "Error setting DCB");
		return -1;
	}

	COMMTIMEOUTS timeouts = { 0 };
	timeouts.ReadIntervalTimeout = MAXDWORD;
	timeouts.ReadTotalTimeoutMultiplier = MAXDWORD;
	timeouts.ReadTotalTimeoutConstant = 200;
	if (!SetCommTimeouts(handle, &timeouts)) {
		throwIOException(env, "Error setting timeouts");
		return -1;
	}

	return (jlong)handle;
}

extern "C"
JNIEXPORT void JNICALL FUNC(close0)(JNIEnv *env, jobject jobj, jlong handle)
{
	CloseHandle((HANDLE)handle);
}

extern "C"
JNIEXPORT jint JNICALL FUNC(read1)(JNIEnv * env, jobject jobj, jlong jhandle, jbyteArray bytes, jint offset, jint size)
{
	OVERLAPPED olp = { 0 };

	olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (olp.hEvent == NULL) {
		throwIOException(env, "Error creating event");
		return -1;
	}

	char buff[256];
	DWORD nread = sizeof(buff) < size ? sizeof(buff) : size;
	HANDLE handle = (HANDLE)jhandle;

	if (!ReadFile(handle, buff, sizeof(buff), &nread, &olp)) {
		if (GetLastError() != ERROR_IO_PENDING) {
			throwIOException(env, "Error reading from port");
			CloseHandle(olp.hEvent);
			return -1;
		}
		else {
			switch (WaitForSingleObject(olp.hEvent, INFINITE)) {
			case WAIT_OBJECT_0:
				if (!GetOverlappedResult(handle, &olp, &nread, FALSE)) {
					if (GetLastError() != ERROR_OPERATION_ABORTED) {
						throwIOException(env, "Error waiting for read");
					}
					CloseHandle(olp.hEvent);
					return -1;
				}
				break;
			}
		}
	}

	if (nread > 0) {
		env->SetByteArrayRegion(bytes, offset, nread, (jbyte *)buff);
	}
	CloseHandle(olp.hEvent);
	return nread;
}

extern "C"
JNIEXPORT void JNICALL FUNC(write0)(JNIEnv *env, jobject jobj, jlong jhandle, jint b)
{
	OVERLAPPED olp = { 0 };

	olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (olp.hEvent == NULL) {
		throwIOException(env, "Error creating event");
		return;
	}

	char buff = (char)b;
	DWORD nwritten;
	HANDLE handle = (HANDLE)jhandle;

	if (!WriteFile(handle, &buff, sizeof(buff), &nwritten, &olp)) {
		if (GetLastError() != ERROR_IO_PENDING) {
			throwIOException(env, "Error writing to port");
		}
		else {
			switch (WaitForSingleObject(olp.hEvent, INFINITE)) {
			case WAIT_OBJECT_0:
				if (!GetOverlappedResult(handle, &olp, &nwritten, FALSE)) {
					throwIOException(env, "Error waiting for write");
				}
			}
		}
	}

	CloseHandle(olp.hEvent);
}

extern "C"
JNIEXPORT void JNICALL FUNC(write1)(JNIEnv *env, jobject jobj, jlong jhandle, jbyteArray bytes, jint offset, jint size)
{
	OVERLAPPED olp = { 0 };

	olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (olp.hEvent == NULL) {
		throwIOException(env, "Error creating event");
		return;
	}

	while (size > 0) {
		char buff[256];
		DWORD nwritten = sizeof(buff) < size ? sizeof(buff) : size;
		env->GetByteArrayRegion(bytes, offset, nwritten, (jbyte *)buff);
		HANDLE handle = (HANDLE)jhandle;

		if (!WriteFile(handle, buff, nwritten, &nwritten, &olp)) {
			if (GetLastError() != ERROR_IO_PENDING) {
				throwIOException(env, "Error writing to port");
				return;
			}
			else {
				switch (WaitForSingleObject(olp.hEvent, INFINITE)) {
				case WAIT_OBJECT_0:
					if (!GetOverlappedResult(handle, &olp, &nwritten, FALSE)) {
						throwIOException(env, "Error waiting for write");
						return;
					}
				}
			}
		}
		size -= nwritten;
		offset += nwritten;
	}

	CloseHandle(olp.hEvent);
}

extern "C"
JNIEXPORT jstring FUNC(getPortName)(JNIEnv *env, jclass cls, jint i)
{
	HKEY key;

	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, L"HARDWARE\\DEVICEMAP\\SERIALCOMM", 0, KEY_READ, &key) != ERROR_SUCCESS) {
		throwIOException(env, "Can not find registry key");
		return NULL;
	}

	wchar_t name[256];
	DWORD len = sizeof(name);
	LONG rc = RegEnumValue(key, (DWORD)i, name, &len, NULL, NULL, NULL, NULL);
	if (rc != ERROR_SUCCESS) {
		if (rc != ERROR_NO_MORE_ITEMS) {
			throwIOException(env, "Can not enum value");
		}
		RegCloseKey(key);
		return NULL;
	}

	wchar_t value[256];
	DWORD type;
	len = sizeof(value);
	if (RegQueryValueEx(key, name, NULL, &type, (BYTE *)value, &len) != ERROR_SUCCESS) {
		throwIOException(env, "Can not query value");
		RegCloseKey(key);
		return NULL;
	}

	jstring result = env->NewString((jchar *)value, (jsize) wcslen(value));
	RegCloseKey(key);
	return result;
}
