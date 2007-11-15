package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;

/**
 * The View Model adapter handles the layout of a given data model within a 
 * set of viewers.  This adapter should be returned by an adapter factory for 
 * the input object of the viewer, and this adapter implementation will then 
 * populate the view contents.  
 */
@ThreadSafe
@SuppressWarnings("restriction")
public interface IVMAdapter
    extends IElementContentProvider, IModelProxyFactory, IColumnPresentationFactory, IViewerInputProvider 
{
    /**
     * Returns the View Model Provider that is registered for the given presentation
     * context.  Returns <code>null</code> if there is none.
     */
    public IVMProvider getVMProvider(IPresentationContext presentationContext);
}
