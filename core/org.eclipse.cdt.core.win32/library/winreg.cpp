#include <windows.h>
#include <jni.h>
#include <string.h>

jstring getErrorMsg(JNIEnv * env, wchar_t * name) {
    wchar_t msg[256];
    wchar_t * msgBuff;
    DWORD err = GetLastError();

    FormatMessage(
        FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
        NULL,
        err,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
        (LPTSTR) &msgBuff,
        0, NULL );

    wsprintf(msg, L"%s failed with error %d: %s", name, err, msgBuff);

    LocalFree(msgBuff);

    return env->NewString((jchar *)msg, wcslen(msg));
}

extern "C"
JNIEXPORT jstring Java_org_eclipse_cdt_utils_WindowsRegistry_getLocalMachineValue(
	JNIEnv * env, jobject obj, jstring subkey, jstring name)
{
	const jchar * csubkey = env->GetStringChars(subkey, NULL);
	const jchar * cname = env->GetStringChars(name, NULL);
	jstring result;
	
	HKEY key;
	LONG rc = RegOpenKeyEx(HKEY_LOCAL_MACHINE, (const wchar_t *)csubkey, 0, KEY_READ, &key);
	if (rc != ERROR_SUCCESS) {
		result = getErrorMsg(env, L"RegOpenKeyEx");
	} else {
		DWORD type;
		wchar_t buffer[256];
		DWORD len = sizeof(buffer);
		rc = RegQueryValueEx(key, (const wchar_t *)cname, NULL, &type, (BYTE *)&buffer, &len);
		if (rc != ERROR_SUCCESS) {
			result = getErrorMsg(env, L"RegQueryValueEx");
		} else {
			result = env->NewString((jchar *)buffer, wcslen(buffer));
		}
	}

	env->ReleaseStringChars(subkey, csubkey);
	env->ReleaseStringChars(name, cname);

	return result;
}
