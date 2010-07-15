/*******************************************************************************
 * Copyright (c) 2005, 2007 Seimens AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Seimens AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command;

public class MIGDBSetNewConsole extends MIGDBSet {

	public MIGDBSetNewConsole(String miVersion) {
		this(miVersion, "on"); //$NON-NLS-1$
	}
	
	public MIGDBSetNewConsole(String miVersion, String param) {
		super(miVersion, new String[] {"new-console", param}); //$NON-NLS-1$
	}

}
