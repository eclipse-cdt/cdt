/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.DebugManualUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IElementUpdateTester;

/**
 * Manual update policy which selectively clears the cache when the expressions 
 * in the expression manager are modified. 
 * Inherit from DebugManualUpdatePolicy so that expression view can return
 * proper update testers for preference format change event and element format
 * change event just like what variables view and registers view do. (Bug 202556)
 */
public class ExpressionsManualUpdatePolicy extends DebugManualUpdatePolicy {

    @Override
    public IElementUpdateTester getElementUpdateTester(Object event) {
        if (event instanceof ExpressionsChangedEvent) {
            return new ExpressionsChangedUpdateTester((ExpressionsChangedEvent)event);
        }
        return super.getElementUpdateTester(event);
    }
}
