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

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegistersChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.ErrorLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IFormattedValueVMContext;
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
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelBackground;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelForeground;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 1.0
 */
public class RegisterVMNode extends AbstractExpressionVMNode 
    implements IElementEditor, IElementLabelProvider, IElementMementoProvider, IElementPropertiesProvider
{
	/**
     * @since 2.0
     */
    private static final String PROP_REGISTER_SHOW_TYPE_NAMES = "register_show_type_names"; //$NON-NLS-1$
    
    protected class RegisterVMC extends DMVMContext
        implements IFormattedValueVMContext
    {
        private IExpression fExpression;
        public RegisterVMC(IDMContext dmc) {
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
            if (other instanceof RegisterVMC && super.equals(other)) {
                RegisterVMC otherReg = (RegisterVMC)other;
                return (otherReg.fExpression == null && fExpression == null) ||
                (otherReg.fExpression != null && otherReg.fExpression.equals(fExpression));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + (fExpression != null ? fExpression.hashCode() : 0);
        }
    }

    protected class RegisterExpressionFactory implements IWatchExpressionFactoryAdapter2 {

        public boolean canCreateWatchExpression(Object element) {
            return element instanceof RegisterVMC;
        }

        /**
         * Expected format: GRP( GroupName ).REG( RegisterName )
         */
        public String createWatchExpression(Object element) throws CoreException {
            IRegisterGroupDMData groupData = getSyncRegisterDataAccess().getRegisterGroupDMData(element);
            IRegisterDMData registerData = getSyncRegisterDataAccess().getRegisterDMData(element);
            
            if (groupData != null && registerData != null) { 
            	StringBuffer exprBuf = new StringBuffer();
            	
            	exprBuf.append("GRP( ");  exprBuf.append(groupData.getName());    exprBuf.append(" )"); //$NON-NLS-1$ //$NON-NLS-2$
            	exprBuf.append(".REG( "); exprBuf.append(registerData.getName()); exprBuf.append(" )"); //$NON-NLS-1$ //$NON-NLS-2$
                
                return exprBuf.toString();
            }

            return null;            
        }
    }

    private IWatchExpressionFactoryAdapter2 fRegisterExpressionFactory = null; 
    final private SyncRegisterDataAccess fSyncRegisterDataAccess; 

    /**
     * The label provider delegate.  This VM node will delegate label updates to this provider
     * which can be created by sub-classes. 
     *  
     * @since 2.0
     */    
    private IElementLabelProvider fLabelProvider;

    public RegisterVMNode(AbstractDMVMProvider provider, DsfSession session, SyncRegisterDataAccess syncDataAccess) {
        super(provider, session, IRegisterDMContext.class);
        fSyncRegisterDataAccess = syncDataAccess;
        fLabelProvider = createLabelProvider();
    }

    private Object[] constructTypeObjects( Map<String, Object> properties ) {
    	int type = 0;
    	if ( Boolean.TRUE.equals(properties.get(IRegisterVMConstants.PROP_IS_FLOAT)) ) { 
    		type = 1;
    	}

    	int readAttr = 0;
    	if ( Boolean.TRUE.equals(properties.get(IRegisterVMConstants.PROP_IS_READABLE)) ) { 
    		readAttr = 1;
    	} else if ( Boolean.TRUE.equals(properties.get(IRegisterVMConstants.PROP_IS_READONCE)) ) {
    		readAttr = 2;
    	}

    	int writeAttr = 0;
    	if ( Boolean.TRUE.equals(properties.get(IRegisterVMConstants.PROP_IS_WRITEABLE)) ) { 
    		writeAttr = 1;
    	} else if ( Boolean.TRUE.equals(properties.get(IRegisterVMConstants.PROP_IS_WRITEONCE)) ) {
    		writeAttr = 2;
    	}
    	Object[] messageAttrs = new Object[] { type, readAttr, writeAttr };
    	return messageAttrs;
    }
    
    /**
     * Creates the label provider delegate.  This VM node will delegate label 
     * updates to this provider which can be created by sub-classes.   
     *  
     * @return Returns the label provider for this node. 
     *  
     * @since 2.0
     */ 
    private LabelBackground columnIdValueBackground; 
    private IPropertyChangeListener fPreferenceChangeListener;

    @Override
    public void dispose() {

    	if ( fPreferenceChangeListener != null ) {
    		DebugUITools.getPreferenceStore().removePropertyChangeListener(fPreferenceChangeListener);
    	}

    	super.dispose();	
    }
  
    protected IElementLabelProvider createLabelProvider() {
    	/*
   	 * Create background which is responsive to the preference color changes.
   	 */
    	columnIdValueBackground = new LabelBackground(
    			DebugUITools.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB()) 
    	{
    		{ 
    			setPropertyNames(new String[] { 
    					IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE, 
    					ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
    					IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT, 
    					ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT}); 
    		}

    		@Override
    		public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
    			Boolean activeFormatChanged = (Boolean)properties.get(
    					ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
    			Boolean activeChanged = (Boolean)properties.get(
    					ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE);
    			return  Boolean.TRUE.equals(activeChanged) && !Boolean.TRUE.equals(activeFormatChanged);
    		}
    	}; 

    	if ( fPreferenceChangeListener != null ) {
    		DebugUITools.getPreferenceStore().removePropertyChangeListener(fPreferenceChangeListener);
    	}

    	fPreferenceChangeListener = new IPropertyChangeListener() {
    		public void propertyChange(PropertyChangeEvent event) {
    			if ( event.getProperty().equals(IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND) ) {
    				columnIdValueBackground.setBackground(DebugUITools.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB());
    			}
    		}
    	};
    	
    	DebugUITools.getPreferenceStore().addPropertyChangeListener(fPreferenceChangeListener);
       
        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();

        // The name column consists of the register name.  
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__NAME,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForRegisterVM.RegisterVMNode_Name_column__text_format, 
                    new String[] { PROP_NAME }),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER)),
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // The description column contains a brief description of the register. 
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__DESCRIPTION,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForRegisterVM.RegisterVMNode_Description_column__text_format, 
                    new String[] { IRegisterVMConstants.PROP_DESCRIPTION }),
                    new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // In the type column add information about register read/write/fload flags.
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__TYPE,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForRegisterVM.RegisterVMNode_Type_column__text_format, 
                    new String[] { 
                        IRegisterVMConstants.PROP_IS_FLOAT, 
                        IRegisterVMConstants.PROP_IS_READABLE, 
                        IRegisterVMConstants.PROP_IS_READONCE, 
                        IRegisterVMConstants.PROP_IS_WRITEABLE, 
                        IRegisterVMConstants.PROP_IS_WRITEONCE
                        }) 
                {
                    @Override
                    public void updateAttribute(ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties) {
                        Object[] messageAttrs = constructTypeObjects( properties );
                        try {
                            update.setLabel(getMessageFormat().format(messageAttrs, new StringBuffer(), null).toString(), columnIndex);
                        } catch (IllegalArgumentException e) {
                            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, 0, "Failed formatting a message for column " + columnIndex + ", for update " + update, e)); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }                    
                },
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // Value column shows the value in the active value format.
        // 
        // In case of error, show the error message in the value column (instead of the usual "...".  This is needed 
        // for the expressions view, where an invalid expression entered by the user is a normal use case.
        //
        // For changed value high-lighting check the value in the active format.  But if the format itself has changed, 
        // ignore the value change.
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__VALUE,
            new LabelColumnInfo(new LabelAttribute[] { 
                new FormattedValueLabelText(), 
                new ErrorLabelText(),
                new LabelForeground(new RGB(255, 0, 0)) // TODO: replace with preference error color
                {
                    { setPropertyNames(new String[] { PROP_NAME }); }

                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        return !status.isOK();
                    }
                },
                columnIdValueBackground,
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // Expression column is visible only in the expressions view.  It shows the expression string that the user 
        // entered.  Expression column images are the same as for the name column.
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__EXPRESSION,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForRegisterVM.RegisterVMNode_Expression_column__text_format, 
                    new String[] { PROP_ELEMENT_EXPRESSION }),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER)),
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
            new LabelColumnInfo(new LabelAttribute[] { 
                new FormattedValueLabelText(
                    MessagesForRegisterVM.RegisterVMNode_No_columns__text_format, 
                    new String[] { PROP_NAME, 
                    		IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE})
                {
					@Override
                    public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                        Boolean showTypeNames = (Boolean) properties.get(PROP_REGISTER_SHOW_TYPE_NAMES);
                        return 
                            showTypeNames != null && 
                           !showTypeNames.booleanValue() &&
                            super.isEnabled(status, properties);
                    }
                },
                new FormattedValueLabelText(
                        MessagesForRegisterVM.RegisterVMNode_No_columns__text_format_with_type, 
                        new String[] { PROP_NAME, 
                        		IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                        		IRegisterVMConstants.PROP_IS_FLOAT, 
                                IRegisterVMConstants.PROP_IS_READABLE, 
                                IRegisterVMConstants.PROP_IS_READONCE, 
                                IRegisterVMConstants.PROP_IS_WRITEABLE, 
                                IRegisterVMConstants.PROP_IS_WRITEONCE,
                                PROP_REGISTER_SHOW_TYPE_NAMES})
                    {
                    	@Override
                    	public void updateAttribute(ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties) {
                    		Object[] messageAttrs = constructTypeObjects( properties );
                    		Object[] combinedAttrs = new Object[ messageAttrs.length + 2 ];
                    		combinedAttrs[0] = super.getPropertyValue(PROP_NAME, status, properties);
                    		combinedAttrs[1] = super.getPropertyValue(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE, status, properties);
                    		for ( int idx = 0 ; idx < messageAttrs.length; idx ++ ) {
                    			combinedAttrs[ idx + 2 ] = messageAttrs[ idx ];
                    		}
                    		try {
                    			update.setLabel(getMessageFormat().format(combinedAttrs, new StringBuffer(), null).toString(), columnIndex);
                    		} catch (IllegalArgumentException e) {
                    			update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, 0, "Failed formatting a message for column " + columnIndex + ", for update " + update, e)); //$NON-NLS-1$ //$NON-NLS-2$
                    		}
                    	}
                    	
                    	@Override
                        public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                            Boolean showTypeNames = (Boolean) properties.get(PROP_REGISTER_SHOW_TYPE_NAMES);
                            return 
                                showTypeNames != null && 
                                showTypeNames.booleanValue() &&
                                super.isEnabled(status, properties);
                        }
                    },
                new ErrorLabelText(
                    MessagesForRegisterVM.RegisterVMNode_No_columns__Error__text_format,
                    new String[] { PROP_NAME }),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_REGISTER)),
                new LabelForeground(
                    DebugUITools.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR).getRGB())
                {
                    { 
                        setPropertyNames(new String[] { 
                            IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE, 
                            ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                            IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT, 
                            ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT}); 
                    }
    
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        Boolean activeFormatChanged = (Boolean)properties.get(
                            ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
                        Boolean activeChanged = (Boolean)properties.get(
                            ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE);
                        return  Boolean.TRUE.equals(activeChanged) && !Boolean.TRUE.equals(activeFormatChanged);
                    }
                },
                new StaleDataLabelBackground(),
                new VariableLabelFont(),
            }));
        
        return provider;
    }
    
    @Override
    public String toString() {
        return "RegisterVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    protected SyncRegisterDataAccess getSyncRegisterDataAccess() {
        return fSyncRegisterDataAccess;
    }

    /**
     * @since 1.1
     */
    public IWatchExpressionFactoryAdapter2 getWatchExpressionFactory() {
    	if ( fRegisterExpressionFactory == null ) {
    		fRegisterExpressionFactory = new RegisterExpressionFactory();
    	}
    	return fRegisterExpressionFactory;
    }
        
    public void update(final ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }

    /**
     * @see IElementPropertiesProvider#update(IPropertiesUpdate[])
     * 
     * @since 2.0
     */    
    public void update(final IPropertiesUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updatePropertiesInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (IPropertiesUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }
    
    //
    //  @param return-value Boolean.TRUE  --> Show Types ICON is     selected/depressed
    //  @param return-value Boolean.FALSE --> Show Types ICON is not selected/depressed
    //
	private Boolean getShowTypeNamesState( IPresentationContext context ) {
      Boolean attribute = (Boolean) context.getProperty(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES);
      
      if (attribute != null) {
    	  return attribute;
      }
      
      return Boolean.FALSE;
    }
    
    /**
     * @since 2.0
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void updatePropertiesInSessionThread(final IPropertiesUpdate[] updates) {
        IRegisters service = getServicesTracker().getService(IRegisters.class, null);

		// Create a counting request monitor to coordinate various activities
		// on the updated objects. Though the update objects will be given to
		// various ViewerDataRequestMonitors, such monitors must make sure to
		// not mark the update objects complete. That needs to be left to the
		// following monitor.
        final CountingRequestMonitor countingRm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), null) {
            @Override
            protected void handleCompleted() {
                for (final IPropertiesUpdate update : updates) {
                    update.done();
                }
            };
        };
        int count = 0;
        
        if (service != null) {
            FormattedValueVMUtil.updateFormattedValues(updates, service, IRegisterDMContext.class, countingRm);
            count++;
        }
        
        for (final IPropertiesUpdate update : updates) {
            IExpression expression = (IExpression)DebugPlugin.getAdapter(update.getElement(), IExpression.class);
            if (expression != null) {
                update.setProperty(AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION, expression.getExpressionText());
            }
            
            final IRegisterDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IRegisterDMContext.class);
            if (dmc == null || service == null) {
            	handleFailedUpdate(update);
                continue;
            }
            
            // Capture the current "Show Type Names" ICON state in case there are no columns.
            if (update.getProperties().contains(PROP_REGISTER_SHOW_TYPE_NAMES)) {
            	update.setProperty(PROP_REGISTER_SHOW_TYPE_NAMES, getShowTypeNamesState(update.getPresentationContext()));
            }
            
            service.getRegisterData(
                dmc,             
                // Use the ViewerDataRequestMonitor in order to propagate the update's cancel request. Use an immediate 
                // executor to avoid the possibility of a rejected execution exception.
                new ViewerDataRequestMonitor<IRegisterDMData>(getSession().getExecutor(), update) { 
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            fillRegisterDataProperties(update, getData());
                        } else {
                            update.setStatus(getStatus());
                        }
                        countingRm.done();
                        
						// Note: we must not call the update's done method
                    }
                });        
            count++;
        }
        countingRm.setDoneCount(count);
    }

    /**
     * @since 2.0
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void fillRegisterDataProperties(IPropertiesUpdate update, IRegisterDMData data) 
    {
        update.setProperty(PROP_NAME, data.getName());
        update.setProperty(IRegisterVMConstants.PROP_DESCRIPTION, data.getDescription());
        update.setProperty(IRegisterVMConstants.PROP_IS_FLOAT, data.isFloat());
        update.setProperty(IRegisterVMConstants.PROP_IS_READABLE, data.isReadable());
        update.setProperty(IRegisterVMConstants.PROP_IS_READONCE, data.isReadOnce());
        update.setProperty(IRegisterVMConstants.PROP_IS_WRITEABLE, data.isWriteable());
        update.setProperty(IRegisterVMConstants.PROP_IS_WRITEONCE, data.isWriteOnce());
        
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate[])
     */
    @Override
    public void update(IHasChildrenUpdate[] updates) {
        // As an optimization, always indicate that register groups have 
        // children.
        for (IHasChildrenUpdate update : updates) {
            update.setHasChilren(true);
            update.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#updateElementsInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate)
     */
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        
        IRegisters regService = getServicesTracker().getService(IRegisters.class);
        
        if ( regService == null ) {
        	handleFailedUpdate(update);
            return;
        }
        
        regService.getRegisters(
            createCompositeDMVMContext(update),
            new ViewerDataRequestMonitor<IRegisterDMContext[]>(getSession().getExecutor(), update) { 
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }
            });            
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#createVMContext(org.eclipse.cdt.dsf.datamodel.IDMContext)
     */
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new RegisterVMC(dmc);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.IVMNode#getDeltaFlags(java.lang.Object)
     */
    public int getDeltaFlags(Object e) {
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IRegistersChangedDMEvent ||
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) ) 
        {
            return IModelDelta.CONTENT;
        } 
        
        if (e instanceof IRegisterChangedDMEvent) {
            return IModelDelta.STATE;
        }
        
        return IModelDelta.NO_CHANGE;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.IVMNode#buildDelta(java.lang.Object, org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, int, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        // The following events can affect any register's values, 
        // refresh the contents of the parent element (i.e. all the registers). 
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IRegistersChangedDMEvent ||
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) ) 
        {
            // Create a delta that the whole register group has changed.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        
        if (e instanceof IRegisterChangedDMEvent) {
            parentDelta.addNode( createVMContext(((IRegisterChangedDMEvent)e).getDMContext()), IModelDelta.STATE );
        } 
        
        rm.done();
    }

    /**
     * Expected format: GRP( GroupName ).REG( RegisterName )
     *              or: $RegisterName
     */
    public boolean canParseExpression(IExpression expression) {
        return parseExpressionForRegisterName(expression.getExpressionText()) != null;
    }
    
    private String parseExpressionForRegisterName(String expression) {
    	if (expression.startsWith("GRP(")) { //$NON-NLS-1$
    		/*
    		 * Get the group portion.
    		 */
    		int startIdx = "GRP(".length(); //$NON-NLS-1$
            int endIdx = expression.indexOf(')', startIdx);
            if ( startIdx == -1 || endIdx == -1 ) {
            	return null;
            }
            String remaining = expression.substring(endIdx+1);
            if ( ! remaining.startsWith(".REG(") ) { //$NON-NLS-1$
                return null;
            }
            
            /*
             * Get the register portion.
             */
            startIdx = ".REG(".length(); //$NON-NLS-1$
            endIdx = remaining.indexOf(')', startIdx);
            if ( startIdx == -1 || endIdx == -1 ) {
            	return null;
            }
            String regName = remaining.substring(startIdx,endIdx);
            return regName.trim();
        }
    	else if ( expression.startsWith("$") ) { //$NON-NLS-1$
    		/*
    		 * At this point I am leaving this code here to represent the register case. To do this
    		 * correctly would be to use the findRegister function and upgrade the register service
    		 * to deal with registers that  do not have a specified group parent context.  I do not
    		 * have the time for this right now.  So by saying we do not handle this the Expression
    		 * VM node will take it and pass it to the debug engine  as a generic expression.  Most
    		 * debug engines ( GDB included )  have an inherent knowledge  of the core registers as
    		 * part of their expression evaluation  and will respond with a flat value for the reg.
    		 * This is not totally complete in that you should be able to express  a register which
    		 * has bit fields for example and the bit fields should be expandable in the expression
    		 * view. With this method it will just appear to have a single value and no sub-fields.
    		 * I will file a defect/enhancement  for this to mark it.  This comment will act as the
    		 * place-holder for the future work.
    		 */
    		return null;
    	}
    	
        return null;
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
        final IRegisterDMContext dmc = DMContexts.getAncestorOfType(((IDMVMContext)element).getDMContext(), IRegisterDMContext.class);
        if (dmc == null) {
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        final String regName = parseExpressionForRegisterName(expression.getExpressionText());
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    IRegisters registersService = getServicesTracker().getService(IRegisters.class);
                    if (registersService != null) {
                        registersService.getRegisterData(
                            dmc, 
                            new DataRequestMonitor<IRegisterDMData>(ImmediateExecutor.getInstance(), rm) {
                                @Override
                                protected void handleSuccess() {
                                    rm.setData( getData().getName().equals(regName) );
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
        if (element instanceof RegisterVMC) {
            ((RegisterVMC)element).setExpression(expression);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode#getDeltaFlagsForExpression(org.eclipse.debug.core.model.IExpression, java.lang.Object)
     */
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        if ( event instanceof IRegisterChangedDMEvent ||
             event instanceof IMemoryChangedEvent ||
             (event instanceof PropertyChangeEvent && 
               ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) )
        {
            return IModelDelta.STATE;
        }
        
        if (event instanceof IRegistersChangedDMEvent ||
            event instanceof ISuspendedDMEvent)
        {
            return IModelDelta.CONTENT;
        }

        return IModelDelta.NO_CHANGE;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpression(org.eclipse.debug.core.model.IExpression, int, java.lang.Object, org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, org.eclipse.jface.viewers.TreePath, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    public void buildDeltaForExpression(IExpression expression, int elementIdx, Object event, VMDelta parentDelta, 
        TreePath path, RequestMonitor rm) 
    {
        // If the register definition has changed, refresh all the 
        // expressions in the expression manager.  This is because some 
        // expressions that were previously invalid, may now represent new 
        // registers.
        if (event instanceof IRegistersChangedDMEvent) {
        	parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }

        // Always refresh the contents of the view upon suspended event.
        if (event instanceof ISuspendedDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }         

        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionVMNode#buildDeltaForExpressionElement(java.lang.Object, int, java.lang.Object, org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta, final RequestMonitor rm) 
    {
        // The following events can affect register values, refresh the state 
        // of the expression. 
        if ( event instanceof IRegisterChangedDMEvent ||
             event instanceof IMemoryChangedEvent ||
             (event instanceof PropertyChangeEvent && 
                ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) )
        {
            parentDelta.addNode(element, IModelDelta.STATE);
        } 

        rm.done();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellEditor(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String, java.lang.Object, org.eclipse.swt.widgets.Composite)
     */
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 
        else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
          /*
           *   See if the register is writable and if so we will created a
           *   cell editor for it.
           */
          IRegisterDMData regData = getSyncRegisterDataAccess().readRegister(element);

          if ( regData != null && regData.isWriteable() ) {
              return new TextCellEditor(parent);
          }
      }
      return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor#getCellModifier(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return new RegisterCellModifier( getDMVMProvider(), getSyncRegisterDataAccess() );
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    private final String MEMENTO_NAME = "REGISTER_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        for ( final IElementCompareRequest request : requests ) {
            final IRegisterDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IRegisterDMContext.class);
            final String mementoName = request.getMemento().getString(MEMENTO_NAME);
            if (regDmc == null || mementoName == null) {
                request.done();
                continue;
            }
            
            //  Now go get the model data for the single register group found.
            try {
                getSession().getExecutor().execute(new DsfRunnable() {
                    public void run() {
                        final IRegisters regService = getServicesTracker().getService(IRegisters.class);
                        if ( regService != null ) {
                            regService.getRegisterData(
                                regDmc, 
                                new DataRequestMonitor<IRegisterDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.setEqual( mementoName.equals( "Register." + getData().getName() ) ); //$NON-NLS-1$
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
            final IRegisterDMContext regDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IRegisterDMContext.class);
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
                            regService.getRegisterData(
                                regDmc, 
                                new DataRequestMonitor<IRegisterDMData>(regService.getExecutor(), null) {
                                    @Override
                                    protected void handleCompleted() {
                                        if ( getStatus().isOK() ) {
                                            // Now make sure the register group is the one we want.
                                            request.getMemento().putString(MEMENTO_NAME, "Register." + getData().getName()); //$NON-NLS-1$
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
