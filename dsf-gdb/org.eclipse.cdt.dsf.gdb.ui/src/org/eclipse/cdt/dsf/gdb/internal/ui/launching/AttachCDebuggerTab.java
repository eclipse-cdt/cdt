/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
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
 * Debugger tab to use for an attach launch configuration.
 * 
 * @since 2.0
 */
public class AttachCDebuggerTab extends CDebuggerTab {

    public AttachCDebuggerTab() {
        // We don't know yet if we are going to do a remote or local session
        super(null, true);
    }
}
