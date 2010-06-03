/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson 		    - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * GDB/MI thread list parsing.
~"\n"
~"     2 Thread 2049 (LWP 29354)  "
~"* 1 Thread 1024 (LWP 29353)  "

 */
public class CLIInfoThreadsInfo extends MIInfo {

	/**
	 * Matcher for 'info threads' output typically returned by gdbservers
	 * running on POSIX systems. Relevant output is in the form: <br>
	 * <p>
	 * <code>
	 * &lt;x&gt; Thread &lt;y&gt; (LWP &lt;z&gt;)
	 * </code>
	 * 
	 * <p>
	 * Where 'y' is a hex number with a '0x' prefix.
	 * 
	 * <p>
	 * Note that the output likely includes non-LWP threads, but they are
	 * intentionally ignored
	 */
    private static final Pattern RESULT_PATTERN_LWP = Pattern.compile(
        "(^\\*?\\s*\\d+)(\\s*[Tt][Hh][Rr][Ee][Aa][Dd]\\s*)(0x[0-9a-fA-F]+|-?\\d+)(\\s*\\([Ll][Ww][Pp]\\s*)(\\d*)",  Pattern.MULTILINE); //$NON-NLS-1$

	/**
	 * Matcher for 'info threads' output typically returned by gdbservers running
	 * on non-POSIX systems. Output is in the more general form: <br>
	 * <p>
	 * <code>
	 * &lt;x&gt; Thread &lt;y&gt; (&lt;text&gt;)
	 * </code>
	 * 
	 * <p>where 'y' is not necessarily numeric and (&lt;text&gt;) is optional 
	 */
    private static final Pattern RESULT_PATTERN = Pattern.compile(
        "(^\\*?\\s*\\d+)(\\s*[Tt][Hh][Rr][Ee][Aa][Dd]\\s*)(\\S+(\\s*\\(.*?\\))?)",  Pattern.MULTILINE); //$NON-NLS-1$
    
	protected List<ThreadInfo> info; 

	public CLIInfoThreadsInfo(MIOutput out) {
		super(out);
		parse();
	}

	public class ThreadInfo {
		String fName;
		String fGdbId;
		String fPid;
		boolean fIsCurrentThread = false;
		
		public ThreadInfo(String tid, String pid, String name, boolean isCurrentThread)
		{
			this.fName = name;
			this.fGdbId = tid;
			this.fPid = pid;
			this.fIsCurrentThread = isCurrentThread;
		}
		
		public String getName(){ return fName ;}
		// GDB id given to a thread. Needed to compare with ID stored in DMC fetched via DsfMIThreadListIds command
		public String getId(){ return fGdbId; }
		public String getOsId(){return fPid; }
		public boolean isCurrentThread(){return fIsCurrentThread; }
	}
	
	public List<ThreadInfo> getThreadInfo(){
		return info; 
	}

	protected void parse() {
		info = new ArrayList<ThreadInfo>();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in finding the current thread
					parseThreadInfo(str.trim(), info);
				}
			}
		}
	}

	protected void parseThreadInfo(String str, List<ThreadInfo> info) {
		// Fetch the OS ThreadId & Find the current thread 
		// Here is an example output from GDB which shows normal threads as well as
		// LWP process threads. We ignore non-LWP threads.
		//
		// [example A]
		// (gdb) info threads
		//   7 Thread 0x941c00 (sleeping)  0x0000000806c6d0df in pthread_mutexattr_init () from /usr/lib/libpthread.so.2
		//   6 Thread 0x953000 (sleeping)  0x0000000806c6d0df in pthread_mutexattr_init () from /usr/lib/libpthread.so.2
		//   5 Thread 0x953400 (sleeping)  0x0000000806c6d0df in pthread_mutexattr_init () from /usr/lib/libpthread.so.2
		//   4 Thread 0x953800 (sleeping)  0x0000000806c6d0df in pthread_mutexattr_init () from /usr/lib/libpthread.so.2
		// * 3 Thread 0x510400 (LWP 100132)  0x0000000806c7489c in pthread_testcancel () from /usr/lib/libpthread.so.2
		//   2 Thread 0x510000 (runnable)  0x0000000806e468ec in read () from /lib/libc.so.6
		//
		// However, 'info threads' output varies, and depends on the gdbserver
		//
		// [example B, observed with FreeBSD] 
		// (gdb) info threads
		//   6 Thread 1286 (tid 38473, running)  0x00000000 in ?? ()
		//   5 Thread 1029 (tid 34369, running)  0x00000000 in ?? ()
		//   4 Thread 772 (tid 39483, running)  0xd037eb94 in clock_gettime ()
		// * 3 Thread 515 (tid 39741, running)  0x00000000 in ?? ()
		//
		// [example C, observed with cygwin and mingw]
		//   2 thread 5264.0x608  0x7c90eb94 in ntdll!LdrAccessResource ()
		//    from /cygdrive/c/WINDOWS/system32/ntdll.dll
		// * 1 thread 5264.0x16f8  main (argc=1, argv=0x661f00) at MultiThread.cc:16
		//
		// Note that windows gdbs returns lower case "thread" , so the matcher 
		// needs to be case-insensitive. 
		//
		// The original code favored the format in example A and so we will 
		// continue to give it precedence. The newly added support for formats 
		// B and C will have lower precedence.
		if(str.length() > 0 ){
			Matcher matcher = RESULT_PATTERN_LWP.matcher(str);	// example A
			boolean isCurrentThread = false;
			if (matcher.find()) {
				String id = matcher.group(1).trim();
				if (id.charAt(0) == '*') {
					isCurrentThread = true;
					id = id.substring(1).trim();
				}
				info.add(new ThreadInfo(id, matcher.group(5), "", isCurrentThread)); //$NON-NLS-1$
			} 
			else {
				matcher = RESULT_PATTERN.matcher(str);	// examples B and C
				if (matcher.find()) {
					String id = matcher.group(1).trim();
					if (id.charAt(0) == '*') {
						isCurrentThread = true;
						id = id.substring(1).trim();
					}
					info.add(new ThreadInfo(id, matcher.group(3), "", isCurrentThread)); //$NON-NLS-1$
				}
			}
		}
	}
}

