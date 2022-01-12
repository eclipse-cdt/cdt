/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
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
 *     Wind River Systems, Inc.
 *******************************************************************************/
#include <jni.h>
#include <stdio.h>
#include <org_eclipse_cdt_utils_spawner_SpawnerInputStream.h>
#include <org_eclipse_cdt_utils_spawner_SpawnerOutputStream.h>
#include <unistd.h>

/* Header for class _org_eclipse_cdt_utils_spawner_SpawnerInputStream */
/* Header for class _org_eclipse_cdt_utils_spawner_SpawnerOutputStream */

static void ThrowByName(JNIEnv *env, const char *name, const char *msg) {
    jclass cls = (*env)->FindClass(env, name);

    if (cls) { /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);
    }

    /* It's a good practice to clean up the local references. */
    (*env)->DeleteLocalRef(env, cls);
}

static int channelToFileDesc(JNIEnv *env, jobject channel) {
    if (channel == 0) {
        ThrowByName(env, "java/io/IOException", "Invalid channel object");
        return -1;
    }

    jclass cls = (*env)->GetObjectClass(env, channel);
    if (cls == 0) {
        ThrowByName(env, "java/io/IOException", "Unable to get channel class");
        return -1;
    }

    jfieldID fid = (*env)->GetFieldID(env, cls, "fd", "I");
    if (fid == 0) {
        ThrowByName(env, "java/io/IOException", "Unable to find fd");
        return -1;
    }

    jint fd = (*env)->GetIntField(env, channel, fid);
    return fd;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerInputStream_read0(JNIEnv *env, jobject jobj,
                                                                                   jobject channel, jbyteArray buf,
                                                                                   jint buf_len) {
    int fd;
    int status;
    jbyte *data;
    int data_len;

    data = (*env)->GetByteArrayElements(env, buf, 0);
    data_len = buf_len;
    fd = channelToFileDesc(env, channel);

    status = read(fd, data, data_len);
    (*env)->ReleaseByteArrayElements(env, buf, data, 0);

    if (status == 0) {
        /* EOF. */
        status = -1;
    } else if (status == -1) {
        /* Error, toss an exception */
        jclass exception = (*env)->FindClass(env, "java/io/IOException");
        if (exception == NULL) {
            /* Give up.  */
            return -1;
        }
        (*env)->ThrowNew(env, exception, "read error");
    }

    return status;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerInputStream_close0(JNIEnv *env, jobject jobj,
                                                                                    jobject channel) {
    int fd = channelToFileDesc(env, channel);
    return close(fd);
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerOutputStream_write0(JNIEnv *env, jobject jobj,
                                                                                     jobject channel, jbyteArray buf,
                                                                                     jint buf_len) {
    int status;
    int fd;
    jbyte *data;
    int data_len;

    data = (*env)->GetByteArrayElements(env, buf, 0);
    data_len = buf_len;
    fd = channelToFileDesc(env, channel);

    status = write(fd, data, data_len);
    (*env)->ReleaseByteArrayElements(env, buf, data, 0);

    return status;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_SpawnerOutputStream_close0(JNIEnv *env, jobject jobj,
                                                                                     jobject channel) {
    int fd = channelToFileDesc(env, channel);
    return close(fd);
}
