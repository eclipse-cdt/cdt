/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.dsf.concurrent.Immutable;

/**
 * GDB/MI thread group parsing.
 * 
 *  The description field can be different depending on the target we are connected to.
 *
 *  -list-thread-groups --available:
 *  ^done,groups=[{id="161",type="process",description="name: JIM_InstallerProcess, type 555481, locked: N, system: N, state: Idle"},
 *                {id="162",type="process",description="name: JIM_TcpSetupHandlerProcess, type 555505, locked: N, system: N, state: Idle"},
 *                {id="165",type="process",description="name: JUnitProcess2_PT, type 1094608, locked: N, system: N, state: Idle"},
 *                {id="166",type="process",description="name: JUnitProcess_PT, type 1094605, locked: N, system: N, state: Idle"}]
 *
 *          	  {id="3602",type="process",description="/usr/sbin/dhcdbd --system",user="root"}
 *          
 *  -list-thread-groups: 
 *  ^done,groups=[{id="162",type="process",pid="162"}]
 *
 *  -list-thread-groups GROUPID, in the case of a running thread or a stopped thread:
 *  ^done,threads=[{id="1",target-id="Thread 162.32942",details="JUnitProcess_PT (Ready) 1030373359 44441",frame={level="0",addr="0x00000000",func="??",args=[]},state="stopped"}]
 *  ^done,threads=[{id="1",target-id="Thread 162.32942",details="JUnitProcess_PT Idle 981333916 42692",state="running"}]
 *  
 *  As of GDB 7.1, a new 'core' output field has been added.  This field is a list 
 *  of integers, each identifying a core that one thread of the group is running on. 
 *  This field may be absent if such information is not available.
 *  
 *  -list-thread-groups
 *  ^done,groups=[{id="12779",type="process",pid="12779",cores=["3"]}]
 *    
 *  -list-thread-groups 12779
 *   ^done,threads=[{id="10",
 *                   target-id="Thread 0xb3d58ba0 (LWP 12876)",
 *                   frame={level="0",addr="0xb7e21b88",func="clone",args=[],from="/lib/libc.so.6"},
 *                   state="stopped",
 *                   core="3"},
 *                  {id="3",
 *                   target-id="Thread 0xb755fba0 (LWP 12811)",
 *                   frame={level="0",addr="0xffffe410",func="__kernel_vsyscall",args=[]},
 *                   state="stopped",
 *                   core="3"},
 *                  {id="2",
 *                   target-id="Thread 0xb7d60ba0 (LWP 12810)",
 *                   frame={level="0",addr="0xffffe410",func="__kernel_vsyscall",args=[]},
 *                   state="stopped",
 *                   core="3"},
 *                  {id="1",
 *                   target-id="Thread 0xb7d616b0 (LWP 12779)",
 *                   frame={level="0",addr="0x08048609",func="main",args=[],file="../src/NonStop.cpp",fullname="/local/runtime-TestDSF/NonStop/src/NonStop.cpp",line="44"},
 *                   state="stopped",
 *                   core="3"}]
 *                   
 *  As of GDB 7.1, the --recurse option has been added and causes a different output
 *  
 *  -list-thread-groups --recurse 1
 *  ^done,groups=[{id="12779",
 *                 type="process",
 *                 pid="12779",
 *                 cores=["3"],
 *                 threads=[{id="10",
 *                           target-id="Thread 0xb3d58ba0 (LWP 12876)",
 *                           frame={level="0",addr="0xb7e21b88",func="clone",args=[],from="/lib/libc.so.6"},
 *                           state="stopped",
 *                           core="3"},
 *                          {id="3",
 *                           target-id="Thread 0xb755fba0 (LWP 12811)",
 *                           frame={level="0",addr="0xffffe410",func="__kernel_vsyscall",args=[]},
 *                           state="stopped",
 *                           core="3"},
 *                          {id="2",
 *                           target-id="Thread 0xb7d60ba0 (LWP 12810)",
 *                           frame={level="0",addr="0xffffe410",func="__kernel_vsyscall",args=[]},
 *                           state="stopped",
 *                           core="3"},
 *                          {id="1",
 *                           target-id="Thread 0xb7d616b0 (LWP 12779)",
 *                           frame={level="0",addr="0x08048609",func="main",args=[],file="../src/NonStop.cpp",fullname="/local/runtime-TestDSF/NonStop/src/NonStop.cpp",line="44"},
 *                           state="stopped",
 *                           core="3"}
 *                         ]
 *                }]
 *  
 * Example of outputs by version on Linux
 * 
 * GDB 7.0
 *  
 *  (when no inferior is running)
 * -list-thread-groups
 * ^done,groups=[]
 *
 * (with an inferior running)
 * -list-thread-groups
 * ^done,groups=[{id="19386",type="process",pid="19386"}]
 * 
 * -list-thread-groups 19386
 * ^done,threads=[{id="1",target-id="process 19386",frame={level="0",addr="0x08048618",func="main",args=[],file="a.cc",fullname="/local/lmckhou/testing/a.cc",line="9"},state="stopped"}]
 * 
 * -list-thread-groups --available 
 * ^done,groups=[{id="19371",type="process",description="gdb.7.0 -i mi testing/a.out",user="lmckhou"},{id="19386",type="process",description="/local/lmckhou/testing/a.out",user="lmckhou"},{id="19413",type="process",description="sleep 5",user="lmckhou"}]
 * 
 * GDB 7.1
 * 
 * (when no inferior is running)
 * -list-thread-groups
 * ^done,groups=[{id="0",type="process",pid="0"}]
 * 
 * (with an inferior running)
 * -list-thread-groups
 * ^done,groups=[{id="19424",type="process",pid="19424",cores=["3"]}]
 * 
 * -list-thread-groups 19424
 * ^done,threads=[{id="1",target-id="process 19424",frame={level="0",addr="0x08048618",func="main",args=[],file="a.cc",fullname="/local/lmckhou/testing/a.cc",line="9"},state="stopped",core="3"}]
 * 
 * -list-thread-groups --available
 * ^done,groups=[{id="19418",type="process",description="gdb.7.1 -i mi testing/a.out",user="lmckhou"},{id="19424",type="process",description="/local/lmckhou/testing/a.out",user="lmckhou"},{id="19438",type="process",description="sleep 5",user="lmckhou"}]
 * 
 * -list-thread-groups --recurse 1
 * ^done,groups=[{id="i2",type="process",pid="11805",executable="/home/lmckhou/Consumer",cores=["0","1"],
 *                threads=[{id="6",target-id="Thread 0xb6516b70 (LWP 11811)",state="running",core="1"},
 *                         {id="5",target-id="Thread 0xb6d17b70 (LWP 11810)",state="running",core="1"},
 *                         {id="4",target-id="Thread 0xb7518b70 (LWP 11809)",
 *                          frame={level="0",addr="0x0804850d",func="main",args=[],file="Consumer.cc",fullname="/home/lmckhou/Consumer.cc",line="5"},
 *                          state="stopped",core="0"},
 *                         {id="3",target-id="Thread 0xb7d19b70 (LWP 11808)",state="running",core="1"},
 *                         {id="2",target-id="Thread 0xb7d1bb30 (LWP 11805)",state="running",core="0"}]},
 *               {id="i1",type="process",pid="11793",executable="/home/lmckhProducer",cores=["0","1"],
 *                threads=[{id="10",target-id="Thread 0xb6516b70 (LWP 11815)",state="running",core="0"},
 *                         {id="8",target-id="Thread 0xb7518b70 (LWP 11813)",state="running",core="0"},
 *                         {id="7",target-id="Thread 0xb7d19b70 (LWP 11812)",state="running",core="1"},
 *                         {id="1",target-id="Thread 0xb7d1bb30 (LWP 11793)",state="running",core="1"}]}]
 *
 * GDB 7.2
 * 
 * (when no inferior is running)
 * -list-thread-groups
 * ^done,groups=[{id="i1",type="process",executable="/local/lmckhou/testing/a.out"}]
 * 
 * (with an inferior running)
 * -list-thread-groups
 * ^done,groups=[{id="i1",type="process",pid="19451",executable="/local/lmckhou/testing/a.out",cores=["2"]}]
 * 
 * -list-thread-groups i1
 * ^done,threads=[{id="1",target-id="process 19451",frame={level="0",addr="0x08048618",func="main",args=[],file="a.cc",fullname="/local/lmckhou/testing/a.cc",line="9"},state="stopped",core="2"}]
 * 
 * -list-thread-groups --available
 * ^done,groups=[{id="19445",type="process",description="gdb.7.2 -i mi testing/a.out",user="lmckhou"},{id="19451",type="process",description="/local/lmckhou/testing/a.out",user="lmckhou"},{id="19462",type="process",description="sleep 5",user="lmckhou"}]
 *
 * @since 1.1
 */

