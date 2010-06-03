/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Marc-Andre Laperle - patch for bug #250037
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.macos;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 *@see MIVarUpdate
 * 
 * Apple gdb needs special handling for MIVarUpdateInfo so we need this class
 * to override getMIInfo to return a MacOSMIVarUpdateInfo instead
 */
class MacOSMIVarUpdate extends MIVarUpdate {

	public MacOSMIVarUpdate(String miVersion) {
		super(miVersion);
	}

	public MacOSMIVarUpdate(String miVersion, String name) {
		super(miVersion, name);
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MacOSMIVarUpdateInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

}
