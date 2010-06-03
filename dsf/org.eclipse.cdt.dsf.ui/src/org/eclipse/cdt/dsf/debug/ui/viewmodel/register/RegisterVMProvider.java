/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.DsfDebugUITools;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.BreakpointHitUpdatePolicy;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.DebugManualUpdatePolicy;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 *  Provides the VIEW MODEL for the DEBUG MODEL REGISTER view.
 */
public class RegisterVMProvider extends AbstractDMVMProvider 
{
    private IPropertyChangeListener fPreferencesListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
			if (property.equals(IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE)) {
                IPreferenceStore store = DsfDebugUITools.getPreferenceStore();
                setDelayEventHandleForViewUpdate(store.getBoolean(property));
            }
        }
    };

    private IPropertyChangeListener fPresentationContextListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            handleEvent(event);
        }        
    };
    
    /*
     *  Current default for register formatting.
     */
    public RegisterVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);

        context.addPropertyChangeListener(fPresentationContextListener);
        
        IPreferenceStore store = DsfDebugUITools.getPreferenceStore();
        store.addPropertyChangeListener(fPreferencesListener);
        setDelayEventHandleForViewUpdate(store.getBoolean(IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE));
        
        /*
         *  Create the register data access routines.
         */
        SyncRegisterDataAccess regAccess = new SyncRegisterDataAccess(session) ;
        
        /*
         *  Create the top level node to deal with the root selection.
         */
        IRootVMNode rootNode = new RegisterRootDMVMNode(this);
        
        /*
         *  Create the Group nodes next. They represent the first level shown in the view.
         */
        IVMNode registerGroupNode = new RegisterGroupVMNode(this, getSession(), regAccess);
        addChildNodes(rootNode, new IVMNode[] { registerGroupNode });
        
        /*
         * Create the next level which is the registers themselves.
         */
        IVMNode registerNode = new RegisterVMNode(this, getSession(), regAccess);
        addChildNodes(registerGroupNode, new IVMNode[] { registerNode });
        
        /*
         * Create the next level which is the bitfield level.
         */
        IVMNode bitFieldNode = new RegisterBitFieldVMNode(this, getSession(), regAccess);
        addChildNodes(registerNode, new IVMNode[] { bitFieldNode });
        
        /*
         *  Now set this schema set as the layout set.
         */
        setRootNode(rootNode);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.update.AbstractCachingVMProvider#createUpdateModes()
     */
    @Override
    protected IVMUpdatePolicy[] createUpdateModes() {
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy(), new DebugManualUpdatePolicy(), new BreakpointHitUpdatePolicy() };
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider#dispose()
     */
    @Override
    public void dispose() {
        DsfDebugUITools.getPreferenceStore().removePropertyChangeListener(fPreferencesListener);
        getPresentationContext().removePropertyChangeListener(fPresentationContextListener);
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider#createColumnPresentation(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new RegisterColumnPresentation();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider#getColumnPresentationId(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return RegisterColumnPresentation.ID;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider#canSkipHandlingEvent(java.lang.Object, java.lang.Object)
     */
    @Override
    protected boolean canSkipHandlingEvent(Object newEvent, Object eventToSkip) {
    	/*
    	 * To optimize the performance of the view when stepping rapidly, skip all
    	 * other events when a suspended event is received, including older suspended
    	 * events. 
    	 */
        return newEvent instanceof ISuspendedDMEvent;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputUpdate)
     */
    @Override
	public void update(IViewerInputUpdate update) {
    	/*
    	 * Use the execution context in the current selection as the input provider.
    	 * This insures that the REGISTER VIEW will not collapse and expand on stepping or on
    	 * re-selection in the DEBUG VIEW.  Currently the register content is not stack frame
    	 * specific. If it were to become so then we would need to modify this policy.
    	 */
    	Object element = update.getElement();
    	if (element instanceof IDMVMContext) {
        	IDMContext ctx = ((IDMVMContext) element).getDMContext();
    
        	IExecutionDMContext execDmc = DMContexts.getAncestorOfType(ctx, IExecutionDMContext.class);
    		if ( execDmc != null ) {
    			/*
    			 * This tells the Flexible Hierarchy that element driving this view has not changed
    			 * and there is no need to redraw the view. Since this is a somewhat fake VMContext
    			 * we provide our Root Layout node as the representative VM node.
    			 */
    			update.setInputElement(new ViewInputElement(RegisterVMProvider.this.getRootVMNode(), execDmc));
    			update.done();
    			return;
    		}
    	}
    	
    	/*
    	 * If we reach here, then we did not override the standard behavior. Invoke the
    	 * super class and this will provide the default standard behavior.
    	 */
    	super.update(update);
    }
    
    /*
     * Provides a local implementation of the IDMVMContext.  This allows us to  return one
     * of our own making, representing the DMContext we want to use as selection criteria.
     */
    private class ViewInputElement extends AbstractVMContext implements IDMVMContext {

		final private IDMContext fDMContext;
		
		public ViewInputElement(IVMNode node, IDMContext dmc)  {
			super(node);
			fDMContext = dmc;
		}
	    
		public IDMContext getDMContext() {
			return fDMContext;
		}

		/**
         * The IAdaptable implementation.  If the adapter is the DM context, 
         * return the context, otherwise delegate to IDMContext.getAdapter().
         */
        @Override
		@SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            Object superAdapter = super.getAdapter(adapter);
            if (superAdapter != null) {
                return superAdapter;
            } else {
                // Delegate to the Data Model to find the context.
                if (adapter.isInstance(fDMContext)) {
                    return fDMContext;
                } else {
                    return fDMContext.getAdapter(adapter);
                }
            }
        }

		@Override
		public boolean equals(Object obj) {
			
			if ( obj instanceof ViewInputElement && ((ViewInputElement) obj).fDMContext.equals(fDMContext) ) {
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return fDMContext.hashCode();
		}
	}
    
    @Override
    public void refresh() {
        super.refresh();
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), getSession().getId());
                    IRegisters registerService = tracker.getService(IRegisters.class);
                    if (registerService instanceof ICachingService) {
                        ((ICachingService)registerService).flushCache(null);
                    }
                    tracker.dispose();
                }
            });
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore.
        }
    }
}
