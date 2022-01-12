#ifndef Sleep_h
#define Sleep_h

#ifdef __MINGW32__ // MinGW has no POSIX support; use Win32 API
 #include <windows.h>
 #define SLEEP(s) Sleep((s)*1000)	// Win32's Sleep takes milliseconds
#else
 #include <unistd.h>
 #define SLEEP(s) sleep(s)			// POSIX sleep takes seconds
#endif

#endif // Sleep_h
