/**********************************************************************
 * Copyright (c) 2006, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * Nokia - Initial API and implementation
 * Marc-Andre Laperle - fix for bug 263689 (spaces in directory)
***********************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.macos;

import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;

public class MacOSMIEnvironmentCD extends MIEnvironmentCD {

	public MacOSMIEnvironmentCD(String miVersion, String path) {
		super(miVersion, path);
		this.setOperation("-environment-cd");//$NON-NLS-1$
	}
	
	@Override
	protected String parametersToString() {
		String[] parameters = getParameters();
		if (parameters != null && parameters.length == 1) {
			// To handle spaces in the path, the command string has this format:
			// -environment-cd "\"/path with spaces\""
			return "\"\\\"" + parameters[0] + "\\\"\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return super.parametersToString();		
	}
	
}
