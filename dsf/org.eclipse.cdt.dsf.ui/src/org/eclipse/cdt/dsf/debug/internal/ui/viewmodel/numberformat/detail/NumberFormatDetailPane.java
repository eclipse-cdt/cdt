/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems, Inc. - extended implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.numberformat.detail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.DetailPaneMaxLengthAction;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.DetailPaneWordWrapAction;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.MessagesForDetailPane;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.TextViewerAction;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.IFormattedDataDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IBitFieldDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.update.AbstractCachingVMProvider;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.OperationHistoryActionHandler;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

@SuppressWarnings("restriction")
public class NumberFormatDetailPane implements IDetailPane, IAdaptable, IPropertyChangeListener {

    /**
     * The <code>IWorkbenchPartSite</code> that the details area (and the 
     * variables view) belongs to.
     */
    private IWorkbenchPartSite fWorkbenchPartSite;
    
    /**
     * Map of actions. Keys are strings, values
     * are <code>IAction</code>.
     */
    private Map<String, IAction> fActionMap = new HashMap<String, IAction>();
    
    /**
     * Collection to track actions that should be updated when selection occurs.
     */
    private List<String> fSelectionActions = new ArrayList<String>();
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#init(org.eclipse.ui.IWorkbenchPartSite)
     */
    public void init(IWorkbenchPartSite workbench) {
        fWorkbenchPartSite = workbench;

    }
    
    /**
     * Adds an action to the Map storing actions.  Removes it if action is null.
     * 
     * @param actionID The ID of the action, used as the key in the Map
     * @param action The action associated with the ID
     */
    protected void setAction(String actionID, IAction action) {
        if (action == null) {
            fActionMap.remove(actionID);
        } else {
            fActionMap.put(actionID, action);
        }
    }
    
    /**
     * Adds the given action to the global action handler for the ViewSite.
     * A call to <code>updateActionBars()</code> must be called after changes
     * to propagate changes through the workbench.
     * 
     * @param actionID The ID of the action
     * @param action The action to be set globally
     */
    protected void setGlobalAction(String actionID, IAction action){
        getViewSite().getActionBars().setGlobalActionHandler(actionID, action);
    }
    
    /**
     * Adds the given action to the list of actions that will be updated when
     * <code>updateSelectionDependentActions()</code> is called.  If the string 
     * is null it will not be added to the list.
     * 
     * @param actionID The ID of the action which should be updated
     */
    protected void setSelectionDependantAction(String actionID){
        if (actionID != null) fSelectionActions.add(actionID);
    }
    
    /**
     * Gets the action out of the map, casts it to an <code>IAction</code>
     * 
     * @param actionID  The ID of the action to find
     * @return The action associated with the ID or null if none is found.
     */
    protected IAction getAction(String actionID) {
        return fActionMap.get(actionID);
    }
    
    /**
     * Calls the update method of the action with the given action ID.
     * The action must exist in the action map and must be an instance of
     * </code>IUpdate</code>
     * 
     * @param actionId The ID of the action to update
     */
    protected void updateAction(String actionId) {
        IAction action= getAction(actionId);
        if (action instanceof IUpdate) {
            ((IUpdate) action).update();
        }
    }
    
    /**
     * Iterates through the list of selection dependent actions and 
     * updates them.  Use <code>setSelectionDependentAction(String actionID)</code>
     * to add an action to the list.  The action must have been added to the known 
     * actions map by calling <code>setAction(String actionID, IAction action)</code>
     * before it can be updated by this method.
     */
    protected void updateSelectionDependentActions() {
        Iterator<String> iterator= fSelectionActions.iterator();
        while (iterator.hasNext()) {
            updateAction(iterator.next());      
        }
    }
    
    /**
     * Gets the view site for this view.  May be null if this detail pane
     * is not part of a view.
     * 
     * @return The site for this view or <code>null</code>
     */
    protected  IViewSite getViewSite(){
        if (fWorkbenchPartSite == null){
            return null;
        } else {
            return (IViewSite) fWorkbenchPartSite.getPart().getSite();
        }
    }

    /**
     * Gets the workbench part site for this view.  May be null if this detail pane
     * is not part of a view.
     * 
     * @return The workbench part site or <code>null</code>
     */
    protected IWorkbenchPartSite getWorkbenchPartSite() {
        return fWorkbenchPartSite;
    }
    
