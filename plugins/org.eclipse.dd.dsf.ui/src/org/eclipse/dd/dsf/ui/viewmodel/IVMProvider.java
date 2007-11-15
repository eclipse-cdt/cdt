package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;

/**
 * The View Model Provider handles the layout of a given model within a 
 * single viewer.  The View Model Adapter delegates calls for view content to 
 * this object for a view that this provider handles.  
 */
@ThreadSafe
@SuppressWarnings("restriction")
public interface IVMProvider 
    extends IElementContentProvider, IModelProxyFactory, IColumnPresentationFactory, IViewerInputProvider
{
    /**
     * Returns the VM Adapter associated with the provider.
     */
    public IVMAdapter getVMAdapter();
    
    /**
     * Returns the root layout node that is configured in this provider.  
     * It may return null, if a root node is not yet configured.
     */
    public IVMRootLayoutNode getRootLayoutNode();
    
    /**
     * Returns the root element of the view model.  If the given view model is 
     * used to populate the entire contents of the view, then this is the input
     * element for the viewer.  If the view model is used to populate only a 
     * sub-tree section of the view, then this is the root element of that 
     * sub-tree.
     */
    public Object getRootElement();
    
    /**
     * Returns the presentation context of the viewer that this provider
     * is configured for. 
     */
    public IPresentationContext getPresentationContext();
    
    /**
     * Allows other subsystems to force the layout mode associated with the specified
     * VM context to refresh. If null is passed then the RootLayoutNode is told to refresh.
     */
    public void refresh(IVMContext element);
    
    /**
     * Cleans up the resources associated with this provider.
     */
    public void dispose();
    
}
