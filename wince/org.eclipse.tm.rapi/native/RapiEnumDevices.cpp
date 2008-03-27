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

#include "org_eclipse_tm_internal_rapi_RapiEnumDevices.h"

#define RAPI_NATIVE(func) Java_org_eclipse_tm_internal_rapi_RapiEnumDevices_##func

JNIEXPORT jint JNICALL RAPI_NATIVE(Next)
  (JNIEnv *env, jobject that, jint arg0, jintArray arg1)
{
	jint rc = E_FAIL;
	jint *lparg1 = NULL;

	if (arg0 == 0) return rc;
	if (arg1) {
		lparg1 = env->GetIntArrayElements(arg1, NULL);
		if (lparg1 == NULL) goto fail;
	}
	IRAPIEnumDevices *pEnumDevices = (IRAPIEnumDevices*) arg0;
	rc = pEnumDevices->Next((IRAPIDevice**)lparg1);
fail:
	if (arg1 && lparg1) env->ReleaseIntArrayElements(arg1, lparg1, 0);
	return rc;
}
