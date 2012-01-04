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

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IGroupChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IGroupsChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.WatchExpressionCellModifier;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableLabelFont;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.StaleDataLabelBackground;
import org.eclipse.cdt.dsf.ui.viewmodel.update.StaleDataLabelForeground;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;

public class RegisterGroupVMNode extends AbstractExpressionVMNode
    implements IElementEditor, IElementLabelProvider, IElementMementoProvider, IElementPropertiesProvider
{
    /**
     * @since 2.0
     */    
    private static final String PROP_REGISTER_GROUP_DESCRIPTION = "register_group_description";  //$NON-NLS-1$

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
        @SuppressWarnings({ "rawtypes", "unchecked" }) 
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
        
        @Override
        public boolean canCreateWatchExpression(Object element) {
            return element instanceof RegisterGroupVMC;
        }
        
        /**
         * Expected format: Group(GroupName)
         */
        @Override
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

    /**
     * The label provider delegate.  This VM node will delegate label updates to this provider
     * which can be created by sub-classes. 
     *  
     * @since 2.0
     */    
    private IElementLabelProvider fLabelProvider;

    public RegisterGroupVMNode(AbstractDMVMProvider provider, DsfSession session, SyncRegisterDataAccess syncDataAccess) {
        super(provider, session, IRegisterGroupDMContext.class);
        fLabelProvider = createLabelProvider();
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

    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new RegisterGroupVMC(dmc);
    }

    /**
     * Creates the label provider delegate.  This VM node will delegate label 
     * updates to this provider which can be created by sub-classes.   
     *  
     * @return Returns the label provider for this node. 
     *  
     * @since 2.0
     */    
    protected IElementLabelProvider createLabelProvider() {
        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();

        // The name column consists of the group name.  
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__NAME,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForRegisterVM.RegisterGroupVMNode_Name_column__text_format, 
                    new String[] { PROP_NAME }),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER_GROUP)),
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // The description column contains a brief description of the register group. 
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__DESCRIPTION,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(MessagesForRegisterVM.RegisterGroupVMNode_Description_column__text_format, 
                new String[] { PROP_REGISTER_GROUP_DESCRIPTION }),
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // Expression column is visible only in the expressions view.  It shows the expression string that the user 
        // entered.  Expression column images are the same as for the name column.
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__EXPRESSION,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForRegisterVM.RegisterGroupVMNode_Expression_column__text_format, 
                    new String[] { PROP_ELEMENT_EXPRESSION }),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER_GROUP)),
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(MessagesForRegisterVM.RegisterGroupVMNode_No_columns__text_format, 
                    new String[] { PROP_NAME, PROP_REGISTER_GROUP_DESCRIPTION}),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER_GROUP)),
                new StaleDataLabelBackground(),
                new VariableLabelFont(),
            }));
        
        return provider;
    }

    @Override
    public void update(final ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }

    /**
     * @see IElementPropertiesProvider#update(IPropertiesUpdate[])
     * 
     * @since 2.0
     */    
    @Override
    public void update(final IPropertiesUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
                public void run() {
                    updatePropertiesInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (IPropertiesUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    /**
     * @since 2.0
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void updatePropertiesInSessionThread(IPropertiesUpdate[] updates) {
        IRegisters service = getServicesTracker().getService(IRegisters.class, null);

        for (final IPropertiesUpdate update : updates) {
            IExpression expression = (IExpression)DebugPlugin.getAdapter(update.getElement(), IExpression.class);
            if (expression != null) {
                update.setProperty(AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION, expression.getExpressionText());
            }
            
            IRegisterGroupDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisterGroupDMContext.class);
            IRegisters regService = getServicesTracker().getService(IRegisters.class);
            
            if ( dmc == null || regService == null) {
            	handleFailedUpdate(update);
                return;
            }
            
            service.getRegisterGroupData(
                dmc, 
                new ViewerDataRequestMonitor<IRegisterGroupDMData>(getSession().getExecutor(), update) { 
                    @Override
                    protected void handleSuccess() {
                        fillRegisterGroupDataProperties(update, getData());
                        update.done();
                    }
                });
        }
    }
    
    /**
     * @since 2.0
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void fillRegisterGroupDataProperties(IPropertiesUpdate update, IRegisterGroupDMData data) {
        update.setProperty(PROP_NAME, data.getName());
        update.setProperty(PROP_REGISTER_GROUP_DESCRIPTION, data.getDescription());
        
        /*
         * If this node has an expression then it has already been filled in by the higher
         * level logic. If not then we need to supply something.  In the  previous version
         * ( pre-property based ) we supplied the name. So we will do that here also.
         */
        IExpression expression = (IExpression)DebugPlugin.getAdapter(update.getElement(), IExpression.class);
        if (expression == null) {
            update.setProperty(AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION, data.getName());
        }
    }

    @Override
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

    @Override
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
    
    @Override
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
            if ( startIdx == -1 || endIdx == -1 ) {
            	return null;
            }
            String groupName = expression.substring(startIdx, endIdx);
            return groupName.trim();
        }
    	
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode#getDeltaFlagsForExpression(org.eclipse.debug.core.model.IExpression, java.lang.Object)
     */
    @Override
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
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpression(org.eclipse.debug.core.model.IExpression, int, java.lang.Object, org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, org.eclipse.jface.viewers.TreePath, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    @Override
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
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpressionElement(java.lang.Object, int, java.lang.Object, org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    @Override
    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        if (event instanceof IGroupChangedDMEvent) {
            parentDelta.addNode(element, IModelDelta.STATE);
        }
        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode#testElementForExpression(java.lang.Object, org.eclipse.debug.core.model.IExpression, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    protected void testElementForExpression(Object element, IExpression expression, final DataRequestMonitor<Boolean> rm) {
        if (!(element instanceof IDMVMContext)) {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        final IRegisterGroupDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)element).getDMContext(), IRegisterGroupDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final String groupName = parseExpressionForGroupName(expression.getExpressionText());
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
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
                        rm.setStatus(new Status(IStatus.WARNING, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Register service not available", null)); //$NON-NLS-1$                        
                        rm.done();
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            rm.setStatus(new Status(IStatus.WARNING, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "DSF session shut down", null)); //$NON-NLS-1$
            rm.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode#associateExpression(java.lang.Object, org.eclipse.debug.core.model.IExpression)
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
    @Override
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
    @Override
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return fWatchExpressionCellModifier;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    private final String MEMENTO_NAME = "GROUP_MEMENTO_NAME"; //$NON-NLS-1$
    
    @Override
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
                    @Override
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
    @Override
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
                    @Override
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