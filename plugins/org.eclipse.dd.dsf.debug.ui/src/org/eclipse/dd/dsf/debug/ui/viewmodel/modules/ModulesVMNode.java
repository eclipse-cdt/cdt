/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.modules;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IModules;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.dd.dsf.debug.service.IModules.IModuleDMData;
import org.eclipse.dd.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.dd.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

@SuppressWarnings("restriction")
public class ModulesVMNode extends AbstractDMVMNode
    implements IElementLabelProvider
{
    public ModulesVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IModuleDMContext.class);
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRegisters.class, null, update)) return;
        
        final ISymbolDMContext symDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), ISymbolDMContext.class) ;
        
        if (symDmc != null) {
            getServicesTracker().getService(IModules.class).getModules(
                symDmc,
                new DataRequestMonitor<IModuleDMContext[]>(getSession().getExecutor(), null) { 
                    @Override
                    public void handleCompleted() {
                        if (!getStatus().isOK()) {
                            update.done();
                            return;
                        }
                        fillUpdateWithVMCs(update, getData());
                        update.done();
                    }}); 
        } else {
            handleFailedUpdate(update);
        }          
        
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
            final IModuleDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IModuleDMContext.class);
            if (!checkDmc(dmc, update) || !checkService(IModules.class, null, update)) continue;
            // Use  different image for loaded and unloaded symbols when event to report loading of symbols is implemented.
            update.setImageDescriptor(DsfDebugUIPlugin.getImageDescriptor(IDsfDebugUIConstants.IMG_OBJS_SHARED_LIBRARY_SYMBOLS_LOADED), 0);
      
            getServicesTracker().getService(IModules.class, null).getModuleData(
                dmc, 
                new DataRequestMonitor<IModuleDMData>(getSession().getExecutor(), null) { 
                    @Override
                    protected void handleCompleted() {
                        /*
                         * The request could fail if the state of the service 
                         * changed during the request, but the view model
                         * has not been updated yet.
                         */ 
                        if (!getStatus().isOK()) {
                            assert getStatus().isOK() || 
                                   getStatus().getCode() != IDsfService.INTERNAL_ERROR || 
                                   getStatus().getCode() != IDsfService.NOT_SUPPORTED;
                            handleFailedUpdate(update);
                            return;
                        }
                        
                        /*
                         * If columns are configured, call the protected methods to 
                         * fill in column values.  
                         */
                        String[] localColumns = update.getPresentationContext().getColumns();
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
