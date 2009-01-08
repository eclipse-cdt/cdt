/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB		  - Modules view for DSF implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMData;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

@SuppressWarnings("restriction")
public class ModulesVMNode extends AbstractDMVMNode
    implements IElementLabelProvider
{
    /**
     * Marker type for the modules VM context.  It allows action enablement 
     * expressions to check for module context type.
     */
    public class ModuleVMContext extends DMVMContext {
        protected ModuleVMContext(IDMContext dmc) {
            super(dmc);
        }
    }
    
    
    public ModulesVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IModuleDMContext.class);
    }
    
    @Override
    public String toString() {
        return "ModulesVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        IModules modulesService = getServicesTracker().getService(IModules.class);
        final ISymbolDMContext symDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), ISymbolDMContext.class) ;
        
        if (modulesService == null || symDmc == null) {
            handleFailedUpdate(update);
            return;
        }
        
        modulesService.getModules(
            symDmc,
            new ViewerDataRequestMonitor<IModuleDMContext[]>(getSession().getExecutor(), update) { 
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                        update.done();
                        return;
                    }
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }}); 
    }
    
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new ModuleVMContext(dmc);
    }
    
    public void update(final ILabelUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updateLabelInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (ILabelUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            IModules modulesService = getServicesTracker().getService(IModules.class);
            final IModuleDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IModuleDMContext.class);
            // If either update or service are not valid, fail the update and exit.
            if ( modulesService == null || dmc == null ) {
            	handleFailedUpdate(update);
                continue;
            }
            
            // Use  different image for loaded and unloaded symbols when event to report loading of symbols is implemented.
            update.setImageDescriptor(DsfUIPlugin.getImageDescriptor(IDsfDebugUIConstants.IMG_OBJS_SHARED_LIBRARY_SYMBOLS_LOADED), 0);
      
            modulesService.getModuleData(
                dmc, 
                new ViewerDataRequestMonitor<IModuleDMData>(getSession().getExecutor(), update) { 
                    @Override
                    protected void handleCompleted() {
                        /*
                         * The request could fail if the state of the service 
                         * changed during the request, but the view model
                         * has not been updated yet.
                         */ 
                        if (!isSuccess()) {
                            assert getStatus().isOK() || 
                                   getStatus().getCode() != IDsfStatusConstants.INTERNAL_ERROR || 
                                   getStatus().getCode() != IDsfStatusConstants.NOT_SUPPORTED;
                            handleFailedUpdate(update);
                            return;
                        }
                        
                        /*
                         * If columns are configured, call the protected methods to 
                         * fill in column values.  
                         */
                        String[] localColumns = update.getColumnIds();
                        if (localColumns == null) localColumns = new String[] { null };
                        
                        for (int i = 0; i < localColumns.length; i++) {
                            fillColumnLabel(dmc, getData(), localColumns[i], i, update);
                        }
                        update.done();
                    }
                });
        }
    }

    protected void fillColumnLabel(IModuleDMContext dmContext, IModuleDMData dmData,
                                   String columnId, int idx, ILabelUpdate update) 
    {
        if ( columnId == null ) {
            /*
             *  If the Column ID comes in as "null" then this is the case where the user has decided
             *  to not have any columns. So we need a default action which makes the most sense  and
             *  is doable. In this case we elect to simply display the name.
             */
            update.setLabel(dmData.getName(), idx);
        }
    }
    
    public int getDeltaFlags(Object e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } 
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that indicates all groups have changed
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        
        rm.done();
    }
}
