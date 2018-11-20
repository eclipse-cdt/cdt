/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
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
 * Martin Oberhuber (Wind River) - Bug 476709 - Fix change_window_size()
 *******************************************************************************/
#include "PTY.h"
#include "openpty.h"

#include <sys/ioctl.h>

/*
 * Class:     org_eclipse_cdt_utils_pty_PTY
 * Method:    forkpty
 * Signature: ()I
 */
JNIEXPORT jstring JNICALL
Java_org_eclipse_cdt_utils_pty_PTY_openMaster (JNIEnv *env, jobject jobj, jboolean console) {
	jfieldID fid; /* Store the field ID */
	jstring jstr = NULL;
	int master = -1;
	char line[1024];	/* FIXME: Should be enough */
	jclass cls;

	line[0] = '\0';

	master = ptym_open(line);
	if (master >= 0) {
		if (console) {
			// turn off echo
			set_noecho(master);
		}

		/* Get a reference to the obj's class */
		cls = (*env)->GetObjectClass(env, jobj);

		/* Set the master fd.  */
		fid = (*env)->GetFieldID(env, cls, "master", "I");
		if (fid == NULL) {
			return NULL;
		}
		(*env)->SetIntField(env, jobj, fid, (jint)master);

		/* Create a new String for the slave.  */
		jstr = (*env)->NewStringUTF(env, line);
	}
	return jstr;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTY_change_1window_1size
  (JNIEnv *env, jobject jobj, jint fdm, jint width, jint height)
{
#ifdef	TIOCSWINSZ
	struct winsize win;

	win.ws_col = width;
	win.ws_row = height;
	win.ws_xpixel = 0;
	win.ws_ypixel = 0;

	return ioctl(fdm, TIOCSWINSZ, &win);
#else
#error no TIOCSWINSZ
	return 0;
#endif
}
