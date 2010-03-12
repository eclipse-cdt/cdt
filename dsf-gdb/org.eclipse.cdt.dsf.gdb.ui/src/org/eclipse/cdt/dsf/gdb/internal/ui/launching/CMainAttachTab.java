/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;


/**
 * Main tab to use for an attach launch configuration.
 * 
 * @since 2.0
 */
public class CMainAttachTab extends CMainTab {
    public CMainAttachTab() {
        super(CMainTab.DONT_CHECK_PROGRAM | CMainTab.INCLUDE_BUILD_SETTINGS);
    }
}
