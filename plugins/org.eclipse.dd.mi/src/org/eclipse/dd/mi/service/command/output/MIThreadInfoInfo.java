/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.output;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 		state="stopped"},
 * 	{id="1",target-id="Thread 0xb7c8b8d0 (LWP 7007)",
 * 		frame={level="0",addr="0x08048a77",func="timer",args=[{name="duration",value="0xbff056f5 \"10\""}],
 * 			file="my_test.cc",fullname="/home/francois/GDB/my_test.cc",line="39"},
 * 		state="stopped"}
 * 	],current-thread-id="2"
 * 
 * 
 * Example 2:
 * 
 * -thread-info 2
 * ^done,threads=[
 * 	{id="2",target-id="Thread 0xb7c8ab90 (LWP 7010)",
 *		frame={level="0",addr="0x08048bba",func="my_func",args=[{name="arg",value="0xbff056f5"}],
 * 			file="my_test.cc",fullname="/home/francois/GDB/my_test.cc",line="26"},
 * 		state="stopped"}
 *  ]
 * 
 * 
 * Example 3 (non-stop):
 *
 * -thread-info
 * ^done,threads=[
 *  {id="2",target-id="Thread 0xb7d6d6b0 (LWP 14494)",state="running"},
 * 	{id="1",target-id="Thread 0xb7c8b8d0 (LWP 7007)",
 * 		frame={level="0",addr="0x08048a77",func="timer",args=[{name="duration",value="0xbff056f5 \"10\""}],
 * 			file="my_test.cc",fullname="/home/francois/GDB/my_test.cc",line="39"},
 * 		state="stopped"}
 * 	],current-thread-id="1"
 * 
 * 
 * Example 4 (non-stop):
 * 
 * -thread-info 1
 * ^done,threads=[{id="1",target-id="Thread 0xb7d6d6b0 (LWP 14494)",state="running"}]
 *
 *
 * Example 5 (Dicos):
 * 
 * -thread-info 1
 * ^done,threads=[
 *  {id="1",target-id="Thread 162.32942",details="JUnitProcess_PT (Ready) 175417582794 8572423",
 *        frame={level="0",addr="0x1559a318",func="mainExpressionTestApp",args=[],
 *            file="/local/home/lmckhou/TSP/TADE/example/JUnitProcess_OU/src/ExpressionTestApp.cc",
 *            fullname="/local/home/lmckhou/TSP/TADE/example/JUnitProcess_OU/src/ExpressionTestApp.cc",line="279"},
 *        state="stopped"}]
 * @since 1.1
 */
public class MIThreadInfoInfo extends MIInfo {

	private String fCurrentThread = null;
	private IThreadInfo[] fThreadList = null;

	public MIThreadInfoInfo(MIOutput out) {
		super(out);
		parse();
	}

	public String getCurrentThread() {
		return fCurrentThread;
	}

	public IThreadInfo[] getThreadList() {
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
							fCurrentThread = ((MIConst) value).getCString().trim();
						}
					}
				}
			}
		}
		if (fThreadList == null) {
			fThreadList = new IThreadInfo[0];
		}
	}

	// General formats:
	//		id="n",target-id="Thread 0xb7c8ab90 (LWP 7010)",frame={...},state="stopped"
	//		id="n",target-id="Thread 0xb7c8eb90 (LWP 7807)",state="running"
	//      id="n",target-id="Thread 162.32942",details="...",frame={...},state="stopped"
	private void parseThreads(MIList list) {
		MIValue[] values = list.getMIValues();
		fThreadList = new IThreadInfo[values.length];
		
		for (int i = 0; i < values.length; i++) {
			MITuple value = (MITuple) values[i];
			MIResult[] results = value.getMIResults();

			String threadId = null;
			String targetId = null;
			String osId = null;
			String parentId = null;
			ThreadFrame topFrame = null;
			String state = null;
			String details = null;

			for (int j = 0; j < results.length; j++) {
				MIResult result = results[j];
				String var = result.getVariable();
				if (var.equals("id")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					if (val instanceof MIConst) {
						threadId = ((MIConst) val).getCString().trim();
					}
				}
				else if (var.equals("target-id")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					if (val instanceof MIConst) {
						targetId = ((MIConst) val).getCString().trim();
						osId = parseOsId(targetId);
						parentId = parseParentId(targetId);
					}
				}
				else if (var.equals("frame")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					topFrame = parseFrame(val);
				}
				else if (var.equals("state")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					if (val instanceof MIConst) {
						state = ((MIConst) val).getCString().trim();
					}
				}
				else if (var.equals("details")) { //$NON-NLS-1$
					MIValue val = results[j].getMIValue();
					if (val instanceof MIConst) {
						details = ((MIConst) val).getCString().trim();
					}
				}
			}
			
			fThreadList[i] = new ThreadInfo(threadId, targetId, osId, parentId, topFrame, details, state);
		}
	}

	// General format:
	// 		"Thread 0xb7c8ab90 (LWP 7010)"
	//      "Thread 162.32942"
	private String parseOsId(String str) {
		Pattern pattern = Pattern.compile("(Thread\\s*)(0x[0-9a-fA-F]+|-?\\d+)(\\s*\\(LWP\\s*)(\\d*)", 0); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(4);
		}
		
		pattern = Pattern.compile("Thread\\s*\\d+\\.(\\d+)", 0); //$NON-NLS-1$
		matcher = pattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		}
		
		return null;
	}

	// General format:
	// 		"Thread 0xb7c8ab90 (LWP 7010)"
	//      "Thread 162.32942"
	private String parseParentId(String str) {
		Pattern pattern = Pattern.compile("Thread\\s*(\\d+)\\.\\d+", 0); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
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

