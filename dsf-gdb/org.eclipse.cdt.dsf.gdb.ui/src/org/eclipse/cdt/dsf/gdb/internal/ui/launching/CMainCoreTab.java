/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

/**
 * Main tab to use for an attach launch configuration.
 *
 * @since 2.0
 */
public class CMainCoreTab extends CMainTab {
	public CMainCoreTab() {
		super(CMainTab.SPECIFY_CORE_FILE | CMainTab.INCLUDE_BUILD_SETTINGS);
	}
}
