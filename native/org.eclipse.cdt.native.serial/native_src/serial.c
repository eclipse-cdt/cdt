/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
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
 *     STMicroelectronics
 *******************************************************************************/
#ifdef __APPLE__
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/uio.h>
#endif
#ifndef __MINGW32__
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <errno.h>
#include <sys/ioctl.h>
#ifndef __APPLE__
#include <linux/serial.h>
#endif
#else
#define WIN32_LEAN_AND_MEAN
#define UNICODE
#include <windows.h>
#endif
#include <jni.h>
#include <org_eclipse_cdt_serial_SerialPort.h>

#define FUNC(x) Java_org_eclipse_cdt_serial_SerialPort_##x

/**
 * Use this method to throw an exception when open fails after the OS open
 * stage. This method obtains the last error from OS to include in the
 * IOException
 */
#ifndef __APPLE__
#ifndef __MINGW32__
static void closeAndthrowIOException(int fd, JNIEnv *env, const char *msg) {
#else
static void closeAndthrowIOException(HANDLE handle, JNIEnv *env, const char *msg) {
#endif
    char buff[256];
#ifndef __MINGW32__
    snprintf(buff, sizeof(buff), "%s: %s", msg, strerror(errno));
    close(fd);
#else
    sprintf_s(buff, sizeof(buff), "%s (%d)", msg, GetLastError());
    CloseHandle(handle);
#endif
    jclass cls = (*env)->FindClass(env, "java/io/IOException");
    (*env)->ThrowNew(env, cls, buff);
}
#endif

static void throwIOException(JNIEnv *env, const char *msg) {
    char buff[256];
#ifndef __MINGW32__
    snprintf(buff, sizeof(buff), "%s: %s", msg, strerror(errno));
#else
    sprintf_s(buff, sizeof(buff), "%s (%d)", msg, GetLastError());
#endif
    jclass cls = (*env)->FindClass(env, "java/io/IOException");
    (*env)->ThrowNew(env, cls, buff);
}

JNIEXPORT jlong JNICALL FUNC(open0)(JNIEnv *env, jobject jobj, jstring portName, jint baudRate, jint byteSize,
                                    jint parity, jint stopBits) {
#ifndef __MINGW32__
    const char *cportName = (*env)->GetStringUTFChars(env, portName, NULL);
    int fd = open(cportName, O_RDWR | O_NOCTTY | O_NDELAY);
    if (fd < 0) {
        char msg[256];
        snprintf(msg, sizeof(msg), "Error opening %s", cportName);
        (*env)->ReleaseStringUTFChars(env, portName, cportName);
        throwIOException(env, msg);
        return fd;
    }
    (*env)->ReleaseStringUTFChars(env, portName, cportName);

    // Turn off all flags
    fcntl(fd, F_SETFL, 0);

    struct termios options;
    tcgetattr(fd, &options);
    options.c_cflag |= (CLOCAL | CREAD);

#ifndef __APPLE__
    speed_t baud;
    switch (baudRate) {
    case 110:
        baud = B110;
        break;
    case 300:
        baud = B300;
        break;
    case 600:
        baud = B600;
        break;
    case 1200:
        baud = B1200;
        break;
    case 2400:
        baud = B2400;
        break;
    case 4800:
        baud = B4800;
        break;
    case 9600:
        baud = B9600;
        break;
    case 19200:
        baud = B19200;
        break;
    case 38400:
        baud = B38400;
        break;
    case 57600:
        baud = B57600;
        break;
    case 115200:
        baud = B115200;
        break;
    case 230400:
        baud = B230400;
        break;
    case 460800:
        baud = B460800;
        break;
    case 500000:
        baud = B500000;
        break;
    case 576000:
        baud = B576000;
        break;
    case 921600:
        baud = B921600;
        break;
    case 1000000:
        baud = B1000000;
        break;
    case 1152000:
        baud = B1152000;
        break;
    case 1500000:
        baud = B1500000;
        break;
    case 2000000:
        baud = B2000000;
        break;
    case 2500000:
        baud = B2500000;
        break;
    case 3000000:
        baud = B3000000;
        break;
    case 3500000:
        baud = B3500000;
        break;
    case 4000000:
        baud = B4000000;
        break;
    default:
        baud = B0;
        break;
    }

    if (baud == B0) {
        // Use custom linux baud rates if possible: https://bugs.eclipse.org/bugs/show_bug.cgi?id=543122#c8
        struct serial_struct serial_options;
        options.c_cflag |= B38400;

        if (ioctl(fd, TIOCGSERIAL, &serial_options) != 0) {
            closeAndthrowIOException(fd, env, "Failed to use custom baud rate. Error using TIOCGSERIAL");
            return -1;
        }
        serial_options.custom_divisor = serial_options.baud_base / baudRate;
        if (serial_options.custom_divisor == 0) {
            serial_options.custom_divisor = 1;
        }

        serial_options.flags &= ~ASYNC_SPD_MASK;
        serial_options.flags |= ASYNC_SPD_CUST;

        if (ioctl(fd, TIOCSSERIAL, &serial_options) != 0) {
            closeAndthrowIOException(fd, env, "Failed to use custom baud rate. Error using TIOCSSERIAL");
            return -1;
        }
    } else {
        // Set baud rate
        cfsetispeed(&options, baud);
        cfsetospeed(&options, baud);
    }

#else
    // On OSX speed_t is simply the baud rate:
    // https://developer.apple.com/library/archive/documentation/System/Conceptual/ManPages_iPhoneOS/man3/cfsetispeed.3.html
    cfsetispeed(&options, baudRate);
    cfsetospeed(&options, baudRate);
#endif

    // set data size
    options.c_cflag &= ~CSIZE;
    switch (byteSize) {
    case 5:
        options.c_cflag |= CS5;
        break;
    case 6:
        options.c_cflag |= CS6;
        break;
    case 7:
        options.c_cflag |= CS7;
        break;
    case 8:
        options.c_cflag |= CS8;
        break;
    }

    // set parity
    switch (parity) {
    case 0: // None
        options.c_cflag &= ~PARENB;
        break;
    case 1: // Even
        options.c_cflag |= PARENB;
        options.c_cflag &= ~PARODD;
        break;
    case 2: // Odd
        options.c_cflag |= (PARENB | PARODD);
        break;
    }

    switch (stopBits) {
    case 0: // 1
        options.c_cflag &= ~CSTOPB;
        break;
    case 1: // 2
        options.c_cflag |= CSTOPB;
        break;
    }

    // raw input
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);

    // ignore parity
    options.c_iflag |= IGNPAR;

    // turn off those bits in the input flag that fiddle with CR and NL
    options.c_iflag &= ~(ICRNL | INLCR | IGNCR);

    options.c_cc[VMIN] = 0;  // min chars to read
    options.c_cc[VTIME] = 2; // 10ths second timeout

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW, &options);

    return fd;