    /**
     * Returns whether this detail pane is being displayed in a view with a workbench part site.
     * 
     * @return whether this detail pane is being displayed in a view with a workbench part site.
     */
    protected boolean isInView(){
        return fWorkbenchPartSite != null;
    }

    /**
     * These are the IDs for the actions in the context menu
     */
    protected static final String DETAIL_COPY_ACTION = ActionFactory.COPY.getId() + ".SourceDetailPane"; //$NON-NLS-1$
    protected static final String DETAIL_SELECT_ALL_ACTION = IDebugView.SELECT_ALL_ACTION + ".SourceDetailPane"; //$NON-NLS-1$
    protected static final String DETAIL_WORD_WRAP_ACTION = DsfUIPlugin.PLUGIN_ID + ".detail_pane_word_wrap"; //$NON-NLS-1$
    protected static final String DETAIL_MAX_LENGTH_ACTION = "MaxLength"; //$NON-NLS-1$
    
    /**
     * The ID, name and description of this pane are stored in constants so that the class
     * does not have to be instantiated to access them.
     */
    public static final String ID = "NumberFormatPane"; //$NON-NLS-1$
    
    /**
     * Data structure for the position label value.
     */
    private static class PositionLabelValue {
        
        public int fValue;
        
        @Override
        public String toString() {
            return String.valueOf(fValue);
        }
    }
    
    /**
     * Internal interface for a cursor listener. I.e. aggregation 
     * of mouse and key listener.
     * @since 3.0
     */
    interface ICursorListener extends MouseListener, KeyListener {
    }
    
    /**
     * Job to compute the details for a selection
     */
    class DetailJob extends Job implements IValueDetailListener {
        private IPresentationContext fPresentationContext;
        private Object fViewerInput;
        private ITreeSelection fElements;
        private boolean fFirst = true;
        private IProgressMonitor fMonitor;
        private boolean fComputed = false;
        
        public DetailJob(IPresentationContext context, Object viewerInput, ITreeSelection elements, 
                        IDebugModelPresentation model) 
        {
            super("compute variable details"); //$NON-NLS-1$
            setSystem(true);
            fPresentationContext = context;
            fViewerInput = viewerInput;
            fElements = elements;
        }
        
