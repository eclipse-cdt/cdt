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
package org.eclipse.cdt.debug.dap;

import org.eclipse.cdt.launch.ui.CAbstractArgumentsTab;

public class CArgumentsTab extends CAbstractArgumentsTab {
	@Override
	public String getId() {
		return "org.eclipse.cdt.debug.dap.argumentsTab"; //$NON-NLS-1$
	}
}
