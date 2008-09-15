/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.register;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IRegisters.IGroupChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IGroupsChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class RegisterGroupVMNode extends AbstractExpressionVMNode
    implements IElementEditor, IElementLabelProvider, IElementMementoProvider
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
                return getWatchExpressionFactory();
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
        
        /**
         * Expected format: Group(GroupName)
         */
        public String createWatchExpression(Object element) throws CoreException {
            IRegisterGroupDMData groupData = getSyncRegisterDataAccess().getRegisterGroupDMData(element);
            if (groupData != null) {
                StringBuffer exprBuf = new StringBuffer();
                exprBuf.append("GRP( "); //$NON-NLS-1$
                exprBuf.append(groupData.getName());
                exprBuf.append(" )"); //$NON-NLS-1$
                return exprBuf.toString();
            }
            
            return null;            
        }
    }

    final private SyncRegisterDataAccess fSyncRegisterDataAccess; 
    private IWatchExpressionFactoryAdapter2 fRegisterGroupExpressionFactory = null; 
    private WatchExpressionCellModifier fWatchExpressionCellModifier = new WatchExpressionCellModifier();

    public RegisterGroupVMNode(AbstractDMVMProvider provider, DsfSession session, SyncRegisterDataAccess syncDataAccess) {
        super(provider, session, IRegisterGroupDMContext.class);
        fSyncRegisterDataAccess = syncDataAccess;
    }

    @Override
    public String toString() {
        return "RegisterGroupVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }

    public SyncRegisterDataAccess getSyncRegisterDataAccess() {
        return fSyncRegisterDataAccess;
    }

    /**
     * @since 1.1
     */
    public IWatchExpressionFactoryAdapter2 getWatchExpressionFactory() {
    	if ( fRegisterGroupExpressionFactory == null ) {
    		fRegisterGroupExpressionFactory = new RegisterGroupExpressionFactory();
    	}
    	return fRegisterGroupExpressionFactory;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#updateElementsInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate)
     */
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
    	
        IRegisters regService = getServicesTracker().getService(IRegisters.class);
        
        if ( regService == null ) {
        	handleFailedUpdate(update);
            return;
        }
        
        regService.getRegisterGroups(
            createCompositeDMVMContext(update),
            new ViewerDataRequestMonitor<IRegisterGroupDMContext[]>(getSession().getExecutor(), update) { 
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#createVMContext(org.eclipse.dd.dsf.datamodel.IDMContext)
     */
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new RegisterGroupVMC(dmc);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate[])
     */
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

    /*
     *  Updates the labels with the required information for each visible column.
     */
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
        	
            final IRegisterGroupDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisterGroupDMContext.class);
            if ( dmc == null ) {
            	handleFailedUpdate(update);
                continue;
            }
            
            IRegisters regService = getServicesTracker().getService(IRegisters.class);
            if ( regService == null ) {
            	handleFailedUpdate(update);
                continue;
            }
            
            getDMVMProvider().getModelData(
                this, 
                update, 
                regService,
                dmc, 
                new ViewerDataRequestMonitor<IRegisterGroupDMData>(getSession().getExecutor(), update) { 
                    @Override
                    protected void handleCompleted() {
                        /*
                         * Check that the request was evaluated and data is still
                         * valid.  The request could fail if the state of the 
                         * service changed during the request, but the view model
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
                },
                getExecutor());
        }
    }

    /*
     * Based on the specified visible column, provide the appropriate value/label.
     */
    protected void fillColumnLabel(IRegisterGroupDMContext dmContext, IRegisterGroupDMData dmData,
                                   String columnId, int idx, ILabelUpdate update) 
    {
    	update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
        
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
   
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.IVMNode#getDeltaFlags(java.lang.Object)
     */
    public int getDeltaFlags(Object e) {
        if (e instanceof ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } 
        else if (e instanceof IGroupsChangedDMEvent) {
            return IModelDelta.CONTENT;
        }
        else if (e instanceof IGroupChangedDMEvent) {
            return IModelDelta.STATE;
        }
        return IModelDelta.NO_CHANGE;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.ui.viewmodel.IVMNode#buildDelta(java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, int, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        // Although the register groups themselves are not affected by the 
        // suspended event, typically all the registers are.  Add a CONTENT changed
        // flag to the parent to repaint all the groups and their registers.
        if (e instanceof ISuspendedDMEvent) {
            // Create a delta that indicates all groups have changed
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        else if (e instanceof IGroupsChangedDMEvent) {
            // Create a delta that indicates all groups have changed
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        else if (e instanceof IGroupChangedDMEvent) {
            // Create a delta that indicates that specific group changed
            parentDelta.addNode( createVMContext(((IGroupChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        }
        rm.done();
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#canParseExpression(org.eclipse.debug.core.model.IExpression)
     */
    public boolean canParseExpression(IExpression expression) {
        return parseExpressionForGroupName(expression.getExpressionText()) != null;
    }

    /**
     * Expected format: Group(GroupName)
     */
    private String parseExpressionForGroupName(String expression) {
    	if (expression.startsWith("GRP(")) { //$NON-NLS-1$
    		/*
    		 * Extract the group name.
    		 */
    		int startIdx = "GRP(".length(); //$NON-NLS-1$
            int endIdx = expression.indexOf(')', startIdx);
            String groupName = expression.substring(startIdx, endIdx);
            return groupName.trim();
        }
    	
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#getDeltaFlagsForExpression(org.eclipse.debug.core.model.IExpression, java.lang.Object)
     */
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {

        if (event instanceof ISuspendedDMEvent ||
            event instanceof IGroupsChangedDMEvent) 
        {
            return IModelDelta.CONTENT;
        }

        if (event instanceof IGroupChangedDMEvent) {
            return IModelDelta.STATE;
        }

        return IModelDelta.NO_CHANGE;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpression(org.eclipse.debug.core.model.IExpression, int, java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, org.eclipse.jface.viewers.TreePath, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDeltaForExpression(IExpression expression, int elementIdx, Object event, VMDelta parentDelta, 
        TreePath path, RequestMonitor rm) 
    {
        if (event instanceof ISuspendedDMEvent) {
            // Mark the parent delta indicating that elements were added and/or removed.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 

        // If the group definitions have changed, refresh the whole expressions
        // view contents since previously invalid expressions may now evaluate 
        // to valid groups 
        if (event instanceof IGroupsChangedDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        

        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpressionElement(java.lang.Object, int, java.lang.Object, org.eclipse.dd.dsf.ui.viewmodel.VMDelta, org.eclipse.dd.dsf.concurrent.RequestMonitor)
     */
    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        if (event instanceof IGroupChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.STATE);
        }
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode#testElementForExpression(java.lang.Object, org.eclipse.debug.core.model.IExpression, org.eclipse.dd.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    protected void testElementForExpression(Object element, IExpression expression, final DataRequestMonitor<Boolean> rm) {
        if (!(element instanceof IDMVMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        final IRegisterGroupDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)element).getDMContext(), IRegisterGroupDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final String groupName = parseExpressionForGroupName(expression.getExpressionText());
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    IRegisters registersService = getServicesTracker().getService(IRegisters.class);
                    if (registersService != null) {
                        registersService.getRegisterGroupData(
                            dmc, 
                            new DataRequestMonitor<IRegisterGroupDMData>(ImmediateExecutor.getInstance(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData( getData().getName().equals(groupName) );
                                    rm.done();
                                }
                            });
                    } else {
                        rm.setStatus(new Status(IStatus.WARNING, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Register service not available", null)); //$NON-NLS-1$                        
                        rm.done();
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.WARNING, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "DSF session shut down", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode#associateExpression(java.lang.Object, org.eclipse.debug.core.model.IExpression)
     */
    @Override
    protected void associateExpression(Object element, IExpression expression) {
        if (element instanceof RegisterGroupVMC) {
            ((RegisterGroupVMC)element).setExpression(expression);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellEditor(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
     */
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellModifier(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return fWatchExpressionCellModifier;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    private final String MEMENTO_NAME = "GROUP_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        for (final IElementCompareRequest request : requests ) {
            final IRegisterGroupDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IRegisterGroupDMContext.class);
            final String mementoName = request.getMemento().getString(MEMENTO_NAME);
            
            if (regDmc == null || mementoName == null) {
                request.done();
                continue;
            }
            
        	// Now go get the model data for the single register group found.
        	try {
                getSession().getExecutor().execute(new DsfRunnable() {
                    public void run() {
                    	final IRegisters regService = getServicesTracker().getService(IRegisters.class);
                    	if ( regService != null ) {
                    		regService.getRegisterGroupData(
                    		    regDmc, 
                                new DataRequestMonitor<IRegisterGroupDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.setEqual( mementoName.equals( "Group." + getData().getName()) ); //$NON-NLS-1$
                                        }
                                        request.done();
                                    }
                                });
                    	} else {
                    		request.done();
                    	}
                    }
                });
            } catch (RejectedExecutionException e) {
                request.done();
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#encodeElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest[])
     */
    public void encodeElements(IElementMementoRequest[] requests) {
    	for ( final IElementMementoRequest request : requests ) {
            final IRegisterGroupDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IRegisterGroupDMContext.class);
            if (regDmc == null) {
                request.done();
                continue;
            }
            
            //  Now go get the model data for the single register group found.
        	try {
                getSession().getExecutor().execute(new DsfRunnable() {
                    public void run() {
                    	final IRegisters regService = getServicesTracker().getService(IRegisters.class);
                    	if ( regService != null ) {
                    		regService.getRegisterGroupData(
                    		    regDmc, 
                    		    new DataRequestMonitor<IRegisterGroupDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.getMemento().putString(MEMENTO_NAME, "Group." + getData().getName()); //$NON-NLS-1$
                                        }
                                        request.done();
                                    }
                                });
                    	} else {
                    		request.done();
                    	}
                    }
                });
            } catch (RejectedExecutionException e) {
                request.done();
            }
    	}
    }
}