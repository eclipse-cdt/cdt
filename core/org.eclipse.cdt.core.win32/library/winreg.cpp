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
