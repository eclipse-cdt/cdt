/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

public class MIInfoSharedLibrary extends MICommand {

	public MIInfoSharedLibrary(String miVersion) {
		super(miVersion, "info sharedlibrary"); //$NON-NLS-1$
	}

	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfoSharedLibraryInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

	public MIInfoSharedLibraryInfo getMIInfoSharedLibraryInfo() throws MIException {
		return (MIInfoSharedLibraryInfo) getMIInfo();
	}

}