#else // __MINGW32__
    const wchar_t *cportName = (const wchar_t *)(*env)->GetStringChars(env, portName, NULL);
    HANDLE handle =
        CreateFile(cportName, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_FLAG_OVERLAPPED, NULL);
    (*env)->ReleaseStringChars(env, portName, cportName);

    if (handle == INVALID_HANDLE_VALUE) {
        char msg[256];
        const char *name = (*env)->GetStringUTFChars(env, portName, NULL);
        sprintf_s(msg, sizeof(msg), "Error opening %s", name);
        (*env)->ReleaseStringUTFChars(env, portName, name);
        throwIOException(env, msg);
        return -1;
    }

    DCB dcb = {0};

    if (!GetCommState(handle, &dcb)) {
        closeAndthrowIOException(handle, env, "Error getting DCB");
        return -1;
    }

    dcb.BaudRate = baudRate;
    dcb.ByteSize = (BYTE)byteSize;

    switch (parity) {
    case 0: // None
        dcb.fParity = FALSE;
        dcb.Parity = NOPARITY;
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
        closeAndthrowIOException(handle, env, "Error setting DCB");
        return -1;
    }

    COMMTIMEOUTS timeouts = {0};
    timeouts.ReadIntervalTimeout = MAXDWORD;
    timeouts.ReadTotalTimeoutMultiplier = MAXDWORD;
    timeouts.ReadTotalTimeoutConstant = 200;
    if (!SetCommTimeouts(handle, &timeouts)) {
        closeAndthrowIOException(handle, env, "Error setting timeouts");
        return -1;
    }

#ifdef _WIN64
    return (jlong)handle;
#else
    return (jlong)(unsigned)handle;
#endif
#endif // __MINGW32__
}

JNIEXPORT void JNICALL FUNC(close0)(JNIEnv *env, jobject jobj, jlong handle) {
#ifndef __MINGW32__
    close(handle);
#else
#ifdef _WIN64
    CloseHandle((HANDLE)handle);
#else
    CloseHandle((HANDLE)(unsigned)handle);
#endif
#endif
}