        /*
         *  This is the routine which will actually process the various format requests
         *  for a given element.  It is expected and required that this routine will be
         *  called from within a DSF executor.
         */
        private void putInformationIntoDetailPane( final AbstractCachingVMProvider provider,
                                                   final IVMNode node, 
                                                   final TreePath path,
                                                   final IFormattedDataDMContext finalDmc , 
        		 								   final IFormattedValues service, 
        										   final IProgressMonitor monitor, 
        										   final String name ) {

            /*
             *  Now that we can process this one. Find out how many formats we can
             *  show this in. We will choose to show all of the supported formats.
             *  Since we are doing this in the background and the debug engines do
             *  typically cache the results so producing multiple formats will not
             *  typically be a burden. We should probably consider perhaps doing a
             *  preference where they can select what formats they want to show.
             */
            
            final DataRequestMonitor<String[]> getAvailableFormatsDone = 
                new DataRequestMonitor<String[]>(service.getExecutor(), null) {
                    @Override
                    protected void handleSuccess() {
                        if (monitor.isCanceled()) {
                        	notifyAll();
                            return;
                        }
                        
                        /*
                         *  Now we have a set of formats for each one fire up an independent
                         *  asynchronous request to get the data in that format. We do not
                         *  go through the cache manager here because when the values are
                         *  edited and written the cache is bypassed.
                         */
                        String[] formats = getData();
                        
                        final List<String> completedFormatStrings = new ArrayList<String>();
                        
                        final CountingRequestMonitor countingRm = new CountingRequestMonitor(service.getExecutor(), null) {
                            @Override
                            protected void handleCompleted() {
                                
                                if (monitor.isCanceled()) {
                                	notifyAll();
                                    return;
                                }
                                
                                /*
                                 *  We sort the array to make the strings appear always in the same order.
                                 */

                                java.util.Collections.sort( completedFormatStrings );

                                int len = completedFormatStrings.size() ;
                                
                                if ( len == 0 ) {
                                	detailComputed(null,""); //$NON-NLS-1$
                                }
                                else {
                                	/*
                                	 *  Add the HEADER which identifies what is being represented. When there
                                	 *  are multiple selections in the view the detail pane contains multiple
                                	 *  entries.  They would be all compressed together  and even though  the 
                                	 *  order of the  entries is the order of the selections in the view  and
                                	 *  it is very hard to know what goes with what. This makes it easy.
                                	 */
                                	String finalResult = "Name : " + name + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                                	int cnt = 0 ;

                                	for (String str : completedFormatStrings) {

                                		finalResult += "   " + str ; //$NON-NLS-1$

                                		if ( ( ++ cnt ) < len ) {
                                			finalResult += "\n" ; //$NON-NLS-1$ 
                                		}
                                	}

                                	detailComputed(null,finalResult);
                                }
                            }
                        };
                        
                        countingRm.setDoneCount(formats.length);
                        
                        for ( final String str : formats ) {
                            /*
                             *  Format has been validated. Get the formatted value.
                             */
                            final FormattedValueDMContext valueDmc = service.getFormattedValueContext(finalDmc, str);
                            
                            provider.getModelData(
                                node, 
                                new IViewerUpdate() {
                                    public void cancel() {}
                                    public void done() {}
                                    public Object getViewerInput() { return fViewerInput; }
                                    public TreePath getElementPath() { return path; }
                                    public Object getElement() { return path.getLastSegment(); }
                                    public IPresentationContext getPresentationContext() { return fPresentationContext; }
                                    public boolean isCanceled() { return monitor.isCanceled(); }
                                    public void setStatus(IStatus status) {}
                                    public IStatus getStatus() { return null; }
                                }, 
                                service, valueDmc, 
                                new DataRequestMonitor<FormattedValueDMData>(service.getExecutor(), null) {
                                    @Override
                                    public void handleCompleted() {
                                        if (getStatus().isOK()) {
                                            /*
                                             *  Show the information indicating the format.
                                             */
                                            if ( str == IFormattedValues.HEX_FORMAT) {
                                                completedFormatStrings.add("Hex.... : " + getData().getFormattedValue()); //$NON-NLS-1$
                                            }
                                            else if ( str == IFormattedValues.OCTAL_FORMAT) {
                                                completedFormatStrings.add("Octal.. : " + getData().getFormattedValue()); //$NON-NLS-1$
                                            }
                                            else if ( str == IFormattedValues.NATURAL_FORMAT) {
                                                completedFormatStrings.add("Natural : " + getData().getFormattedValue()); //$NON-NLS-1$
                                            }
                                            else if ( str == IFormattedValues.BINARY_FORMAT) {
                                                completedFormatStrings.add("Binary. : " + getData().getFormattedValue()); //$NON-NLS-1$
                                            }
                                            else if ( str == IFormattedValues.DECIMAL_FORMAT) {
                                                completedFormatStrings.add("Decimal : " + getData().getFormattedValue()); //$NON-NLS-1$
                                            }
                                            else if ( str == IFormattedValues.STRING_FORMAT) {
                                                completedFormatStrings.add("String : " + getData().getFormattedValue()); //$NON-NLS-1$
                                            }
                                            else {
                                                completedFormatStrings.add("Other.. : (" + str + ") " + getData().getFormattedValue()); //$NON-NLS-1$  //$NON-NLS-2$
                                            }
                                        }
                                        countingRm.done();
                                    }
                                },
                                provider.getExecutor());
                        }
                    }
                };

                /*
                 *  Get the supported formats.
                 */
                service.getAvailableFormats(finalDmc, getAvailableFormatsDone);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        protected IStatus run(final IProgressMonitor monitor) {
            
            String message = null;
            if ( fMonitor != null && ! fMonitor.isCanceled() ) {
                fMonitor.setCanceled(true);
            }
            fMonitor = monitor;
            TreePath[] paths = fElements.getPaths();
            for (int i = 0; i < paths.length; i++) {
                if (monitor.isCanceled()) {
                    break;
                }
                final TreePath path = paths[i];
                Object element = paths[i].getLastSegment();
                
                /*
                 *  Make sure this is an element we want to deal with.
                 */
                if ( element instanceof IDMVMContext) {
                    IDMVMContext vmc = (IDMVMContext)element;
                    /*
                     *  We are specifically looking to support the following Data Model Contexts
                     *  
                     *  IRegisterDMContext
                     *  IBitFieldDMContext
                     *  IExpressionDMContext
                     *  
                     *  At first you might think that we should just use the service which is
                     *  associated with the dmc. But there are implementations where the data
                     *  model contexts are extended but the services do not extend each other
                     *  ( this is the case with the WindRiver OCD extensions for example ).
                     *  
                     *  So here we specifically look for the service which knows how to  deal
                     *  with the formatted data.
                     *  
                     *  Please note that the order or searching for the ancestor is important.
                     *  A BitField Data Model Context will have a Register Data Model Context 
                     *  as its parent so if we search for a Register DMC first when we actually
                     *  have a BitField DMC we will get the register and show the value of  the
                     *  register not the bit field.
                     */

                    DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), vmc.getDMContext().getSessionId());

                    final AbstractCachingVMProvider provider = (AbstractCachingVMProvider)vmc.getAdapter(AbstractCachingVMProvider.class);
                    final IVMNode node = (IVMNode)vmc.getAdapter(IVMNode.class);
                                        
                    final IBitFieldDMContext bitfieldDmc = DMContexts.getAncestorOfType(((IDMVMContext) element).getDMContext(), IBitFieldDMContext.class);
                    if ( bitfieldDmc != null ) {
                    	/*
                         *  Get the name so we can construct the header which identifies the detail
                         *  set of values.
                         */
                    	final IRegisters regService = tracker.getService(IRegisters.class);
                    	
                    	regService.getExecutor().submit(
                    		new Runnable() {
                    			public void run() {
                    				regService.getBitFieldData(
                    					bitfieldDmc,
                    					new DataRequestMonitor<IBitFieldDMData>(regService.getExecutor(), null) {
                    						@Override
                    						public void handleCompleted() {
                    							if (getStatus().isOK()) {
                    								putInformationIntoDetailPane(provider, node, path, bitfieldDmc , regService, monitor, getData().getName());
                    							}
                    						}
                    					}
                    				);
                    			}
                    		}
                        );
                    }
                    else {
                        final IRegisterDMContext regDmc = DMContexts.getAncestorOfType(((IDMVMContext) element).getDMContext(), IRegisterDMContext.class);

                        if ( regDmc != null ) {
                            /*
                             *  Get the name so we can construct the header which identifies the detail
                             *  set of values.
                             */
                        	final IRegisters regService = tracker.getService(IRegisters.class);
                        	
                            regService.getExecutor().submit(
                            	new Runnable() {
                            		public void run() {
                            			regService.getRegisterData(
                                           	regDmc,
                                            new DataRequestMonitor<IRegisterDMData>(regService.getExecutor(), null) {
                                           		@Override
                                           		public void handleCompleted() {
                                           			if (getStatus().isOK()) {
                                           				putInformationIntoDetailPane(provider, node, path, regDmc , regService, monitor, getData().getName());
                                           			}
                                           		}
                                           	}
                            			);
                            		}
                            	}
                            );
                        }
                        else {
                            final IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(((IDMVMContext) element).getDMContext(), IExpressionDMContext.class);

                            if ( exprDmc != null ) {
                                final IExpressions exprService = tracker.getService(IExpressions.class);;
                                
                                exprService.getExecutor().submit(
                                    new Runnable() {
                                    	public void run() {
                                    		putInformationIntoDetailPane(provider, node, path, exprDmc , exprService, monitor, exprDmc.getExpression());
                                    	}
                                    }
                                );
                            }
                            else {
                            	/*
                            	 *  For whatever reason we are seeing some form of context we do not handle. So we 
                            	 *  will skip this one  and try and process any remaining ones.  At least this way
                            	 *  we can fill the detail pane with those we do understand.
                            	 */
                            	continue;
                            }
                        }
                    }
                    tracker.dispose();
                    
                    /*
                     *  We need to wait until all the values are in. This causes the work
                     *  to in effect be synchronous,  but if we do not wait  then when we
                     *  are stepping fast if we exit  before the job is finished then  we
                     *  will enter and start to update the pane before the previous  work
                     *  is actually done.  This causes  the data to be screwed  up in  an 
                     *  overlapped way.  It should be the case that most of the jobs that
                     *  occur in the middle will be cancelled,  so there is not a lot  of
                     *  waste actually going on.
                     */
                    
                    synchronized (this) {
                       	try {
                       		// wait for a max of 30 seconds for result, then cancel
                       		wait(30000);
                       		if (!fComputed) {
                       			fMonitor.setCanceled(true);
                       		}
                       	} catch (InterruptedException e) {
                       		break;
                       	}
                    }
                }
                else {
//                    IValue val = null;
//                    if (element instanceof IVariable) {
//                        try {
//                            val = ((IVariable)element).getValue();
//                        } catch (DebugException e) {
//                            detailComputed(null, e.getStatus().getMessage());
//                        }
//                    } else if (element instanceof IExpression) {
//                        val = ((IExpression)element).getValue();
//                    }
                    if (element instanceof String) {
                        message = (String) element;
                    }
                    else {
                    	message = element.toString();
                    }
                    fComputed = true;
                }
                // If no details were computed for the selected variable, clear the pane
                // or use the message, if the variable was a java.lang.String
                if (!fComputed){
                    if (message == null) {
                        detailComputed(null,""); //$NON-NLS-1$
                    } else {
                        detailComputed(null, message);
                    }
                }
            }
            
