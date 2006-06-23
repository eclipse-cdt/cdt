/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc.
 *******************************************************************************/
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <string.h>
#include <jni.h>

#include "exec0.h"
#include <Spawner.h>


#define DEBUGIT 0


/*
 * Header for class org_eclipse_cdt_utils_spawner_Spawner 
 */


#if DEBUGIT
static void print_array(char **c_array)
{
    if (c_array) {
        char **p = c_array;
        for (; *p; p++) {
            if (*p) {
                fprintf(stderr, " %s", *p);
            }
        }
    } else {
        fprintf(stderr, "null");
    }
    fprintf(stderr, "\n");
}
#endif


static char **alloc_c_array(JNIEnv * env, jobjectArray j_array)
{
    int i;
    jint c_array_size = (*env)->GetArrayLength(env, j_array);
    char **c_array = calloc(c_array_size + 1, sizeof(*c_array));

    if (c_array == NULL)
        return NULL;

    for (i = 0; i < c_array_size; i++) {
        jstring j_str =
            (jstring) (*env)->GetObjectArrayElement(env, j_array, i);
        const char *c_str = (*env)->GetStringUTFChars(env, j_str, NULL);
        c_array[i] = (char *) strdup(c_str);
        (*env)->ReleaseStringUTFChars(env, j_str, c_str);
        (*env)->DeleteLocalRef(env, j_str);
    }

    return c_array;
}


static void free_c_array(char **c_array)
{
    if (c_array) {
        char **p = c_array;
        for (; *p; p++) {
            if (*p) {
                free(*p);
            }
        }
        free(c_array);
    }
}


/*
 * Class:     org_eclipse_cdt_utils_spawner_Spawner
 * Method:    exec2
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[ILorg/eclipse/cdt/utils/pty/PTY;)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec2
  (JNIEnv *env, jobject jobj, jobjectArray jcmd, jobjectArray jenv, jstring jdir, jintArray jchannels,
   jstring jslaveName, jint masterFD)
{
    jint *channels = (*env)->GetIntArrayElements(env, jchannels, 0);
    const char *dirpath = (*env)->GetStringUTFChars(env, jdir, NULL);
    const char *pts_name = (*env)->GetStringUTFChars(env, jslaveName, NULL);
    char **cmd;
    char **envp;
    int fd[3];
    pid_t pid = -1;

    if (channels == NULL)
        goto bail_out;

    cmd = alloc_c_array(env, jcmd);
    if (cmd == NULL)
        goto bail_out;

    envp = alloc_c_array(env, jenv);
    if (envp == NULL)
        goto bail_out;

#if DEBUGIT
    fprintf(stderr, "command:");
    print_array(cmd);
    fprintf(stderr, "Envp:");
    print_array(envp);
    fprintf(stderr, "dirpath: %s\n", dirpath);
    fprintf(stderr, "pts_name: %s\n", pts_name);
#endif

    pid = exec_pty(cmd[0], cmd, envp, dirpath, fd, pts_name, masterFD);
    if (pid < 0)
        goto bail_out;

    channels[0] = fd[0];
    channels[1] = fd[1];
    channels[2] = fd[2];

  bail_out:
    (*env)->ReleaseIntArrayElements(env, jchannels, channels, 0);
    (*env)->ReleaseStringUTFChars(env, jdir, dirpath);
    (*env)->ReleaseStringUTFChars(env, jslaveName, pts_name);
    if (cmd)
        free_c_array(cmd);
    if (envp)
        free_c_array(envp);
    return pid;
}
 

JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_spawner_Spawner_exec1(JNIEnv * env, jobject jobj,
                                               jobjectArray jcmd,
                                               jobjectArray jenv,
                                               jstring jdir)
{
    const char *dirpath = (*env)->GetStringUTFChars(env, jdir, NULL);
    char **cmd;
    char **envp;
    pid_t pid = -1;

    cmd = alloc_c_array(env, jcmd);
    if (cmd == NULL)
        goto bail_out;

    envp = alloc_c_array(env, jenv);
    if (envp == NULL)
        goto bail_out;

#if DEBUGIT
    fprintf(stderr, "command:");
    print_array(cmd);
    fprintf(stderr, "Envp:");
    print_array(envp);
    fprintf(stderr, "dirpath: %s\n", dirpath);
#endif

    pid = exec0(cmd[0], cmd, envp, dirpath, NULL);
    if (pid < 0)
        goto bail_out;

  bail_out:
    (*env)->ReleaseStringUTFChars(env, jdir, dirpath);
    if (cmd)
        free_c_array(cmd);
    if (envp)
        free_c_array(envp);
    return pid;
}

/*
 * Class:     org_eclipse_cdt_utils_spawner_Spawner
 * Method:    exec0
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[I)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_spawner_Spawner_exec0(JNIEnv * env, jobject jobj,
                                               jobjectArray jcmd,
                                               jobjectArray jenv,
                                               jstring jdir,
                                               jintArray jchannels)
{
    jint *channels = (*env)->GetIntArrayElements(env, jchannels, 0);
    const char *dirpath = (*env)->GetStringUTFChars(env, jdir, NULL);
    char **cmd;
    char **envp;
    int fd[3];
    pid_t pid = -1;

    if (channels == NULL)
        goto bail_out;

    cmd = alloc_c_array(env, jcmd);
    if (cmd == NULL)
        goto bail_out;

    envp = alloc_c_array(env, jenv);
    if (envp == NULL)
        goto bail_out;

#if DEBUGIT
    fprintf(stderr, "command:");
    print_array(cmd);
    fprintf(stderr, "Envp:");
    print_array(envp);
    fprintf(stderr, "dirpath: %s\n", dirpath);
#endif

    pid = exec0(cmd[0], cmd, envp, dirpath, fd);
    if (pid < 0)
        goto bail_out;

    channels[0] = fd[0];
    channels[1] = fd[1];
    channels[2] = fd[2];

  bail_out:
    (*env)->ReleaseIntArrayElements(env, jchannels, channels, 0);
    (*env)->ReleaseStringUTFChars(env, jdir, dirpath);
    if (cmd)
        free_c_array(cmd);
    if (envp)
        free_c_array(envp);
    return pid;
}

/*
 * Class:     org_eclipse_cdt_utils_spawner_Spawner
 * Method:    raise
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_spawner_Spawner_raise(JNIEnv * env, jobject jobj,
                                               jint pid, jint sig)
{
    int status = -1;

    switch (sig) {
    case 0:                    /* NOOP */
	status = killpg(pid, 0);
	if(status == -1) {
        	status = kill(pid, 0);
	}
	break;

    case 2:                    /* INTERRUPT */
	status = killpg(pid, SIGINT);
	if(status == -1) {
        	status = kill(pid, SIGINT);
	}
	break;

    case 9:                    /* KILL */
	status = killpg(pid, SIGKILL);
	if(status == -1) {
        	status = kill(pid, SIGKILL);
	}
	break;

    case 15:                   /* TERM */
	status = killpg(pid, SIGTERM);
	if(status == -1) {
        	status = kill(pid, SIGTERM);
	}
	break;

    default:
	status = killpg(pid, sig);        /* WHAT ?? */
	if(status == -1) {
        	status = kill(pid, sig);        /* WHAT ?? */
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
JNIEXPORT jint JNICALL
Java_org_eclipse_cdt_utils_spawner_Spawner_waitFor(JNIEnv * env,
                                                 jobject jobj, jint pid)
{
    return wait0(pid);
}
