/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
