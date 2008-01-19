package org.eclipse.dd.dsf.ui.viewmodel;

import java.util.concurrent.Executor;

import org.eclipse.dd.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;

/**
 * The view model provider handles the layout of a given model within a 
 * single viewer.  The View Model Adapter delegates calls for view content to 
 * this object for a view that this provider handles.
 *   
 * <p/>
 * A given view model provider is typically configured with a number of 
 * {@link IVMNode} objects which are organized in a parent-child hierarchy.  
 * The node hierarchy has a root node which is retrieved using {@link #getRootVMNode()}.    
 *
 * <p/>
 * Note on concurency: The view model provider is single-threaded and it has to be 
 * accessed only using the <code>Executor</code> returned by {@link #getExecutor()}.  
 * The thread of this executor should be the display thread used by the viewer
 * corresponding to the view model provider.  Currently the flexible hierarchy  
 * interfaces that this interface extends do not guarantee that their methods
 * will be called on the display thread, although from their use we are making 
 * this assumption (bug 213629).  {@link IElementContentProvider} is an 
 * exception to this, it is called by the TreeModelViewer on a background 
 * thread, however it is not expected that the viewer will be calling the 
 * IVMProvider directly. Rather, it is expected that the viewer will call 
 * {@link IVMAdapter} which implements <code>IElementContentProvider</code>, 
 * and <code>IVMAdapter</code> implementation is expected to switch to 
 * provider's thread before delegating the call to it.
 */
@ConfinedToDsfExecutor("#getExecutor()")
@SuppressWarnings("restriction")
public interface IVMProvider 
    extends IElementContentProvider, IModelProxyFactory, IColumnPresentationFactory, IViewerInputProvider
{
    /**
     * Returns the VM Adapter associated with the provider.
     */
    public IVMAdapter getVMAdapter();
    
    /**
     * Returns the executor that needs to be used to access this provider. 
     */
    public Executor getExecutor();
    
    /**
     * Returns the root node that is configured in this provider.  
     * It may return null, if a root node is not yet configured.
     */
    public IRootVMNode getRootVMNode();

    /**
     * Returns an array of nodes which are configured as child nodes of the given node.
     */
    public IVMNode[] getChildVMNodes(IVMNode node);

    /**
     * Retrieves the list of all nodes configured for this provider.
     */
    public IVMNode[] getAllVMNodes();
    
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
