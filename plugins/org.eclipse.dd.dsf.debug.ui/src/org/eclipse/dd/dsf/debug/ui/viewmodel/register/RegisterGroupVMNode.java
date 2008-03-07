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
package org.eclipse.dd.dsf.debug.ui.viewmodel.register;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRegisters.IGroupChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class RegisterGroupVMNode extends AbstractExpressionVMNode
    implements IElementEditor, IElementLabelProvider
{

    protected class RegisterGroupVMC extends DMVMContext
    {
        private IExpression fExpression;
        public RegisterGroupVMC(IDMContext dmc) {
            super(dmc);
        }
        
        public void setExpression(IExpression expression) {
            fExpression = expression;
        }
        
        @Override
        @SuppressWarnings("unchecked") 
        public Object getAdapter(Class adapter) {
            if (fExpression != null && adapter.isAssignableFrom(fExpression.getClass())) {
                return fExpression;
            } else if (adapter.isAssignableFrom(IWatchExpressionFactoryAdapter2.class)) {
                return fRegisterGroupExpressionFactory;
            } else {
                return super.getAdapter(adapter);
            }
        }
        
        @Override
        public boolean equals(Object other) {
            if (other instanceof RegisterGroupVMC && super.equals(other)) {
                RegisterGroupVMC otherGroup = (RegisterGroupVMC)other;
                return (otherGroup.fExpression == null && fExpression == null) ||
                       (otherGroup.fExpression != null && otherGroup.fExpression.equals(fExpression));
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() + (fExpression != null ? fExpression.hashCode() : 0);
        }
    }
    
    protected class RegisterGroupExpressionFactory implements IWatchExpressionFactoryAdapter2 {
        
        public boolean canCreateWatchExpression(Object element) {
            return element instanceof RegisterGroupVMC;
        }
        
        public String createWatchExpression(Object element) throws CoreException {
            RegisterGroupVMC registerVmc = ((RegisterGroupVMC)element);

            StringBuffer exprBuf = new StringBuffer();
            IRegisterGroupDMContext groupDmc = DMContexts.getAncestorOfType(registerVmc.getDMContext(), IRegisterGroupDMContext.class);
            if (groupDmc != null) {
                exprBuf.append("$$\""); //$NON-NLS-1$
                exprBuf.append(groupDmc.getName());
                exprBuf.append('"');
                return exprBuf.toString();
            }
            
            return null;            
        }
    }

    final private SyncRegisterDataAccess fSyncRegisterDataAccess; 
    final protected RegisterGroupExpressionFactory fRegisterGroupExpressionFactory = new RegisterGroupExpressionFactory(); 
    private WatchExpressionCellModifier fWatchExpressionCellModifier = new WatchExpressionCellModifier();

    public RegisterGroupVMNode(AbstractDMVMProvider provider, DsfSession session, SyncRegisterDataAccess syncDataAccess) {
        super(provider, session, IRegisters.IRegisterGroupDMContext.class);
        fSyncRegisterDataAccess = syncDataAccess;
    }
    
    public SyncRegisterDataAccess getSyncRegisterDataAccess() {
        return fSyncRegisterDataAccess;
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRegisters.class, null, update)) return;
        getServicesTracker().getService(IRegisters.class).getRegisterGroups(
            createCompositeDMVMContext(update),
            new DataRequestMonitor<IRegisterGroupDMContext[]>(getSession().getExecutor(), null) { 
                @Override
                public void handleCompleted() {
                    if (!getStatus().isOK()) {
                        update.done();
                        return;
                    }
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }}); 
    }
    
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new RegisterGroupVMC(dmc);
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
            final IRegisterGroupDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisterGroupDMContext.class);
            if (!checkDmc(dmc, update) || !checkService(IRegisters.class, null, update)) continue;
            
            getDMVMProvider().getModelData(
                this, update, 
                getServicesTracker().getService(IRegisters.class, null),
                dmc, 
                new DataRequestMonitor<IRegisterGroupDMData>(getSession().getExecutor(), null) { 
                    @Override
                    protected void handleCompleted() {
                        /*
                         * Check that the request was evaluated and data is still
                         * valid.  The request could fail if the state of the 
                         * service changed during the request, but the view model
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
                },
                getExecutor());
        }
    }

    protected void fillColumnLabel(IRegisterGroupDMContext dmContext, IRegisterGroupDMData dmData,
                                   String columnId, int idx, ILabelUpdate update) 
    {
        if (IDebugVMConstants.COLUMN_ID__NAME.equals(columnId)) {
            update.setLabel(dmData.getName(), idx);
            update.setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER_GROUP), idx);
        } else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
            update.setLabel("", idx); //$NON-NLS-1$
        } else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(columnId)) {
            update.setLabel(dmData.getDescription(), idx);
        } else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(columnId)) {
            update.setLabel("", idx); //$NON-NLS-1$
        } else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            IVMContext vmc = (IVMContext)update.getElement();
            IExpression expression = (IExpression)vmc.getAdapter(IExpression.class);
            if (expression != null) {
                update.setLabel(expression.getExpressionText(), idx);
            } else {
                update.setLabel(dmData.getName(), idx);
            }
        }
        else if ( columnId == null ) {
            /*
             *  If the Column ID comes in as "null" then this is the case where the user has decided
             *  to not have any columns. So we need a default action which makes the most sense  and
             *  is doable. In this case we elect to simply display the name.
             */
            update.setLabel(dmData.getName(), idx);
            update.setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER_GROUP), idx);
        }
    }
    
    public int getDeltaFlags(Object e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } 
        else if (e instanceof IRegisters.IGroupsChangedDMEvent) {
            return IModelDelta.CONTENT;
        }
        else if (e instanceof IRegisters.IGroupChangedDMEvent) {
            return IModelDelta.STATE;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that indicates all groups have changed
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        else if (e instanceof IRegisters.IGroupsChangedDMEvent) {
            // Create a delta that indicates all groups have changed
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        else if (e instanceof IRegisters.IGroupChangedDMEvent) {
            // Create a delta that indicates that specific group changed
            parentDelta.addNode( createVMContext(((IGroupChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        }
        rm.done();
    }
    
    public boolean canParseExpression(IExpression expression) {
        return parseExpressionForGroupName(expression.getExpressionText()) != null;
    }

    /**
     * Expected format: $$"Group Name"$Register_Name.Bit_Field_Name
     */
    private String parseExpressionForGroupName(String expression) {
        if (expression.startsWith("$$\"")) { //$NON-NLS-1$
            int secondQuoteIdx = expression.indexOf('"', "$$\"".length()); //$NON-NLS-1$
            if (secondQuoteIdx > 0) {
                return expression.substring(3, secondQuoteIdx);
            }
        } 
        return null;
    }
    
    public int getExpressionLength(String expression) {
        if (expression.startsWith("$$\"")) { //$NON-NLS-1$
            int secondQuoteIdx = expression.indexOf('"', "$$\"".length()); //$NON-NLS-1$
            if (secondQuoteIdx > 0) {
                return secondQuoteIdx + 1;
            }
        } 
        return -1;
    }
    
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }

        return IModelDelta.NO_CHANGE;
    }
    
    public void buildDeltaForExpression(IExpression expression, int elementIdx, Object event, VMDelta parentDelta, 
        TreePath path, RequestMonitor rm) 
    {
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            // Mark the parent delta indicating that elements were added and/or removed.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        rm.done();
    }

    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        if (event instanceof IRegisters.IGroupsChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.CONTENT);
        } 
        if (event instanceof IRegisters.IGroupChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.STATE);
        }
        rm.done();
    }

    @Override
    protected void testElementForExpression(Object element, IExpression expression, DataRequestMonitor<Boolean> rm) {
        if (!(element instanceof IDMVMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        final IRegisterGroupDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)element).getDMContext(), IRegisterGroupDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        String groupName = parseExpressionForGroupName(expression.getExpressionText());
        if (dmc.getName().equals(groupName)) {
            rm.setData(Boolean.TRUE);
        } else {
            rm.setData(Boolean.FALSE);
        }
        rm.done();
    }
    
    @Override
    protected void associateExpression(Object element, IExpression expression) {
        if (element instanceof RegisterGroupVMC) {
            ((RegisterGroupVMC)element).setExpression(expression);
        }
    }
    
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 
        return null;
    }
    
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return fWatchExpressionCellModifier;
    }

}
