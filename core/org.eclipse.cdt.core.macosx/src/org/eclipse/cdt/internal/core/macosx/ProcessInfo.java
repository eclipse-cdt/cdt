package org.eclipse.cdt.internal.core.macosx;

import org.eclipse.cdt.core.IProcessInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ProcessInfo implements IProcessInfo {

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

}
