/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson AB			- Modified for DSF Reference Implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.output;

import java.math.BigInteger;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dd.dsf.concurrent.Immutable;

/**
 * GDB/MI thread list parsing.
 * 
 * Example 1:
 * 
 * -thread-info
 * ^done,threads=[
 * 	{id="2",target-id="Thread 0xb7c8ab90 (LWP 7010)",
 *		frame={level="0",addr="0x08048bba",func="my_func",args=[{name="arg",value="0xbff056f5"}],
 * 			file="my_test.cc",fullname="/home/francois/GDB/my_test.cc",line="26"},
 * 			running="0"},
 * 	{id="1",target-id="Thread 0xb7c8b8d0 (LWP 7007)",
 * 		frame={level="0",addr="0x08048a77",func="timer",args=[{name="duration",value="0xbff056f5 \"10\""}],
 * 			file="my_test.cc",fullname="/home/francois/GDB/my_test.cc",line="39"},
 * 			running="0"}
 * 	],current-thread-id="2"
 * 

 * Example 2:
 * 
 * -thread-info 2
 * ^done,threads=[
 * 	{id="2",target-id="Thread 0xb7c8ab90 (LWP 7010)",
 *		frame={level="0",addr="0x08048bba",func="my_func",args=[{name="arg",value="0xbff056f5"}],
 * 			file="my_test.cc",fullname="/home/francois/GDB/my_test.cc",line="26"},
 * 			running="0"}
 *  ]
 * 
 * 
 * Example 3 (non-stop):
 *
 * -thread-info
 * ^done,threads=[
 * 	{id="2",target-id="Thread 0xb7c8eb90 (LWP 7807)",running="1"},
 * 	{id="1",target-id="Thread 0xb7c8b8d0 (LWP 7007)",
 * 		frame={level="0",addr="0x08048a77",func="timer",args=[{name="duration",value="0xbff056f5 \"10\""}],
 * 			file="my_test.cc",fullname="/home/francois/GDB/my_test.cc",line="39"},
 * 			running="0"}
 * 	],current-thread-id="1"
 */
public class MIThreadInfoInfo extends MIInfo {

	@Immutable
	public class ThreadInfo {

		final private String      fGdbId;
		final private String      fTargetId;
		final private String      fOsId;
		final private ThreadFrame fTopFrame; 
		final private boolean     fIsRunning;
		
		public ThreadInfo(String gdbId, String targetId, String osId, ThreadFrame topFrame, boolean isRunning) {
			fGdbId     = gdbId;
			fTargetId  = targetId;
			fOsId      = osId;
			fTopFrame  = topFrame;
			fIsRunning = isRunning;
		}

		public String getGdbId()         { return fGdbId;     }
		public String getTargetId()      { return fTargetId;  }
		public String getOsId()          { return fOsId;      }
		public ThreadFrame getTopFrame() { return fTopFrame;  } 
		public boolean isRunning()       { return fIsRunning; }
	}

	@Immutable
	public class ThreadFrame {
		final private int        fStackLevel;
		final private BigInteger fAddress;
		final private String     fFunction;
		final private ThreadFrameFunctionArgs[] fArgs;
		final private String     fFileName;
		final private String     fFullName;
		final private int        fLineNumber;
		
		public ThreadFrame(int stackLevel, BigInteger address, String function,
				ThreadFrameFunctionArgs[] args,	String file, String fullName, int line)
		{
			fStackLevel = stackLevel;
			fAddress    = address;
			fFunction   = function;
			fArgs       = args;
			fFileName   = file;
			fFullName   = fullName;
			fLineNumber = line;
		}

		public int        getStackLevel() { return fStackLevel; }
		public BigInteger getAddress()    { return fAddress;    }
		public String     getFucntion()   { return fFunction;   }
		public ThreadFrameFunctionArgs[] getArgs() { return fArgs; }
		public String     getFileName()   { return fFileName;   }
		public String     getFullName()   { return fFullName;   }
		public int        getLineNumber() { return fLineNumber; }
	}

	@Immutable
	public class ThreadFrameFunctionArgs {
	}

	private int fCurrentThread = -1;
	private List<ThreadInfo> fThreadInfoList = null;
	private int[] fThreadList = null;

	public MIThreadInfoInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getCurrentThread() {
		return fCurrentThread;
	}

	public List<ThreadInfo> getThreadInfoList() {
		return fThreadInfoList;
	}

	public int[] getThreadList() {
		return fThreadList;
	}

	// General format:
	//		threads=[{...}],current-thread-id="n"
	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("threads")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							parseThreads((MIList) val);
						}
					}
					else if (var.equals("current-thread-id")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String str = ((MIConst) value).getCString();
							try {
								fCurrentThread = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
								fCurrentThread = -1;
							}
						}
					}
				}
			}
		}
		if (fThreadInfoList == null) {
			fThreadInfoList = new Vector<ThreadInfo>(0);
			fThreadList = new int[0];
		}
	}

	// General formats:
	//		id="n",target-id="Thread 0xb7c8ab90 (LWP 7010)",frame={...},running="0"
	//		id="n",target-id="Thread 0xb7c8eb90 (LWP 7807)",running="1"
	private void parseThreads(MIList list) {
		MIValue[] values = list.getMIValues();
		fThreadInfoList = new Vector<ThreadInfo>(values.length);
		fThreadList = new int[values.length];
		
		for (int i = 0; i < values.length; i++) {
			MITuple value = (MITuple) values[i];
			MIResult[] results = value.getMIResults();

			String gdbId = null;
			String targetId = null;
			String osId = null;
			ThreadFrame topFrame = null;
			boolean isRunning = false;

			for (int j = 0; j < results.length; j++) {
				MIResult result = results[j];
				String var = result.getVariable();
				if (var.equals("id")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					if (val instanceof MIConst) {
						gdbId = ((MIConst) val).getCString();
					}
				}
				else if (var.equals("target-id")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					if (val instanceof MIConst) {
						targetId = ((MIConst) val).getCString();
						osId = parseOsId(targetId);
					}
				}
				else if (var.equals("frame")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					topFrame = parseFrame(val);
				}
				else if (var.equals("running")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					if (val instanceof MIConst) {
						String v = ((MIConst) val).getCString();
						isRunning = v.equals("1"); //$NON-NLS-1$
					}
				}
			}
			
			fThreadInfoList.add(new ThreadInfo(gdbId, targetId, osId, topFrame, isRunning));
			try {
				fThreadList[i] = Integer.parseInt(gdbId);
			} catch (NumberFormatException e) {
			}
		}
	}

	// General format:
	// 		"Thread 0xb7c8ab90 (LWP 7010)"
	private String parseOsId(String str) {
		Pattern pattern = Pattern.compile("(Thread\\s*)(0x[0-9a-fA-F]+|-?\\d+)(\\s*\\(LWP\\s*)(\\d*)", 0); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(4);
		}
		return null;
	}

	// General format:
	// 		level="0",addr="0x08048bba",func="func",args=[...],file="file.cc",fullname="/path/file.cc",line="26"
	private ThreadFrame parseFrame(MIValue val) {
		// TODO Auto-generated method stub
		return null;
	}

}

