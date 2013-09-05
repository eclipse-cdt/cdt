/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.ErrorLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionUpdate;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.IRegisterVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.MessagesForRegisterVM;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.MessagesForVariablesVM;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableLabelFont;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.GDBRegisters;
import org.eclipse.cdt.dsf.gdb.service.GDBRegisters.GDBRegisterDMC;
import org.eclipse.cdt.dsf.gdb.service.GDBRegisters.GDBRegisterData;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterGroupDMC;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

/**
 * The implementation of the register node based on {@link VariableVMNode} to use GDB/MI 
 * variable objects.
 * 
 * See {@link GDBRegisters}
 */
public class GdbRegisterVMNode extends VariableVMNode {

    class GdbWatchExpressionFactory implements IWatchExpressionFactoryAdapter2 {

        @Override
		public String createWatchExpression( Object element ) throws CoreException {
            MIRegisterGroupDMC groupDMC = null;
            if ( element instanceof IDMVMContext ) {
                groupDMC = DMContexts.getAncestorOfType( ((IDMVMContext)element).getDMContext(), MIRegisterGroupDMC.class );
            }

            GDBRegisterDMC regDMC = null;
            if ( element instanceof IDMVMContext ) {
                regDMC = DMContexts.getAncestorOfType( ((IDMVMContext)element).getDMContext(), GDBRegisterDMC.class );
            }

            StringBuffer exprBuf = new StringBuffer();
            if ( groupDMC != null ) {
                exprBuf.append( String.format( "GRP( %s ).", groupDMC.getName() ) ); //$NON-NLS-1$
            }
            
            if ( regDMC != null ) {
                exprBuf.append( String.format( "REG( %s )", regDMC.getName() ) ); //$NON-NLS-1$
            }

            return exprBuf.toString();
        }

        @Override
		public boolean canCreateWatchExpression( Object variable ) {
            return ( variable instanceof GdbRegisterVMC );
        }
    }

    class GdbRegisterVMC extends VariableExpressionVMC {

        public GdbRegisterVMC( IDMContext dmc ) {
            super( dmc );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode.VariableExpressionVMC#getAdapter(java.lang.Class)
         */
        @SuppressWarnings( { "unchecked", "rawtypes" } )
        @Override
        public Object getAdapter( Class adapter ) {
            if ( adapter.isAssignableFrom(IWatchExpressionFactoryAdapter2.class) )
                return getWatchExpressionFactory();
            return super.getAdapter( adapter );
        }
    }

    private static final String PROP_REGISTER_SHOW_TYPE_NAMES = "register_show_type_names"; //$NON-NLS-1$

    LabelBackground fColumnIdValueBackground; 
    private IPropertyChangeListener fPreferenceChangeListener;
    private GdbWatchExpressionFactory fWatchExpressionFactory;