            return Status.OK_STATUS;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#canceling()
         */
        @Override
        protected void canceling() {
            super.canceling();
            synchronized (this) {
                notifyAll();
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.debug.ui.IValueDetailListener#detailComputed(org.eclipse.debug.core.model.IValue, java.lang.String)
         */
        public void detailComputed(IValue value, final String result) {
        	synchronized (this) {
				fComputed = true;
			}
            if (!fMonitor.isCanceled()) {
                WorkbenchJob append = new WorkbenchJob("append details") { //$NON-NLS-1$
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (!fMonitor.isCanceled()) {
                            String insert = result;
                            int length = 0;
                            if (!fFirst) {
                                length = getDetailDocument().getLength();
                            }
                            if (length > 0) {
                                insert = "\n" + result; //$NON-NLS-1$
                            }
                            try {
                                int max = DsfUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
                                if (max > 0 && insert.length() > max) {
                                    insert = insert.substring(0, max) + "..."; //$NON-NLS-1$
                                }
                                if (fFirst) {
                                    getDetailDocument().set(insert);
                                    fFirst = false;
                                } else {
                                    getDetailDocument().replace(length, 0,insert);
                                }
                            } catch (BadLocationException e) {
                            	DsfUIPlugin.log(e);
                            }
                        }
                        return Status.OK_STATUS;
                    }
                };
                append.setSystem(true);
                append.schedule();
            }
            synchronized (this) {
                notifyAll();
            }
        }
    }
    
