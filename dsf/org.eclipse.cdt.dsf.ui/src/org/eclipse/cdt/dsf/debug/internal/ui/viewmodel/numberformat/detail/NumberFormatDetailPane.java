/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems, Inc. - extended implementation
 *     Navid Mehregani (TI) - Bugzilla 310191 - Detail pane does not clear up when DSF-GDB session is terminated
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.numberformat.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.internal.ui.IDsfDebugHelpContextIds;
import org.eclipse.cdt.dsf.debug.internal.ui.IInternalDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.DetailPaneMaxLengthAction;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.DetailPaneWordWrapAction;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.MessagesForDetailPane;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.TextViewerAction;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat.FormattedValueVMUtil;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.concurrent.SimpleDisplayExecutor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMPropertiesUpdate;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.IDetailPane2;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
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
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.IUpdate;

public class NumberFormatDetailPane implements IDetailPane2, IAdaptable, IPropertyChangeListener {

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
    @Override
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
    protected static final String DETAIL_COPY_ACTION = ActionFactory.COPY.getId() + ".TextDetailPane"; //$NON-NLS-1$
    protected static final String DETAIL_SELECT_ALL_ACTION = IDebugView.SELECT_ALL_ACTION + ".TextDetailPane"; //$NON-NLS-1$
    protected static final String DETAIL_WORD_WRAP_ACTION = DsfUIPlugin.PLUGIN_ID + ".detail_pane_word_wrap"; //$NON-NLS-1$
    protected static final String DETAIL_MAX_LENGTH_ACTION = "MaxLength"; //$NON-NLS-1$
    
    /**
     * The ID, name and description of this pane are stored in constants so that the class
     * does not have to be instantiated to access them.
     */
    public static final String ID = "NumberFormatPane"; //$NON-NLS-1$
    
    /**
     * Useful shortened names for the internationalized strings.
     */
    public static String FORMAT_SEPARATOR = MessagesForNumberFormatDetailPane.NumberFormatDetailPane_format_separator__label;
    public static String NAME    = MessagesForNumberFormatDetailPane.NumberFormatDetailPane_Name_label;
    public static String SPACES  = MessagesForNumberFormatDetailPane.NumberFormatDetailPane_Spaces_label;   
    public static String CRLF    = MessagesForNumberFormatDetailPane.NumberFormatDetailPane_CarriageReturn_label;
    public static String DOTS    = MessagesForNumberFormatDetailPane.NumberFormatDetailPane_DotDotDot_label;
    
    /**
     * Job to compute the details for a selection
     */
    class DetailJob extends Job implements IValueDetailListener {
        private IPresentationContext fPresentationContext;
        private Object fViewerInput;
        private ITreeSelection fElements;
        private boolean fFirst = true;
        private IProgressMonitor fMonitor;
        
        public DetailJob(IPresentationContext context, Object viewerInput, ITreeSelection elements, 
                        IDebugModelPresentation model) 
        {
            super("compute variable details"); //$NON-NLS-1$
            setSystem(true);
            fPresentationContext = context;
            fViewerInput = viewerInput;
            fElements = elements;
        }
                
