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

import org.eclipse.dd.dsf.concurrent.Immutable;

/**
 * GDB/MI thread group parsing.
 * 
 *  The description field can be different depending on the target we are connected to.
 *
 *  This output is from -list-thread-groups --available:
 *  ^done,groups=[{id="160",description="name: JIM_InstallerProcess, type 555481, locked: N, system: N, state: Idle"},
 *               {id="161",description="name: JIM_TcpSetupHandlerProcess, type 555505, locked: N, system: N, state: Idle"},
 *               {id="162",description="name: JUnitProcess_PT, type 1094605, locked: N, system: N, state: Idle"}]
 *               
 *  This output is from -list-thread-groups: 
 *  ^done,groups=[{id="162",type="process",pid="162"}]
 *
 *  This output is from -list-thread-groups GROUPID, in the case of a running thread or a stopped thread:
 *  ^done,threads=[{id="1",target-id="Thread 162.32942",details="JUnitProcess_PT (Ready) 1030373359 44441",frame={level="0",addr="0x00000000",func="??",args=[]},state="stopped"}]
 *  ^done,threads=[{id="1",target-id="Thread 162.32942",details="JUnitProcess_PT Idle 981333916 42692",state="running"}]
 */
public class MIListThreadGroupsInfo extends MIInfo {
	
	public interface IThreadGroupInfo {
		String getGroupId();
		String getPid();
		String getName();
		String getDesciption();
	}
	
	@Immutable
	private static class ThreadGroupInfo implements IThreadGroupInfo {
		final String fGroupId;
		final String fDescription;
		final String fName;
		
		public ThreadGroupInfo(String id, String description) {
			fGroupId = id;
			fDescription = description;

			fName = parseName(fDescription);
		}
		
		private static String parseName(String desc) {
			String name = ""; //$NON-NLS-1$

			// Find the string "name: " followed by the smallest set of characters that
			// is followed by a comma, or by the end of the line.
			Pattern pattern = Pattern.compile("name: (.*?)(, |$)", Pattern.MULTILINE); //$NON-NLS-1$
        	Matcher matcher = pattern.matcher(desc);
        	if (matcher.find()) {
        		name = matcher.group(1);
        	}

			return name;
		}
		
		public String getGroupId() { return fGroupId; }
		public String getPid() { return fGroupId; }

		public String getName() { return fName;	}

		public String getDesciption() { return fDescription; }
	}
	
	public interface IThreadInfo {
		String getThreadId();
		String getOSId();
		String getState();
	}
	
	@Immutable
	private static class ThreadInfo implements IThreadInfo {
		final String fThreadId;
		final String fOSId;
		final String fState;
		
		public ThreadInfo(String id, String osId, String state) {
			fThreadId = id;
			fOSId = osId;
			fState = state;
		}
		
		public String getThreadId() { return fThreadId; }
		public String getOSId() { return fOSId; }
		public String getState() { return fState; }
	}
	
	IThreadGroupInfo[] fGroupList;
	IThreadInfo[] fThreadList;
	
    public MIListThreadGroupsInfo(MIOutput out) {
        super(out);
        parse();
	}
	
	public IThreadGroupInfo[] getGroupList() { return fGroupList; }
	public IThreadInfo[] getThreadList() { return fThreadList; }
	
	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("groups")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							parseGroups((MIList)val);
						}
					} else if (var.equals("threads")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIList) {
							parseThreads((MIList)val);
						}
					}

				}
			}
		}
		if (fGroupList == null) {
			fGroupList = new IThreadGroupInfo[0];
		}
		if (fThreadList == null) {
			fThreadList = new IThreadInfo[0];
		}
	}

	private void parseGroups(MIList list) {
		MIValue[] values = list.getMIValues();
		fGroupList = new ThreadGroupInfo[values.length];
		for (int i = 0; i < values.length; i++) {
			MIResult[] results = ((MITuple)values[i]).getMIResults();
			String id = "", desc = "";//$NON-NLS-1$//$NON-NLS-2$
			
			for (MIResult result : results) {
				String var = result.getVariable();
				if (var.equals("id")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						id = str.trim();
					}
				} else if (var.equals("description")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						desc = str.trim();

					}
				}
			}
			fGroupList[i] = new ThreadGroupInfo(id, desc);
		}
	}
	
	private void parseThreads(MIList list) {
		MIValue[] values = list.getMIValues();
		fThreadList = new ThreadInfo[values.length];
		for (int i = 0; i < values.length; i++) {
			MIResult[] results = ((MITuple)values[i]).getMIResults();
			String id = "", osId = "", state = "";//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			
			for (MIResult result : results) {
				String var = result.getVariable();
				if (var.equals("id")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						id = str.trim();
					}
				} else if (var.equals("target-id")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						osId = str.trim();

					}
				} else if (var.equals("state")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						state = str.trim();

					}
				}
			}
			fThreadList[i] = new ThreadInfo(id, osId, state);

		}
	}
}
