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

package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.variable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.MultiRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.AbstractExpressionVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.expression.IExpressionUpdate;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat.IFormattedValueVMContext;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.service.IExpressions;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IStack;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.dd.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter2;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;

@SuppressWarnings({"restriction", "nls"})
public class VariableVMNode extends AbstractExpressionVMNode 
    implements IElementEditor, IElementLabelProvider, IElementMementoProvider 
{
    
    private final IFormattedValuePreferenceStore fFormattedPrefStore;
    
    private final SyncVariableDataAccess fSyncVariableDataAccess;
    
    public class VariableExpressionVMC extends DMVMContext implements IFormattedValueVMContext  {
        
        private IExpression fExpression;
        
        public VariableExpressionVMC(IDMContext dmc) {
            super(dmc);
        }

        public IFormattedValuePreferenceStore getPreferenceStore() {
            return fFormattedPrefStore;
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

    public VariableVMNode(IFormattedValuePreferenceStore prefStore, AbstractDMVMProvider provider,
        DsfSession session, SyncVariableDataAccess syncVariableDataAccess) 
    {
        super(provider, session, IExpressions.IExpressionDMContext.class);
        fFormattedPrefStore = prefStore;
        fSyncVariableDataAccess = syncVariableDataAccess;
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

    private void fillInExpressionErrorInfo( ILabelUpdate update, IExpressionDMContext dmc, IStatus status ) { 
    	/*
    	 *  Instead of just failing this outright we are going to attempt to do more here.
    	 *  Failing it outright causes the view to display ... for all columns in the line
    	 *  and this is uninformative about what is happening. It will be very common that
    	 *  one or more variables at that given instance in time are not evaluatable. They
    	 *  may be out of scope and will come back into scope later.
    	 */
    	String[] localColumns = update.getColumnIds();
    	if (localColumns == null)
    		localColumns = new String[] { IDebugVMConstants.COLUMN_ID__NAME };

    	for (int idx = 0; idx < localColumns.length; idx++) {
    		if (IDebugVMConstants.COLUMN_ID__NAME.equals(localColumns[idx])) {
    			update.setLabel(dmc.getExpression(), idx);
    		} else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(localColumns[idx])) {
    			update.setLabel("", idx);
    		} else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
    			update.setLabel("Error : " + status.getMessage(), idx);
    		} else if (IDebugVMConstants.COLUMN_ID__ADDRESS.equals(localColumns[idx])) {
    			update.setLabel("", idx);
    		} else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(localColumns[idx])) {
    			update.setLabel("", idx);
    		} else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(localColumns[idx])) {
    			update.setLabel(dmc.getExpression(), idx);
    		} else {
    			update.setLabel("", idx);
    		}
    		update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
    	}
    }

    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            
            final IExpressionDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExpressions.IExpressionDMContext.class);
            
            if ( dmc == null ) {
                // Workaround for a bug in platform, where the find operation may use wrong label provider.
                // See bug 246618.
                update.done();
                continue;
            }

            getDMVMProvider().getModelData(
        		this, update, 
        		getServicesTracker().getService(IExpressions.class, null),
        		dmc, 
        		new ViewerDataRequestMonitor<IExpressionDMData>(getSession().getExecutor(), update) { 
        			@Override
        			protected void handleCompleted() {
        				// Check that the request was evaluated and data is still valid.  The request could
        				// fail if the state of the  service changed during the request, but the view model
        				// has not been updated yet.
       					if (!isSuccess()) {
       						assert getStatus().isOK() || 
       						getStatus().getCode() != IDsfStatusConstants.INTERNAL_ERROR || 
       						getStatus().getCode() != IDsfStatusConstants.NOT_SUPPORTED;
       						
       						fillInExpressionErrorInfo( update, dmc, getStatus() );

       						update.done();
       						return;
       					}

       					// If columns are configured, extract the selected values for each understood column.
       					// First, we fill all of those columns which can be filled without extra data mining.
       					// We also note if we  do have to do extra data mining.  Any columns need to set the
       					// processing flag so we know we have further work to do.  If there are more columns
       					// which need data extraction they need to be added in both "for" loops.
       					String[] localColumns = update.getColumnIds();
       					if (localColumns == null)
       						localColumns = new String[] { IDebugVMConstants.COLUMN_ID__NAME };

       					int extractingFormattedDataIndex = -1;
       					int extractingAddressDataIndex = -1;

       					for (int idx = 0; idx < localColumns.length; idx++) {
       						if (IDebugVMConstants.COLUMN_ID__NAME.equals(localColumns[idx])) {
       							update.setLabel(getData().getName(), idx);
       						} else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(localColumns[idx])) {
       							update.setLabel(getData().getTypeName(), idx);
       						} else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(localColumns[idx])) {
       							extractingFormattedDataIndex = idx;
       						} else if (IDebugVMConstants.COLUMN_ID__ADDRESS.equals(localColumns[idx])) {
       							extractingAddressDataIndex = idx;
       						} else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(localColumns[idx])) {
       							update.setLabel("", idx);
       						} else if (IDebugVMConstants.COLUMN_ID__EXPRESSION.equals(localColumns[idx])) {
       							IVMContext vmc = (IVMContext)update.getElement();
       							IExpression expression = (IExpression)vmc.getAdapter(IExpression.class);
       							if (expression != null) {
       								update.setLabel(expression.getExpressionText(), idx);
       							} else {
       								update.setLabel(getData().getName(), idx);
       							}
       						}
       						update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], idx);
       					}

       					if ( ( extractingFormattedDataIndex == -1 ) && ( extractingAddressDataIndex == -1 ) ) {
       						update.done();
       					} else {
       						/*
       						 * We are either updating the value or the address or possibly both.
       						 * We will create a overarching monitor to handle completing the update
       						 * when either/both of the lower level updates are done.
       						 */
       					    final DsfExecutor dsfExecutor = getSession().getExecutor();
       					    
       						final MultiRequestMonitor<RequestMonitor> mrm =
       	                        new MultiRequestMonitor<RequestMonitor>(dsfExecutor, null) {
       	                            @Override
       	                            public void handleCompleted() {
       	                                if (!isSuccess()) {
       	                                    handleFailedUpdate(update);
       	                                    return;
       	                                }
       	                                update.done();
       	                            }
       	                    };
       						
       	                    /*
       	                     * Deal with the value.
       	                     */
       	                    if ( extractingFormattedDataIndex != -1 ) {
       	                    	RequestMonitor rm = new RequestMonitor(dsfExecutor, null) {
       	                    		@Override
       	                    		public void handleCompleted() {
       	                    			mrm.requestMonitorDone(this);
       	                    		}
       	                    	};

       	                    	mrm.add(rm);
       	                    	updateFormattedExpressionValue(update, extractingFormattedDataIndex, dmc, getData(),rm);
       	                    }
       	                    
       	                    /*
       	                     * Deal with the address.
       	                     */
       	                    if ( extractingAddressDataIndex != -1 ) {
       	                    	RequestMonitor rm = new RequestMonitor(dsfExecutor, null) {
       	                    		@Override
       	                    		public void handleCompleted() {
       	                    			mrm.requestMonitorDone(this);
       	                    		}
       	                    	};

       	                    	mrm.add(rm);
       	                    	updateAddressData(update, extractingAddressDataIndex, dmc, rm);
       	                    }
       					}
       				}
       			},
       			getExecutor()
        	);
        }
    }

    /**
     *  Private data access routine which performs the extra level of data access needed to
     *  get the formatted data value for a specific register.
     */
    private void updateAddressData(final ILabelUpdate update,
    		                       final int labelIndex,
                                   final IExpressionDMContext dmc, 
                                   final RequestMonitor monitor)
    { 
        /*
         *  First select the format to be used. This involves checking so see that the preference
         *  page format is supported by the register service. If the format is not supported then 
         *  we will pick the first available format.
         */
    	final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
        
		// Get the variable information and update the corresponding memory locations
        if (expressionService != null) {
        	expressionService.getExpressionAddressData(dmc,
        		new DataRequestMonitor<IExpressionDMAddress>(getExecutor(), monitor) {
        			@Override
        			protected void handleCompleted() {
        				if ( isSuccess() ) {
        					// Figure out which memory area was modified
        					IExpressionDMAddress expression = getData();
        					IAddress expAddress = expression.getAddress();
        					if (expAddress instanceof Addr64) {
        						update.setLabel( "0x" + ((Addr64) expAddress).toString(16), labelIndex);
        					}
        					else if (expAddress instanceof Addr32) {
        						update.setLabel( "0x" + ((Addr32) expAddress).toString(16), labelIndex);
        					}
        					else {
        						update.setLabel( "Unknown address format", labelIndex);
        					}
        				}
        				else {
        					/*
        					 *  We could not get the format. Currently GDB does not handle getting the address of
        					 *  a constant for example. We could put the error message in,  but that would not be
        					 *  all that helpful top the user.  The interface is a new one and perhaps failing to
        					 *  return a valid set of information  is just saying it does not exist. Anyway,  for
        					 *  now we will just put nothing in.
        					 */
        					update.setLabel( "", labelIndex);
        				}
        				
        				update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], labelIndex);
        				monitor.done();
        	    	}
        		}
        	);
        }
    }
    
    /**
     *  Private data access routine which performs the extra level of data access needed to
     *  get the formatted data value for a specific register.
     */
    private void updateFormattedExpressionValue(final ILabelUpdate update,
    		                                    final int labelIndex,
                                                final IExpressionDMContext dmc, 
                                                final IExpressionDMData expressionDMData,
                                                final RequestMonitor monitor)
    { 
        final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
        /*
         *  First select the format to be used. This involves checking so see that the preference
         *  page format is supported by the register service. If the format is not supported then 
         *  we will pick the first available format.
         */
        final IPresentationContext context = update.getPresentationContext();
        final String preferencePageFormatId = fFormattedPrefStore.getCurrentNumericFormat(context) ;
        
        expressionService.getAvailableFormats(
            dmc,
            new DataRequestMonitor<String[]>(getSession().getExecutor(), monitor) {
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                    	monitor.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Format information not available", null));
                        monitor.done();
                        return;
                    }
                    
                    /*
                     *  See if the desired format is supported.
                     */
                    final String[] formatIds = getData();
                    String   finalFormatId = IFormattedValues.NATURAL_FORMAT;
                    boolean  requestedFormatIsSupported = false;
                    
                    for ( String fId : formatIds ) {
                        if ( preferencePageFormatId.equals(fId) ) {
                            // The desired format is supported.

                            finalFormatId = preferencePageFormatId;
                            requestedFormatIsSupported = true;
                            break;
                        }
                    }
                    
                    if ( ! requestedFormatIsSupported ) {
                        /*
                         *  Desired format is not supported. If there are any formats supported
                         *  then use the first available.
                         */
                        if ( formatIds.length != 0 ) {
                            finalFormatId = formatIds[0];
                        }
                        else {
                            // Expression service does not support any format.
                            
                        	monitor.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Service does not support any formats", null));
                            monitor.done();
                            return;
                        }
                    }
                    
                    /*
                     *  Format has been validated. Get the formatted value.
                     */
                    final FormattedValueDMContext valueDmc = expressionService.getFormattedValueContext(dmc, finalFormatId);
                    
                    getDMVMProvider().getModelData(
                    	VariableVMNode.this, 
                    	update,
                    	expressionService,
                    	valueDmc, 
                    	new DataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), monitor) {
                    		@Override
                    		public void handleCompleted() {
                    			if (!isSuccess()) {
                    				monitor.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, getStatus().getMessage(), null));
                    				monitor.done();
                    				return;
                    			}

                    			final String formattedValue = getData().getFormattedValue();
                    			final String formattedStringId = valueDmc.getFormatID();

                    			if ( formattedStringId.equals(IFormattedValues.STRING_FORMAT) ) {
                    				/*
                    				 *  In this case we are being asked to fill in the value information with STRING_FORMAT.
                    				 *  So we do not need to append it to the value as we did in the past.
                    				 */
                    				completeFillinInUpdateWithValue(update,	labelIndex, valueDmc, formattedValue, null, null, monitor);
                    			}
                    			else {
                    				/*
                    				 *  The format specified is not STRING_FORMAT and as we did before we need to append 
                    				 *  the string information to the value ( if it exists ). So first see if STRING_FORMAT 
                    				 *  is supported by the service.
                    				 */
                   					boolean foundStringFormat = false;

                   					for ( String format : formatIds ) {
                   						if ( format.equals(IFormattedValues.STRING_FORMAT) ) {
                   							foundStringFormat = true;
                   						}
                   					}

                   					if ( foundStringFormat ) {
                   						/*
                   						 *  So STRING_FORMAT is supported so we can go get it and append it to the value.
                   						 * 
                   						 *  Note : Currently the Reference Model MI Expression Service  does not support the
                   						 *         STRING_FORMAT. The view still pretty much looks the same however,  to one
                   						 *         where the STRING_FORMAT is supplied.  This is because when GDB is ask  to
                   						 *         evaluate a variable it will return the STRING_FORMAT information appended
                   						 *         to the address so it looks good. GDB appends all kinds of usefull info to
                   						 *         requests for data values, based on the value types. So the expressions do
                   						 *         look good. If the Reference Model Expression Service  ever does implement
                   						 *         STRING_FORMAT  this will need  to be revisited.  There would be duplicate
                   						 *         information displayed and the view would look broken.  However this needs
                   						 *         to be put back in to satisfy Bugzilla defect "225612", which represents a
                   						 *         regression in the display of data from 0.9 to 1.x.
                   						 */
                   						final FormattedValueDMContext stringDmc = expressionService.getFormattedValueContext(dmc, IFormattedValues.STRING_FORMAT);
                    	                    
                   	                    getDMVMProvider().getModelData(
                  	                   		VariableVMNode.this, 
                  	                   		update,
                    	               		expressionService,
                    	               		stringDmc, 
                    	               		new DataRequestMonitor<FormattedValueDMData>(getSession().getExecutor(), monitor) {
                    	               			@Override
                    	               			public void handleCompleted() {
                    	               				if (!isSuccess()) {
                    	               					monitor.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, getStatus().getMessage(), null));
                    	               					monitor.done();
                    	               					return;
                    	               				}

                    	               				String stringValue = getData().getFormattedValue();
                    	                    				
                    	                    		completeFillinInUpdateWithValue(update,	labelIndex, valueDmc, formattedValue, stringDmc, stringValue, monitor);
                    	               			}
                    	               		},
                    	               		getExecutor()
                   	                    );
                   					}
                    				else {
                    					/*
                    					 * The STRING_FORMAT is not supported. So all we can do is fill it in without it.
                    					 */
                    					completeFillinInUpdateWithValue(update,	labelIndex, valueDmc, formattedValue, null, null, monitor);
                    				}
                    			}
                    		}
                    	},
                    	getExecutor()
                    );
                }
            }
        );
    }
    
    private void completeFillinInUpdateWithValue(ILabelUpdate update,
    		                                     int labelIndex,
                                                 FormattedValueDMContext valueDmc,
                                                 String value,
                                                 FormattedValueDMContext stringFormatDmc,
                                                 String stringFormatValue,
                                                 RequestMonitor monitor)
    {
    	/*
    	 * Complete filling in the VALUE. The form is
    	 * 
    	 *    "Numerical value" "STRING_FORMAT value"
    	 *    
    	 * This makes it so if the value is a pointer to something else we conveniently
    	 * fill in the something else ( typically a string ).
    	 */

    	StringBuffer stringValueBuf = new StringBuffer(value);
    	if(stringFormatValue != null && stringFormatValue.length() > 0)
    	{
    		stringValueBuf.append(" ");
    		stringValueBuf.append(stringFormatValue);
    	}
    	update.setLabel(stringValueBuf.toString(), labelIndex);
    	update.setFontData(JFaceResources.getFontDescriptor(IInternalDebugUIConstants.VARIABLE_TEXT_FONT).getFontData()[0], labelIndex);

    	/*
    	 * Get old values for comparison ( if available ).
    	 */
    	FormattedValueDMData oldStringData = null;
    	FormattedValueDMData oldData = 
    		
    		(FormattedValueDMData) getDMVMProvider().getArchivedModelData(VariableVMNode.this, update, valueDmc);
    	
    	if ( stringFormatDmc != null) {
    		oldStringData = (FormattedValueDMData) getDMVMProvider().getArchivedModelData(VariableVMNode.this, update, stringFormatDmc);
    	}

    	/*
    	 *  Highlight the value if either the value (address) has changed or the string (memory at the value) has changed
    	 */
    	if ( ( oldData != null       && ! oldData.getFormattedValue().equals(value)                   ) ||
       	     ( oldStringData != null && ! oldStringData.getFormattedValue().equals(stringFormatValue) )    
       	   ) {
       		RGB rgb = DebugUIPlugin.getPreferenceColor(IInternalDebugUIConstants.PREF_CHANGED_VALUE_BACKGROUND).getRGB();
       		update.setBackground(rgb, labelIndex);
       	}

    	/*
    	 * Now we finally can complete this one.
    	 */
    	monitor.done();
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
        return new VariableCellModifier(getDMVMProvider(), fFormattedPrefStore, fSyncVariableDataAccess);
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
            update.setStatus(new Status(IStatus.ERROR, DsfDebugUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Update failed", null)); //$NON-NLS-1$
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
                        
                        getDMVMProvider().getModelData(VariableVMNode.this, update, stackFrameService, localDMC, rm, getExecutor());
                    }
                }
        };

        // Make the asynchronous call to IStack.getLocals().  The results are processed in the
        // DataRequestMonitor.handleCompleted() above.

        stackFrameService.getLocals(frameDmc, rm);
    }
    
    //private final static int MAX_STRING_VALUE_LENGTH = 40;
    
    public int getDeltaFlags(Object e) {
        if ( e instanceof ISuspendedDMEvent || 
             e instanceof IMemoryChangedEvent ||
             e instanceof IExpressionChangedDMEvent ||
             (e instanceof PropertyChangeEvent &&
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) ) 
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
              ((PropertyChangeEvent)e).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) ) 
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
              ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) )
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
                ((PropertyChangeEvent)event).getProperty() == IDebugVMConstants.CURRENT_FORMAT_STORAGE) )
        {
            parentDelta.addNode(element, IModelDelta.CONTENT);
        } 

        rm.done();
    }
    

    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    private String produceExpressionElementName( String viewName , IExpressionDMContext expression ) {
    	
    	return "Variable." + expression.getExpression(); //$NON-NLS-1$
    }

    private final String MEMENTO_NAME = "VARIABLE_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        
        for ( IElementCompareRequest request : requests ) {
        	
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            String mementoName = memento.getString(MEMENTO_NAME); //$NON-NLS-1$
            
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#encodeElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest[])
     */
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
