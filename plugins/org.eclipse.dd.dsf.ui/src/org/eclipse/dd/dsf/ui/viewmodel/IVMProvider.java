package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactoryAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * The View Model Provider handles the layout of a given model within a 
 * single viewer.  The View Model Adapter delegates calls for view content to 
 * this object for a view that this provider handles.  
 */
@ThreadSafe
@SuppressWarnings("restriction")
public interface IVMProvider 
    extends IElementContentProvider, IModelProxyFactoryAdapter, IColumnPresentationFactoryAdapter 
{
    /**
     * Returns the root layout node that is configured in this provider.  
     * It may return null, if a root node is not yet configured.
     */
    public IVMRootLayoutNode getRootLayoutNode();
    
    /**
     * Returns the presentation context of the viewer that this provider
     * is configured for. 
     */
    public IPresentationContext getPresentationContext();
    
    /**
     * Cleans up the resources associated with this provider.
     */
    public void dispose();
}
