/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/

#include <WinSock2.h>
#include <rapi2.h>

#include "org_eclipse_tm_internal_rapi_RapiSession.h"

#define RAPI_NATIVE(func) Java_org_eclipse_tm_internal_rapi_RapiSession_##func

JNIEXPORT jint JNICALL RAPI_NATIVE(CeRapiInit)
  (JNIEnv *env, jobject that, jint arg0)
{
	if (arg0 == 0) return E_FAIL;
	IRAPISession *pSession = (IRAPISession*) arg0;
	return pSession->CeRapiInit();
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeRapiUninit)
  (JNIEnv *env, jobject that, jint arg0)
{
	if (arg0 == 0) return E_FAIL;
	IRAPISession *pSession = (IRAPISession*) arg0;
	return pSession->CeRapiUninit();
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeRapiGetError)
  (JNIEnv *env, jobject that, jint arg0)
{
	if (arg0 == 0) return 0;
	IRAPISession *pSession = (IRAPISession*) arg0;
	return pSession->CeRapiGetError();
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeGetLastError)
  (JNIEnv *env, jobject that, jint arg0)
{
	if (arg0 == 0) return 0;
	IRAPISession *pSession = (IRAPISession*) arg0;
	return pSession->CeGetLastError();
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeCreateFile)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1, jint arg2, jint arg3, jint arg4, jint arg5)
{
	HANDLE h = INVALID_HANDLE_VALUE;
	const jchar *lparg1 = NULL;

	if (arg0 == 0) goto fail;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	h = pSession->CeCreateFile((LPCWSTR)lparg1, arg2, arg3, NULL, arg4, arg5, 0);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	return (jint)h;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeReadFile)
  (JNIEnv *env, jobject that, jint arg0, jint arg1, jbyteArray arg2, jint arg3, jintArray arg4)
{
	jboolean rc = 0;
	jbyte *lparg2 = NULL;
	jint *lparg4 = NULL;

	if (arg0 == 0) return rc;
	if (arg2) {
		lparg2 = env->GetByteArrayElements(arg2, NULL);
		if (lparg2 == NULL) goto fail;
	}
	if (arg4) {
		lparg4 = env->GetIntArrayElements(arg4, NULL);
		if (lparg4 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeReadFile((HANDLE)arg1, lparg2, arg3, (LPDWORD)lparg4, NULL);
fail:
	if (arg2 && lparg2) env->ReleaseByteArrayElements(arg2, lparg2, 0);
	if (arg4 && lparg4) env->ReleaseIntArrayElements(arg4, lparg4, 0);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeWriteFile)
  (JNIEnv *env, jobject that, jint arg0, jint arg1, jbyteArray arg2, jint arg3, jintArray arg4)
{
	jboolean rc = 0;
	jbyte *lparg2 = NULL;
	jint *lparg4 = NULL;

	if (arg0 == 0) return rc;
	if (arg2) {
		lparg2 = env->GetByteArrayElements(arg2, NULL);
		if (lparg2 == NULL) goto fail;
	}
	if (arg4) {
		lparg4 = env->GetIntArrayElements(arg4, NULL);
		if (lparg4 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeWriteFile((HANDLE)arg1, lparg2, arg3, (LPDWORD)lparg4, NULL);
fail:
	if (arg2 && lparg2) env->ReleaseByteArrayElements(arg2, lparg2, 0);
	if (arg4 && lparg4) env->ReleaseIntArrayElements(arg4, lparg4, 0);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeCloseHandle)
  (JNIEnv *env, jobject that, jint arg0, jint arg1)
{
	if (arg0 == 0) return 0;
	IRAPISession *pSession = (IRAPISession*) arg0;
	return pSession->CeCloseHandle((HANDLE)arg1);
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeCopyFile)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1, jstring arg2, jboolean arg3)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;
	const jchar *lparg2 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	if (arg2) {
		lparg2 = env->GetStringChars(arg2, NULL);
		if (lparg2 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeCopyFile((LPCWSTR)lparg1, (LPCWSTR)lparg2, arg3);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	if (arg2 && lparg2) env->ReleaseStringChars(arg2, lparg2);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeDeleteFile)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeDeleteFile((LPCWSTR)lparg1);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeMoveFile)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1, jstring arg2)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;
	const jchar *lparg2 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	if (arg2) {
		lparg2 = env->GetStringChars(arg2, NULL);
		if (lparg2 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeMoveFile((LPCWSTR)lparg1, (LPCWSTR)lparg2);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	if (arg2 && lparg2) env->ReleaseStringChars(arg2, lparg2);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeCreateDirectory)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeCreateDirectory((LPCWSTR) lparg1, NULL);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeRemoveDirectory)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeRemoveDirectory((LPCWSTR) lparg1);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	return rc;
}

struct FIND_DATA_FID_CACHE
{
	int cached;
	jclass clazz;
	jfieldID fileAttributes, creationTime, lastAccessTime;
	jfieldID lastWriteTime, fileSize, oid, fileName;
};

FIND_DATA_FID_CACHE FIND_DATAFc;

void cacheFIND_DATAFields(JNIEnv *env, jobject lpObject)
{
	if (FIND_DATAFc.cached) return;
	FIND_DATAFc.clazz = env->GetObjectClass(lpObject);
	FIND_DATAFc.fileAttributes = env->GetFieldID(FIND_DATAFc.clazz, "fileAttributes", "I");
	FIND_DATAFc.creationTime = env->GetFieldID(FIND_DATAFc.clazz, "creationTime", "J");
	FIND_DATAFc.lastAccessTime = env->GetFieldID(FIND_DATAFc.clazz, "lastAccessTime", "J");
	FIND_DATAFc.lastWriteTime = env->GetFieldID(FIND_DATAFc.clazz, "lastWriteTime", "J");
	FIND_DATAFc.fileSize = env->GetFieldID(FIND_DATAFc.clazz, "fileSize", "J");
	FIND_DATAFc.oid = env->GetFieldID(FIND_DATAFc.clazz, "oid", "I");
	FIND_DATAFc.fileName = env->GetFieldID(FIND_DATAFc.clazz, "fileName", "Ljava/lang/String;");
	FIND_DATAFc.cached = 1;
}

jlong FILETIME2jlong(FILETIME ft)
{
	jlong res = ft.dwHighDateTime;
	// well, this is a potential overflow since jlong is signed
	// we should take care about this in the java part
	res <<= 32;
	res |= ft.dwLowDateTime;
	return res;
}

LPFILETIME jlong2FILETIME(jlong jl, LPFILETIME ft)
{
	ft->dwLowDateTime = (DWORD)jl;
	jl >>= 32;
	ft->dwHighDateTime = (DWORD)jl;
	return ft;
}

void setFIND_DATAFields(JNIEnv *env, jobject lpObject, CE_FIND_DATA *pFindData)
{
	if (!FIND_DATAFc.cached) cacheFIND_DATAFields(env, lpObject);
	env->SetIntField(lpObject, FIND_DATAFc.fileAttributes, (jint)pFindData->dwFileAttributes);

	jlong creationTime = FILETIME2jlong(pFindData->ftCreationTime);
	env->SetLongField(lpObject, FIND_DATAFc.creationTime, creationTime);

	jlong lastAccessTime = FILETIME2jlong(pFindData->ftLastAccessTime);
	env->SetLongField(lpObject, FIND_DATAFc.lastAccessTime, lastAccessTime);

	jlong lastWriteTime = FILETIME2jlong(pFindData->ftLastWriteTime);
	env->SetLongField(lpObject, FIND_DATAFc.lastWriteTime, lastWriteTime);

	jlong fileSize = pFindData->nFileSizeHigh;
	fileSize <<= 32;
	fileSize |= pFindData->nFileSizeLow;
	env->SetLongField(lpObject, FIND_DATAFc.fileSize, fileSize);

	int fileNameLen = lstrlen(pFindData->cFileName);
	jstring fileName = env->NewString((const jchar*)pFindData->cFileName, fileNameLen);
	env->SetObjectField(lpObject, FIND_DATAFc.fileName, fileName);
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeFindFirstFile)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1, jobject arg2)
{
	HANDLE h = INVALID_HANDLE_VALUE;
	CE_FIND_DATA findData;
	const jchar *lparg1 = NULL;

	if (arg0 == 0) goto fail;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	h = pSession->CeFindFirstFile((LPCWSTR)lparg1, &findData);
	if (h != INVALID_HANDLE_VALUE && arg2) setFIND_DATAFields(env, arg2, &findData);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	return (jint)h;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeFindNextFile)
  (JNIEnv *env, jobject that, jint arg0, jint arg1, jobject arg2)
{
	jboolean rc = 0;
	CE_FIND_DATA findData;

	if (arg0 == 0) return 0;
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeFindNextFile((HANDLE)arg1, &findData);
	if (rc && arg2) setFIND_DATAFields(env, arg2, &findData);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeFindClose)
  (JNIEnv *env, jobject that, jint arg0, jint arg1)
{
	if (arg0 == 0) return 0;
	IRAPISession *pSession = (IRAPISession*) arg0;
	return pSession->CeFindClose((HANDLE)arg1);
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeFindAllFiles)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1, jint arg2, jintArray arg3, jintArray arg4)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;
	jint *lparg3 = NULL;
	jint *lparg4 = NULL;

	if (arg0 == 0) goto fail;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	if (arg3) {
		lparg3 = env->GetIntArrayElements(arg3, NULL);
		if (lparg3 == NULL) goto fail;
	}
	if (arg4) {
		lparg4 = env->GetIntArrayElements(arg4, NULL);
		if (lparg4 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeFindAllFiles((LPCWSTR)lparg1, arg2, (LPDWORD)lparg3, (LPLPCE_FIND_DATA)lparg4);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	if (arg3 && lparg3) env->ReleaseIntArrayElements(arg3, lparg3, 0);
	if (arg4 && lparg4) env->ReleaseIntArrayElements(arg4, lparg4, 0);
	return rc;
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeFindAllFilesEx)
  (JNIEnv *env, jobject that, jint arg0, jint arg1, jint arg2, jobjectArray arg3)
{
	if (arg0 == 0 || arg2 == 0 || arg3 == 0) return E_FAIL;
	LPCE_FIND_DATA findDataArray = (LPCE_FIND_DATA) arg2;
	for (int i = 0 ; i < arg1 ; i++) {
		jobject obj = env->GetObjectArrayElement(arg3, i);
		setFIND_DATAFields(env, obj, &findDataArray[i]);
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	return pSession->CeRapiFreeBuffer(findDataArray);
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeGetFileAttributes)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1)
{
	jint rc = 0xFFFFFFFF;
	const jchar *lparg1 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeGetFileAttributes((LPCWSTR)lparg1);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	return rc;
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CeGetFileSize)
  (JNIEnv *env, jobject that, jint arg0, jint arg1, jintArray arg2)
{
	jint rc = INVALID_FILE_SIZE;
	jint *lparg2 = NULL;

	if (arg0 == 0) return rc;
	if (arg2) {
		lparg2 = env->GetIntArrayElements(arg2, NULL);
		if (lparg2 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeGetFileSize((HANDLE)arg1, (LPDWORD)lparg2);
fail:
	if (arg2 && lparg2) env->ReleaseIntArrayElements(arg2, lparg2, 0);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeGetFileTime)
  (JNIEnv *env, jobject that, jint arg0, jint arg1, jlongArray arg2, jlongArray arg3, jlongArray arg4)
{
	jboolean rc = 0;
	jlong *lparg2 = NULL;
	jlong *lparg3 = NULL;
	jlong *lparg4 = NULL;
	FILETIME crTime, laTime, lwTime;

	if (arg0 == 0) return rc;
	if (arg2) {
		lparg2 = env->GetLongArrayElements(arg2, NULL);
		if (lparg2 == NULL) goto fail;
	}
	if (arg3) {
		lparg3 = env->GetLongArrayElements(arg3, NULL);
		if (lparg3 == NULL) goto fail;
	}
	if (arg4) {
		lparg4 = env->GetLongArrayElements(arg4, NULL);
		if (lparg4 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeGetFileTime((HANDLE)arg1, &crTime, &laTime, &lwTime);
	if (!rc) goto fail;
	lparg2[0] = FILETIME2jlong(crTime);
	lparg3[0] = FILETIME2jlong(laTime);
	lparg4[0] = FILETIME2jlong(lwTime);
fail:
	if (arg2 && lparg2) env->ReleaseLongArrayElements(arg2, lparg2, 0);
	if (arg3 && lparg3) env->ReleaseLongArrayElements(arg3, lparg3, 0);
	if (arg4 && lparg4) env->ReleaseLongArrayElements(arg4, lparg4, 0);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeSetFileAttributes)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1, jint arg2)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeSetFileAttributes((LPCWSTR)lparg1, arg2);
fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	return rc;
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeSetFileTime)
  (JNIEnv *env, jobject that, jint arg0, jint arg1, jlongArray arg2, jlongArray arg3, jlongArray arg4)
{
	jboolean rc = 0;
	FILETIME crTime, laTime, lwTime;
	LPFILETIME pcrTime = NULL, plaTime = NULL, plwTime = NULL;
	jlong *lparg2 = NULL, *lparg3 = NULL, *lparg4 = NULL;

	if (arg0 == 0) return 0;
	if (arg2) {
		lparg2 = env->GetLongArrayElements(arg2, NULL);
		if (lparg2 == NULL) goto fail;
		pcrTime = jlong2FILETIME(lparg2[0], &crTime);
	}
	if (arg3) {
		lparg3 = env->GetLongArrayElements(arg3, NULL);
		if (lparg3 == NULL) goto fail;
		plaTime = jlong2FILETIME(lparg3[0], &laTime);
	}
	if (arg4) {
		lparg4 = env->GetLongArrayElements(arg4, NULL);
		if (lparg4 == NULL) goto fail;
		plwTime = jlong2FILETIME(lparg4[0], &lwTime);
	}

	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeSetFileTime((HANDLE)arg1, pcrTime, plaTime, plwTime);
fail:
	if (arg2 && lparg2) env->ReleaseLongArrayElements(arg2, lparg2, 0);
	if (arg3 && lparg3) env->ReleaseLongArrayElements(arg3, lparg3, 0);
	if (arg4 && lparg4) env->ReleaseLongArrayElements(arg4, lparg4, 0);
	return rc;
}

struct PROCESS_INFORMATION_FID_CACHE
{
	int cached;
	jclass clazz;
	jfieldID hProcess, hThread;
	jfieldID dwProcessId, dwThreadId;
};

PROCESS_INFORMATION_FID_CACHE PROCESS_INFORMATIONFc;

void cachePROCESS_INFORMATIONFields(JNIEnv *env, jobject lpObject)
{
	if (PROCESS_INFORMATIONFc.cached) return;
	PROCESS_INFORMATIONFc.clazz = env->GetObjectClass(lpObject);
	PROCESS_INFORMATIONFc.hProcess = env->GetFieldID(PROCESS_INFORMATIONFc.clazz, "hProcess", "I");
	PROCESS_INFORMATIONFc.hThread = env->GetFieldID(PROCESS_INFORMATIONFc.clazz, "hThread", "I");
	PROCESS_INFORMATIONFc.dwProcessId = env->GetFieldID(PROCESS_INFORMATIONFc.clazz, "dwProcessId", "I");
	PROCESS_INFORMATIONFc.dwThreadId = env->GetFieldID(PROCESS_INFORMATIONFc.clazz, "dwThreadId", "I");
	PROCESS_INFORMATIONFc.cached = 1;
}

void setPROCESS_INFORMATIONFields(JNIEnv *env, jobject lpObject, PROCESS_INFORMATION *pi)
{
	if (!PROCESS_INFORMATIONFc.cached) cachePROCESS_INFORMATIONFields(env, lpObject);
	env->SetIntField(lpObject, PROCESS_INFORMATIONFc.hProcess, (jint)pi->hProcess);
	env->SetIntField(lpObject, PROCESS_INFORMATIONFc.hThread, (jint)pi->hThread);
	env->SetIntField(lpObject, PROCESS_INFORMATIONFc.dwProcessId, (jint)pi->dwProcessId);
	env->SetIntField(lpObject, PROCESS_INFORMATIONFc.dwThreadId, (jint)pi->dwThreadId);
}

JNIEXPORT jboolean JNICALL RAPI_NATIVE(CeCreateProcess)
  (JNIEnv *env, jobject that, jint arg0, jstring arg1, jstring arg2, jint arg3, jobject arg4)
{
	jboolean rc = 0;
	const jchar *lparg1 = NULL;
	const jchar *lparg2 = NULL;
	PROCESS_INFORMATION pi;

	if (arg0 == 0) return 0;
	if (arg1) {
		lparg1 = env->GetStringChars(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	if (arg2) {
		lparg2 = env->GetStringChars(arg2, NULL);
		if (lparg2 == NULL) goto fail;
	}
	IRAPISession *pSession = (IRAPISession*) arg0;
	rc = pSession->CeCreateProcess((LPCWSTR)lparg1, (LPCWSTR)lparg2, NULL, NULL, FALSE, arg3, NULL, NULL, NULL, &pi);
	if (!rc) goto fail;
	if (arg4) setPROCESS_INFORMATIONFields(env, arg4, &pi);

fail:
	if (arg1 && lparg1) env->ReleaseStringChars(arg1, lparg1);
	if (arg2 && lparg2) env->ReleaseStringChars(arg2, lparg2);
	return rc;
}