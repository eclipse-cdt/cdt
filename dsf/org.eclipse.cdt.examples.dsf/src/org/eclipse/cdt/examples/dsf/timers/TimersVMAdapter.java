/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.timers;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * This is the adapter that implements the flexible hierarchy viewer interfaces
 * for providing content, labels, and event processing for the viewer.  This 
 * adapter is registered with the DSF Session object, and is returned by the
 * IDMContext.getAdapter() and IVMContext.getAdapter() methods, 
 * which both call {@link DsfSession#getModelAdapter(Class)}.
 */
@SuppressWarnings("restriction")
@ThreadSafe
public class TimersVMAdapter extends AbstractDMVMAdapter
{
    @Override
    protected IVMProvider createViewModelProvider(IPresentationContext context) {
        if ( TimersView.ID_VIEW_TIMERS.equals(context.getId()) ) {
            return new TimersVMProvider(this, context, getSession());
        }
        return null;
    }
    
    public TimersVMAdapter(DsfSession session, IPresentationContext presentationContext) {
        super(session);
    }    
}
