/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarUpdateInfo;

/**
 * 
 *     -var-update {NAME | "*"}
 *
 *  Update the value of the variable object NAME by evaluating its
 * expression after fetching all the new values from memory or registers.
 * A `*' causes all existing variable objects to be updated.
 * 
 */
public class MIVarUpdate extends MICommand {

	public MIVarUpdate() {
		this("*");
	}
	
	public MIVarUpdate(String name) {
		super("-var-update", new String[] { name });
	}
	
	public MIVarUpdateInfo getMIVarUpdateInfo() throws MIException {
		return (MIVarUpdateInfo)getMIInfo();
	}
	
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarUpdateInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
