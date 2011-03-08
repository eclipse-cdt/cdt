/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Wind River Systems - flexible hierarchy Signals view (bug 338908)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.cdt.debug.internal.ui.views.modules.ModuleContentProvider;
import org.eclipse.cdt.debug.internal.ui.views.signals.SignalsContentProvider;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.internal.ui.model.elements.DebugTargetContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Delegating provider implementation.  If the memento request is for the 
 * modules view, the provider impelementation delegates the request to the 
 * modules view-specific provider.  Otherwise, it calls the default superclass
 * implementation.
 */
public class CDebugTargetContentProvider extends DebugTargetContentProvider {
    private final ModuleContentProvider fModuleContentProvider = new ModuleContentProvider();
    private final SignalsContentProvider fSignalsContentProvider = new SignalsContentProvider();
    
    @Override
    public void update(IChildrenCountUpdate[] updates) {
        String id = updates[0].getPresentationContext().getId();
		if (IDebugUIConstants.ID_MODULE_VIEW.equals(id)) {
            fModuleContentProvider.update(updates);
		} else if (ICDebugUIConstants.ID_SIGNALS_VIEW.equals(id)) {
			fSignalsContentProvider.update(updates);
        } else {
            super.update(updates);
        }
    }

    @Override
    public void update(IHasChildrenUpdate[] updates) {
        String id = updates[0].getPresentationContext().getId();
		if (IDebugUIConstants.ID_MODULE_VIEW.equals(id)) {
            fModuleContentProvider.update(updates);
		} else if (ICDebugUIConstants.ID_SIGNALS_VIEW.equals(id)) {
			fSignalsContentProvider.update(updates);
        } else {
            super.update(updates);
        }
    }

    @Override
    public void update(IChildrenUpdate[] updates) {
        String id = updates[0].getPresentationContext().getId();
		if (IDebugUIConstants.ID_MODULE_VIEW.equals(id)) {
            fModuleContentProvider.update(updates);
		} else if (ICDebugUIConstants.ID_SIGNALS_VIEW.equals(id)) {
			fSignalsContentProvider.update(updates);
        } else {
            super.update(updates);
        }
    }
}
