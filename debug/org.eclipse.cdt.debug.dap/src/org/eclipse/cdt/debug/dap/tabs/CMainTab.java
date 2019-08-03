/*******************************************************************************
 * Copyright (c) 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.dap.tabs;

import org.eclipse.cdt.launch.ui.CMainTab2;

public class CMainTab extends CMainTab2 {
	@Override
	public String getId() {
		return "org.eclipse.cdt.debug.dap.mainTab"; //$NON-NLS-1$
	}
}
