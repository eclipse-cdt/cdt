/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
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
			if(str.length() > 0 ){
				Pattern pattern = Pattern.compile("(^\\*?\\s*\\d+)(\\s*Thread\\s*)(0x[0-9a-fA-F]+|-?\\d+)(\\s*\\(LWP\\s*)(\\d*)",  Pattern.MULTILINE); //$NON-NLS-1$
				Matcher matcher = pattern.matcher(str);
				boolean isCurrentThread = false;
				if (matcher.find()) {
					String id = matcher.group(1).trim();
					if (id.charAt(0) == '*') {
						isCurrentThread = true;
						id = id.substring(1).trim();
					}
					info.add(new ThreadInfo(id, matcher.group(5), "", isCurrentThread)); //$NON-NLS-1$
				}
			}
	}
}