    /**
     * The model presentation used to produce the string details for a 
     * selected variable.
     */
    private String fDebugModelIdentifier;
    
    /**
     * Controls the status line while the details area has focus.
     * Displays the current cursor position in the text (line:character).
     */
    private StatusLineContributionItem fStatusLineItem;

    /**
     * The source viewer in which the computed string detail
     * of selected variables will be displayed.
     */
    private SourceViewer fSourceViewer;
    
    /**
     * The last selection displayed in the source viewer.
     */
    private IStructuredSelection fLastDisplayed = null;
    
    /**
     * Variables used to create the detailed information for a selection
     */
    private IDocument fDetailDocument;
    private DetailJob fDetailJob = null;
    private final String fPositionLabelPattern = MessagesForDetailPane.DetailPane_LabelPattern;
    private final PositionLabelValue fLineLabel = new PositionLabelValue();
    private final PositionLabelValue fColumnLabel = new PositionLabelValue();
    private final Object[] fPositionLabelPatternArguments = new Object[] {fLineLabel, fColumnLabel };
    private ICursorListener fCursorListener;
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
     */
    public Control createControl(Composite parent) {
        
        createSourceViewer(parent);
        
        if (isInView()){
            createViewSpecificComponents();
            createActions();
            DsfUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
            JFaceResources.getFontRegistry().addListener(this);
        }
        
        return fSourceViewer.getControl();
    }

