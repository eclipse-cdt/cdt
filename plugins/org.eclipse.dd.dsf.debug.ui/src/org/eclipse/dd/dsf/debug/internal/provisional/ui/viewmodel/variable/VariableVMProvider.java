/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.FormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.update.BreakpointHitUpdatePolicy;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.ICachingService;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.ManualUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

@SuppressWarnings("restriction")
public class VariableVMProvider extends AbstractDMVMProvider 
    implements IPropertyChangeListener, IColumnPresentationFactory 
{

	public VariableVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);

        context.addPropertyChangeListener(this);

        /*
         *  Create the variable data access routines.
         */
        SyncVariableDataAccess varAccess = new SyncVariableDataAccess(session) ;

        /*
         *  Create the top level node to deal with the root selection.
         */
        IRootVMNode rootNode = new RootDMVMNode(this);
        setRootNode(rootNode);
        
        /*
         * Create the next level which represents members of structs/unions/enums and elements of arrays.
         */
        IVMNode subExpressioNode = new VariableVMNode(FormattedValuePreferenceStore.getDefault(), this, getSession(), varAccess);
        addChildNodes(rootNode, new IVMNode[] { subExpressioNode });

        // Configure the sub-expression node to be a child of itself.  This way the content
        // provider will recursively drill-down the variable hierarchy.
        addChildNodes(subExpressioNode, new IVMNode[] { subExpressioNode });
    }
	
    @Override
    public void dispose() {
        getPresentationContext().removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new VariableColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return VariableColumnPresentation.ID;
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        handleEvent(event);
    }
    
    @Override
    protected IVMUpdatePolicy[] createUpdateModes() {
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy(), new ManualUpdatePolicy(), new BreakpointHitUpdatePolicy() };
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
                    DsfServicesTracker tracker = new DsfServicesTracker(DsfDebugUIPlugin.getBundleContext(), getSession().getId());
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
