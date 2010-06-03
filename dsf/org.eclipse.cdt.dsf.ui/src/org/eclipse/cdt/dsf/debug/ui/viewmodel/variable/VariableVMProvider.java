/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.variable;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.DsfCastToTypeSupport;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.DsfDebugUITools;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.BreakpointHitUpdatePolicy;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.update.DebugManualUpdatePolicy;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class VariableVMProvider extends AbstractDMVMProvider 
    implements IColumnPresentationFactory 
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
        public void propertyChange(final PropertyChangeEvent event) {
            getExecutor().execute(new DsfRunnable() {
                public void run() {
                    handleEvent(event);                    
                };
            });
        }        
    };
    
	public VariableVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);

        context.addPropertyChangeListener(fPresentationContextListener);

        IPreferenceStore store = DsfDebugUITools.getPreferenceStore();
        store.addPropertyChangeListener(fPreferencesListener);
        setDelayEventHandleForViewUpdate(store.getBoolean(IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE));

        configureLayout();
	}
	
    @Override
    public void dispose() {
        DsfDebugUITools.getPreferenceStore().removePropertyChangeListener(fPreferencesListener);
        getPresentationContext().removePropertyChangeListener(fPresentationContextListener);
        super.dispose();
    }
    
    /**
     * Configures the nodes of this provider.  This method may be over-ridden by
     * sub classes to create an alternate configuration in this provider.
     * 
     * @since 2.1
     */
    protected void configureLayout() {

        // Create the variable data access routines.
        SyncVariableDataAccess varAccess = new SyncVariableDataAccess(getSession()) ;

        // Create the top level node to deal with the root selection.
        IRootVMNode rootNode = new RootDMVMNode(this);
        setRootNode(rootNode);
        
        // Create the next level which represents members of structs/unions/enums and elements of arrays.
        VariableVMNode subExpressioNode = new VariableVMNode(this, getSession(), varAccess);
        addChildNodes(rootNode, new IVMNode[] { subExpressioNode });
        
        // Wire up the casting support if the IExpressions2 service is available.
        hookUpCastingSupport(varAccess, subExpressioNode);

        // Configure the sub-expression node to be a child of itself.  This way the content
        // provider will recursively drill-down the variable hierarchy.
        addChildNodes(subExpressioNode, new IVMNode[] { subExpressioNode });
    }

	private void hookUpCastingSupport(final SyncVariableDataAccess syncvarDataAccess,
			final VariableVMNode variableNode) {
		 try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), getSession().getId());
                    IExpressions2 expressions2 = tracker.getService(IExpressions2.class);
                    if (expressions2 != null) {
                    	variableNode.setCastToTypeSupport(new DsfCastToTypeSupport(
                    			getSession(), VariableVMProvider.this, syncvarDataAccess));
                    }
                    tracker.dispose();
                }
            });
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore.
        }
	}
    

    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new VariableColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return VariableColumnPresentation.ID;
    }
    
    @Override
    protected IVMUpdatePolicy[] createUpdateModes() {
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy(), new DebugManualUpdatePolicy(), new BreakpointHitUpdatePolicy() };
    }

    @Override
    protected boolean canSkipHandlingEvent(Object newEvent, Object eventToSkip) {
        // To optimize the performance of the view when stepping rapidly, skip all 
        // other events when a suspended event is received, including older suspended
        // events.
        return newEvent instanceof ISuspendedDMEvent;
    }
    
    @Override
    public void refresh() {
        super.refresh();
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), getSession().getId());
                    IExpressions expressionsService = tracker.getService(IExpressions.class);
                    if (expressionsService instanceof ICachingService) {
                        ((ICachingService)expressionsService).flushCache(null);
                    }
                    tracker.dispose();
                }
            });
        } catch (RejectedExecutionException e) {
            // Session disposed, ignore.
        }
    }
}
