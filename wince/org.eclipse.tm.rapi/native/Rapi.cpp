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

#define _WIN32_DCOM
#include <WinSock2.h>
#include <rapi2.h>

#include "org_eclipse_tm_rapi_Rapi.h"

#define RAPI_NATIVE(func) Java_org_eclipse_tm_rapi_Rapi_##func

JNIEXPORT jint JNICALL RAPI_NATIVE(CoInitializeEx)
  (JNIEnv *env, jclass that, jint arg0, jint arg1)
{
	return (jint)CoInitializeEx((LPVOID)arg0, arg1);
}

JNIEXPORT void JNICALL RAPI_NATIVE(CoUninitialize)
  (JNIEnv *env, jclass that)
{
	CoUninitialize();
}

JNIEXPORT jint JNICALL RAPI_NATIVE(CreateRapiDesktop)
  (JNIEnv *env, jclass that, jintArray arg0)
{
	jint rc = 0;
	jint *lparg0 = NULL;
	if (arg0) {
		lparg0 = env->GetIntArrayElements(arg0, NULL);
		if (lparg0 == NULL) goto fail;
	}
	rc = CoCreateInstance(CLSID_RAPI, NULL, CLSCTX_INPROC_SERVER, 
		IID_IRAPIDesktop, (void**)lparg0);
fail:
	if (arg0 && lparg0) env->ReleaseIntArrayElements(arg0, lparg0, 0);
	return rc;
}

JNIEXPORT void JNICALL RAPI_NATIVE(ReleaseIUnknown)
  (JNIEnv *env, jclass that, jint arg0)
{
	if (arg0 == 0) return;
	IUnknown *ptr = (IUnknown*) arg0;
	ptr->Release();
}
