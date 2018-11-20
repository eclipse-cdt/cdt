/*******************************************************************************
 * Copyright (c) 2013, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

#include "PTY.h"
#include "PTYInputStream.h"
#include "PTYOutputStream.h"
#include "winpty.h"

#include <string>
#include <vector>
#include <map>
#include <stdlib.h>
#include <assert.h>
#include <ctime>

static std::map<int, winpty_t*> fd2pty;
static std::map<int, int> fd2rc;

/*
 * Class:     org_eclipse_cdt_utils_pty_PTY
 * Method:    openMaster
 * Signature: (Z)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_eclipse_cdt_utils_pty_PTY_openMaster(JNIEnv *env, jobject jobj, jboolean console) 
{
	jfieldID fid; /* Store the field ID */
	jstring jstr = NULL;
	jclass cls;

	int master = -1;
	char line[1024];

	line[0] = '\0';

	/* Open new winpty handle */
	winpty_t* winpty = winpty_open(80, 40);
	if (winpty == NULL) {
		return NULL;
	}

    /* Configure console mode */
    if (console) {
        winpty_set_console_mode(winpty, 1);
    }

	/* Generate masterFD based on current system time */
	srand((unsigned int)time(NULL));
	master = rand();

	/* Make sure masterFD does not exist */
	while (fd2pty.find(master) != fd2pty.end())
		master++;

	sprintf(line, "winpty_%i", master);

    /* Remember the winpty handle for the generated masterFD */
    fd2pty.insert(std::pair<int, winpty_t*>(master, winpty));
	
	/* Get a reference to the obj's class */
	cls = env->GetObjectClass(jobj);

	/* Set the master fd.  */
	fid = env->GetFieldID(cls, "master", "I");
	if (fid == NULL) {
		return NULL;
	}
	env->SetIntField(jobj, fid, (jint)master);

	/* Create a new String for the slave.  */
	jstr = env->NewStringUTF(line);

	return jstr;
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTY
 * Method:    change_window_size
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTY_change_1window_1size(JNIEnv *env, jobject jobj, jint fdm, jint width, jint height)
{
    int fd;
    std::map<int, winpty_t*> :: const_iterator fd2pty_Iter;

    fd = fdm;
    fd2pty_Iter = fd2pty.find(fd);
    if (fd2pty_Iter != fd2pty.end()) {
        winpty_t* winpty = fd2pty_Iter -> second;
	    if (winpty != NULL)
		    return winpty_set_size(winpty, width, height);
    }

	return 0;
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTYInputStream
 * Method:    read0
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTYInputStream_read0
  (JNIEnv *env, jobject jobj, jint jfd, jbyteArray buf, jint buf_len)
{
	DWORD amount = -1;
	OVERLAPPED over;
    int fd;
    std::map<int, winpty_t*> :: const_iterator fd2pty_Iter;

    fd = jfd;
    fd2pty_Iter = fd2pty.find(fd);
    if (fd2pty_Iter != fd2pty.end()) {
        winpty_t* winpty = fd2pty_Iter -> second;
    	if (winpty != NULL) {
	    	/* Get the pipe handle */
    		HANDLE handle = winpty_get_data_pipe(winpty);

            memset(&over, 0, sizeof(over));
    		over.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

	    	char *buffer = new char[buf_len];
    		memset(buffer, 0, sizeof(*buffer));

	    	jbyte *data = env->GetByteArrayElements(buf, 0);
    		memset(data, 0, sizeof(*data));

	    	amount = 0;
    		BOOL ret = ReadFile(handle, buffer, buf_len, &amount, &over);
	    	if (!ret) {
		    	DWORD error = GetLastError();
			    if (error == ERROR_IO_PENDING)
				    ret = GetOverlappedResult(handle, &over, &amount, TRUE);
		    }

		    if (ret && amount > 0)
			    memcpy(data, buffer, amount);

            if (!ret || amount == 0)
                amount = -1;

            if (!ret && fd2pty.find(fd) != fd2pty.end()) {
                int rc = winpty_get_exit_code(winpty);
                fd2rc.insert(std::pair<int, int>(fd, rc));
            }

            delete[] buffer;
		    env->ReleaseByteArrayElements(buf, data, 0);
		    ResetEvent(over.hEvent);
	    }
    }

	return amount;
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTYInputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTYInputStream_close0(JNIEnv *env, jobject jobj, jint jfd)
{
    int fd;
    std::map<int, winpty_t*> :: iterator fd2pty_Iter;

    fd = jfd;
    fd2pty_Iter = fd2pty.find(fd);
    if (fd2pty_Iter != fd2pty.end()) {
        winpty_t* winpty = fd2pty_Iter -> second;
        fd2pty.erase(fd2pty_Iter);
	    if (winpty != NULL) {
		    winpty_close(winpty);
		    winpty = NULL;
	    }
    }

	return 0;
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTYOutputStream
 * Method:    write0
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTYOutputStream_write0(JNIEnv *env, jobject jobj, jint jfd, jbyteArray buf, jint buf_len)
{
	DWORD written = -1;
	OVERLAPPED over;
    int fd;
    std::map<int, winpty_t*> :: iterator fd2pty_Iter;

    fd = jfd;
    fd2pty_Iter = fd2pty.find(fd);
    if (fd2pty_Iter != fd2pty.end()) {
        winpty_t* winpty = fd2pty_Iter -> second;
	    if (winpty != NULL) {
		    /* Get the pipe handle */
		    HANDLE handle = winpty_get_data_pipe(winpty);
		
            memset(&over, 0, sizeof(over));
		    over.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

		    char *buffer = new char[buf_len];
		    memset(buffer, 0, sizeof(*buffer));

		    jbyte *data = env->GetByteArrayElements(buf, 0);
		    memcpy(buffer, data, buf_len);

		    BOOL ret = WriteFile(handle, buffer, buf_len, &written, &over);
		    env->ReleaseByteArrayElements(buf, data, 0);

		    if (!ret && GetLastError() == ERROR_IO_PENDING)
			    ret = GetOverlappedResult(handle, &over, &written, TRUE);
            if (!ret || (int)written != buf_len)
                written = -1;

            delete[] buffer;
	    }
    }

	return written;
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTYOutputStream
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTYOutputStream_close0(JNIEnv *env, jobject jobj, jint jfd)
{
    int fd;
    std::map<int, winpty_t*> :: iterator fd2pty_Iter;

    fd = jfd;
    fd2pty_Iter = fd2pty.find(fd);
    if (fd2pty_Iter != fd2pty.end()) {
        winpty_t* winpty = fd2pty_Iter -> second;
        fd2pty.erase(fd2pty_Iter);
	    if (winpty != NULL) {
		    winpty_close(winpty);
    		winpty = NULL;
    	}
    }

	return 0;
}

/*
 * Convert convert slashes to backslashes.
 */
static std::wstring convertSlashes(const wchar_t *path)
{
    std::wstring ret;

	for (int i = 0; path[i] != L'\0'; ++i) {
        if (path[i] == L'/')
            ret.push_back(L'\\');
        else
            ret.push_back(path[i]);
    }

    return ret;
}

// Convert argc/argv into a Win32 command-line following the escaping convention
// documented on MSDN.  (e.g. see CommandLineToArgvW documentation)
static std::wstring argvToCommandLine(const std::vector<std::wstring> &argv)
{
    std::wstring result;
    for (size_t argIndex = 0; argIndex < argv.size(); ++argIndex) {
        if (argIndex > 0)
            result.push_back(L' ');
        const wchar_t *arg = argv[argIndex].c_str();
        const bool quote =
            wcschr(arg, L' ') != NULL ||
            wcschr(arg, L'\t') != NULL ||
            *arg == L'\0';
        if (quote)
            result.push_back(L'\"');
        int bsCount = 0;
        for (const wchar_t *p = arg; *p != L'\0'; ++p) {
            if (*p == L'\\') {
                bsCount++;
            } else if (*p == L'\"') {
                result.append(bsCount * 2 + 1, L'\\');
                result.push_back(L'\"');
                bsCount = 0;
            } else {
                result.append(bsCount, L'\\');
                bsCount = 0;
                result.push_back(*p);
            }
        }
        if (quote) {
            result.append(bsCount * 2, L'\\');
            result.push_back(L'\"');
        } else {
            result.append(bsCount, L'\\');
        }
    }
    return result;
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTY
 * Method:    exec2
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;[ILjava/lang/String;IZ)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTY_exec2
	(JNIEnv *env, jobject jobj, jobjectArray jcmd, jobjectArray jenv, jstring jdir, jintArray jchannels, jstring jslaveName, jint masterFD, jboolean console)
{
    int fd;
    std::map<int, winpty_t*> :: iterator fd2pty_Iter;

    jint *channels = env->GetIntArrayElements(jchannels, 0);
    const wchar_t *cwdW = (const wchar_t *) env->GetStringChars(jdir, NULL);
    const char *pts_name = env->GetStringUTFChars(jslaveName, NULL);

	int pid = -1;

	int i;
    jint argc = env->GetArrayLength(jcmd);
    jint envc = env->GetArrayLength(jenv);

    if (channels == NULL)
        goto bail_out;

    fd = masterFD;
    fd2pty_Iter = fd2pty.find(fd);
    if (fd2pty_Iter != fd2pty.end()) {
        winpty_t* winpty = fd2pty_Iter -> second;
    	if (winpty != NULL) {
	    	std::vector<std::wstring> argVector;

		    for (i = 0; i < argc; i++) {
			    jstring j_str = (jstring) env->GetObjectArrayElement(jcmd, i);
			    const wchar_t *w_str = (const wchar_t *) env->GetStringChars(j_str, NULL);
			    if (i == 0) argVector.push_back(convertSlashes(w_str));
			    else argVector.push_back(w_str);
	            env->ReleaseStringChars(j_str, (const jchar *) w_str);
		        env->DeleteLocalRef(j_str);
		    }

            std::wstring envp;

		    for (i = 0; i < envc; i++) {
			    jstring j_str = (jstring) env->GetObjectArrayElement(jenv, i);
			    const wchar_t *w_str = (const wchar_t *) env->GetStringChars(j_str, NULL);
                envp.append(w_str);
                envp.push_back(L'\0');
	            env->ReleaseStringChars(j_str, (const jchar *) w_str);
		        env->DeleteLocalRef(j_str);
		    }

            std::wstring cmdLine = argvToCommandLine(argVector);
            const wchar_t *cmdLineW = cmdLine.c_str();

		    int ret = winpty_start_process(winpty,
                                           NULL,
                                           cmdLineW,
                                           cwdW,
                                           envp.c_str());

            if (ret == 0) {
			    // Success. Get the process id.
			    pid = winpty_get_process_id(winpty);
            }
	    }
    }

bail_out:
    env->ReleaseIntArrayElements(jchannels, channels, 0);
    env->ReleaseStringChars(jdir, (const jchar *) cwdW);
    env->ReleaseStringUTFChars(jslaveName, pts_name);

	return pid;
}

/*
 * Class:     org_eclipse_cdt_utils_pty_PTY
 * Method:    waitFor
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_pty_PTY_waitFor
  (JNIEnv *env, jobject jobj, jint masterFD, jint pid)
{
	int status = -1;
    DWORD flags;

    int fd;
    std::map<int, winpty_t*> :: iterator fd2pty_Iter;
    std::map<int, int> :: iterator fd2rc_Iter;

    fd = masterFD;
    fd2pty_Iter = fd2pty.find(fd);
    if (fd2pty_Iter != fd2pty.end()) {
        winpty_t* winpty = fd2pty_Iter -> second;
    	if (winpty != NULL) {
            HANDLE handle = winpty_get_data_pipe(winpty);
            BOOL success;
	    	do {
                success = GetHandleInformation(handle, &flags);
                if (success) Sleep(500);
		    } while (success);

            fd2rc_Iter = fd2rc.find(fd);
            if (fd2rc_Iter != fd2rc.end()) {
                status = fd2rc_Iter -> second;
                fd2rc.erase(fd2rc_Iter);
            }
	    }
    }

	return status;
}
