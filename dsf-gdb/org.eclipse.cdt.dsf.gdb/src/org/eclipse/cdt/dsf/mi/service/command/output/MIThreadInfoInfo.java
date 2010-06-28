/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Wind River Systems - refactored to match pattern in package
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;



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
 *        
 * With GDB 7.1, a new 'core' field is present to indicate which core the thread is on.
 * The parsing of this new field is handled by {@link MIThread}
 * 
 * -thread-info
 * ^done,threads=[
 *  {id="1",target-id="process 1307",
 *   frame={level="0",addr="0x08048618",func="main",args=[],
 *          file="a.cc",fullname="/local/lmckhou/testing/a.cc",line="9"},
 *   state="stopped",
 *   core="2"}],
 *  current-thread-id="1"
 *  
 * @since 1.1
 */
public class MIThreadInfoInfo extends MIInfo {

	private String fCurrentThread = null;
	private MIThread[] fThreadList = null;

	public MIThreadInfoInfo(MIOutput out) {
		super(out);
		parse();
	}

	public String getCurrentThread() {
		return fCurrentThread;
	}

	public MIThread[] getThreadList() {
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
			fThreadList = new MIThread[0];
		}
	}

	// General formats:
	//		id="n",target-id="Thread 0xb7c8ab90 (LWP 7010)",frame={...},state="stopped"
	//		id="n",target-id="Thread 0xb7c8eb90 (LWP 7807)",state="running"
	//      id="n",target-id="Thread 162.32942",details="...",frame={...},state="stopped"
	private void parseThreads(MIList list) {
		MIValue[] values = list.getMIValues();
		fThreadList = new MIThread[values.length];
		
		for (int i = 0; i < values.length; i++) {
			fThreadList[i] = MIThread.parse((MITuple) values[i]);
		}
	}
}

