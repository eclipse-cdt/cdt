/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarDeleteInfo;

/**
 * 
 *    -var-delete NAME
 *
 *  Deletes a previously created variable object and all of its children.
 *
 *  Returns an error if the object NAME is not found.
 * 
 */
public class MIVarDelete extends MICommand 
{
	public MIVarDelete(String name) {
		super("-var-delete", new String[]{name});
	}
	
	public MIVarDeleteInfo getMIVarDeleteInfo() throws MIException {
		return (MIVarDeleteInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarDeleteInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
