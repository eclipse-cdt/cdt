/*******************************************************************************
 * Copyright (c) 2002 - 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *
 *  raise.c
 *
 *  This is a part of JNI implementation of spawner 
***********************************************************************/
#include "stdafx.h"
#include "Spawner.h"


#include "jni.h"

extern void JNICALL ThrowByName(JNIEnv *env, const char *name, const char *msg);


static HWND consoleHWND;


/////////////////////////////////////////////////////////////////////////////////////
// Check if window is a console of process with pid
// Arguments:  
//			hwnd - window handler
//			arg  - process PID
// Return : TRUE if yes
/////////////////////////////////////////////////////////////////////////////////////
static BOOL CALLBACK
find_child_console (HWND hwnd, LPARAM arg)
{
  DWORD thread_id;
  DWORD process_id;
  DWORD pid = arg;

  thread_id = GetWindowThreadProcessId (hwnd, &process_id);
  if (process_id == pid)
    {
      char window_class[32];

      GetClassName (hwnd, window_class, sizeof (window_class));
      if (strcmp (window_class,	 "ConsoleWindowClass") == 0)
		{
		consoleHWND = hwnd;
		return FALSE;
		}
    }
  /* keep looking */
  return TRUE;
}


/////////////////////////////////////////////////////////////////////////////////////
// Function implements interrupt process (Ctrl-C emulation)
// Arguments:  
//			pid - process' pid
// Return : 0 if OK or error code
/////////////////////////////////////////////////////////////////////////////////////
int interruptProcess(int pid) 
{
#ifdef DEBUG_MONITOR
    _TCHAR buffer[1000];
#endif
	int rc;
	// Try another method
	rc = 0;
	consoleHWND = NULL;

#ifdef DEBUG_MONITOR
		_stprintf(buffer, _T("Try to interrupt process %i\n"), pid);
		OutputDebugStringW(buffer);
#endif
	// Find console
	EnumWindows (find_child_console, (LPARAM) pid);

	if(NULL != consoleHWND) // Yes, we found out it
	{
	  // We are going to switch focus to console, 
	  // send Ctrl-C and then restore focus
	  BYTE control_scan_code = (BYTE) MapVirtualKey (VK_CONTROL, 0);
	  /* Fake Ctrl-C for SIGINT, and Ctrl-Break for SIGQUIT.  */
	  BYTE vk_c_code = 'C';
	  BYTE vk_break_code = VK_CANCEL;
	  BYTE c_scan_code = (BYTE) MapVirtualKey (vk_c_code, 0);
	  BYTE break_scan_code = (BYTE) MapVirtualKey (vk_break_code, 0);
	  HWND foreground_window;
		

	  foreground_window = GetForegroundWindow ();
	  if (foreground_window)
	    {
         /* NT 5.0, and apparently also Windows 98, will not allow
		 a Window to be set to foreground directly without the
		 user's involvement. The workaround is to attach
		 ourselves to the thread that owns the foreground
		 window, since that is the only thread that can set the
		 foreground window.  */
        DWORD foreground_thread, child_thread;
        foreground_thread =
			GetWindowThreadProcessId (foreground_window, NULL);
	    if (foreground_thread == GetCurrentThreadId ()
                  || !AttachThreadInput (GetCurrentThreadId (),
                                         foreground_thread, TRUE))
            foreground_thread = 0;

        child_thread = GetWindowThreadProcessId (consoleHWND, NULL);
	    if (child_thread == GetCurrentThreadId ()
                  || !AttachThreadInput (GetCurrentThreadId (),
                                         child_thread, TRUE))
            child_thread = 0;

        /* Set the foreground window to the child.  */
        if (SetForegroundWindow (consoleHWND))
           {
		   if(0 != break_scan_code) {
			   /* Generate keystrokes as if user had typed Ctrl-Break */
			   keybd_event (VK_CONTROL, control_scan_code, 0, 0);
			   keybd_event (vk_break_code, break_scan_code,	KEYEVENTF_EXTENDEDKEY, 0);
			   keybd_event (vk_break_code, break_scan_code,
					KEYEVENTF_EXTENDEDKEY | KEYEVENTF_KEYUP, 0);
			   keybd_event (VK_CONTROL, control_scan_code,  KEYEVENTF_KEYUP, 0);
		   }

          /* Sleep for a bit to give time for respond */
           Sleep (100);

           SetForegroundWindow (foreground_window);
           }
         /* Detach from the foreground and child threads now that
            the foreground switching is over.  */
        if (foreground_thread)
           AttachThreadInput (GetCurrentThreadId (),
                                   foreground_thread, FALSE);
        if (child_thread)
           AttachThreadInput (GetCurrentThreadId (),
                                   child_thread, FALSE);
#ifdef DEBUG_MONITOR
		_stprintf(buffer, _T("Sent Ctrl-C & Ctrl-Break to process %i\n"), pid);
		OutputDebugStringW(buffer);
#endif
        }
    } 
#ifdef DEBUG_MONITOR
	else {
		_stprintf(buffer, _T("Cannot find console for process %i\n"), pid);
		OutputDebugStringW(buffer);
	}
#endif

	return rc;
}