JNIEXPORT jint JNICALL FUNC(available0)(JNIEnv *env, jobject jobj, jlong jhandle) {
#ifndef __MINGW32__
    int result = 0;
    if (ioctl(jhandle, FIONREAD, &result) < 0) {
        throwIOException(env, "Error calling ioctl");
        return 0;
    }
    return result;
#else
    COMSTAT stat;
    DWORD errCode;
#ifdef _WIN64
    HANDLE handle = (HANDLE)jhandle;
#else
    HANDLE handle = (HANDLE)(unsigned)jhandle;
#endif

    if (ClearCommError(handle, &errCode, &stat) == 0) {
        throwIOException(env, "Error calling ClearCommError");
        return -1;
    }
    return (int)stat.cbInQue;
#endif
}

JNIEXPORT jint JNICALL FUNC(read1)(JNIEnv *env, jobject jobj, jlong jhandle, jbyteArray bytes, jint offset, jint size) {
#ifndef __MINGW32__
    jbyte buff[256];
    int n = size < sizeof(buff) ? size : sizeof(buff);
    n = read(jhandle, buff, n);
    if (n > 0) {
        (*env)->SetByteArrayRegion(env, bytes, offset, n, buff);
    }
    return n;
#else
    OVERLAPPED olp = {0};

    olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
    if (olp.hEvent == NULL) {
        throwIOException(env, "Error creating event");
        return -1;
    }

    char buff[256];
    DWORD nread = sizeof(buff) < size ? sizeof(buff) : size;
#ifdef _WIN64
    HANDLE handle = (HANDLE)jhandle;
#else
    HANDLE handle = (HANDLE)(unsigned)jhandle;
#endif

    if (!ReadFile(handle, buff, sizeof(buff), &nread, &olp)) {
        if (GetLastError() != ERROR_IO_PENDING) {
            throwIOException(env, "Error reading from port");
            CloseHandle(olp.hEvent);
            return -1;
        } else {
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
        (*env)->SetByteArrayRegion(env, bytes, offset, nread, (jbyte *)buff);
    }
    CloseHandle(olp.hEvent);
    return nread;
#endif
}

JNIEXPORT void JNICALL FUNC(write0)(JNIEnv *env, jobject jobj, jlong jhandle, jint b) {
#ifndef __MINGW32__
    char buff = b;
    write(jhandle, &buff, 1);
#else
    OVERLAPPED olp = {0};

    olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
    if (olp.hEvent == NULL) {
        throwIOException(env, "Error creating event");
        return;
    }

    char buff = (char)b;
    DWORD nwritten;
#ifdef _WIN64
    HANDLE handle = (HANDLE)jhandle;
#else
    HANDLE handle = (HANDLE)(unsigned)jhandle;
#endif

    if (!WriteFile(handle, &buff, sizeof(buff), &nwritten, &olp)) {
        if (GetLastError() != ERROR_IO_PENDING) {
            throwIOException(env, "Error writing to port");
        } else {
            switch (WaitForSingleObject(olp.hEvent, INFINITE)) {
            case WAIT_OBJECT_0:
                if (!GetOverlappedResult(handle, &olp, &nwritten, FALSE)) {
                    throwIOException(env, "Error waiting for write");
                }
            }
        }
    }

    CloseHandle(olp.hEvent);
#endif
}

JNIEXPORT void JNICALL FUNC(write1)(JNIEnv *env, jobject jobj, jlong jhandle, jbyteArray bytes, jint offset,
                                    jint size) {
#ifndef __MINGW32__
    while (size > 0) {
        jbyte buff[256];
        int n = size < sizeof(buff) ? size : sizeof(buff);
        (*env)->GetByteArrayRegion(env, bytes, offset, n, buff);
        n = write(jhandle, buff, n);
        if (n < 0) {
            return;
        }
        size -= n;
        offset += n;
    }
#else
    OVERLAPPED olp = {0};

    olp.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
    if (olp.hEvent == NULL) {
        throwIOException(env, "Error creating event");
        return;
    }

    while (size > 0) {
        char buff[256];
        DWORD nwritten = sizeof(buff) < size ? sizeof(buff) : size;
        (*env)->GetByteArrayRegion(env, bytes, offset, nwritten, (jbyte *)buff);
#ifdef _WIN64
        HANDLE handle = (HANDLE)jhandle;
#else
        HANDLE handle = (HANDLE)(unsigned)jhandle;
#endif

        if (!WriteFile(handle, buff, nwritten, &nwritten, &olp)) {
            if (GetLastError() != ERROR_IO_PENDING) {
                throwIOException(env, "Error writing to port");
                return;
            } else {
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
#endif
}


JNIEXPORT jobjectArray JNICALL Java_org_eclipse_cdt_serial_SerialPort_list0(JNIEnv *env, jclass cls) {
    jclass exception = (*env)->FindClass(env, "java/io/IOException");
    (*env)->ThrowNew(env, exception, "Not implemented for this OS");
    return NULL;
}
