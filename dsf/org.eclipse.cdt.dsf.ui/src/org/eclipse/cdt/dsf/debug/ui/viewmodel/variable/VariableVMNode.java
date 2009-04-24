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

package org.eclipse.cdt.dsf.debug.ui.viewmodel.variable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.ErrorLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionUpdate;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.IFormattedValueVMContext;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;

@SuppressWarnings("restriction")
public class VariableVMNode extends AbstractExpressionVMNode 
                            implements IElementEditor, IElementLabelProvider, IElementPropertiesProvider, IElementMementoProvider 
{
    /**
     * @since 2.0
     */    
    private static final String PROP_VARIABLE_TYPE_NAME = "variable_type_name";  //$NON-NLS-1$

    /**
     * @since 2.0
     */    
    private static final String PROP_VARIABLE_BASIC_TYPE = "variable_basic_type";  //$NON-NLS-1$

    /**
     * @since 2.0
     */    
    private static final String PROP_VARIABLE_ADDRESS = "variable_address";  //$NON-NLS-1$
    
    /**
     * @since 2.0
     */
    private static final String PROP_VARIABLE_SHOW_TYPE_NAMES = "variable_show_type_names"; //$NON-NLS-1$
    
    /**
     * @since 2.0
     */    
    private static final String PROP_VARIABLE_ADDRESS_CHANGED = ICachingVMProvider.PROP_IS_CHANGED_PREFIX + PROP_VARIABLE_ADDRESS;

    private final SyncVariableDataAccess fSyncVariableDataAccess;
    
    /**
     * The label provider delegate.  This VM node will delegate label updates to this provider
     * which can be created by sub-classes. 
     *  
     * @since 2.0
     */    
    private IElementLabelProvider fLabelProvider;

    public class VariableExpressionVMC extends DMVMContext implements IFormattedValueVMContext  {
        
        private IExpression fExpression;
        
        public VariableExpressionVMC(IDMContext dmc) {
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
                return fVariableExpressionFactory;
            } else {
                return super.getAdapter(adapter);
            }
        }
        
        @Override
        public boolean equals(Object other) {
            if (other instanceof VariableExpressionVMC && super.equals(other)) {
                VariableExpressionVMC otherGroup = (VariableExpressionVMC)other;
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
    
    protected class VariableExpressionFactory implements IWatchExpressionFactoryAdapter2 {

        public boolean canCreateWatchExpression(Object element) {
            return element instanceof VariableExpressionVMC;
        }

        public String createWatchExpression(Object element) throws CoreException {
            
            VariableExpressionVMC exprVmc = (VariableExpressionVMC) element;
            
            IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(exprVmc.getDMContext(), IExpressionDMContext.class);
            if (exprDmc != null) {
                return exprDmc.getExpression();
            }

            return null;     
        }
    }

    final protected VariableExpressionFactory fVariableExpressionFactory = new VariableExpressionFactory();

    public VariableVMNode(AbstractDMVMProvider provider, DsfSession session, 
        SyncVariableDataAccess syncVariableDataAccess) 
    {
        super(provider, session, IExpressions.IExpressionDMContext.class);
        fSyncVariableDataAccess = syncVariableDataAccess;
        fLabelProvider = createLabelProvider();
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

        // 
        // Create label image objects which are used in more than one column. 
        //
        
        // Pointer image is used for variable and function pointers.
        LabelImage pointerLabelImage = new LabelImage(CDebugImages.DESC_OBJS_VARIABLE_POINTER) {
            { setPropertyNames(new String[] { PROP_VARIABLE_BASIC_TYPE }); }

            @Override
            public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                String type = (String)properties.get(PROP_VARIABLE_BASIC_TYPE);
                return IExpressionDMData.BasicType.pointer.name().equals(type) ||
                    IExpressionDMData.BasicType.function.name().equals(type);
            };
        };
        
        // Aggregate image is used for array, struct, etc.
        LabelImage aggregateLabelImage = new LabelImage(CDebugImages.DESC_OBJS_VARIABLE_AGGREGATE) {
            { setPropertyNames(new String[] { PROP_VARIABLE_BASIC_TYPE }); }

            @Override
            public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                String type = (String)properties.get(PROP_VARIABLE_BASIC_TYPE);
                return IExpressionDMData.BasicType.array.name().equals(type) ||
                    IExpressionDMData.BasicType.composite.name().equals(type);
            };
        };
        
        // Simple variable image is used for all other types, except when there is no type specified.
        LabelImage simpleLabelImage = new LabelImage(CDebugImages.DESC_OBJS_VARIABLE_SIMPLE) {
            { setPropertyNames(new String[] { PROP_VARIABLE_BASIC_TYPE }); }
            
            @Override
            public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                String type = (String)properties.get(PROP_VARIABLE_BASIC_TYPE);
                return type != null;
            };
        };
        
        // The name column consists of the expression name.  The name column image depends on the variable type. 
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__NAME,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForVariablesVM.VariableVMNode_Name_column__text_format, 
                    new String[] { PROP_NAME }),
                pointerLabelImage,
                aggregateLabelImage, 
                simpleLabelImage,
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // Expression column is visible only in the expressions view.  It shows the expression string that the user 
        // entered.  Expression column images are the same as for the name column.
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__EXPRESSION,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForVariablesVM.VariableVMNode_Expression_column__text_format, 
                    new String[] { PROP_ELEMENT_EXPRESSION }),
                pointerLabelImage,
                aggregateLabelImage, 
                simpleLabelImage,
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));
        
        // Type column only contains the type name.
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__TYPE,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForVariablesVM.VariableVMNode_Type_column__text_format, 
                    new String[] { PROP_VARIABLE_TYPE_NAME }),
                new LabelText( MessagesForVariablesVM.VariableVMNode_Type_column__Error__text_format, new String[] {}),
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));
            
        // Value column is more complicated:
        // - If a STRING value format is supported.  Then the value label consists of the active format label followed 
        //   by the string format.
        // - If the STRIGN value format is not supported.  Then only show the active value format.  The GDB reference
        //   implementation currently does not support the string format, but by default it does append extra 
        //   information to the value label itself.
        // 
        // In case of error, show the error message in the value column (instead of the usual "...".  This is needed 
        // for the expressions view, where an invalid expression entered by the user is a normal use case.  
        // 
        // For changed value high-lighting check both the string value and the value in the active format.  But,  
        // ignore the active format value change if the format itself has changed.
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__VALUE,
            new LabelColumnInfo(new LabelAttribute[] { 
                new FormattedValueLabelText(
                    MessagesForVariablesVM.VariableVMNode_Value_column__text_format, 
                    new String[] { 
                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                        FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT),
                        IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS})
                {
                    @Override                    
                    public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                        String[] formatIds = 
                            (String[])properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS);
                        String activeFormat = (String)properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
                        return 
                            !IFormattedValues.STRING_FORMAT.equals(activeFormat) &&
                            formatIds != null &&
                            Arrays.asList(formatIds).contains(IFormattedValues.STRING_FORMAT) &&
                            super.isEnabled(status, properties);
                    }
                },
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
                // 
                new LabelBackground(
                    DebugUITools.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB()) 
                {
                    { 
                        setPropertyNames(new String[] { 
                            FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT),
                            ICachingVMProvider.PROP_IS_CHANGED_PREFIX + FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT),
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
                        Boolean stringChanged = (Boolean)properties.get(
                            ICachingVMProvider.PROP_IS_CHANGED_PREFIX + FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT));
                        return Boolean.TRUE.equals(stringChanged) || 
                            ( Boolean.TRUE.equals(activeChanged) && !Boolean.TRUE.equals(activeFormatChanged));
                    };                    
                },
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // Address column shows the variable's address.  It is highlighted with the change background color when the 
        // address value changes. 
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__ADDRESS,
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(
                    MessagesForVariablesVM.VariableVMNode_Address_column__text_format, 
                    new String[] { PROP_VARIABLE_ADDRESS }),
                new LabelText(MessagesForVariablesVM.VariableVMNode_Address_column__Error__text_format, new String[] {}), 
                new LabelBackground(
                    DebugUITools.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB()) 
                {
                    { setPropertyNames(new String[] { PROP_VARIABLE_ADDRESS, PROP_VARIABLE_ADDRESS_CHANGED}); }

                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        Boolean changed = (Boolean)properties.get(PROP_VARIABLE_ADDRESS_CHANGED);
                        return Boolean.TRUE.equals(changed);
                    };                    
                },
                new StaleDataLabelForeground(),
                new VariableLabelFont(),
            }));

        // Description column is shown in the expression view, but is not supported for variables. 
        provider.setColumnInfo(
            IDebugVMConstants.COLUMN_ID__DESCRIPTION,

            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(MessagesForVariablesVM.VariableVMNode_Description_column__text_format, new String[] {}),
                new VariableLabelFont(),
            }));

        // Configure the case where there are no columns visible.  It basically combines the name and the value columns only.
        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS,
            new LabelColumnInfo(new LabelAttribute[] { 
                new FormattedValueLabelText(
                    MessagesForVariablesVM.VariableVMNode_NoColumns_column__text_format, 
                    new String[] { 
                        PROP_NAME, 
                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                        FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT),
                        IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS, 
                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT,
                        IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE,
                        PROP_VARIABLE_SHOW_TYPE_NAMES}) 
                {
					@Override
                    public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                        Boolean showTypeNames = (Boolean) properties.get(PROP_VARIABLE_SHOW_TYPE_NAMES);
                        String[] formatIds = (String[]) properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS);
                        String activeFormat = (String) properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
                        return 
                            showTypeNames != null && 
                           !showTypeNames.booleanValue() &&
                           !IFormattedValues.STRING_FORMAT.equals(activeFormat) &&
                            formatIds != null &&
                            Arrays.asList(formatIds).contains(IFormattedValues.STRING_FORMAT) &&
                            super.isEnabled(status, properties);
                    }
                },
                new FormattedValueLabelText(
                    MessagesForVariablesVM.VariableVMNode_NoColumns_column__No_string__text_format, 
                    new String[] { 
                        PROP_NAME, 
                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                        IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS,
                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT,
                        IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE,
                        PROP_VARIABLE_SHOW_TYPE_NAMES})
                {
                	@Override
                	public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                		Boolean showTypeNames = (Boolean) properties.get(PROP_VARIABLE_SHOW_TYPE_NAMES);
                		return
                		     showTypeNames != null && 
                		    !showTypeNames.booleanValue() &&
                             super.isEnabled(status, properties);
                	}
                },
                new FormattedValueLabelText(
                        MessagesForVariablesVM.VariableVMNode_NoColumns_column__text_format_with_type, 
                        new String[] { 
                            PROP_NAME, 
                            IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                            FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT),
                            PROP_VARIABLE_TYPE_NAME,
                            IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS, 
                            IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT,
                            IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE,
                            PROP_VARIABLE_SHOW_TYPE_NAMES}) 
                    {
    					@Override
                        public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                            Boolean showTypeNames = (Boolean) properties.get(PROP_VARIABLE_SHOW_TYPE_NAMES);
                            String[] formatIds = (String[]) properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS);
                            String activeFormat = (String) properties.get(IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT);
                            return 
                                showTypeNames != null && 
                                showTypeNames.booleanValue() &&
                               !IFormattedValues.STRING_FORMAT.equals(activeFormat) &&
                                formatIds != null &&
                                Arrays.asList(formatIds).contains(IFormattedValues.STRING_FORMAT) &&
                                super.isEnabled(status, properties);
                        }
                    },
                    new FormattedValueLabelText(
                        MessagesForVariablesVM.VariableVMNode_NoColumns_column__No_string__text_format_with_type, 
                        new String[] { 
                            PROP_NAME, 
                            IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                            PROP_VARIABLE_TYPE_NAME,
                            IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS,
                            IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT,
                            IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE,
                            PROP_VARIABLE_SHOW_TYPE_NAMES})
                    {
                    	@Override
                    	public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                    		Boolean showTypeNames = (Boolean) properties.get(PROP_VARIABLE_SHOW_TYPE_NAMES);
                    		return
               		            showTypeNames != null && 
               		            showTypeNames.booleanValue() &&
                                super.isEnabled(status, properties);
                    	}
                    },
                new ErrorLabelText(
                    MessagesForVariablesVM.VariableVMNode_NoColumns_column__Error__text_format, 
                    new String[] { PROP_NAME }),
                pointerLabelImage,
                aggregateLabelImage, 
                simpleLabelImage,
                new LabelForeground(new RGB(255, 0, 0)) // TODO: replace with preference error color
                {
                    { setPropertyNames(new String[] { PROP_NAME }); }

                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        return !status.isOK();
                    }
                },
                new LabelForeground(
                    DebugUITools.getPreferenceColor(IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR).getRGB())
                {
                    { 
                        setPropertyNames(new String[] { 
                            FormattedValueVMUtil.getPropertyForFormatId(IFormattedValues.STRING_FORMAT), 
                            IDebugVMConstants.PROP_IS_STRING_FORMAT_VALUE_CHANGED, 
                            IDebugVMConstants.PROP_IS_ACTIVE_FORMATTED_VALUE_CHANGED}); 
                    }

                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        Boolean stringChanged = 
                            (Boolean)properties.get(IDebugVMConstants.PROP_IS_STRING_FORMAT_VALUE_CHANGED);
                        Boolean activeChanged = 
                            (Boolean)properties.get(IDebugVMConstants.PROP_IS_ACTIVE_FORMATTED_VALUE_CHANGED);
                        return Boolean.TRUE.equals(stringChanged) || Boolean.TRUE.equals(activeChanged);
                    };                    
                },
                new StaleDataLabelBackground(),
                new VariableLabelFont(),
            }));
        
        return provider;
    }
    
    @Override
    public String toString() {
        return "VariableVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new VariableExpressionVMC(dmc);
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
    // We encapsulate  the determination  of the state  of the "Show Type Names" ICON
    // selection to this routine. This attribute information is not readily available
    // from standard Interface definitions. So we use reflection to get the info. But
    // in the future this will change and then we can just change this routine.  This
    // really should just be as simple as the code below,  which could be done inline
    // once it comes to pass.
    // 
    //  Boolean attribute = (Boolean) context.getProperty(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES);
    //	if (attribute != null) {
    //		return attribute;
    //	}
    //	return Boolean.FALSE;
    //
    //  @param return-value Boolean.TRUE  --> Show Types ICON is     selected/depressed
    //  @param return-value Boolean.FALSE --> Show Types ICON is not selected/depressed
    //
    
    @SuppressWarnings("unchecked")
	private Boolean getShowTypeNamesState( IPresentationContext context ) {
    	// This is fairly ugly stuff.  It should be the case  that the "getPropery()"
    	// method of the presentation context  should contain this attribute,  but it
    	// does not. So we have to go mining for it. As it turns out the presentation
    	// context is actually an implementation class  DebugModelPresentationContext
    	// from which you can get the IDebugModelPresentation instance. In turn  this
    	// is a DelegatingModelPresentation  instance which allows  you to obtain the
    	// current set of internal attributes,  which contains  the current state  of 
    	// "Show Type Names" ICON selection.
    	Class<? extends IPresentationContext> contextClass = context.getClass();
    	Method contextMethod = null;
    	try {
    		// Will return the instance of the class "DebugModelPresentationContext"
    		// if this is indeed the implementation we are expecting.
    		contextMethod = contextClass.getMethod( "getModelPresentation" , new Class[] {} ); //$NON-NLS-1$
    	}
    	catch ( Exception ex ) {}
    		
    	IDebugModelPresentation debugModelPresentation = null ;
    	if (contextMethod != null) {
    		try {
    			// We invoke the "getModelPresntation" method to get the actual instance
    			// of the "DelegatingModelPresentation".
    			debugModelPresentation = (IDebugModelPresentation) contextMethod.invoke(context , new Object[0]);
    		}
    		catch ( Exception e ) {}
    	}
    	
    	if (debugModelPresentation != null) {
    		Class<? extends IDebugModelPresentation> presentationClass = debugModelPresentation.getClass();
        	Method presentationMethod = null;
        	try {
        		// The "getAttributeMethod" is a public method which returns the internal
        		// attributes. These should be part of the Interface but they are not. So
        		// we get them through this available method.
        		presentationMethod = presentationClass.getMethod( "getAttributeMap" , new Class[] {} ); //$NON-NLS-1$
        	}
        	catch ( Exception ex ) {}
        	
        	HashMap attributeMap = null;
        	if (presentationMethod != null) {
        		try {
        			// Now get the actual HashMap attribute list so we can see if there is
        			// state information about the "Show Type Names" ICON.
        			attributeMap = (HashMap) presentationMethod.invoke(debugModelPresentation , new Object[0]);
        		}
        		catch ( Exception e ) {}
        	}
        	
        	if (attributeMap != null) {
        		// This attribute ( which is globally defined ) reflect the state of the ICON.
        		// If is exists is contains the current state.  Non-existence would mean  that
        		// the ICON has never been selected or changed.  This is so at  the very start
        		// when the view is first brought up.
        		Boolean attribute = (Boolean) attributeMap.get(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES);
        		
        		if (attribute != null) {
        			return attribute;
        		}
        	}
    	}
    	
    	// Could not get to the one of the methods needs to determine the state of the
    	// ICON or we could not get the attribute.  Assume we are not showing the TYPE 
    	// NAMES.
    	return Boolean.FALSE;
    }
    
    /**
     * @since 2.0
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void updatePropertiesInSessionThread(final IPropertiesUpdate[] updates) {
        IExpressions service = getServicesTracker().getService(IExpressions.class, null);

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
            FormattedValueVMUtil.updateFormattedValues(updates, service, IExpressionDMContext.class, countingRm);
            count++;
        }
        
        for (final IPropertiesUpdate update : updates) {
            IExpression expression = (IExpression)DebugPlugin.getAdapter(update.getElement(), IExpression.class);
            if (expression != null) {
            	update.setProperty(AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION, expression.getExpressionText());
            }
            
            // Capture the current "Show Type Names" ICON state in case there are no columns.
            if (update.getProperties().contains(PROP_VARIABLE_SHOW_TYPE_NAMES)) {
            	update.setProperty(PROP_VARIABLE_SHOW_TYPE_NAMES, getShowTypeNamesState(update.getPresentationContext()));
            }
            
            IExpressionDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExpressions.IExpressionDMContext.class);
            if ( dmc == null || service == null) {
                update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_STATE,  "Invalid context or service not available.", null)); //$NON-NLS-1$
                continue;
            }
            
            if (update.getProperties().contains(PROP_NAME) ||
                update.getProperties().contains(PROP_VARIABLE_TYPE_NAME) || 
                update.getProperties().contains(PROP_VARIABLE_BASIC_TYPE)) 
            {
                service.getExpressionData(
                    dmc, 
                    // Use the ViewerDataRequestMonitor in order to propagate the update's cancel request. Use an immediate 
                    // executor to avoid the possibility of a rejected execution exception.
                    new ViewerDataRequestMonitor<IExpressionDMData>(ImmediateExecutor.getInstance(), update) { 
                        @Override
                        protected void handleCompleted() {
                            if (isSuccess()) {
                                fillExpressionDataProperties(update, getData());
                            } else {
                                // In case of an error fill in the expression next in the name column.
                                IExpressionDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExpressions.IExpressionDMContext.class);
                                if (dmc != null && dmc.getExpression() != null) {
                                    update.setProperty(PROP_NAME, dmc.getExpression());
                                }
                                update.setStatus(getStatus());
                            }
                            countingRm.done();
                        }
                    });

                count++;
            }
            
            if (update.getProperties().contains(PROP_VARIABLE_ADDRESS)) {
                service.getExpressionAddressData(
                    dmc,
                    // Use the ViewerDataRequestMonitor in order to propagate the update's cancel request. Use an immediate 
                    // executor to avoid the possibility of a rejected execution exception.
                    new ViewerDataRequestMonitor<IExpressionDMAddress>(ImmediateExecutor.getInstance(), update) {
                        @Override
                        protected void handleCompleted() {
                            if (isSuccess()) {
                                fillAddressDataProperties(update, getData());
                            } else if (getStatus().getCode() != IDsfStatusConstants.NOT_SUPPORTED &&
                                       getStatus().getCode() != IDsfStatusConstants.INVALID_STATE) 
                            {
                                update.setStatus(getStatus());
                            }
                            countingRm.done();
                        }
                    });
                count++;
            }
        }
        countingRm.setDoneCount(count);
    }
    
    /**
     * @since 2.0
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void fillExpressionDataProperties(IPropertiesUpdate update, IExpressionDMData data) 
    {
        update.setProperty(PROP_NAME, data.getName());
        update.setProperty(PROP_VARIABLE_TYPE_NAME, data.getTypeName());
        IExpressionDMData.BasicType type = data.getBasicType();
        if (type != null) {
            update.setProperty(PROP_VARIABLE_BASIC_TYPE, type.name());
        }
        
        //
        // If this node has an expression then it has already been filled in by the higher
        // level logic. If not then we need to supply something.  In the  previous version
        // ( pre-property based ) we supplied the name. So we will do that here also.
        //
        IExpression expression = (IExpression)DebugPlugin.getAdapter(update.getElement(), IExpression.class);
        if (expression == null) {
            update.setProperty(AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION, data.getName());
        }
    }

    /**
     *  Private data access routine which performs the extra level of data access needed to
     *  get the formatted data value for a specific register.
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void fillAddressDataProperties(IPropertiesUpdate update, IExpressionDMAddress address)
    { 
	    IExpressionDMAddress expression = address;
	    IAddress expAddress = expression.getAddress();
	    update.setProperty(PROP_VARIABLE_ADDRESS, "0x" + expAddress.toString(16)); //$NON-NLS-1$
    }
    
    public CellEditor getCellEditor(IPresentationContext context, String columnId, Object element, Composite parent) {
        if (IDebugVMConstants.COLUMN_ID__VALUE.equals(columnId)) {
            return new TextCellEditor(parent);
        }
        else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(columnId)) {
            return new TextCellEditor(parent);
        } 

        return null;
    }

    public ICellModifier getCellModifier(IPresentationContext context, Object element) {
        return new VariableCellModifier(getDMVMProvider(), fSyncVariableDataAccess);
    }
    
    public boolean canParseExpression(IExpression expression) {
    	// At this point we are going to say we will allow anything as an expression.
    	// Since the evaluation  of VM Node implementations searches  in the order of
    	// registration  and we always make sure we register the VariableVMNode last,
    	// we know that the other possible handlers have passed the expression by. So
    	// we are going to say OK and let the expression evaluation of whatever debug
    	// backend is connected to decide. This does not allow us to put up any  good
    	// diagnostic error message ( instead the error will come from the backend ).
    	// But it does allow for the most flexibility
    	
    	return true;
    }
    
    @Override
    public void update(final IExpressionUpdate update) {
        try {
            getSession().getExecutor().execute(new Runnable() {
                public void run() {
                    final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
                    if (expressionService != null) {
                        IExpressionDMContext expressionDMC = expressionService.createExpression(
                            createCompositeDMVMContext(update), 
                            update.getExpression().getExpressionText());
                        VariableExpressionVMC variableVmc = new VariableExpressionVMC(expressionDMC);
                        variableVmc.setExpression(update.getExpression());
                        
                        update.setExpressionElement(variableVmc);
                        update.done();
                    } else {
                        handleFailedUpdate(update);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            handleFailedUpdate(update);
        }
    }
    
    
    @Override
    protected void handleFailedUpdate(IViewerUpdate update) {
        if (update instanceof IExpressionUpdate) {
            update.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Update failed", null)); //$NON-NLS-1$
            update.done();
        } else {
            super.handleFailedUpdate(update);
        }
    }
    @Override
    protected void associateExpression(Object element, IExpression expression) {
        if (element instanceof VariableExpressionVMC) {
            ((VariableExpressionVMC)element).setExpression(expression);
        }
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        // Get the data model context object for the current node in the hierarchy.
        
        final IExpressionDMContext expressionDMC = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExpressionDMContext.class);
        
        if ( expressionDMC != null ) {
            getSubexpressionsUpdateElementsInSessionThread( update );
        }
        else {
            getLocalsUpdateElementsInSessionThread( update );
        }
    }
    
    private void getSubexpressionsUpdateElementsInSessionThread(final IChildrenUpdate update) {

        final IExpressionDMContext expressionDMC = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExpressionDMContext.class);
        
        if ( expressionDMC != null ) {

            // Get the services we need to use.
            
            final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
            
            if (expressionService == null) {
                handleFailedUpdate(update);
                return;
            }

            final DsfExecutor dsfExecutor = getSession().getExecutor();
            
            // Call IExpressions.getSubExpressions() to get an Iterable of IExpressionDMContext objects representing
            // the sub-expressions of the expression represented by the current expression node.
            
            final DataRequestMonitor<IExpressionDMContext[]> rm =
                new ViewerDataRequestMonitor<IExpressionDMContext[]>(dsfExecutor, update) {
                    @Override
                    public void handleCompleted() {
                        if (!isSuccess()) {
                            handleFailedUpdate(update);
                            return;
                        }
                        fillUpdateWithVMCs(update, getData());
                        update.done();
                    }
            };

            // Make the asynchronous call to IExpressions.getSubExpressions().  The results are processed in the
            // DataRequestMonitor.handleCompleted() above.

            expressionService.getSubExpressions(expressionDMC, rm);
        } else {
            handleFailedUpdate(update);
        }
    }
    
    private void getLocalsUpdateElementsInSessionThread(final IChildrenUpdate update) {

        final IFrameDMContext frameDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IFrameDMContext.class);

        // Get the services we need to use.
        
        final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
        final IStack stackFrameService = getServicesTracker().getService(IStack.class);
        
        if ( frameDmc == null || expressionService == null || stackFrameService == null) {
            handleFailedUpdate(update);
            return;
        }

        final DsfExecutor dsfExecutor = getSession().getExecutor();
        
        // Call IStack.getLocals() to get an array of IVariableDMContext objects representing the local
        // variables in the stack frame represented by frameDmc.
         
        final DataRequestMonitor<IVariableDMContext[]> rm =
            new ViewerDataRequestMonitor<IVariableDMContext[]>(dsfExecutor, update) {
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }
                    
                    // For each IVariableDMContext object returned by IStack.getLocals(), call
                    // MIStackFrameService.getModelData() to get the IVariableDMData object.  This requires
                    // a MultiRequestMonitor object.
                    
                    // First, get the data model context objects for the local variables.
                    
                    IVariableDMContext[] localsDMCs = getData();
                    
                    if (localsDMCs == null) {
                        handleFailedUpdate(update);
                        return;
                    }
                    
                    if ( localsDMCs.length == 0 ) {
                        // There are no locals so just complete the request
                        update.done();
                        return;
                    }
                    
                    // Create a List in which we store the DM data objects for the local variables.  This is
                    // necessary because there is no MultiDataRequestMonitor. :)
                    
                    final List<IVariableDMData> localsDMData = new ArrayList<IVariableDMData>();
                    
                    // Create the MultiRequestMonitor to handle completion of the set of getModelData() calls.
                    
                    final MultiRequestMonitor<DataRequestMonitor<IVariableDMData>> mrm =
                        new MultiRequestMonitor<DataRequestMonitor<IVariableDMData>>(dsfExecutor, null) {
                            @Override
                            public void handleCompleted() {
                                // Now that all the calls to getModelData() are complete, we create an
                                // IExpressionDMContext object for each local variable name, saving them all
                                // in an array.

                                if (!isSuccess()) {
                                    handleFailedUpdate(update);
                                    return;
                                }
         
                                IExpressionDMContext[] expressionDMCs = new IExpressionDMContext[localsDMData.size()];
                                
                                int i = 0;
                                
                                for (IVariableDMData localDMData : localsDMData) {
                                    expressionDMCs[i++] = expressionService.createExpression(frameDmc, localDMData.getName());
                                }

                                // Lastly, we fill the update from the array of view model context objects
                                // that reference the ExpressionDMC objects for the local variables.  This is
                                // the last code to run for a given call to updateElementsInSessionThread().
                                // We can now leave anonymous-inner-class hell.

                                fillUpdateWithVMCs(update, expressionDMCs);
                                update.done();
                            }
                    };
                    
                    // Perform a set of getModelData() calls, one for each local variable's data model
                    // context object.  In the handleCompleted() method of the DataRequestMonitor, add the
                    // IVariableDMData object to the localsDMData List for later processing (see above).
                    
                    for (IVariableDMContext localDMC : localsDMCs) {
                        DataRequestMonitor<IVariableDMData> rm =
                            new ViewerDataRequestMonitor<IVariableDMData>(dsfExecutor, update) {
                                @Override
                                public void handleCompleted() {
                                    localsDMData.add(getData());
                                    mrm.requestMonitorDone(this);
                                }
                        };
                        
                        mrm.add(rm);
                        
                        stackFrameService.getVariableData(localDMC, rm);
                    }
                }
        };

        // Make the asynchronous call to IStack.getLocals().  The results are processed in the
        // DataRequestMonitor.handleCompleted() above.

        stackFrameService.getLocals(frameDmc, rm);
    }
    
    public int getDeltaFlags(Object e) {
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IExpressionChangedDMEvent ||
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) ) 
        {
            // Create a delta that the whole register group has changed.
            return IModelDelta.CONTENT;
        } 

        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(final Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {

        // The following events can affect any expression's values, 
        // refresh the contents of the parent element (i.e. all the expressions). 
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IExpressionChangedDMEvent ||
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) ) 
        {
            // Create a delta that the whole register group has changed.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 

        requestMonitor.done();
    }
    
    public int getDeltaFlagsForExpression(IExpression expression, Object event) {
        if ( event instanceof IExpressionChangedDMEvent ||
             event instanceof IMemoryChangedEvent ||
             (event instanceof PropertyChangeEvent && 
              ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) )
        {
            return IModelDelta.CONTENT;
        } 

        if (event instanceof ISuspendedDMEvent)
        {
            return IModelDelta.CONTENT;
        }

        return IModelDelta.NO_CHANGE;
    }
    
    public void buildDeltaForExpression(IExpression expression, int elementIdx, Object event, VMDelta parentDelta, 
        TreePath path, RequestMonitor rm) 
    {
        // Always refresh the contents of the view upon suspended event.
        if (event instanceof ISuspendedDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }         

        rm.done();
    }
    
    public void buildDeltaForExpressionElement(Object element, int elementIdx, Object event, VMDelta parentDelta,
        RequestMonitor rm) 
    {
        // The following events can affect expression values, refresh the state 
        // of the expression. 
        if ( event instanceof IExpressionChangedDMEvent ||
             event instanceof IMemoryChangedEvent ||
             (event instanceof PropertyChangeEvent && 
                ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE) )
        {
            parentDelta.addNode(element, IModelDelta.CONTENT);
        } 

        rm.done();
    }
    

    
    private String produceExpressionElementName( String viewName , IExpressionDMContext expression ) {
    	
    	return "Variable." + expression.getExpression(); //$NON-NLS-1$
    }

    private final String MEMENTO_NAME = "VARIABLE_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        
        for ( IElementCompareRequest request : requests ) {
        	
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            String mementoName = memento.getString(MEMENTO_NAME);
            
            if (mementoName != null) {
                if (element instanceof IDMVMContext) {
                	
                    IDMContext dmc = ((IDMVMContext)element).getDMContext();
                    
                    if ( dmc instanceof IExpressionDMContext) {
                    	
                    	String elementName = produceExpressionElementName( request.getPresentationContext().getId(), (IExpressionDMContext) dmc );
                    	request.setEqual( elementName.equals( mementoName ) );
                    } 
                }
            }
            request.done();
        }
    }
    
    public void encodeElements(IElementMementoRequest[] requests) {
    	
    	for ( IElementMementoRequest request : requests ) {
    		
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            
            if (element instanceof IDMVMContext) {

            	IDMContext dmc = ((IDMVMContext)element).getDMContext();

            	if ( dmc instanceof IExpressionDMContext) {

            		String elementName = produceExpressionElementName( request.getPresentationContext().getId(), (IExpressionDMContext) dmc );
            		memento.putString(MEMENTO_NAME, elementName);
            	} 
            }
            request.done();
        }
    }
}
