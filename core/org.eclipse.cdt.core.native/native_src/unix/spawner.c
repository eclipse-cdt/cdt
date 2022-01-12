/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
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
 *     Mikhail Zabaluev (Nokia) - bug 82744
 *******************************************************************************/
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <string.h>
#include <jni.h>
#include <stdbool.h>

#include "exec0.h"
#include <org_eclipse_cdt_utils_spawner_Spawner.h>

static bool trace_enabled = false;

static void print_array(FILE *stream, const char *str, char **c_array) {
    if (c_array) {
        bool hasElement = false;

        fprintf(stream, "%s [", str);

        for (char **p = c_array; *p; p++) {
            if (*p) {
                if (hasElement) {
                    fprintf(stream, ",");
                }
                hasElement = true;
                fprintf(stream, "\n    \"%s\"", *p);
            }
        }

        if (hasElement) {
            fprintf(stream, "\n");
        }

        fprintf(stream, "]\n");
    } else {
        fprintf(stream, "%s null\n", str);
    }
}

static char **alloc_c_array(JNIEnv *env, jobjectArray j_array) {
    jint c_array_size = (*env)->GetArrayLength(env, j_array);
    char **c_array = calloc(c_array_size + 1, sizeof(char *));

    if (c_array == NULL) {
        return NULL;
    }

    for (int i = 0; i < c_array_size; i++) {
        jstring j_str = (jstring)(*env)->GetObjectArrayElement(env, j_array, i);
        const char *c_str = (*env)->GetStringUTFChars(env, j_str, NULL);
        c_array[i] = (char *)strdup(c_str);
        (*env)->ReleaseStringUTFChars(env, j_str, c_str);
        (*env)->DeleteLocalRef(env, j_str);
    }

    return c_array;
}

static void free_c_array(char **c_array) {
    if (c_array) {
        for (char **p = c_array; *p; p++) {
            free(*p);
        }
        free(c_array);
    }
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec2(JNIEnv *env, jobject jobj, jobjectArray jcmd,
                                                                        jobjectArray jenv, jstring jdir,
                                                                        jobjectArray jchannels, jstring jslaveName,
                                                                        jint masterFD, jboolean console) {
    const char *dirpath = (*env)->GetStringUTFChars(env, jdir, NULL);
    const char *pts_name = (*env)->GetStringUTFChars(env, jslaveName, NULL);
    char **cmd = NULL;
    char **envp = NULL;
    int fd[3];
    pid_t pid = -1;

    if (!jchannels) {
        goto bail_out;
    }

    cmd = alloc_c_array(env, jcmd);
    if (!cmd) {
        goto bail_out;
    }

    envp = alloc_c_array(env, jenv);
    if (!envp) {
        goto bail_out;
    }

    if (trace_enabled) {
        print_array(stderr, "command:", cmd);
        print_array(stderr, "Envp:", envp);
        fprintf(stderr, "dirpath: %s\n", dirpath);
        fprintf(stderr, "pts_name: %s\n", pts_name);
    }

    pid = exec_pty(cmd[0], cmd, envp, dirpath, fd, pts_name, masterFD, console);
    if (pid < 0) {
        goto bail_out;
    }

    jobject cls = (*env)->FindClass(env, "org/eclipse/cdt/utils/spawner/Spawner$UnixChannel");
    jmethodID constructor = (*env)->GetMethodID(env, cls, "<init>", "(I)V");
    for (jsize i = 0; i < 3; i++) {
        jobject chan = (*env)->NewObject(env, cls, constructor, fd[i]);
        (*env)->SetObjectArrayElement(env, jchannels, i, chan);
    }

bail_out:
    (*env)->ReleaseStringUTFChars(env, jdir, dirpath);
    (*env)->ReleaseStringUTFChars(env, jslaveName, pts_name);
    free_c_array(cmd);
    free_c_array(envp);
    return pid;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec1(JNIEnv *env, jobject jobj, jobjectArray jcmd,
                                                                        jobjectArray jenv, jstring jdir) {
    const char *dirpath = (*env)->GetStringUTFChars(env, jdir, NULL);
    char **cmd = NULL;
    char **envp = NULL;
    pid_t pid = -1;

    cmd = alloc_c_array(env, jcmd);
    if (!cmd) {
        goto bail_out;
    }

    envp = alloc_c_array(env, jenv);
    if (!envp) {
        goto bail_out;
    }

    if (trace_enabled) {
        print_array(stderr, "command:", cmd);
        print_array(stderr, "Envp:", envp);
        fprintf(stderr, "dirpath: %s\n", dirpath);
    }

    pid = exec0(cmd[0], cmd, envp, dirpath, NULL);
    if (pid < 0) {
        goto bail_out;
    }

bail_out:
    (*env)->ReleaseStringUTFChars(env, jdir, dirpath);
    free_c_array(cmd);
    free_c_array(envp);
    return pid;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec0(JNIEnv *env, jobject jobj, jobjectArray jcmd,
                                                                        jobjectArray jenv, jstring jdir,
                                                                        jobjectArray jchannels) {
    const char *dirpath = (*env)->GetStringUTFChars(env, jdir, NULL);
    char **cmd = NULL;
    char **envp = NULL;
    int fd[3];
    pid_t pid = -1;
    jclass channelClass = NULL;
    jmethodID channelConstructor = NULL;

    if (!jchannels) {
        goto bail_out;
    }

    channelClass = (*env)->FindClass(env, "org/eclipse/cdt/utils/spawner/Spawner$UnixChannel");
    if (channelClass == 0) {
        goto bail_out;
    }

    channelConstructor = (*env)->GetMethodID(env, channelClass, "<init>", "(I)V");
    if (!channelConstructor) {
        goto bail_out;
    }

    cmd = alloc_c_array(env, jcmd);
    if (!cmd) {
        goto bail_out;
    }

    envp = alloc_c_array(env, jenv);
    if (!envp) {
        goto bail_out;
    }

    if (trace_enabled) {
        print_array(stderr, "command:", cmd);
        print_array(stderr, "Envp:", envp);
        fprintf(stderr, "dirpath: %s\n", dirpath);
    }
    pid = exec0(cmd[0], cmd, envp, dirpath, fd);
    if (pid < 0) {
        goto bail_out;
    }

    for (jsize i = 0; i < 3; i++) {
        jobject chan = (*env)->NewObject(env, channelClass, channelConstructor, fd[i]);
        (*env)->SetObjectArrayElement(env, jchannels, i, chan);
    }

bail_out:
    (*env)->ReleaseStringUTFChars(env, jdir, dirpath);
    free_c_array(cmd);
    free_c_array(envp);
    return pid;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_raise(JNIEnv *env, jobject jobj, jint pid, jint sig) {
    int status = killpg(pid, sig);
    if (status == -1) {
        status = kill(pid, sig);
    }

    return status;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_waitFor(JNIEnv *env, jobject jobj, jint pid) {
    return wait0(pid);
}

JNIEXPORT void JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_configureNativeTrace(
    JNIEnv *env, jclass cls, jboolean spawner, jboolean spawnerDetails, jboolean starter, jboolean readReport) {
    if (spawner) {
        trace_enabled = true;
    }
}
