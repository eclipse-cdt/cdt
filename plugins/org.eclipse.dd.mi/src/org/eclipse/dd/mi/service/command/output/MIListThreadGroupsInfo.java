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

import org.eclipse.cdt.core.IProcessInfo;

/**
 * GDB/MI thread group parsing.
 * 
 * ^done,groups=[{id="p133",type="process",pid="133"},{id="p162",type="process",pid="162"}]
 * or
 * ^done,threads=[{id="1",target-id="Thread 162.32942",details="JUnitProcess_PT (Ready) 1275938023 3473",frame={level="0",addr="0x00000000",func="??",args=[]}}]
 */
public class MIListThreadGroupsInfo extends MIInfo {
	
	public class ThreadGroupInfo implements IProcessInfo {
		int pid;
		String name;
		
		public ThreadGroupInfo(String name, String pidStr) {
			try {
				this.pid = Integer.parseInt(pidStr);
			} catch (NumberFormatException e) {
			}
			this.name = name;
		}
		
		public ThreadGroupInfo(String name, int pid) {
			this.pid = pid;
			this.name = name;
		}
		
		/**
		 * @see org.eclipse.cdt.core.IProcessInfo#getName()
		 */
		public String getName() {
			return name;
		}

		/**
		 * @see org.eclipse.cdt.core.IProcessInfo#getPid()
		 */
		public int getPid() {
			return pid;
		}
	}
	
	public class ThreadId {
		String fId;
		
		public ThreadId(String id) {
			fId = id;
		}
		public String getId() {
			return fId;
		}
	}
	
	IProcessInfo[] fProcessList;
	ThreadId[] fThreadList;
	
    public MIListThreadGroupsInfo(MIOutput out) {
        super(out);
        parse();
	}
	
	public IProcessInfo[] getGroupList() {
		return fProcessList;
	}

	public ThreadId[] getThreadList() {
		return fThreadList;
	}
	
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
		if (fProcessList == null) {
			fProcessList = new ThreadGroupInfo[0];
		}
		if (fThreadList == null) {
			fThreadList = new ThreadId[0];
		}
	}

	private void parseGroups(MIList list) {
		MIValue[] values = list.getMIValues();
		fProcessList = new ThreadGroupInfo[values.length];
		for (int i = 0; i < values.length; i++) {
			MIResult[] results = ((MITuple)values[i]).getMIResults();
			String name = "", pid = "";//$NON-NLS-1$//$NON-NLS-2$
			
			for (MIResult result : results) {
				String var = result.getVariable();
				if (var.equals("id")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						name = str.trim();

					}
				} else if (var.equals("pid")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						pid = str.trim();

					}
				}
			}
			fProcessList[i] = new ThreadGroupInfo(name, pid);
		}
	}
	
	private void parseThreads(MIList list) {
		MIValue[] values = list.getMIValues();
		fThreadList = new ThreadId[values.length];
		for (int i = 0; i < values.length; i++) {
			MIResult[] results = ((MITuple)values[i]).getMIResults();
			
			for (MIResult result : results) {
				String var = result.getVariable();
				if (var.equals("id")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						fThreadList[i] = new ThreadId(str.trim());
						break;
					}
				}
			}
		}
	}
}
