/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarShowAttributesInfo;

/**
 * 
 *    -var-show-attributes NAME
 *
 *  List attributes of the specified variable object NAME:
 *
 *    status=ATTR [ ( ,ATTR )* ]
 *
 * where ATTR is `{ { editable | noneditable } | TBD }'.
 * 
 */
public class MIVarShowAttributes extends MICommand 
{
	public MIVarShowAttributes(String name) {
		super("-var-show-attributes", new String[]{name});
	}

	public MIVarShowAttributesInfo getMIVarShowAttributesInfo() throws MIException {
		return (MIVarShowAttributesInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarShowAttributesInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
