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
import org.eclipse.cdt.debug.mi.core.command.factories.StandardCommandFactory;

public class StandardMacOSCommandFactory extends StandardCommandFactory {

	/**
	 * Constructor for StandardMacOSCommandFactory.
	 */
	public StandardMacOSCommandFactory() {
		super();
	}

	/**
	 * Constructor for StandardMacOSCommandFactory.
	 */
	public StandardMacOSCommandFactory( String miVersion ) {
		super( miVersion );
	}

	public MIEnvironmentCD createMIEnvironmentCD(String pathdir) {
		return new MacOSMIEnvironmentCD(getMIVersion(), pathdir);
	}

}