public class MIListThreadGroupsInfo extends MIInfo {
	
	/**
	 * @noextend This interface is not intended to be extended by clients.
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public interface IThreadGroupInfo {
		String getGroupId();
		String getPid();
		String getName();
		String getDesciption();
		/**@since 4.0 */
		String getUser();
		/**@since 4.0 */
		String getType();
		/**@since 4.0 */
		String[] getCores();
		/**@since 4.0 */
		String getExecutable();
	}
	
	/**
	 * @since 4.1
	 */
	public interface IThreadGroupInfo2 extends IThreadGroupInfo {
		MIThread[] getThreads();
	}
	
	@Immutable
	private static class ThreadGroupInfo implements IThreadGroupInfo2 {
		final String fGroupId;
		final String fDescription;
		final String fName;
		final String fType;
		final String fUser;
		final String fPid;
		final String[] fCores;
		final String fExecutable;
		final MIThread[] fThreadList;
		
		public ThreadGroupInfo(String id, String description, String type, String pid, 
				               String user, String[] cores, String exec, MIThread[] threads) {
			fGroupId = id;
			fDescription = description;
			fType = type;
			fUser = user;
			fPid = pid;
			fCores = cores;				
			
			fExecutable = exec;
			
			fName = parseName(fDescription);
			
			fThreadList = threads;
		}
		
		private static String parseName(String desc) {
			String name = ""; //$NON-NLS-1$

			// Find the string "name: " followed by the smallest set of characters that
			// is followed by a comma, or by the end of the line.
			Pattern pattern = Pattern.compile("name: (.*?)(, |$)", Pattern.MULTILINE); //$NON-NLS-1$
        	Matcher matcher = pattern.matcher(desc);
        	if (matcher.find()) {
        		name = matcher.group(1);
        	} else {
        		// If we didn't get the form "name: " then we expect to have the form
        		//   "/usr/sbin/dhcdbd --system"
        		// or (starting with GDB 7.4)
        		//   "[migration/0]"  where the integer represents the core, if the process 
        		//                    has an instance of many cores
        		//   "[kacpid]"       when the process only runs on one core
        		//   "[async/mgr]"          
        	    //   "[jbd2/dm-1-8]"
        		//   The brackets indicate that the startup parameters are not available
        		//   We handle this case by removing the brackets and the core indicator
        		//   since GDB already tells us the core separately.
        		if (desc.length() > 0 && desc.charAt(0) == '[') {
        			// Remove brackets
        			name = desc.substring(1, desc.length()-1);
        			
        			// Look for [name/coreNum] pattern to remove /coreNum
        			pattern = Pattern.compile("(.+?)(/\\d+)", Pattern.MULTILINE); //$NON-NLS-1$
                	matcher = pattern.matcher(name);
                	if (matcher.find()) {
                		// Found a pattern /coreNum, so ignore it
                		name = matcher.group(1);
                	}
                	// else, no /coreNum pattern, so the name is correct already
        		} else {
        			name = desc.split("\\s", 2)[0]; //$NON-NLS-1$
        		}
        	}

			return name;
		}
		
		@Override
		public String getGroupId() { return fGroupId; }
		@Override
		public String getPid() { return fPid; }

		@Override
		public String getName() { return fName;	}

		@Override
		public String getDesciption() { return fDescription; }
		@Override
		public String[] getCores() { return fCores; }
		@Override
		public String getUser() { return fUser;	}

		@Override
		public String getType() { return fType;	}
		@Override
		public String getExecutable() { return fExecutable; }

		@Override
		public MIThread[] getThreads() { return fThreadList; }
	}
	
	
	private IThreadGroupInfo[] fGroupList;
	private MIThreadInfoInfo fThreadInfo;
	
    public MIListThreadGroupsInfo(MIOutput out) {
        super(out);
        parse();
	}
	
	public IThreadGroupInfo[] getGroupList() { return fGroupList; }
	public MIThreadInfoInfo getThreadInfo() { return fThreadInfo; }
	
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
						// Re-use the MIThreadInfoInfo parsing
						fThreadInfo = new MIThreadInfoInfo(out);
					}
				}
			}
		}
		if (fGroupList == null) {
			fGroupList = new IThreadGroupInfo[0];
		}
		if (fThreadInfo == null) {
			fThreadInfo = new MIThreadInfoInfo(null);
		}
	}

	private void parseGroups(MIList list) {
		MIValue[] values = list.getMIValues();
		fGroupList = new IThreadGroupInfo[values.length];
		for (int i = 0; i < values.length; i++) {
			MIResult[] results = ((MITuple)values[i]).getMIResults();
			String id, desc, type, pid, exec, user;
			id = desc = type = pid = exec = user = "";//$NON-NLS-1$
			MIThread[] threads = null;
			
			String[] cores = null;
			
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
				} else if (var.equals("type")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						type = str.trim();
					}
				} else if (var.equals("pid")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						pid = str.trim();
					}
				}  else if (var.equals("user")) { //$NON-NLS-1$
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						user = str.trim();
					}
				} else if (var.equals("cores")) { //$NON-NLS-1$
					// Staring with GDB 7.1
					MIValue value = result.getMIValue();
					if (value instanceof MIList) {
						cores = parseCores((MIList)value);
					}
				} else if (var.equals("executable")) { //$NON-NLS-1$
					// Staring with GDB 7.2
					MIValue value = result.getMIValue();
					if (value instanceof MIConst) {
						String str = ((MIConst)value).getCString();
						exec = str.trim();
					}
				} else if (var.equals("threads")) { //$NON-NLS-1$
					// Staring with GDB 7.1
					// Re-use the MIThreadInfoInfo parsing
					MIValue value = result.getMIValue();
					if (value instanceof MIList) {
						threads = MIThreadInfoInfo.parseThreads(((MIList)value));
					}
				}
			}
			// In the case of -list-thread-groups --available, the pid field is not present, but the
			// pid is used as the main id.  To know we are in this case, we check that we have
			// a description, that only happens for -list-thread-groups --available
			// We must check this because with GDB 7.2, there will be no pid field as a result
			// of -list-thread-groups, if no process is actually running yet.
			if (pid.equals("") && !desc.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				pid = id;
			}
			fGroupList[i] = new ThreadGroupInfo(id, desc, type, pid, user, cores, exec, threads);
		}
	}
	
	private String[] parseCores(MIList list) {
		List<String> cores = new ArrayList<String>();
		
		MIValue[] values = list.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				cores.add(((MIConst)values[i]).getCString());
			}
		}
		return cores.toArray(new String[cores.size()]);
	}
}
