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

#include "org_eclipse_tm_internal_rapi_RapiDevice.h"

#define RAPI_NATIVE(func) Java_org_eclipse_tm_internal_rapi_RapiDevice_##func

struct RAPI_DEVICEINFO_FID_CACHE
{
	int cached;
	jclass clazz;
	jfieldID id, versionMajor, versionMinor, name, platform;
};

struct RAPI_CONNECTIONINFO_FID_CACHE
{
	int cached;
	jclass clazz;
	jfieldID connectionType;
};

RAPI_DEVICEINFO_FID_CACHE RAPI_DEVICEINFOFc;
RAPI_CONNECTIONINFO_FID_CACHE RAPI_CONNECTIONINFOFc;

JNIEXPORT jint JNICALL RAPI_NATIVE(CreateSession)
  (JNIEnv *env, jobject that, jint arg0, jintArray arg1)
{
	jint rc = E_FAIL;
	jint *lparg1 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetIntArrayElements(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPIDevice *pDevice = (IRAPIDevice*) arg0;
	rc = pDevice->CreateSession((IRAPISession**)lparg1);
fail:
	if (arg1 && lparg1) env->ReleaseIntArrayElements(arg1, lparg1, 0);
	return rc;
}

void cacheRAPI_DEVICEINFOFields(JNIEnv *env, jobject lpObject)
{
	if (RAPI_DEVICEINFOFc.cached) return;
	RAPI_DEVICEINFOFc.clazz = env->GetObjectClass(lpObject);
	RAPI_DEVICEINFOFc.id = env->GetFieldID(RAPI_DEVICEINFOFc.clazz, "id", "Ljava/lang/String;");
	RAPI_DEVICEINFOFc.versionMajor = env->GetFieldID(RAPI_DEVICEINFOFc.clazz, "versionMajor", "I");
	RAPI_DEVICEINFOFc.versionMinor = env->GetFieldID(RAPI_DEVICEINFOFc.clazz, "versionMinor", "I");
	RAPI_DEVICEINFOFc.name = env->GetFieldID(RAPI_DEVICEINFOFc.clazz, "name", "Ljava/lang/String;");
	RAPI_DEVICEINFOFc.platform = env->GetFieldID(RAPI_DEVICEINFOFc.clazz, "platform", "Ljava/lang/String;");
	RAPI_DEVICEINFOFc.cached = 1;
}

void cacheRAPI_CONNECTIONINFOFields(JNIEnv *env, jobject lpObject)
{
	if (RAPI_CONNECTIONINFOFc.cached) return;
	RAPI_CONNECTIONINFOFc.clazz = env->GetObjectClass(lpObject);
	RAPI_CONNECTIONINFOFc.connectionType = env->GetFieldID(RAPI_CONNECTIONINFOFc.clazz, "connectionType", "I");
	RAPI_CONNECTIONINFOFc.cached = 1;
}

void setRAPI_DEVICEINFOFields(JNIEnv *env, jobject lpObject, RAPI_DEVICEINFO *pDeviceInfo)
{
	if (!RAPI_DEVICEINFOFc.cached) cacheRAPI_DEVICEINFOFields(env, lpObject);
	WCHAR *pwszGUID = NULL;
	if (StringFromCLSID(pDeviceInfo->DeviceId, &pwszGUID) == S_OK) {
		int idLen = lstrlen(pwszGUID);
		jstring id = env->NewString((const jchar*)pwszGUID, idLen);
		env->SetObjectField(lpObject, RAPI_DEVICEINFOFc.id, id);
		CoTaskMemFree(pwszGUID);
	}
	env->SetIntField(lpObject, RAPI_DEVICEINFOFc.versionMajor, (jint)pDeviceInfo->dwOsVersionMajor);
	env->SetIntField(lpObject, RAPI_DEVICEINFOFc.versionMinor, (jint)pDeviceInfo->dwOsVersionMinor);
	// create new String from the native BSTR
	UINT nameLen = SysStringLen(pDeviceInfo->bstrName);
	jstring name = env->NewString((const jchar*)pDeviceInfo->bstrName, nameLen);
	env->SetObjectField(lpObject, RAPI_DEVICEINFOFc.name, name);
	// create new String from the native BSTR
	UINT platformLen = SysStringLen(pDeviceInfo->bstrPlatform);
	jstring platform = env->NewString((const jchar*)pDeviceInfo->bstrPlatform, platformLen);
	env->SetObjectField(lpObject, RAPI_DEVICEINFOFc.platform, platform);
}

void setRAPI_CONNECTIONINFOFields(JNIEnv *env, jobject lpObject, RAPI_CONNECTIONINFO *pConnectionInfo)
{
	if (!RAPI_CONNECTIONINFOFc.cached) cacheRAPI_CONNECTIONINFOFields(env, lpObject);
	env->SetIntField(lpObject, RAPI_CONNECTIONINFOFc.connectionType, (jint)pConnectionInfo->connectionType);
}

JNIEXPORT jint JNICALL RAPI_NATIVE(GetDeviceInfo)
  (JNIEnv *env, jobject that, jint arg0, jobject arg1)
{
	jint rc = E_FAIL;
	RAPI_DEVICEINFO deviceInfo = {0};

	if (arg0 == 0) return rc;
	IRAPIDevice *pDevice = (IRAPIDevice*) arg0;
	rc = pDevice->GetDeviceInfo(&deviceInfo);
	if (rc != NOERROR) goto fail;
	if (arg1) setRAPI_DEVICEINFOFields(env, arg1, &deviceInfo);
fail:
	FreeDeviceInfoData(&deviceInfo);
	return rc;
}

JNIEXPORT jint JNICALL RAPI_NATIVE(GetConnectionInfo)
  (JNIEnv *env, jobject that, jint arg0, jobject arg1)
{
	jint rc = E_FAIL;
	RAPI_CONNECTIONINFO connectionInfo = {0};

	if (arg0 == 0) return rc;
	IRAPIDevice *pDevice = (IRAPIDevice*) arg0;
	rc = pDevice->GetConnectionInfo(&connectionInfo);
	if (rc != NOERROR) goto fail;
	if (arg1) setRAPI_CONNECTIONINFOFields(env, arg1, &connectionInfo);
fail:
	return rc;
}

JNIEXPORT jint JNICALL RAPI_NATIVE(GetConnectStat)
  (JNIEnv *env, jobject that, jint arg0, jintArray arg1)
{
	jint rc = E_FAIL;
	jint *lparg1 = NULL;
	RAPI_DEVICESTATUS devStatus;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetIntArrayElements(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPIDevice *pDevice = (IRAPIDevice*) arg0;
	rc = pDevice->GetConnectStat(&devStatus);
	lparg1[0] = (int)devStatus;
fail:
	if (arg1 && lparg1) env->ReleaseIntArrayElements(arg1, lparg1, 0);
	return rc;
}