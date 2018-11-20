/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