        public IProgressMonitor getDetailMonitor() {
        	return fMonitor;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        protected IStatus run(final IProgressMonitor monitor) {
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
                
                final IElementPropertiesProvider propertiesProvider = 
                    (IElementPropertiesProvider)DebugPlugin.getAdapter(element, IElementPropertiesProvider.class);
                
                Display display;
                if (fWorkbenchPartSite != null) {
                	display = fWorkbenchPartSite.getShell().getDisplay();
                } else {
                	display = PlatformUI.getWorkbench().getDisplay();
                }
				final Executor executor = SimpleDisplayExecutor.getSimpleDisplayExecutor(display);
                Set<String> properties = new HashSet<String>(1);
                properties.add(IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS);
                propertiesProvider.update(new IPropertiesUpdate[] { new VMPropertiesUpdate(
                    properties, path, fViewerInput, fPresentationContext, 
                    new DataRequestMonitor<Map<String,Object>>(executor, null) {
                        @Override
                        protected void handleCompleted() {
                        	
                        	// Bugzilla 310191: Detail pane does not clear up when DSF-GDB session is terminated
                        	if (fMonitor.isCanceled())
                        		return;
                        	
                            Set<String> properties = new HashSet<String>(1);
                            properties.add(IElementPropertiesProvider.PROP_NAME);
                            final String[] formats = (String[])getData().get(
                                IDebugVMConstants.PROP_FORMATTED_VALUE_AVAILABLE_FORMATS);
                            if (formats != null) {
                                for (String format : formats) {
                                    properties.add(FormattedValueVMUtil.getPropertyForFormatId(format, null));
                                }
                            }
                            
                            propertiesProvider.update(new IPropertiesUpdate[] { new VMPropertiesUpdate(
                                properties, path, fViewerInput, fPresentationContext,
                                new DataRequestMonitor<Map<String,Object>>(executor, null) {
                                    @Override
                                    protected void handleSuccess() {
                                        StringBuffer finalResult = new StringBuffer();
                                        finalResult.append(NAME).append(getData().get(IElementPropertiesProvider.PROP_NAME)).append(CRLF);

                                        if (formats != null) {
                                        	for (int i = 0; i < formats.length; i++) {
                                        		String formatId = formats[i];
                                        		finalResult.append(SPACES);
                                        		finalResult.append( FormattedValueVMUtil.getFormatLabel(formatId) );
                                        		finalResult.append(FORMAT_SEPARATOR);
                                        		finalResult.append( getData().get(FormattedValueVMUtil.getPropertyForFormatId(formatId, null)) );                                            
                                        		if ( i < formats.length + 1 ) {
                                        			finalResult.append(CRLF); 
                                        		}
                                        	}
                                        }
                                        detailComputed(null, finalResult.toString());
                                    }
                                    
                                    @Override
                                    protected void handleErrorOrWarning() {
                                        detailComputed(null, getStatus().getMessage());  
                                    };
                                }) 
                            });
                        }
                    }) 
                });
                continue;
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
        @Override
		public void detailComputed(IValue value, final String result) {
        	synchronized (this) {
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
                                insert = CRLF + result;
                            }
                            try {
                                int max = DsfUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
                                if (max > 0 && insert.length() > max) {
                                    insert = insert.substring(0, max) + DOTS;
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
     * The text viewer in which the computed string detail
     * of selected variables will be displayed.
     */
    private TextViewer fTextViewer;
    
    /**
     * The last selection displayed in the text viewer.
     */
    private IStructuredSelection fLastDisplayed = null;
    
    /**
     * Variables used to create the detailed information for a selection
     */
    private IDocument fDetailDocument;
    private DetailJob fDetailJob = null;
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public Control createControl(Composite parent) {
        
        createTextViewer(parent);
        
        if (isInView()){
            createViewSpecificComponents();
            createActions();
            DsfUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
            JFaceResources.getFontRegistry().addListener(this);
        }
        
        return fTextViewer.getControl();
    }

    /**
     * Creates the text viewer in the given parent composite
     * 
     * @param parent Parent composite to create the text viewer in
     */
    private void createTextViewer(Composite parent) {
        
        // Create & configure a TextViewer
        fTextViewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        fTextViewer.setDocument(getDetailDocument());
        fTextViewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
        fTextViewer.getTextWidget().setWordWrap(DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP));
        fTextViewer.setEditable(false);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fTextViewer.getTextWidget(), IDsfDebugHelpContextIds.DETAIL_PANE);
        Control control = fTextViewer.getControl();
        GridData gd = new GridData(GridData.FILL_BOTH);
        control.setLayoutData(gd);  
    }

    /**
     * Creates listeners and other components that should only be added to the
     * text viewer when this detail pane is inside a view.
     */
    private void createViewSpecificComponents(){
        
        // Add a document listener so actions get updated when the document changes
        getDetailDocument().addDocumentListener(new IDocumentListener() {
            @Override
			public void documentAboutToBeChanged(DocumentEvent event) {}
            @Override
			public void documentChanged(DocumentEvent event) {
                updateSelectionDependentActions();
            }
        });
        
        // Add the selection listener so selection dependent actions get updated.
        fTextViewer.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
			public void selectionChanged(SelectionChangedEvent event) {
                updateSelectionDependentActions();
            }
        });
        
        // Add a focus listener to update actions when details area gains focus
        fTextViewer.getControl().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                
                setGlobalAction(IDebugView.SELECT_ALL_ACTION, getAction(DETAIL_SELECT_ALL_ACTION));
                setGlobalAction(IDebugView.COPY_ACTION, getAction(DETAIL_COPY_ACTION));
                
                getViewSite().getActionBars().updateActionBars();
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                
                setGlobalAction(IDebugView.SELECT_ALL_ACTION, null);
                setGlobalAction(IDebugView.COPY_ACTION, null);
                
                getViewSite().getActionBars().updateActionBars();
            }
        });
        
