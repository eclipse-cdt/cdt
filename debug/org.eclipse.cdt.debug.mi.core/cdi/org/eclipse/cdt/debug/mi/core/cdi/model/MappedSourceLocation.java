/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressToSource;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoLineInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class MappedSourceLocation implements ICDIAddressToSource.IMappedSourceLocation {

	private CLIInfoLineInfo lineInfo;
	private IAddress address;
	private String executable;

	public MappedSourceLocation(IAddress address, CLIInfoLineInfo lineInfo, String executable) {
		this.address = address;
		this.lineInfo = lineInfo;
		this.executable = executable;
	}

	@Override
	public IAddress getAddress() {
		return address;
	}

	@Override
	public IPath getExecutable() {
		return Path.fromOSString(executable);
	}

	@Override
	public String getFunctionName() {
		return lineInfo.getStartLocation();
	}

	@Override
	public int getLineNumber() {
		return lineInfo.getLineNumber();
	}

	@Override
	public IPath getSourceFile() {
		return Path.fromOSString(lineInfo.getFileName());
	}

	@Override
	public String getUnmangledFunctionName() {
		return lineInfo.getStartLocation();
	}

	@Override
	public int compareTo(Object arg0) {
		return address.compareTo(arg0);
	}

}
