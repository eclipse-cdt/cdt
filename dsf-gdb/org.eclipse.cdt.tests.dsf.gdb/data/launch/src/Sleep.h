#ifndef Sleep_h
#define Sleep_h

#ifdef __MINGW32__ // MinGW has no POSIX support; use Win32 API
 #include <windows.h>
 void SLEEP(s) {
	 Sleep(s * 1000);
 }
 
 void MSLEEP(int ms) {
	 Sleep(ms)
 }
#else
 #include <unistd.h>
 void SLEEP(int s) {
	 sleep(s);
 }
 
 void MSLEEP(int ms) {
	 usleep(ms * 1000);
 }
#endif

#endif // Sleep_h
