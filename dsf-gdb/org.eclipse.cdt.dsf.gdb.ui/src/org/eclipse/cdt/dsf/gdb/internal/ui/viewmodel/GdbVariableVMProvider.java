/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     Axel Mueller            - Bug 306555 - Add support for cast to type / view as array (IExpressions2)     
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.DsfCastToTypeSupport;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMProvider;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.GdbVariableVMNode.IncompleteChildrenVMC;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;

/**
 * A specialization of VariableVMProvider that uses a GDB-specific variable VM
 * node. To understand why this is necessary, see GdbVariableVMNode.
 */
@SuppressWarnings("restriction")
public class GdbVariableVMProvider extends VariableVMProvider {

	private IPropertyChangeListener fPreferencesListener;

	/**
	 * Constructor (passthru)
	 */
	public GdbVariableVMProvider(AbstractVMAdapter adapter,
			IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
		
        final IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
        
        Integer childCountLimit = store.getInt(IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS);
        if (childCountLimit != 0) {
        	getPresentationContext().setProperty(IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS,
        			childCountLimit);
        }

        fPreferencesListener = new IPropertyChangeListener() {
            @Override
			public void propertyChange(final PropertyChangeEvent event) {
				handlePropertyChanged(store, event);
			}};
        store.addPropertyChangeListener(fPreferencesListener);
	}

    @Override
	public void dispose() {
		super.dispose();
		
        final IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		store.removePropertyChangeListener(fPreferencesListener);
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMProvider#configureLayout(org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess)
     */
    @Override
	protected void configureLayout() {
        // Create the variable data access routines.
        SyncVariableDataAccess varAccess = new SyncVariableDataAccess(getSession()) ;
    	
        // Create the top level node to deal with the root selection.
        IRootVMNode rootNode = new RootDMVMNode(this);
        setRootNode(rootNode);
        
        // Create the next level which represents members of structs/unions/enums and elements of arrays.
        IVMNode subExpressioNode = new GdbVariableVMNode(this, getSession(), varAccess);
        addChildNodes(rootNode, new IVMNode[] { subExpressioNode });

		/* Wire up the casting support. IExpressions2 service is always available
		 * for gdb. No need to call hookUpCastingSupport */
		((VariableVMNode) subExpressioNode).setCastToTypeSupport(
				new DsfCastToTypeSupport(getSession(), GdbVariableVMProvider.this, varAccess));
        
        // Configure the sub-expression node to be a child of itself.  This way the content
        // provider will recursively drill-down the variable hierarchy.
        addChildNodes(subExpressioNode, new IVMNode[] { subExpressioNode });
    }

	@Override
	public void handleEvent(Object event, final RequestMonitor rm) {
        if (event instanceof DoubleClickEvent && !isDisposed()) {

        	final ISelection selection= ((DoubleClickEvent) event).getSelection();
            if (selection instanceof IStructuredSelection) {
            	
                Object element= ((IStructuredSelection) selection).getFirstElement();
                if (element instanceof IncompleteChildrenVMC) {
                	
                    IncompleteChildrenVMC incompleteChildrenVmc = ((IncompleteChildrenVMC) element); 
                    IVMNode node = incompleteChildrenVmc.getVMNode();
                    if (node instanceof GdbVariableVMNode && node.getVMProvider() == this) {
                    	
            			if (selection instanceof ITreeSelection) {
            				
            				ITreeSelection treeSelection = (ITreeSelection) selection;
            				TreePath path = treeSelection.getPaths()[0];
            				IExpressionDMContext exprCtx = incompleteChildrenVmc.getParentDMContext();
            				((GdbVariableVMNode) node).incrementChildCountLimit(exprCtx);

            				// replace double click event with the fetch more children event.
							final FetchMoreChildrenEvent fetchMoreChildrenEvent = new FetchMoreChildrenEvent(
									exprCtx, path);
            				getExecutor().execute(new DsfRunnable() {
            	                @Override
            					public void run() {
            						handleEvent(fetchMoreChildrenEvent, rm);
            					}
            				});
            				
            				return;
            			}
                    }
                }
            }
        }
        
		super.handleEvent(event, rm);
	}
	
	/**
	 * @param store
	 * @param event
	 * 
	 * @since 3.0
	 */
	protected void handlePropertyChanged(final IPreferenceStore store, final PropertyChangeEvent event) {
		String property = event.getProperty();
		if (IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS.equals(property)) {
			Integer childCountLimit = store.getInt(IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS);
			
	        if (childCountLimit != 0) {
	        	getPresentationContext().setProperty(IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS,
	        			childCountLimit);
	        } else {
	        	getPresentationContext().setProperty(IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS,
	        			null);
	        }
	        
			getExecutor().execute(new DsfRunnable() {
                @Override
			    public void run() {
			        handleEvent(event);
			    }
			});
		}
	}
}
