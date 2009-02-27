/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.dsf.gdb.service.SessionType;

/**
 * Debugger tab to use for a remote application launch configuration.
 * 
 * @since 2.0
 */
public class RemoteApplicationCDebuggerTab extends CDebuggerTab {

    public RemoteApplicationCDebuggerTab() {
        super(SessionType.REMOTE, false);
    }
}
