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
package org.eclipse.cdt.examples.dsf.filebrowser;

import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * This is the adapter that implements the flexible hierarchy viewer interfaces
 * for providing content, labels, and event proxy-ing for the viewer.  This 
 * adapter is registered with the DSF Session object, and is returned by the
 * IDMContext.getAdapter() and IVMContext.getAdapter() methods, 
 * which both call {@link DsfSession#getModelAdapter(Class)}.
 * <p>
 * The adapter implementation for this exercise is hard-coded to provide 
 * contents for only one view.  In turn the view contents are determined using 
 * the configurable ViewModelProvider.  For demonstration purposes, this model
 * adapter has two different layout configurations that can be used.  These 
 * layout configurations can be set by calling the {@link #setViewLayout} method.
 * <p>
 * This class is primarily accessed by the flexible hierarchy viewer from a 
 * non-executor thread.  So the class is thread-safe, except for a view methods
 * which must be called on the executor thread.
 * 
 * @see AbstractDMVMProvider
 */
@SuppressWarnings("restriction")
@ThreadSafe
public class FileBrowserModelAdapter extends AbstractVMAdapter
{
    FileBrowserVMProvider fViewModelProvider; 

    @Override
    protected IVMProvider createViewModelProvider(IPresentationContext context) {
        /*
         * In this example there is only one viewer, so there is only one 
         * VMProvider.
         */
        return fViewModelProvider;
    }
    
    public FileBrowserModelAdapter(IPresentationContext presentationContext) {
        super();
        fViewModelProvider = new FileBrowserVMProvider(this, presentationContext);
    }    

    FileBrowserVMProvider getVMProvider() {
        return fViewModelProvider;
    }
}
