/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat;

import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IDebugElement;
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
public class AbstractSetFormatStyle implements IViewActionDelegate, IDebugContextListener {

    /*
     *  Local private storage.
     */
    private IViewPart fpart      = null;
    private Object fViewInput    = null;
    private IAction fAction      = null;
    
    /*
     *  This routine is meant to be overidden so extenders of this class tell us what 
     *  to use for the default.
     */
    protected String getFormatStyle() {
        return IFormattedValues.NATURAL_FORMAT;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
        /*
         *  Save the view information for later reference and data retrieval.
         */
        fpart = view;
        
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
        
        /*
         *  Update the current state of affairs.
         */
        update();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        
        /*
         *   Make sure we have a valid set of information. Otherwise we cannot go forward.
         */
        if ( fpart instanceof AbstractDebugView && fViewInput != null ) 
        {
            Viewer viewer = ( (AbstractDebugView) fpart).getViewer();

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
                 *  Store the new style. So it will be picked up by the view when the view changes.
                 */
                context.setProperty( IDebugVMConstants.CURRENT_FORMAT_STORAGE, getFormatStyle() );
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        
        /*
         *  Since we are creating a generic central handler ( "update" ). It needs to get the
         *  action information later.
         */
        fAction = action;
        
        /*
         *  We need to what our input is. This will either be a selection from the debug view
         *  or something selected in our view.  
         */
        if (selection instanceof IStructuredSelection) {
            
            Object element = ( (IStructuredSelection) selection ).getFirstElement();
            
            if (element instanceof IDMVMContext ) { fViewInput = element; }
            else {
                /*
                 *  We deliberately do nothing here. A valid structured selection has already been
                 *  selected. It comes from the Debug View and is a valid Debug Element. We do not
                 *  want to overwrite it.
                 */
            }
                 
        } else {
            fViewInput = null;
        }
        update();
    }
    
    /*
     *  This is the common processing routine which is called from the various selection routines.
     *  Its job is to determine if we are valid for OCD and if so what is the proper execution dmc
     *  we should be operating on
     */

    private void update() {
        
        if ( fAction != null ) {
            /*
             *  If the element is a debug view context selection then we want to be active since
             *  a possible OCD selection is there. We will let the connection type determine  if
             *  we are active or not.
             */
            if ( fViewInput instanceof IDebugElement )
            {
                fAction.setEnabled(true);
            }
            else if ( fViewInput instanceof IDMVMContext ) 
            {
                fAction.setEnabled(true);
            }
            else {
                /*
                 *  It is not us  and we will mark ourselves not available. Remember on reselection will
                 *  will renable again. The Debug View change handler we have will deal with being moved
                 *  back into the picture.
                 */
                fAction.setEnabled(false);
            }
        }
    }
}