    /**
     * Creates the source viewer in the given parent composite
     * 
     * @param parent Parent composite to create the source viewer in
     */
    private void createSourceViewer(Composite parent) {
        
        // Create & configure a SourceViewer
        fSourceViewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
        fSourceViewer.setDocument(getDetailDocument());
        fSourceViewer.getTextWidget().setFont(JFaceResources.getFont(IDsfDebugUIConstants.DETAIL_PANE_FONT));
        fSourceViewer.getTextWidget().setWordWrap(DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP));
        fSourceViewer.setEditable(false);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fSourceViewer.getTextWidget(), IDsfDebugUIConstants.DETAIL_PANE);
        Control control = fSourceViewer.getControl();
        GridData gd = new GridData(GridData.FILL_BOTH);
        control.setLayoutData(gd);  
    }

    /**
     * Creates listeners and other components that should only be added to the
     * source viewer when this detail pane is inside a view.
     */
    private void createViewSpecificComponents(){
        
        // Add a document listener so actions get updated when the document changes
        getDetailDocument().addDocumentListener(new IDocumentListener() {
            public void documentAboutToBeChanged(DocumentEvent event) {}
            public void documentChanged(DocumentEvent event) {
                updateSelectionDependentActions();
            }
        });
        
        // Add the selection listener so selection dependent actions get updated.
        fSourceViewer.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                updateSelectionDependentActions();
            }
        });
        
        // Add a focus listener to update actions when details area gains focus
        fSourceViewer.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                
                getViewSite().setSelectionProvider(fSourceViewer.getSelectionProvider());
                
                setGlobalAction(IDebugView.SELECT_ALL_ACTION, getAction(DETAIL_SELECT_ALL_ACTION));
                setGlobalAction(IDebugView.COPY_ACTION, getAction(DETAIL_COPY_ACTION));
                
                getViewSite().getActionBars().updateActionBars();
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                
                getViewSite().setSelectionProvider(null);
                
                setGlobalAction(IDebugView.SELECT_ALL_ACTION, null);
                setGlobalAction(IDebugView.COPY_ACTION, null);
                
                getViewSite().getActionBars().updateActionBars();
            }
        });
        
        // Create a status line item displaying the current cursor location
        fStatusLineItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$
        IStatusLineManager manager= getViewSite().getActionBars().getStatusLineManager();
        manager.add(fStatusLineItem);
        fSourceViewer.getTextWidget().addMouseListener(getCursorListener());
        fSourceViewer.getTextWidget().addKeyListener(getCursorListener());
        
        // Add a context menu to the detail area
        createDetailContextMenu(fSourceViewer.getTextWidget()); 
    }
    
    /**
     * Creates the actions to add to the context menu
     */
    private void createActions() {
       
        TextViewerAction textAction= new TextViewerAction(fSourceViewer, ITextOperationTarget.SELECT_ALL);
        textAction.configureAction(MessagesForDetailPane.DetailPane_Select_All, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ 
        textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.SELECT_ALL);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, IDsfDebugUIConstants.DETAIL_PANE_SELECT_ALL_ACTION);
        setAction(DETAIL_SELECT_ALL_ACTION, textAction);
        
        textAction= new TextViewerAction(fSourceViewer, ITextOperationTarget.COPY);
        textAction.configureAction(MessagesForDetailPane.DetailPane_Copy, "", "");  //$NON-NLS-1$ //$NON-NLS-2$
        textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, IDsfDebugUIConstants.DETAIL_PANE_COPY_ACTION);
        setAction(DETAIL_COPY_ACTION, textAction);
        
        setSelectionDependantAction(DETAIL_COPY_ACTION);
            
        updateSelectionDependentActions();
        
        IAction action = new DetailPaneWordWrapAction(fSourceViewer);
        setAction(DETAIL_WORD_WRAP_ACTION, action);
        
        action = new DetailPaneMaxLengthAction(fSourceViewer.getControl().getShell());
        setAction(DETAIL_MAX_LENGTH_ACTION,action);
    }
    
    /**
     * Create the context menu particular to the detail pane.  Note that anyone
     * wishing to contribute an action to this menu must use
     * <code>IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID</code> as the
     * <code>targetID</code> in the extension XML.
     */
    protected void createDetailContextMenu(Control menuControl) {
        MenuManager menuMgr= new MenuManager(); 
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillDetailContextMenu(mgr);
            }
        });
        Menu menu= menuMgr.createContextMenu(menuControl);
        menuControl.setMenu(menu);

        getViewSite().registerContextMenu(IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr, fSourceViewer.getSelectionProvider());
    }
    
    /**
    * Adds items to the detail pane's context menu including any extension defined
    * actions.
    * 
    * @param menu The menu to add the item to.
    */
    protected void fillDetailContextMenu(IMenuManager menu) {
        
        menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));
        menu.add(new Separator());
        menu.add(getAction(DETAIL_COPY_ACTION)); 
        menu.add(getAction(DETAIL_SELECT_ALL_ACTION));
        menu.add(new Separator());
        menu.add(getAction(DETAIL_WORD_WRAP_ACTION));
        menu.add(getAction(DETAIL_MAX_LENGTH_ACTION));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#display(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void display(IStructuredSelection selection) {
        
        if (selection == null){
            clearSourceViewer();
            return;
        }
                
        fLastDisplayed = selection;
                        
        if ( selection.isEmpty() || !(selection instanceof ITreeSelection) ) {
            clearSourceViewer();
            return;
        }
        
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IAdaptable) {
            IDebugModelProvider debugModelProvider = 
                (IDebugModelProvider)((IAdaptable)firstElement).getAdapter(IDebugModelProvider.class);
            if (debugModelProvider != null) {
                String[] ids = debugModelProvider.getModelIdentifiers();
                if (ids != null && ids.length > 0) {
                    setDebugModel(ids[0]);
                }
            }
        }
        
        synchronized (this) {
            if (fDetailJob != null) {
                fDetailJob.cancel();
            }
            IWorkbenchPart part = fWorkbenchPartSite.getPart(); 
            if (part instanceof IDebugView) {
                Viewer viewer = ((IDebugView)part).getViewer();
                Object input = viewer.getInput();
                if (input != null && viewer instanceof TreeModelViewer) {
                    TreeModelViewer treeModelViewer = (TreeModelViewer)viewer;
                    fDetailJob = new DetailJob(treeModelViewer.getPresentationContext(), input, 
                        (ITreeSelection)selection, null);
                    fDetailJob.schedule();
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#setFocus()
     */
    public boolean setFocus(){
        if (fSourceViewer != null){
            fSourceViewer.getTextWidget().setFocus();
            return true;
        }
        return false;
    }
   
    /*
     * 
     */
    public void dispose(){
        fActionMap.clear();
        fSelectionActions.clear();
        
        if (fDetailJob != null) fDetailJob.cancel();
        fDebugModelIdentifier = null; // Setting this to null makes sure the source viewer is reconfigured with the model presentation after disposal
        if (fSourceViewer != null && fSourceViewer.getControl() != null) fSourceViewer.getControl().dispose();
        
        if (isInView()){
            disposeUndoRedoAction(ITextEditorActionConstants.UNDO);
            disposeUndoRedoAction(ITextEditorActionConstants.REDO);
            
            getViewSite().getActionBars().getStatusLineManager().remove(fStatusLineItem);
            
            DsfUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
            JFaceResources.getFontRegistry().removeListener(this);  
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#getDescription()
     */
    public String getDescription() {
        return MessagesForDetailPane.NumberFormatDetailPane_Description;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#getID()
     */
    public String getID() {
        return ID;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#getName()
     */
    public String getName() {
        return MessagesForDetailPane.NumberFormatDetailPane_Name;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class required) {
        if (IFindReplaceTarget.class.equals(required)) {
            return fSourceViewer.getFindReplaceTarget();
        }
        if (ITextViewer.class.equals(required)) {
            return fSourceViewer;
        }
        return null;
    }
    
    /**
     * Lazily instantiate and return a Document for the detail pane text viewer.
     */
    protected IDocument getDetailDocument() {
        if (fDetailDocument == null) {
            fDetailDocument = new Document();
        }
        return fDetailDocument;
    }
    
    /**
     * Clears the source viewer, removes all text.
     */
    protected void clearSourceViewer(){
        if (fDetailJob != null) {
            fDetailJob.cancel();
        }
        fLastDisplayed = null;
        fDetailDocument.set(""); //$NON-NLS-1$
        fSourceViewer.setEditable(false);
    }

    /**
     * Configures the details viewer for the debug model
     * currently being displayed
     */
    protected void configureDetailsViewer() {
        SourceViewerConfiguration svc = new SourceViewerConfiguration();
        
        fSourceViewer.setEditable(false);
        fSourceViewer.unconfigure();
        fSourceViewer.configure(svc);
        
        if (isInView()){
            createUndoRedoActions();
        }
    }

    /**
     * @return The formatted string describing cursor position
     */
    protected String getCursorPosition() {
        
        if (fSourceViewer == null) {
            return ""; //$NON-NLS-1$
        }
        
        StyledText styledText= fSourceViewer.getTextWidget();
        int caret= styledText.getCaretOffset();
        IDocument document= fSourceViewer.getDocument();
    
        if (document == null) {
            return ""; //$NON-NLS-1$
        }
    
        try {
            
            int line= document.getLineOfOffset(caret);
    
            int lineOffset= document.getLineOffset(line);
            int tabWidth= styledText.getTabs();
            int column= 0;
            for (int i= lineOffset; i < caret; i++)
                if ('\t' == document.getChar(i)) {
                    column += tabWidth - (tabWidth == 0 ? 0 : column % tabWidth);
                } else {
                    column++;
                }
                    
            fLineLabel.fValue= line + 1;
            fColumnLabel.fValue= column + 1;
            return MessageFormat.format(fPositionLabelPattern, fPositionLabelPatternArguments);
            
        } catch (BadLocationException x) {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Returns this view's "cursor" listener to be installed on the view's
     * associated details viewer. This listener is listening to key and mouse button events.
     * It triggers the updating of the status line.
     * 
     * @return the listener
     */
    private ICursorListener getCursorListener() {
        if (fCursorListener == null) {
            fCursorListener= new ICursorListener() {
                
                public void keyPressed(KeyEvent e) {
                    fStatusLineItem.setText(getCursorPosition());
                }
                
                public void keyReleased(KeyEvent e) {
                }
                
                public void mouseDoubleClick(MouseEvent e) {
                }
                
                public void mouseDown(MouseEvent e) {
                }
                
                public void mouseUp(MouseEvent e) {
                    fStatusLineItem.setText(getCursorPosition());
                }
            };
        }
        return fCursorListener;
    }
    
    /**
     * Returns the identifier of the debug model being displayed
     * in this view, or <code>null</code> if none.
     * 
     * @return debug model identifier
     */
    protected String getDebugModel() {
        return fDebugModelIdentifier;
    }   

    /**
     * Sets the identifier of the debug model being displayed
     * in this view, or <code>null</code> if none.
     * 
     * @param id debug model identifier of the type of debug
     *  elements being displayed in this view
     */
    protected void setDebugModel(String id) {
        if (id != fDebugModelIdentifier) {
            fDebugModelIdentifier = id;
            configureDetailsViewer();
        }
    }
    
    /**
     * Creates this editor's undo/re-do actions.
     * <p>
     * Subclasses may override or extend.</p>
     *
     * @since 3.2
     */
    protected void createUndoRedoActions() {
        disposeUndoRedoAction(ITextEditorActionConstants.UNDO);
        disposeUndoRedoAction(ITextEditorActionConstants.REDO);
        IUndoContext undoContext= getUndoContext();
        if (undoContext != null) {
            // Use actions provided by global undo/re-do
            
            // Create the undo action
            OperationHistoryActionHandler undoAction= new UndoActionHandler(getViewSite(), undoContext);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(undoAction, IAbstractTextEditorHelpContextIds.UNDO_ACTION);
            undoAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.UNDO);
            setAction(ITextEditorActionConstants.UNDO, undoAction);
            setGlobalAction(ITextEditorActionConstants.UNDO, undoAction);           
            
            // Create the re-do action.
            OperationHistoryActionHandler redoAction= new RedoActionHandler(getViewSite(), undoContext);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(redoAction, IAbstractTextEditorHelpContextIds.REDO_ACTION);
            redoAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.REDO);
            setAction(ITextEditorActionConstants.REDO, redoAction);
            setGlobalAction(ITextEditorActionConstants.REDO, redoAction);
            
            getViewSite().getActionBars().updateActionBars();
        }
    }   
    
    /**
     * Disposes of the action with the specified ID
     * 
     * @param actionId the ID of the action to disposed
     */
    protected void disposeUndoRedoAction(String actionId) {
        OperationHistoryActionHandler action = (OperationHistoryActionHandler) getAction(actionId);
        if (action != null) {
            action.dispose();
            setAction(actionId, null);
        }
    }
    
    /**
     * Returns this editor's viewer's undo manager undo context.
     *
     * @return the undo context or <code>null</code> if not available
     * @since 3.2
     */
    private IUndoContext getUndoContext() {
        IUndoManager undoManager= fSourceViewer.getUndoManager();
        if (undoManager instanceof IUndoManagerExtension)
            return ((IUndoManagerExtension)undoManager).getUndoContext();
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName= event.getProperty();
        if (propertyName.equals(IDsfDebugUIConstants.DETAIL_PANE_FONT)) {
            fSourceViewer.getTextWidget().setFont(JFaceResources.getFont(IDsfDebugUIConstants.DETAIL_PANE_FONT));
        } else if (propertyName.equals(IDsfDebugUIConstants.PREF_MAX_DETAIL_LENGTH)) {
            display(fLastDisplayed);
        } else if (propertyName.equals(IDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP)) {
            fSourceViewer.getTextWidget().setWordWrap(DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP));
            getAction(DETAIL_WORD_WRAP_ACTION).setChecked(DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP));    
        }
    }
}
