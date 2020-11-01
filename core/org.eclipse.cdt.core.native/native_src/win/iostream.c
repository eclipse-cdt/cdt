/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *
 *  raise.c
 *
 *  This is a part of JNI implementation of spawner
 *  Includes implementation of JNI methods (see Spawner.java)
 *******************************************************************************/
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include <windows.h>

#include "util.h"

#include "org_eclipse_cdt_utils_spawner_Spawner.h"

void ThrowByName(JNIEnv *env, const char *name, const char *msg);

#define BUFF_SIZE (1024)

static HANDLE channelToHandle(JNIEnv *env, jobject channel) {
    if (channel == 0) {
        ThrowByName(env, "java/io/IOException", "Invalid channel object");
        return NULL;
    }

    jclass cls = (*env)->GetObjectClass(env, channel);
    if (cls == NULL) {
        ThrowByName(env, "java/io/IOException", "Unable to get channel class");
        return NULL;
    }

    jfieldID fid = (*env)->GetFieldID(env, cls, "handle", "J");
    if (fid == NULL) {
        ThrowByName(env, "java/io/IOException", "Unable to find handle");
        return NULL;
    }

    jlong handle = (*env)->GetLongField(env, channel, fid);
    return (HANDLE)handle;
}

/* Inaccessible static: skipBuffer */
#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_SpawnerInputStream_read0(JNIEnv *env, jobject proc, jobject channel,
                                                                jbyteArray buf, jint len) {
    jbyte tmpBuf[BUFF_SIZE];
    int nBuffOffset = 0;
    HANDLE handle = channelToHandle(env, channel);
    OVERLAPPED overlapped;
    overlapped.Offset = 0;
    overlapped.OffsetHigh = 0;
    overlapped.hEvent = CreateEvent(NULL,  // no security attribute
                                    TRUE,  // manual-reset event
                                    TRUE,  // initial state = signaled
                                    NULL); // unnamed event object

    if (NULL == overlapped.hEvent) {
        char *lpMsgBuf;
        FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS, NULL,
                      GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
                      (wchar_t *)&lpMsgBuf, 0, NULL);

        ThrowByName(env, "java/io/IOException", lpMsgBuf);
        // Free the buffer.
        LocalFree(lpMsgBuf);
    }

    if (isTraceEnabled(CDT_TRACE_MONITOR) && isTraceEnabled(CDT_TRACE_READ_REPORT)) {
        cdtTrace(L"Start read %p\n", handle);
    }

    while (len > nBuffOffset) {
        DWORD nNumberOfBytesToRead = min(len - nBuffOffset, BUFF_SIZE);
        DWORD nNumberOfBytesRead;
        if (0 == ReadFile(handle, tmpBuf, nNumberOfBytesToRead, &nNumberOfBytesRead, &overlapped)) {
            int err = GetLastError();

            if (err == ERROR_IO_PENDING) {
                // asynchronous i/o is still in progress
                // check on the results of the asynchronous read
                if (GetOverlappedResult(handle, &overlapped, &nNumberOfBytesRead, TRUE)) {
                    err = 0;
                } else { // if there was a problem ...
                    err = GetLastError();
                }
            }
            if (err == ERROR_BROKEN_PIPE) { // Pipe was closed
                break;
            }
            if (err != 0) {
                char *lpMsgBuf;
                if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                    cdtTrace(L"Read failed - %p, error %i\n", handle, err);
                }
                if (err !=
                    ERROR_MORE_DATA) { // Otherwise error means just that there are more data than buffer can accept
                    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM |
                                      FORMAT_MESSAGE_IGNORE_INSERTS,
                                  NULL, err, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
                                  (wchar_t *)&lpMsgBuf, 0, NULL);

                    ThrowByName(env, "java/io/IOException", lpMsgBuf);
                    LocalFree(lpMsgBuf);
                    nBuffOffset = 0;
                    break;
                } else {
                    // buffer overflow?
                    // according to msdn this happens in message read mode only
                    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                        cdtTrace(L"Buffer full - %p, bytes read: %i\n", handle, nNumberOfBytesRead);
                    }
                    // nNumberOfBytesRead can be 0 here for unknown reason (bug 269223)
                    nNumberOfBytesRead = nNumberOfBytesToRead;
                }
            }
        }
        if (nNumberOfBytesRead > 0) {
            (*env)->SetByteArrayRegion(env, buf, nBuffOffset, nNumberOfBytesRead, tmpBuf);
        } else {
            break;
        }
        nBuffOffset += nNumberOfBytesRead;
        if (nNumberOfBytesRead != nNumberOfBytesToRead) {
            break;
        } else {
            // Is there data left in the pipe?
            DWORD bytesAvailable = 0;
            if (!PeekNamedPipe(handle, NULL, 0, NULL, &bytesAvailable, NULL) || bytesAvailable == 0) {
                // No bytes left
                break;
            }
        }
    }
    CloseHandle(overlapped.hEvent);
    if (isTraceEnabled(CDT_TRACE_MONITOR) && isTraceEnabled(CDT_TRACE_READ_REPORT)) {
        cdtTrace(L"End read %p - bytes read: %d\n", handle, nBuffOffset);
    }
    return nBuffOffset; // This is a real full readed length
}

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_SpawnerInputStream_close0(JNIEnv *env, jobject proc, jobject channel) {
    int rc;
    HANDLE handle = channelToHandle(env, channel);
    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(L"Close %p\n", handle);
    }
    rc = (CloseHandle(handle) ? 0 : -1);
    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(L"Closed %p\n", handle);
    }
    return (rc ? GetLastError() : 0);
}

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_SpawnerInputStream_available0(JNIEnv *env, jobject proc, jobject channel) {
    DWORD nAvail = 0;
    HANDLE handle = channelToHandle(env, channel);

    if (0 == PeekNamedPipe(handle, NULL, 0, NULL, &nAvail, NULL)) {
        // error
        return 0;
    }
    return nAvail;
}

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_SpawnerOutputStream_write0(JNIEnv *env, jobject proc, jobject channel,
                                                                  jbyteArray buf, jint len) {
    jbyte tmpBuf[BUFF_SIZE];
    int nBuffOffset = 0;
    HANDLE handle = channelToHandle(env, channel);

    while (len > nBuffOffset) {
        DWORD nNumberOfBytesToWrite = min(len - nBuffOffset, BUFF_SIZE);
        DWORD nNumberOfBytesWritten;
        (*env)->GetByteArrayRegion(env, buf, nBuffOffset, nNumberOfBytesToWrite, tmpBuf);
        if (0 == WriteFile(handle, tmpBuf, nNumberOfBytesToWrite, &nNumberOfBytesWritten, NULL)) {
            char *lpMsgBuf;
            FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                          NULL, GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
                          (wchar_t *)&lpMsgBuf, 0, NULL);

            ThrowByName(env, "java/io/IOException", lpMsgBuf);
            LocalFree(lpMsgBuf);
            return 0;
        }
        nBuffOffset += nNumberOfBytesWritten;
    }
    return 0;
}

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_SpawnerOutputStream_close0(JNIEnv *env, jobject proc, jobject channel) {
    int rc;
    HANDLE handle = channelToHandle(env, channel);
    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(L"Close %p\n", handle);
    }
    FlushFileBuffers(handle);
    rc = (CloseHandle(handle) ? 0 : -1);
    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(L"Closed %p\n", handle);
    }
    return (rc ? GetLastError() : 0);
}
