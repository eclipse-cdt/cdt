/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
#include "stdafx.h"

static jstring getValue(JNIEnv * env, HKEY key, jstring subkey, jstring name) {
	const jchar * csubkey = env->GetStringChars(subkey, NULL);
	const jchar * cname = env->GetStringChars(name, NULL);
	jstring result = NULL;

	HKEY skey;
	LONG rc = RegOpenKeyEx(key, (const wchar_t *)csubkey, 0, KEY_READ, &skey);
	if (rc == ERROR_SUCCESS) {
		DWORD type;
		wchar_t buffer[256];
		DWORD len = sizeof(buffer);
		rc = RegQueryValueEx(skey, (const wchar_t *)cname, NULL, &type, (BYTE *)&buffer, &len);
		if (rc == ERROR_SUCCESS) {
			result = env->NewString((jchar *) buffer, (jsize) wcslen(buffer));
		}
		RegCloseKey(skey);
	}

	env->ReleaseStringChars(subkey, csubkey);
	env->ReleaseStringChars(name, cname);

	return result;
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_eclipse_cdt_utils_WindowsRegistry_getLocalMachineValue(
JNIEnv * env, jobject obj, jstring subkey, jstring name)
{
	return getValue(env, HKEY_LOCAL_MACHINE, subkey, name);
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_eclipse_cdt_utils_WindowsRegistry_getCurrentUserValue(
JNIEnv * env, jobject obj, jstring subkey, jstring name)
{
	return getValue(env, HKEY_CURRENT_USER, subkey, name);
}

/*
* Given a subkey (string) under HKEY_LOCAL_MACHINE, and an index (starting from 0)
* to the key's array of values, return the name of the indexed value.
* The return value is null on any error or when the index is invalid.
*/

static jstring getValueName(JNIEnv * env, HKEY key, jstring subkey, jint index) {
	const jchar * csubkey = env->GetStringChars(subkey, NULL);
	jstring 	result = NULL;

	HKEY skey;
	LONG rc = RegOpenKeyEx(key, (const wchar_t *)csubkey, 0, KEY_READ, &skey);
	if (rc != ERROR_SUCCESS)
		return NULL;

	wchar_t valueName[256];
	DWORD 	nameSize = sizeof(valueName) + 2;

	rc = RegEnumValue(skey, index,
		valueName, 		// UNICODE string
		&nameSize,
		NULL, NULL,
		NULL, 			// data string
		NULL);			// size in BYTE of data.

	if (rc == ERROR_SUCCESS)
	{
		result = env->NewString((jchar *)valueName, nameSize);
	}

	RegCloseKey(skey);

	env->ReleaseStringChars(subkey, csubkey);

	return result;
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_eclipse_cdt_utils_WindowsRegistry_getLocalMachineValueName(
JNIEnv * env, jobject obj, jstring subkey, jint index)
{
	return getValueName(env, HKEY_LOCAL_MACHINE, subkey, index);
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_eclipse_cdt_utils_WindowsRegistry_getCurrentUserValueName(
JNIEnv * env, jobject obj, jstring subkey, jint index)
{
	return getValueName(env, HKEY_CURRENT_USER, subkey, index);
}

/*
* Given a subkey (string) under HKEY_LOCAL_MACHINE, and an index (starting from 0)
* to the key's array of keys, return the name of the indexed key.
* The return value is null on any error or when the index is invalid.
*/

static jstring getKeyName(JNIEnv * env, HKEY key, jstring subkey, jint index) {
	const jchar * csubkey = env->GetStringChars(subkey, NULL);
	jstring 	result = NULL;

	HKEY skey;
	LONG rc = RegOpenKeyEx(key, (const wchar_t *)csubkey, 0, KEY_READ, &skey);
	if (rc != ERROR_SUCCESS)
		return NULL;

	wchar_t keyName[256];
	DWORD 	nameSize = sizeof(keyName) + 2;

	rc = RegEnumKeyEx(skey, index,
		keyName, 		// UNICODE string
		&nameSize,
		NULL, NULL,
		NULL,
		NULL);			// size in BYTE of data.

	if (rc == ERROR_SUCCESS)
	{
		result = env->NewString((jchar *)keyName, nameSize);
	}

	RegCloseKey(skey);

	env->ReleaseStringChars(subkey, csubkey);

	return result;
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_eclipse_cdt_utils_WindowsRegistry_getLocalMachineKeyName(
JNIEnv * env, jobject obj, jstring subkey, jint index)
{
	return getKeyName(env, HKEY_LOCAL_MACHINE, subkey, index);
}

extern "C"
JNIEXPORT jstring JNICALL Java_org_eclipse_cdt_utils_WindowsRegistry_getCurrentUserKeyName(
JNIEnv * env, jobject obj, jstring subkey, jint index)
{
	return getKeyName(env, HKEY_CURRENT_USER, subkey, index);
}
