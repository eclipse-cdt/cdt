#include <windows.h>
#include <jni.h>
#include <string.h>

extern "C"
JNIEXPORT jstring Java_org_eclipse_cdt_utils_WindowsRegistry_getLocalMachineValue(
	JNIEnv * env, jobject obj, jstring subkey, jstring name)
{
	const jchar * csubkey = env->GetStringChars(subkey, NULL);
	const jchar * cname = env->GetStringChars(name, NULL);
	jstring result = NULL;
	
	HKEY key;
	LONG rc = RegOpenKeyEx(HKEY_LOCAL_MACHINE, (const wchar_t *)csubkey, 0, KEY_READ, &key);
	if (rc == ERROR_SUCCESS) {
		DWORD type;
		wchar_t buffer[256];
		DWORD len = sizeof(buffer);
		rc = RegQueryValueEx(key, (const wchar_t *)cname, NULL, &type, (BYTE *)&buffer, &len);
		if (rc == ERROR_SUCCESS) {
			result = env->NewString((jchar *)buffer, wcslen(buffer));
		}
		RegCloseKey(key);
	}

	env->ReleaseStringChars(subkey, csubkey);
	env->ReleaseStringChars(name, cname);

	return result;
}

/*
 * Given a subkey (string) under HKEY_LOCAL_MACHINE, and an index (starting from 0)
 * to the key's array of values, return the name of the indexed value. 
 * The return value is null on any error or when the index is invalid.
 */
 
extern "C"
JNIEXPORT jstring Java_org_eclipse_cdt_utils_WindowsRegistry_getLocalMachineValueName(
	JNIEnv * env, jobject obj, jstring subkey, jint index)
{
	const jchar * csubkey = env->GetStringChars(subkey, NULL);
	jstring 	result = NULL;

	HKEY key;
	LONG rc = RegOpenKeyEx(HKEY_LOCAL_MACHINE, (const wchar_t *)csubkey, 0, KEY_READ, &key);
	if (rc != ERROR_SUCCESS)
		return NULL;
	
	wchar_t valueName[256];
	DWORD 	nameSize = sizeof(valueName) + 2;
	
	rc = RegEnumValue(key, index, 
			valueName, 		// UNICODE string
			&nameSize, 
			NULL, NULL, 
			NULL, 			// data string
			NULL);			// size in BYTE of data.
	
	if (rc == ERROR_SUCCESS)
	{
		result = env->NewString((jchar *)valueName, nameSize);
	}
	
	RegCloseKey(key);
	
	env->ReleaseStringChars(subkey, csubkey);

	return result;
}