        // Add a context menu to the detail area
        createDetailContextMenu(fTextViewer.getTextWidget()); 
    }
    
    /**
     * Creates the actions to add to the context menu
     */
    private void createActions() {
       
        TextViewerAction textAction= new TextViewerAction(fTextViewer, ITextOperationTarget.SELECT_ALL);
        textAction.configureAction(MessagesForDetailPane.DetailPane_Select_All, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ 
        textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, IDsfDebugHelpContextIds.DETAIL_PANE_SELECT_ALL_ACTION);
        setAction(DETAIL_SELECT_ALL_ACTION, textAction);
        
        textAction= new TextViewerAction(fTextViewer, ITextOperationTarget.COPY);
        textAction.configureAction(MessagesForDetailPane.DetailPane_Copy, "", "");  //$NON-NLS-1$ //$NON-NLS-2$
        textAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, IDsfDebugHelpContextIds.DETAIL_PANE_COPY_ACTION);
        setAction(DETAIL_COPY_ACTION, textAction);
        
        setSelectionDependantAction(DETAIL_COPY_ACTION);
            
        updateSelectionDependentActions();
        
        IAction action = new DetailPaneWordWrapAction(fTextViewer);
        setAction(DETAIL_WORD_WRAP_ACTION, action);
        
        action = new DetailPaneMaxLengthAction(fTextViewer.getControl().getShell());
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
            @Override
			public void menuAboutToShow(IMenuManager mgr) {
                fillDetailContextMenu(mgr);
            }
        });
        Menu menu= menuMgr.createContextMenu(menuControl);
        menuControl.setMenu(menu);

        getViewSite().registerContextMenu(IDebugUIConstants.VARIABLE_VIEW_DETAIL_ID, menuMgr, fTextViewer.getSelectionProvider());
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
    @Override
	public void display(IStructuredSelection selection) {
        
        if (selection == null){
            clearTextViewer();
            return;
        }
                
        fLastDisplayed = selection;
                        
        if ( selection.isEmpty() || !(selection instanceof ITreeSelection) ) {
            clearTextViewer();
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
            if (fWorkbenchPartSite != null) {
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
            } else if (firstElement instanceof IDMVMContext) {
            	IVMNode vmNode = ((IDMVMContext) firstElement).getVMNode();
            	if (vmNode != null) {
            	    Object input = firstElement;
	            	IVMProvider vmProvider = vmNode.getVMProvider();
	                final IPresentationContext context= vmProvider.getPresentationContext();
	                if (IDsfDebugUIConstants.ID_EXPRESSION_HOVER.equals(context.getId())) {
	                    // magic access to viewer input - see ExpressionVMProvider
	                    input = context.getProperty("__viewerInput"); //$NON-NLS-1$
	                }
                    fDetailJob = new DetailJob(context, input, (ITreeSelection)selection, null);
	                fDetailJob.schedule();
            	}
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#setFocus()
     */
    @Override
	public boolean setFocus(){
        if (fTextViewer != null){
            fTextViewer.getTextWidget().setFocus();
            return true;
        }
        return false;
    }
   
    /*
     * 
     */
    @Override
	public void dispose(){
        fActionMap.clear();
        fSelectionActions.clear();
        
        if (fDetailJob != null) fDetailJob.cancel();
        fDebugModelIdentifier = null; // Setting this to null makes sure the text viewer is reconfigured with the model presentation after disposal
        if (fTextViewer != null && fTextViewer.getControl() != null) {
        	fTextViewer.getControl().dispose();
        	fTextViewer = null;
        }
        
        if (isInView()){
            DsfUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
            JFaceResources.getFontRegistry().removeListener(this);  
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#getDescription()
     */
    @Override
	public String getDescription() {
        return MessagesForDetailPane.NumberFormatDetailPane_Description;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#getID()
     */
    @Override
	public String getID() {
        return ID;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#getName()
     */
    @Override
	public String getName() {
        return MessagesForDetailPane.NumberFormatDetailPane_Name;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
	@SuppressWarnings("rawtypes")
    public Object getAdapter(Class required) {
        if (IFindReplaceTarget.class.equals(required)) {
            return fTextViewer.getFindReplaceTarget();
        }
        if (ITextViewer.class.equals(required)) {
            return fTextViewer;
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
     * Clears the text viewer, removes all text.
     */
    protected void clearTextViewer(){
        if (fDetailJob != null) {
            fDetailJob.cancel();
            
            // Bugzilla 310191: Detail pane does not clear up when DSF-GDB session is terminated
            IProgressMonitor progressMonitor = fDetailJob.getDetailMonitor();
            if (progressMonitor!=null)
            	progressMonitor.setCanceled(true);
        }
        fLastDisplayed = null;
        fDetailDocument.set(""); //$NON-NLS-1$
        fTextViewer.setEditable(false);
    }

    /**
     * Configures the details viewer for the debug model
     * currently being displayed
     */
    protected void configureDetailsViewer() {
        fTextViewer.setEditable(false);
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
	public void propertyChange(PropertyChangeEvent event) {
        String propertyName= event.getProperty();
        if (propertyName.equals(IDebugUIConstants.PREF_DETAIL_PANE_FONT)) {
            fTextViewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
        } else if (propertyName.equals(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH)) {
            display(fLastDisplayed);
        } else if (propertyName.equals(IInternalDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP)) {
            fTextViewer.getTextWidget().setWordWrap(DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP));
            getAction(DETAIL_WORD_WRAP_ACTION).setChecked(DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP));    
        }
    }

	/*
	 * @see org.eclipse.debug.ui.IDetailPane2#getSelectionProvider()
	 */
	@Override
	public ISelectionProvider getSelectionProvider() {
		if (fTextViewer != null) {
			return fTextViewer.getSelectionProvider();
		}
		return null;
	}
}
