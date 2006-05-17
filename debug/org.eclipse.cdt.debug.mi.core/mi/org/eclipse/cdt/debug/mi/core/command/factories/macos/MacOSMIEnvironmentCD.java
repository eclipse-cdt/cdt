/**********************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * Nokia - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.macos;

import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;

public class MacOSMIEnvironmentCD extends MIEnvironmentCD {

	public MacOSMIEnvironmentCD(String miVersion, String path) {
		super(miVersion, path);
		this.setOperation("cd");//$NON-NLS-1$
	}
	
	protected String parametersToString() {
		String[] parameters = getParameters();
		if (parameters != null && parameters.length == 1) {
			return '"' + parameters[0] + '"';
		}
		return super.parametersToString();		
	}
	
}
