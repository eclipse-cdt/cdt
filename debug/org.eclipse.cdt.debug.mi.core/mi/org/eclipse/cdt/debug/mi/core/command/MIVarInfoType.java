/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarInfoTypeInfo;

/**
 * 
 *     -var-info-type NAME
 *
 *   Returns the type of the specified variable NAME.  The type is
 * returned as a string in the same format as it is output by the GDB CLI:
 *
 *     type=TYPENAME
 * 
 */
public class MIVarInfoType extends MICommand 
{
	public MIVarInfoType(String miVersion, String name) {
		super(miVersion, "-var-info-type", new String[]{name}); //$NON-NLS-1$
	}

	public MIVarInfoTypeInfo getMIVarInfoTypeInfo() throws MIException {
		return (MIVarInfoTypeInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarInfoTypeInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

}
