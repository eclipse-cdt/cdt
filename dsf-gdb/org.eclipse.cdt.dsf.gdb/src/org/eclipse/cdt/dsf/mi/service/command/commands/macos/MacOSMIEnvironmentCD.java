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

public class MacOSMIEnvironmentCD extends MIEnvironmentCD {

	public MacOSMIEnvironmentCD(ICommandControlDMContext ctx, String path) {
		super(ctx, '\"' + path + '\"');
	}
}
