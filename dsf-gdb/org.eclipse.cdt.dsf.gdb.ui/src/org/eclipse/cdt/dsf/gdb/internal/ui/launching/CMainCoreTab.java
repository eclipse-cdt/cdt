/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
