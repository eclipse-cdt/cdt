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
package org.eclipse.cdt.dsf.mi.service.command.commands.macos;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIEnvironmentCD;

/**
 * @since 3.0
 */
public class MacOSMIEnvironmentCD extends MIEnvironmentCD {

	// We need to send the following format:
	//	-environment-cd "\"/path/without/any/spaces\"" or -environment-cd /path/without/any/spaces
	//	-environment-cd "\"/path/with spaces\""
	public MacOSMIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		super(ctx, (path == null) ? null : "\"\\\"" + path + "\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	protected String parametersToString() {
		// Apple's GDB is very picky.  We override the parameter formatting
		// so that we can control exactly what will be sent to GDB.
		StringBuffer buffer = new StringBuffer();
		for (String parameter : getParameters()) {
			buffer.append(' ').append(parameter);
		}
		return buffer.toString().trim();	
	}
}
