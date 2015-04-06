/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/uio.h>
#include <unistd.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <jni.h>

#define FUNC(x) Java_org_eclipse_cdt_serial_SerialPort_ ## x

JNIEXPORT jlong JNICALL FUNC(open0)(JNIEnv *env, jobject jobj, jstring portName, jint baudRate, jint byteSize, jint parity, jint stopBits)
{
	const char * cportName = (*env)->GetStringUTFChars(env, portName, NULL);
	int fd = open(cportName, O_RDWR | O_NOCTTY | O_NDELAY);
	if (fd < 0) {
		return fd;
	}

	// Turn off all flags
	fcntl(fd, F_SETFL, 0);

	struct termios options;
	tcgetattr(fd, &options);
	options.c_cflag |= (CLOCAL | CREAD);

	// Set baud rate
	cfsetispeed(&options, baudRate);
	cfsetospeed(&options, baudRate);

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

	options.c_cc[VMIN]     = 0;   // min chars to read
	options.c_cc[VTIME]    = 10;   // 10ths second timeout

	tcflush(fd, TCIFLUSH);
	tcsetattr(fd, TCSANOW, &options);

	return fd;
}

JNIEXPORT void JNICALL FUNC(close0)(JNIEnv *env, jobject jobj, jlong handle)
		{
	close(handle);
		}

JNIEXPORT jint JNICALL FUNC(read0)(JNIEnv *env, jobject jobj, jlong handle)
		{
	char buff;
	int res = read(handle, &buff, 1);
	return res < 0 ? -1 : buff;
		}

JNIEXPORT jint JNICALL FUNC(read1)(JNIEnv * env, jobject jobj, jlong handle, jbyteArray bytes, jint offset, jint size) {
	jbyte buff[256];
	int n = size < sizeof(buff) ? size : sizeof(buff);
	n = read(handle, buff, n);
	if (n > 0) {
		(*env)->SetByteArrayRegion(env, bytes, offset, n, buff);
	}
	return n;
}

JNIEXPORT void JNICALL FUNC(write0)(JNIEnv *env, jobject jobj, jlong handle, jint b)
		{
	char buff = b;
	write(handle, &buff, 1);
		}
