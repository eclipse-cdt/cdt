/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.dsf.debug.ui.viewmodel.update.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMProviderWithCache;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;

@SuppressWarnings("restriction")
public class RefreshActionDelegate implements IViewActionDelegate, IDebugContextListener {

    protected IViewPart fView = null;
    private Object fViewInput = null;
	
	public void init(IViewPart view) {
        /*
         *  Save the view information for later reference and data retrieval.
         */
        fView = view;
        
        /*
         *  Get the current selection from the DebugView so we can determine if we want this menu action to be live or not.
         */
        IViewSite site = (IViewSite) view.getSite();
        String combinedViewId = site.getId() + (site.getSecondaryId() != null ? (":" + site.getSecondaryId()) : "");  //$NON-NLS-1$ //$NON-NLS-2$
        
        DebugUITools.getDebugContextManager().getContextService(view.getSite().getWorkbenchWindow()).addPostDebugContextListener(this, combinedViewId);
        ISelection sel = DebugUITools.getDebugContextManager().getContextService(view.getSite().getWorkbenchWindow()).getActiveContext();
         
        if ( sel instanceof IStructuredSelection ) {
            /*
             *  Save the view selection as well so we can later determine if we want our action to be valid or not.
             */
            fViewInput = ( (IStructuredSelection) sel ).getFirstElement();
        }
	}
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
     */
    public void debugContextChanged(DebugContextEvent event) {
        /*
         *  This handler is called whenever a selection in the debug view is changed. So here is
         *  where we will know when we need to reenable the menu actions.
         */
        ISelection sel = event.getContext();
        
        if (sel instanceof IStructuredSelection) {
            fViewInput = ((IStructuredSelection)sel).getFirstElement();
        }
    }

	public void run(IAction action) {
        /*
         *   Make sure we have a valid set of information. Otherwise we cannot go forward.
         */
        if ( fView instanceof AbstractDebugView && fViewInput != null ) 
        {
            Viewer viewer = ( (AbstractDebugView) fView).getViewer();

            /*
             *  Now we need to make sure this is one of the Flexible Hierarchy viewws.
             */
            if ( viewer instanceof TreeModelViewer ) {
                /*
                 *  Get the presentation context and see if there is a numeric property there. If so then this
                 *  is a view implementation which supports changing the format.
                 */
                TreeModelViewer treeViewer = (TreeModelViewer) viewer;
                IPresentationContext context = treeViewer.getPresentationContext();

                /*
                 *  Now go tell the view to update. We do so by finding the VM provider for this view
                 *  and telling it to redraw the entire view.
                 */
                if (fViewInput instanceof IAdaptable) {
                    IVMAdapter adapter = (IVMAdapter) ((IAdaptable)fViewInput).getAdapter(IVMAdapter.class);

                    if ( adapter != null ) {
                        IVMProvider provider = adapter.getVMProvider(context);

                        if ( provider != null ) {
                            
                            if ( provider instanceof AbstractDMVMProviderWithCache ) {
                                AbstractDMVMProviderWithCache prov = (AbstractDMVMProviderWithCache) provider;
                                
                                prov.flush();
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        /*
         *  As a fallback we will flush the cache without involving the provider if it for some
         *  reason does not measure up.
         */
        
        VMCacheManager.getVMCacheManager().flush(getContext());
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
    private Object getContext()
	{
		return ((TreeModelViewer) ((AbstractDebugView) fView).getViewer()).getPresentationContext();
	}
}
