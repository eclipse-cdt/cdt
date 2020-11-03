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
#include <jni_util.h>

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

/*
 * Class:     org_eclipse_cdt_utils_spawner_Spawner
 * Method:    raise
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_raise(JNIEnv *env, jobject jobj, jint pid, jint sig) {
    int status = -1;

    switch (sig) {
    case 0: /* NOOP */
        status = killpg(pid, 0);
        if (status == -1) {
            status = kill(pid, 0);
        }
        break;

    case 2: /* INTERRUPT */
        status = killpg(pid, SIGINT);
        if (status == -1) {
            status = kill(pid, SIGINT);
        }
        break;

    case 9: /* KILL */
        status = killpg(pid, SIGKILL);
        if (status == -1) {
            status = kill(pid, SIGKILL);
        }
        break;

    case 15: /* TERM */
        status = killpg(pid, SIGTERM);
        if (status == -1) {
            status = kill(pid, SIGTERM);
        }
        break;

    default:
        status = killpg(pid, sig); /* WHAT ?? */
        if (status == -1) {
            status = kill(pid, sig); /* WHAT ?? */
        }
        break;
    }

    return status;
}

/*
 * Class:     org_eclipse_cdt_utils_spawner_Spawner
 * Method:    waitFor
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_waitFor(JNIEnv *env, jobject jobj, jint pid) {
    return wait0(pid);
}

JNIEXPORT void JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_configureNativeTrace(JNIEnv *env, jclass cls) {
    jclass clsPlatform = (*env)->FindClass(env, "org/eclipse/core/runtime/Platform");
    if (!clsPlatform) {
        ThrowByName(env, "java/lang/UnsatisfiedLinkError", "Unable to find org.eclipse.core.runtime.Platform");
        return;
    }

    jmethodID funcGetDebugBoolean =
        (*env)->GetStaticMethodID(env, clsPlatform, "getDebugBoolean", "(Ljava/lang/String;)Z");
    if (!funcGetDebugBoolean) {
        ThrowByName(env, "java/lang/UnsatisfiedLinkError", "Unable to find Platform#getDebugBoolean(String) method");
        return;
    }

    jclass clsCNativePlugin = (*env)->FindClass(env, "org/eclipse/cdt/internal/core/natives/CNativePlugin");
    if (!clsPlatform) {
        ThrowByName(env, "java/lang/UnsatisfiedLinkError",
                    "Unable to find org.eclipse.cdt.internal.core.natives.CNativePlugin");
        return;
    }

    jfieldID fieldPLUGIN_ID = (*env)->GetStaticFieldID(env, clsCNativePlugin, "PLUGIN_ID", "Ljava/lang/String;");
    if (!fieldPLUGIN_ID) {
        ThrowByName(env, "java/lang/UnsatisfiedLinkError", "Unable to find CNativePlugin#PLUGIN_ID field");
        return;
    }

    jstring PLUGIN_ID = (jstring)(*env)->GetStaticObjectField(env, clsCNativePlugin, fieldPLUGIN_ID);
    if (!PLUGIN_ID) {
        ThrowByName(env, "java/lang/UnsatisfiedLinkError", "Unable to get CNativePlugin#PLUGIN_ID field");
        return;
    }

    const char *prefix = (*env)->GetStringUTFChars(env, PLUGIN_ID, NULL);
    if (prefix) {
        const char *suffix = "/debug/native/spawner";

        int bufferLen = strlen(prefix) + strlen(suffix) + 1;
        char *buffer = (char *)malloc(bufferLen * sizeof(char));
        if (buffer) {
            *buffer = '\0';
            strncat(buffer, prefix, bufferLen);
            strncat(buffer, suffix, bufferLen);

            jstring option = (*env)->NewStringUTF(env, buffer);
            if (option) {
                jboolean isEnabled = (*env)->CallStaticBooleanMethod(env, clsPlatform, funcGetDebugBoolean, option);
                if (isEnabled) {
                    trace_enabled = true;
                }
            } else {
                ThrowByName(env, "java/lang/UnsatisfiedLinkError", "Unable to create option string");
                free(buffer);
                (*env)->ReleaseStringUTFChars(env, PLUGIN_ID, prefix);
                return;
            }
        } else {
            ThrowByName(env, "java/lang/OutOfMemoryError", "Unable to malloc buffer");
            (*env)->ReleaseStringUTFChars(env, PLUGIN_ID, prefix);
            return;
        }

        free(buffer);
        (*env)->ReleaseStringUTFChars(env, PLUGIN_ID, prefix);
    } else {
        ThrowByName(env, "java/lang/UnsatisfiedLinkError", "Unable to get CNativePlugin#PLUGIN_ID native string value");
        return;
    }
}
