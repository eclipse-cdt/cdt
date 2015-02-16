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

JNIEXPORT jlong JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_open0
(JNIEnv *env, jobject jobj, jstring portName, jint baudRate, jint byteSize, jint parity, jint stopBits)
{
	const char * cportName = (*env)->GetStringUTFChars(env, portName, NULL);
	int fd = open(cportName, O_RDWR | O_NOCTTY | O_NDELAY);
	if (fd >= 0) {
		// Turn off NDELAY
		int flags = fcntl(fd, F_GETFL, 0);
		flags &= ~O_NDELAY;
		fcntl(fd, F_SETFL, flags);

		struct termios newtio;
		bzero(&newtio, sizeof(newtio));
		newtio.c_cflag = CLOCAL | CREAD;

		cfsetispeed(&newtio, baudRate);
		cfsetospeed(&newtio, baudRate);

		switch (byteSize) {
		case 5:
			newtio.c_cflag |= CS5;
			break;
		case 6:
			newtio.c_cflag |= CS6;
			break;
		case 7:
			newtio.c_cflag |= CS7;
			break;
		case 8:
			newtio.c_cflag |= CS8;
			break;

		}

		switch (parity) {
		case 0: // None
			break;
		case 1: // Even
			newtio.c_cflag |= PARENB;
			break;
		case 2: // Odd
			newtio.c_cflag |= (PARENB | PARODD);
			break;
		}

		switch (stopBits) {
		case 0: // 1
			break;
		case 1: // 2
			newtio.c_cflag |= CSTOPB;
			break;
		}

		newtio.c_iflag = IGNPAR;
		newtio.c_oflag = 0;

		newtio.c_lflag = 0;

		newtio.c_cc[VTIME]    = 0;   /* inter-character timer unused */
		newtio.c_cc[VMIN]     = 1;   /* blocking read until 5 chars received */

		tcflush(fd, TCIFLUSH);
		tcsetattr(fd, TCSANOW, &newtio);
	}

	return fd;
}

JNIEXPORT void JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_close0
(JNIEnv *env, jobject jobj, jlong handle)
{
	close(handle);
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_read0
(JNIEnv *env, jobject jobj, jlong handle)
{
	char buff;
	int res = read(handle, &buff, 1);
	return res < 0 ? -1 : buff;
}

JNIEXPORT void JNICALL Java_org_eclipse_cdt_utils_serial_SerialPort_write0
(JNIEnv *env, jobject jobj, jlong handle, jint b)
{
	char buff = b;
	write(handle, &buff, 1);
}
