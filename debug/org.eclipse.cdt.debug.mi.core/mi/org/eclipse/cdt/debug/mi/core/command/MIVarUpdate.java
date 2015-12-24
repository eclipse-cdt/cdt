/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
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

	public MIVarUpdate(String miVersion) {
		this(miVersion, "*"); //$NON-NLS-1$
	}
	
	public MIVarUpdate(String miVersion, String name) {
		super(miVersion, "-var-update", new String[] { name }); //$NON-NLS-1$
	}
	
	public MIVarUpdateInfo getMIVarUpdateInfo() throws MIException {
		return (MIVarUpdateInfo)getMIInfo();
	}
	
	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarUpdateInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
