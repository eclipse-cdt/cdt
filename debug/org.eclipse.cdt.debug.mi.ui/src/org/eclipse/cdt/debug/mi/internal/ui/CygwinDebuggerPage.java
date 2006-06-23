/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui;

import org.eclipse.swt.widgets.TabFolder;

/**
 * CygWin-specific extension of <code>GDBDebuggerPage</code>.
 */
public class CygwinDebuggerPage extends GDBDebuggerPage {

	public String getName() {
		return MIUIMessages.getString( "CygwinDebuggerPage.0" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.internal.ui.GDBDebuggerPage#createTabs(org.eclipse.swt.widgets.TabFolder)
	 */
	public void createTabs( TabFolder tabFolder ) {
		createMainTab( tabFolder );
	}
}
