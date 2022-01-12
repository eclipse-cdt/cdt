/*******************************************************************************
 * Copyright (c) 2005, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *     Ericsson             - Updated for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.launch.ui.CAbstractArgumentsTab;

/**
 * A launch configuration tab that displays and edits program arguments,
 * and working directory launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 */
public class CArgumentsTab extends CAbstractArgumentsTab {
	/**
	 * Tab identifier used for ordering of tabs added using the
	 * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
	 * extension point.
	 *
	 * @since 2.0
	 */
	public static final String TAB_ID = "org.eclipse.cdt.dsf.gdb.launch.argumentsTab"; //$NON-NLS-1$

	@Override
	public String getId() {
		return TAB_ID;
	}
}
