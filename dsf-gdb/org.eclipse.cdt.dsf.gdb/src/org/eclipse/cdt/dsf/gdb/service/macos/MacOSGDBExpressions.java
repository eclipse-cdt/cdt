/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.macos;

import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIVariableManager;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Specific ExpressionService for MacOS
 * 
 * @since 2.1
 */
public class MacOSGDBExpressions extends MIExpressions {

    public MacOSGDBExpressions(DsfSession session) {
        super(session);
    }

    @Override
    protected MIVariableManager createMIVariableManager() {
        return new MacOSGDBVariableManager(getSession(), getServicesTracker());
    }
}
