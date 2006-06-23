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
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowSolibSearchPathInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -gdb-show directories
 *
 *   Show the current value of a GDB variable(directories).
 * 
 */
public class MIGDBShowSolibSearchPath extends MIGDBShow {
	public MIGDBShowSolibSearchPath(String miVersion) {
		super(miVersion, new String[] { "solib-search-path" }); //$NON-NLS-1$
	}

	public MIGDBShowSolibSearchPathInfo getMIGDBShowSolibSearchPathInfo() throws MIException {
		return (MIGDBShowSolibSearchPathInfo)getMIInfo();
	}
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowSolibSearchPathInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
