/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.launch.ui.CMainTab2;
/**
 * A launch configuration tab that displays and edits project and main type name launch
 * configuration attributes.
 * 
 * @deprecated Replaced with org.eclipse.cdt.launch.ui.CMainTab2
 */
@Deprecated
public class CMainTab extends CMainTab2 {
	public CMainTab() {
		super();
	}

	public CMainTab(int flags) {
		super(flags);
	}

    @Override
    public String getId() {
    	// Return the old id as to be backwards compatible
        return "org.eclipse.cdt.dsf.gdb.launch.mainTab"; //$NON-NLS-1$
    }
}
