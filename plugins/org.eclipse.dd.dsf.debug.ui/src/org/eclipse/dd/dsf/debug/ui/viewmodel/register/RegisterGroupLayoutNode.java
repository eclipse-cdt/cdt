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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRegisters.IGroupChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.AbstractExpressionLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class RegisterGroupLayoutNode extends AbstractExpressionLayoutNode<IRegisterGroupDMData>
    implements IElementEditor
{

    protected class RegisterGroupVMC extends DMVMContext implements IVariable
    {
        private IExpression fExpression;
        public RegisterGroupVMC(IDMContext<?> dmc) {
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
            } else if (adapter.isAssignableFrom(IWatchExpressionFactoryAdapterExtension.class)) {
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

        public String getName() throws DebugException { return toString(); }
        public String getReferenceTypeName() throws DebugException { return ""; } //$NON-NLS-1$
        public IValue getValue() throws DebugException { return null; }
        public boolean hasValueChanged() throws DebugException { return false; }
        public void setValue(IValue value) throws DebugException {}
        public void setValue(String expression) throws DebugException {}
        public boolean supportsValueModification() { return false; }
        public boolean verifyValue(IValue value) throws DebugException { return false; }
        public boolean verifyValue(String expression) throws DebugException { return false; }
        public IDebugTarget getDebugTarget() { return null;}
        public ILaunch getLaunch() { return null; }
        public String getModelIdentifier() { return DsfDebugUIPlugin.PLUGIN_ID; }
    }
    
    protected class RegisterGroupExpressionFactory implements IWatchExpressionFactoryAdapterExtension {
        
        public boolean canCreateWatchExpression(IVariable variable) {
            return variable instanceof RegisterGroupVMC;
        }
        
        public String createWatchExpression(IVariable variable) throws CoreException {
            RegisterGroupVMC registerVmc = ((RegisterGroupVMC)variable);

            StringBuffer exprBuf = new StringBuffer();
            IRegisterGroupDMContext groupDmc = DMContexts.getAncestorOfType(registerVmc.getDMC(), IRegisterGroupDMContext.class);
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

    public RegisterGroupLayoutNode(AbstractVMProvider provider, DsfSession session, SyncRegisterDataAccess syncDataAccess) {
        super(provider, session, IRegisters.IRegisterGroupDMContext.class);
        fSyncRegisterDataAccess = syncDataAccess;
    }
    
    public SyncRegisterDataAccess getSyncRegisterDataAccess() {
        return fSyncRegisterDataAccess;
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRegisters.class, null, update)) return;
        
        final IExecutionDMContext execDmc = findDmcInPath(update.getElementPath(), IExecutionDMContext.class) ;
        
        if (execDmc != null) {
            getServicesTracker().getService(IRegisters.class).getRegisterGroups(
                execDmc,
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
        } else {
            handleFailedUpdate(update);
        }          
        
    }
    
    @Override
    protected IVMContext createVMContext(IDMContext<IRegisterGroupDMData> dmc) {
        return new RegisterGroupVMC(dmc);
    }

    
    @Override
    protected void fillColumnLabel(IDMContext<IRegisterGroupDMData> dmContext, IRegisterGroupDMData dmData,
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
    
    @Override
    protected int getNodeDeltaFlagsForDMEvent(IDMEvent<?> e) {
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

    @Override
    protected void buildDeltaForDMEvent(IDMEvent<?> e, VMDelta parent, int nodeOffset, RequestMonitor rm) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that indicates all groups have changed
            parent.addFlags(IModelDelta.CONTENT);
        } 
        else if (e instanceof IRegisters.IGroupsChangedDMEvent) {
        	// flush the cache
        	VMCacheManager.getVMCacheManager().flush(super.getVMProvider().getPresentationContext());
            
            // Create a delta that indicates all groups have changed
            parent.addFlags(IModelDelta.CONTENT);
        } 
        else if (e instanceof IRegisters.IGroupChangedDMEvent) {
            // flush the cache
            VMCacheManager.getVMCacheManager().flush(super.getVMProvider().getPresentationContext());
            
            // Create a delta that indicates that specific group changed
            parent.addNode( createVMContext(((IGroupChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        }
        
        super.buildDeltaForDMEvent(e, parent, nodeOffset, rm);
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
    
    @Override
    protected int getDeltaFlagsForExpressionPart(Object event) {
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        }

        return IModelDelta.NO_CHANGE;
    }
    
    @Override
    public void buildDeltaForExpression(final IExpression expression, final int elementIdx, final String expressionText, final Object event, final VMDelta parentDelta, final TreePath path, final RequestMonitor rm) 
    {
        if (event instanceof IRunControl.ISuspendedDMEvent) {
            // Mark the partent delta indicating that elements were added and/or removed.
            parentDelta.addFlags(IModelDelta.CONTENT);
        } 
        
        super.buildDeltaForExpression(expression, elementIdx, expressionText, event, parentDelta, path, rm);
    }

    @Override
    protected void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        if (event instanceof IRegisters.IGroupsChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.CONTENT);
        } 
        if (event instanceof IRegisters.IGroupChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.STATE);
        }
        
        super.buildDeltaForExpressionElement(element, elementIdx, event, parentDelta, rm);
    }

    @Override
    protected void testContextForExpression(Object element, final String expression, final DataRequestMonitor<Boolean> rm) {
        if (!(element instanceof AbstractDMVMLayoutNode.DMVMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        final IRegisterGroupDMContext dmc = DMContexts.getAncestorOfType(((DMVMContext)element).getDMC(), IRegisterGroupDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfService.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        int startIdx = "$$\"".length(); //$NON-NLS-1$
        int endIdx = expression.indexOf('"', startIdx);
        String groupName = expression.substring(startIdx, endIdx);
        if (groupName.equals(dmc.getName())) {
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
