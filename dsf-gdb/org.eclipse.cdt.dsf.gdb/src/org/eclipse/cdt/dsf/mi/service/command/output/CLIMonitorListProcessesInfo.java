/*******************************************************************************
 * Copyright (c) 2008  Ericsson and others.
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

import org.eclipse.cdt.core.IProcessInfo;


/**
 * 
 * @"   26 N             0 N       32794       Idle     2 Http daemon\n"
 */
public class CLIMonitorListProcessesInfo extends MIInfo {

	@Deprecated
	public class ProcessInfo implements IProcessInfo, Comparable<ProcessInfo> {
		int pid;
		String name;
		
		public ProcessInfo(String pidString, String name) {
			try {
				pid = Integer.parseInt(pidString);
			} catch (NumberFormatException e) {
			}
			this.name = name;
		}
		
		public ProcessInfo(int pid, String name) {
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

		public int compareTo(ProcessInfo other) {
		    int nameCompare = getName().compareTo(other.getName());
		    if (nameCompare != 0) return nameCompare;
		    else return (getPid() < other.getPid()) ? -1 : 1;
		}
	}
	
	IProcessInfo[] fProcessList;
	
	public CLIMonitorListProcessesInfo(MIOutput out) {
		super(out);
		parse();
	}
	
	public IProcessInfo[] getProcessList() {
		return fProcessList;
	}
	
	private void parse() {
		List<IProcessInfo> aList = new ArrayList<IProcessInfo>();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MITargetStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString().trim();
					if (str.length() > 0) {
						// Parsing pattern of type @"   26 N             0 N       32794       Idle     2 Http daemon\n"
						Pattern pattern = Pattern.compile("(\\d*)\\s(Y|N)\\s*\\d*\\s(Y|N)\\s*\\d*\\s*\\D*\\s*\\d\\s(.*)",  Pattern.MULTILINE); //$NON-NLS-1$
						Matcher matcher = pattern.matcher(str); 
						if (matcher.find()) {
							ProcessInfo proc = new ProcessInfo(matcher.group(1), matcher.group(4));
							aList.add(proc);
			            }
					}
				}
			}
		}
		fProcessList = aList.toArray(new IProcessInfo[aList.size()]);
	}
}