    public GdbRegisterVMNode( AbstractDMVMProvider provider, DsfSession session, SyncVariableDataAccess syncVariableDataAccess ) {
        super( provider, session, syncVariableDataAccess );
        fWatchExpressionFactory = new GdbWatchExpressionFactory();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate[])
     */
    @Override
    public void update( IHasChildrenUpdate[] updates ) {
        // As an optimization, always indicate that register groups have 
        // children.
        for( IHasChildrenUpdate update : updates ) {
            update.setHasChilren( true );
            update.done();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#update(org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.IExpressionUpdate)
     */
    @Override
    public void update( final IExpressionUpdate update ) {
        if (!canParseExpression(update.getExpression())) {
            // This method should not be called if canParseExpression() returns false.
            // Return an internal error status.
            update.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Cannot parse expression", null)); //$NON-NLS-1$
            update.done();
            return;
        }
        
        // Retrieve the list of all elements from the sub-class.  Then compare 
        // each returned element to the expression in the update, using 
        // testElementForExpression().  The element that matches the expression
        // is returned to the client.
        // If no matching element is found, the createInvalidExpressionVMContext() 
        // method is called to a special context.
        update(new IChildrenUpdate[] { new VMChildrenUpdate(
            update, -1, -1,
            new ViewerDataRequestMonitor<List<Object>>(getExecutor(), update) {
                @Override
                protected void handleSuccess() {
                    if (getData().size() == 0) {
                        update.setExpressionElement(createInvalidExpressionVMContext(update.getExpression()));
                        update.done();
                    } else {
                        final List<Object> elements = getData();

                        final MultiRequestMonitor<DataRequestMonitor<Boolean>> multiRm = new MultiRequestMonitor<DataRequestMonitor<Boolean>>(getExecutor(), null) {
                            @Override
                            protected void handleCompleted() {
                                if (isSuccess()) {
                                    boolean foundMatchingContext = false;
                                    for (int i = 0; i < getRequestMonitors().size(); i++) {
                                        if (getRequestMonitors().get(i).getData()) {
                                            Object element = elements.get(i);
                                            associateExpression(element, update.getExpression());
                                            update.setExpressionElement(element);
                                            foundMatchingContext = true;
                                            break;
                                        }
                                    }
                                    if (!foundMatchingContext) {
                                        update.setExpressionElement(createInvalidExpressionVMContext(update.getExpression()));
                                    }
                                } else {
                                    update.setStatus(getStatus());
                                }
                                update.done();
                            }
                        }; 
                        multiRm.requireDoneAdding();
                            
                        for (Object element : elements) {
                            testElementForExpression(
                                element, update.getExpression(), 
                                multiRm.add(
                                    new DataRequestMonitor<Boolean>(getExecutor(), null) { 
                                        @Override
                                        protected void handleCompleted() {
                                            multiRm.requestMonitorDone(this);
                                        }
                                    }));
                        }
                        multiRm.doneAdding();                        
                    }
                }
                
                @Override
                protected void handleFailure() {
                    update.setStatus(getStatus());
                    update.done();
                }
            })}
        );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#updateElementsInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate)
     */
    @Override
    protected void updateElementsInSessionThread( final IChildrenUpdate update ) {
    	IDMContext context = createCompositeDMVMContext( update );
        
        final IRegisters regService = getServicesTracker().getService( IRegisters.class );
        final IExpressions expressionService = getServicesTracker().getService( IExpressions.class );
        
        if ( context == null || expressionService == null || regService == null ) {
            handleFailedUpdate( update );
            return;
        }

        final DsfExecutor dsfExecutor = getSession().getExecutor();
        
        final DataRequestMonitor<IRegisterDMContext[]> rm = 
            new ViewerDataRequestMonitor<IRegisterDMContext[]>( dsfExecutor, update ) {
            
            @SuppressWarnings( "synthetic-access" )
            @Override
            public void handleCompleted() {
                if ( !isSuccess() ) {
                    handleFailedUpdate( update );
                    return;
                }

                final IRegisterDMContext[] regDMCs = getData();

                if ( regDMCs == null ) {
                    handleFailedUpdate( update );
                    return;
                }

                if ( regDMCs.length == 0 ) {
                    // There are no registers so just complete the request
                    update.done();
                    return;
                }

                fillUpdateWithVMCs( update, regDMCs );
                update.done();
            }
       };
       regService.getRegisters( context, rm );        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#updatePropertiesInSessionThread(org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate[])
     */
    @Override
    protected void updatePropertiesInSessionThread( final IPropertiesUpdate[] updates ) {
        final IRegisters regService = getServicesTracker().getService( IRegisters.class );
        final IExpressions expService = getServicesTracker().getService( IExpressions.class, null );

        final CountingRequestMonitor countingRm = new CountingRequestMonitor( ImmediateExecutor.getInstance(), null ) {
            @Override
            protected void handleCompleted() {
                for( final IPropertiesUpdate update : updates ) {
                    update.done();
                }
            }
        };
        int count = 0;

        if ( regService != null ) {
            FormattedValueVMUtil.updateFormattedValues( updates, regService, IRegisterDMContext.class, countingRm );
            count++;
        }

        for( final IPropertiesUpdate update : updates ) {
            IExpression expression = (IExpression)DebugPlugin.getAdapter( update.getElement(), IExpression.class );
            if ( expression != null ) {
                update.setProperty( AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION, expression.getExpressionText() );
            }

            // Capture the current "Show Type Names" ICON state in case there
            // are no columns.
            if ( update.getProperties().contains( PROP_REGISTER_SHOW_TYPE_NAMES ) ) {
                update.setProperty( PROP_REGISTER_SHOW_TYPE_NAMES, getShowTypeNamesState( update.getPresentationContext() ) );
            }

            final IRegisterDMContext regDMC = findDmcInPath( update.getViewerInput(), update.getElementPath(), IRegisterDMContext.class );
            if ( regService == null || expService == null || !(regDMC instanceof GDBRegisterDMC) ) {
                update.setStatus( new Status( IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Invalid context or service not available.", null ) ); //$NON-NLS-1$
                continue;
            }

            if ( update.getProperties().contains( PROP_NAME ) 
                 || update.getProperties().contains( PROP_VARIABLE_TYPE_NAME )
                 || update.getProperties().contains( PROP_VARIABLE_BASIC_TYPE ) ) {
                regService.getRegisterData( regDMC,
                // Use the ViewerDataRequestMonitor in order to propagate the
                // update's cancel request. Use an immediate
                // executor to avoid the possibility of a rejected execution
                // exception.
                        new ViewerDataRequestMonitor<IRegisterDMData>( ImmediateExecutor.getInstance(), update ) {
                            @SuppressWarnings( "synthetic-access" )
                            @Override
                            protected void handleCompleted() {
                                if ( isSuccess() ) {
                                    fillDataProperties( update, (GDBRegisterData)getData() );
                                }
                                else {
                                    update.setStatus( getStatus() );
                                }
                                IRegisterDMContext regDMC = findDmcInPath( update.getViewerInput(), update.getElementPath(), IRegisterDMContext.class );
                                if ( regDMC != null ) {
                                    update.setProperty( PROP_NAME, ((GDBRegisterDMC)regDMC).getName() );
                                }
                                countingRm.done();
                            }
                        } );

                count++;
            }
        }
        countingRm.setDoneCount( count );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#createLabelProvider()
     */
    @Override
    protected IElementLabelProvider createLabelProvider() {
        // Create background which is responsive to the preference color
        // changes.
        fColumnIdValueBackground = new LabelBackground( DebugUITools.getPreferenceColor( IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND ).getRGB() ) {
            {
                setPropertyNames( new String[] { 
                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                        ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                        IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT,
                        ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT 
                    } );
            }

            @Override
            public boolean isEnabled( IStatus status, java.util.Map<String, Object> properties ) {
                Boolean activeFormatChanged = (Boolean)properties.get( ICachingVMProvider.PROP_IS_CHANGED_PREFIX
                        + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT );
                Boolean activeChanged = (Boolean)properties.get( ICachingVMProvider.PROP_IS_CHANGED_PREFIX
                        + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE );
                return Boolean.TRUE.equals( activeChanged ) && !Boolean.TRUE.equals( activeFormatChanged );
            }
        };

        if ( fPreferenceChangeListener != null ) {
            DebugUITools.getPreferenceStore().removePropertyChangeListener( fPreferenceChangeListener );
        }

        fPreferenceChangeListener = new IPropertyChangeListener() {
            
            @Override
			public void propertyChange( PropertyChangeEvent event ) {
                if ( event.getProperty().equals( IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND ) ) {
                    fColumnIdValueBackground.setBackground( DebugUITools.getPreferenceColor( IDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND ).getRGB() );
                }
            }
        };
        DebugUITools.getPreferenceStore().addPropertyChangeListener( fPreferenceChangeListener );

        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();

        // The name column consists of the register name.
        provider.setColumnInfo( 
                IDebugVMConstants.COLUMN_ID__NAME,
                new LabelColumnInfo( new LabelAttribute[] {
                    new LabelText( MessagesForRegisterVM.RegisterVMNode_Name_column__text_format, new String[] { PROP_NAME } ),
                    new LabelImage( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_REGISTER ) ), new StaleDataLabelForeground(),
                    new VariableLabelFont(), 
                } ) );

        // The description column contains a brief description of the register.
        provider.setColumnInfo( 
                IDebugVMConstants.COLUMN_ID__DESCRIPTION, 
                new LabelColumnInfo( new LabelAttribute[] {
                    new LabelText( MessagesForRegisterVM.RegisterVMNode_Description_column__text_format, new String[] { IRegisterVMConstants.PROP_DESCRIPTION } ),
                    new StaleDataLabelForeground(), 
                    new VariableLabelFont(), 
                } ) );

        // In the type column contains the type of the register and its children
        provider.setColumnInfo( 
                IDebugVMConstants.COLUMN_ID__TYPE, 
                new LabelColumnInfo( new LabelAttribute[] {
                    new LabelText( MessagesForRegisterVM.RegisterVMNode_Type_column__text_format, new String[] { PROP_VARIABLE_TYPE_NAME } ) {
                        @Override
                        public void updateAttribute( ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties ) {
                            Object label = properties.get( PROP_VARIABLE_TYPE_NAME );
                            if ( label != null ) {
                                update.setLabel( label.toString(), columnIndex );
                            }
                        }
                    }, 
                    new StaleDataLabelForeground(), 
                    new VariableLabelFont(), 
                } ) );


        // Expression column is visible only in the expressions view.  
        provider.setColumnInfo( 
                IDebugVMConstants.COLUMN_ID__EXPRESSION, 
                new LabelColumnInfo( new LabelAttribute[] {
                        new LabelText( MessagesForRegisterVM.RegisterVMNode_Expression_column__text_format, new String[] { PROP_NAME } ),
                        new LabelImage( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_REGISTER ) ), 
                        new StaleDataLabelForeground(),
                        new VariableLabelFont(), } ) );
        
        // Value column shows the value in the active value format.
        //
        // In case of error, show the error message in the value column (instead
        // of the usual "...". This is needed
        // for the expressions view, where an invalid expression entered by the
        // user is a normal use case.
        //
        // For changed value high-lighting check the value in the active format.
        // But if the format itself has changed,
        // ignore the value change.
        provider.setColumnInfo( 
                IDebugVMConstants.COLUMN_ID__VALUE, 
                new LabelColumnInfo( new LabelAttribute[] { 
                        new FormattedValueLabelText(),
                        new ErrorLabelText(),
                        // TODO: replace with a preference
                        new LabelForeground( new RGB( 255, 0, 0 ) ) {
                            {
                                setPropertyNames( new String[] { PROP_NAME } );
                            }
        
                            @Override
                            public boolean isEnabled( IStatus status, Map<String, Object> properties ) {
                                return !status.isOK();
                            }
                        }, 
                        fColumnIdValueBackground, 
                        new StaleDataLabelForeground(), 
                        new VariableLabelFont(), 
                } ) );

        provider.setColumnInfo( 
                PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
                new LabelColumnInfo( new LabelAttribute[] {
                        new FormattedValueLabelText( 
                            MessagesForVariablesVM.VariableVMNode_NoColumns_column__text_format, 
                            new String[] { 
                                PROP_NAME,
                                IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                                FormattedValueVMUtil.getPropertyForFormatId( IFormattedValues.STRING_FORMAT ),
                                IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS, 
                                IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT,
                                IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, 
                                PROP_REGISTER_SHOW_TYPE_NAMES } ) {
                            
                            @Override
                            public boolean isEnabled( IStatus status, Map<String, Object> properties ) {
                                Boolean showTypeNames = (Boolean)properties.get( PROP_REGISTER_SHOW_TYPE_NAMES );
                                String[] formatIds = (String[])properties.get( IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS );
                                String activeFormat = (String)properties.get( IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT );
                                return showTypeNames != null && !showTypeNames.booleanValue() && !IFormattedValues.STRING_FORMAT.equals( activeFormat )
                                        && formatIds != null && Arrays.asList( formatIds ).contains( IFormattedValues.STRING_FORMAT )
                                        && super.isEnabled( status, properties );
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
                                PROP_REGISTER_SHOW_TYPE_NAMES } ) {
                            
                            @Override
                            public boolean isEnabled( IStatus status, Map<String, Object> properties ) {
                                Boolean showTypeNames = (Boolean)properties.get( PROP_REGISTER_SHOW_TYPE_NAMES );
                                return showTypeNames != null && !showTypeNames.booleanValue() && super.isEnabled( status, properties );
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
                                    PROP_REGISTER_SHOW_TYPE_NAMES } ) {
                                @Override
                                public boolean isEnabled( IStatus status, Map<String, Object> properties ) {
                                    Boolean showTypeNames = (Boolean)properties.get( PROP_REGISTER_SHOW_TYPE_NAMES );
                                    String[] formatIds = (String[])properties.get( IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS );
                                    String activeFormat = (String)properties.get( IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT );
                                    return showTypeNames != null 
                                           && showTypeNames.booleanValue() 
                                           && !IFormattedValues.STRING_FORMAT.equals( activeFormat )
                                           && formatIds != null 
                                           && Arrays.asList( formatIds ).contains( IFormattedValues.STRING_FORMAT )
                                           && super.isEnabled( status, properties );
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
                                    PROP_REGISTER_SHOW_TYPE_NAMES } ) {
                                @Override
                                public boolean isEnabled( IStatus status, Map<String, Object> properties ) {
                                    Boolean showTypeNames = (Boolean)properties.get( PROP_REGISTER_SHOW_TYPE_NAMES );
                                    return showTypeNames != null 
                                           && showTypeNames.booleanValue() 
                                           && super.isEnabled( status, properties );
                                }
                            },
                    new ErrorLabelText( 
                            MessagesForRegisterVM.RegisterVMNode_No_columns__Error__text_format, 
                            new String[] { PROP_NAME } ),
                    new LabelImage( DebugUITools.getImageDescriptor( IDebugUIConstants.IMG_OBJS_REGISTER ) ),
                    new LabelForeground( DebugUITools.getPreferenceColor( IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR ).getRGB() ) {
                        {
                            setPropertyNames( new String[] { IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                                    ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE,
                                    IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT,
                                    ICachingVMProvider.PROP_IS_CHANGED_PREFIX + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT } );
                        }
    
                        @Override
                        public boolean isEnabled( IStatus status, java.util.Map<String, Object> properties ) {
                            Boolean activeFormatChanged = (Boolean)properties.get( ICachingVMProvider.PROP_IS_CHANGED_PREFIX
                                    + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT );
                            Boolean activeChanged = (Boolean)properties.get( ICachingVMProvider.PROP_IS_CHANGED_PREFIX
                                    + IDebugVMConstants.PROP_FORMATTED_VALUE_ACTIVE_FORMAT_VALUE );
                            return Boolean.TRUE.equals( activeChanged ) && !Boolean.TRUE.equals( activeFormatChanged );
                        }
                    }, 
                    new StaleDataLabelBackground(), 
                    new VariableLabelFont(), 
                } ) );

        return provider;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#canParseExpression(org.eclipse.debug.core.model.IExpression)
     */
    @Override
    public boolean canParseExpression( IExpression expression ) {
        return ( parseExpressionForRegisterName( expression.getExpressionText() ) != null );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.expression.AbstractExpressionVMNode#testElementForExpression(java.lang.Object, org.eclipse.debug.core.model.IExpression, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    protected void testElementForExpression( Object element, IExpression expression, DataRequestMonitor<Boolean> rm ) {
        if ( !(element instanceof IDMVMContext) ) {
            rm.setStatus( new Status( IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null ) ); //$NON-NLS-1$
            rm.done();
            return;
        }
        
        IRegisterDMContext dmc = DMContexts.getAncestorOfType( ((IDMVMContext)element).getDMContext(), IRegisterDMContext.class );
        if ( !(dmc instanceof GDBRegisterDMC) ) {
            rm.setStatus( new Status( IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null ) ); //$NON-NLS-1$
            rm.done();
            return;
        }

        String regName = parseExpressionForRegisterName( expression.getExpressionText() );
        rm.setData( ((GDBRegisterDMC)dmc).getName().equals( regName ) );
        rm.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#createVMContext(org.eclipse.cdt.dsf.datamodel.IDMContext)
     */
    @Override
    protected IDMVMContext createVMContext( IDMContext dmc ) {
        return new GdbRegisterVMC( dmc );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#toString()
     */
    @Override
    public String toString() {
        return String.format( "GdbRegisterVMNode(%s)", getSession().getId() ); //$NON-NLS-1$
    }

    protected IWatchExpressionFactoryAdapter2 getWatchExpressionFactory() {
        return fWatchExpressionFactory;
    }

    private Boolean getShowTypeNamesState( IPresentationContext context ) {
        Boolean attribute = (Boolean)context.getProperty( IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES );
        if ( attribute != null ) {
            return attribute;
        }
        return Boolean.FALSE;
    }

    void fillDataProperties( IPropertiesUpdate update, GDBRegisterData data ) {
        update.setProperty( PROP_NAME, data.getName() );
        update.setProperty( PROP_VARIABLE_TYPE_NAME, data.getTypeName() );
        IExpressionDMData.BasicType type = data.getBasicType();
        if ( type != null ) {
            update.setProperty( PROP_VARIABLE_BASIC_TYPE, type.name() );
        }

        //
        // If this node has an expression then it has already been filled in by
        // the higher
        // level logic. If not then we need to supply something. In the previous
        // version
        // ( pre-property based ) we supplied the name. So we will do that here
        // also.
        //
        IExpression expression = (IExpression)DebugPlugin.getAdapter( update.getElement(), IExpression.class );
        if ( expression == null ) {
            update.setProperty( AbstractExpressionVMNode.PROP_ELEMENT_EXPRESSION, data.getExpression() );
        }
    }

    private String parseExpressionForRegisterName( String expression ) {
        if ( expression.startsWith( "GRP(" ) ) { //$NON-NLS-1$
            /*
             * Get the group portion.
             */
            int startIdx = "GRP(".length(); //$NON-NLS-1$
            int endIdx = expression.indexOf( ')', startIdx );
            if ( startIdx == -1 || endIdx == -1 ) {
                return null;
            }
            String remaining = expression.substring( endIdx + 1 );
            if ( !remaining.startsWith( ".REG(" ) ) { //$NON-NLS-1$
                return null;
            }

            /*
             * Get the register portion.
             */
            startIdx = ".REG(".length(); //$NON-NLS-1$
            endIdx = remaining.indexOf( ')', startIdx );
            if ( startIdx == -1 || endIdx == -1 ) {
                return null;
            }
            String regName = remaining.substring( startIdx, endIdx );
            return regName.trim();
        }
        else if ( expression.startsWith( "REG(" ) ) { //$NON-NLS-1$
            int startIdx = "REG(".length(); //$NON-NLS-1$
            int endIdx = expression.indexOf( ')', startIdx );
            if ( startIdx == -1 || endIdx == -1 ) {
                return null;
            }
            String regName = expression.substring( startIdx, endIdx );
            return regName.trim();
        }
        else if ( expression.startsWith( "$" ) ) { //$NON-NLS-1$
            /*
             * At this point I am leaving this code here to represent the
             * register case. To do this correctly would be to use the
             * findRegister function and upgrade the register service to deal
             * with registers that do not have a specified group parent context.
             * I do not have the time for this right now. So by saying we do not
             * handle this the Expression VM node will take it and pass it to
             * the debug engine as a generic expression. Most debug engines (
             * GDB included ) have an inherent knowledge of the core registers
             * as part of their expression evaluation and will respond with a
             * flat value for the reg. This is not totally complete in that you
             * should be able to express a register which has bit fields for
             * example and the bit fields should be expandable in the expression
             * view. With this method it will just appear to have a single value
             * and no sub-fields. I will file a defect/enhancement for this to
             * mark it. This comment will act as the place-holder for the future
             * work.
             */
            return null;
        }

        return null;
    }
}
