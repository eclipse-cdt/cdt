/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarInfoNumChildrenInfo;

/**
 * 
 *     -var-info-num-children NAME
 *
 *  Returns the number of children of a variable object NAME:
 *
 *     numchild=N
 * 
 */
public class MIVarInfoNumChildren extends MICommand 
{
	public MIVarInfoNumChildren(String name) {
		super("-var-info-num-children", new String[]{name}); //$NON-NLS-1$
	}

	public MIVarInfoNumChildrenInfo getMIVarInfoNumChildrenInfo() throws MIException {
		return (MIVarInfoNumChildrenInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarInfoNumChildrenInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
