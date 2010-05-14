/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *     Wind River Systems -  adopted to use with Modules view
 *     Ericsson AB		  -  Modules view for DSF implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules.detail;


import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.ui.IDsfDebugHelpContextIds;
import org.eclipse.cdt.dsf.debug.internal.ui.IInternalDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.MessagesForDetailPane;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.TextViewerAction;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMData;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


/**
 * 
 */
public class ModuleDetailPane extends ModulesAbstractDetailPane implements IAdaptable, IPropertyChangeListener {

    /**
     * These are the IDs for the actions in the context menu
     */
    protected static final String DETAIL_COPY_ACTION = ActionFactory.COPY.getId() + ".SourceDetailPane"; //$NON-NLS-1$
    protected static final String DETAIL_SELECT_ALL_ACTION = IDebugView.SELECT_ALL_ACTION + ".SourceDetailPane"; //$NON-NLS-1$
    
    /**
     * The ID, name and description of this pane are stored in constants so that the class
     * does not have to be instantiated to access them.
     */
    public static final String ID = "ModuleDetailPane"; //$NON-NLS-1$
    public static final String NAME = "Module Viewer"; //$NON-NLS-1$
    public static final String DESCRIPTION = "A detail pane that is based on a source viewer.  Displays as text and has actions for assigning values, content assist and text modifications."; //$NON-NLS-1$
	
    
    /**
     * The source viewer in which the computed string detail
     * of selected modules will be displayed.
     */
    private SourceViewer fSourceViewer;
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
    
    private DetailJob fDetailJob = null;
	public void display(IStructuredSelection selection) {
        if (selection == null){
            clearSourceViewer();
            return;
        }
                
        if (isInView()){
            fSourceViewer.setEditable(true);
        }
                        
        if (selection.isEmpty()){
            clearSourceViewer();
            return;
        }
        
        synchronized (this) {
            if (fDetailJob != null) {
                fDetailJob.cancel();
            }
            fDetailJob = new DetailJob(selection.getFirstElement());
            fDetailJob.schedule();
        }
		
	}
	
    /**
     * Clears the source viewer, removes all text.
     */
    protected void clearSourceViewer(){
        if (fDetailJob != null) {
            fDetailJob.cancel();
        }
        fDetailDocument.set(""); //$NON-NLS-1$
        fSourceViewer.setEditable(false);
    }
    
	@Override
	public void dispose() {
		super.dispose();
        if (fDetailJob != null) fDetailJob.cancel();
        if (fSourceViewer != null && fSourceViewer.getControl() != null) fSourceViewer.getControl().dispose();
        
        if (isInView()){
        	DsfUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
            JFaceResources.getFontRegistry().removeListener(this);  
        }
		
	}
	public String getDescription() {
        return DESCRIPTION;
	}
	public String getID() {
        return ID;
	}
	public String getName() {
        return NAME;
	}
	
	public boolean setFocus() {
        if (fSourceViewer != null){
            fSourceViewer.getTextWidget().setFocus();
            return true;
        }
        return false;
	}
	
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
        if (ITextViewer.class.equals(adapter)) {
            return fSourceViewer;
        }
        return null;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
        String propertyName= event.getProperty();
        if (propertyName.equals(IDebugUIConstants.PREF_DETAIL_PANE_FONT)) {
            fSourceViewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
        }
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
        fSourceViewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
        fSourceViewer.getTextWidget().setWordWrap(DsfUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP));
        fSourceViewer.setEditable(false);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fSourceViewer.getTextWidget(), IDsfDebugHelpContextIds.DETAIL_PANE);
        Control control = fSourceViewer.getControl();
        GridData gd = new GridData(GridData.FILL_BOTH);
        control.setLayoutData(gd);  
    } 
    
    /**
     * Variables used to create the detailed information for a selection
     */
    private IDocument fDetailDocument;

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
        
        // Add a context menu to the detail area
        createDetailContextMenu(fSourceViewer.getTextWidget()); 
    }

    /**
     * Create the context menu particular to the detail pane.  Note that anyone
     * wishing to contribute an action to this menu must use
     * <code>ICDebugUIConstants.MODULES_VIEW_DETAIL_ID</code> as the
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

        getViewSite().registerContextMenu(ICDebugUIConstants.MODULES_VIEW_DETAIL_ID, menuMgr, fSourceViewer.getSelectionProvider());

    }
    /**
     * Adds items to the detail pane's context menu including any extension defined
     * actions.
     * 
     * @param menu The menu to add the item to.
     */
     protected void fillDetailContextMenu(IMenuManager menu) {
         
         menu.add(new Separator(ICDebugUIConstants.MODULES_GROUP));
         menu.add(new Separator());
         menu.add(getAction(DETAIL_COPY_ACTION)); 
         menu.add(getAction(DETAIL_SELECT_ALL_ACTION));
         menu.add(new Separator());
         menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
         
     }
    /**
     * Creates the actions to add to the context menu
     */
    private void createActions() {
        TextViewerAction textAction= new TextViewerAction(fSourceViewer, ITextOperationTarget.SELECT_ALL);
        textAction.configureAction(MessagesForDetailPane.DetailPane_Select_All, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ 
        textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.SELECT_ALL);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, IDsfDebugHelpContextIds.DETAIL_PANE_SELECT_ALL_ACTION);
        setAction(DETAIL_SELECT_ALL_ACTION, textAction);
        
        textAction= new TextViewerAction(fSourceViewer, ITextOperationTarget.COPY);
        textAction.configureAction(MessagesForDetailPane.DetailPane_Copy, "", "");  //$NON-NLS-1$ //$NON-NLS-2$
        textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, IDsfDebugHelpContextIds.DETAIL_PANE_COPY_ACTION);
        setAction(DETAIL_COPY_ACTION, textAction);

        setSelectionDependantAction(DETAIL_COPY_ACTION);
        
        updateSelectionDependentActions();
    }
    
    
    /**
     * Job to compute the details for a selection
     */
    class DetailJob extends Job {
        
        private Object fElement;
        // whether a result was collected
        private IProgressMonitor fMonitor;
        
        public DetailJob(Object element) {
            super("compute module details"); //$NON-NLS-1$
            setSystem(true);
            fElement = element;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
		protected IStatus run(IProgressMonitor monitor) {
            fMonitor = monitor;
            /*
             *  Make sure this is an element we want to deal with.
             */
            IModuleDMContext dmc = null;
            if (fElement instanceof IDMVMContext) {
                IDMContext vmcdmc = ((IDMVMContext)fElement).getDMContext();
                dmc = DMContexts.getAncestorOfType(vmcdmc, IModuleDMContext.class);
            }
            
            if (dmc == null) return Status.OK_STATUS;
            DsfSession session = DsfSession.getSession(dmc.getSessionId());
            if (session == null) return Status.OK_STATUS;
            
            /*
             * Create the query to write the value to the service. Note: no need to
             * guard against RejectedExecutionException, because
             * DsfSession.getSession() above would only return an active session.
             */
            GetModuleDetailsQuery query = new GetModuleDetailsQuery(dmc);
            session.getExecutor().execute(query);

            /*
             * Now we have the data, go and get it. Since the call is completed now
             * the ".get()" will not suspend it will immediately return with the
             * data.
             */
            try {
                detailComputed(getModuleDetail((IModuleDMData) query.get()));
            } catch (InterruptedException e) {
                assert false;
                return Status.OK_STATUS;
            } catch (ExecutionException e) {
                return Status.OK_STATUS;
            }
            return Status.OK_STATUS;
        }
        
        /**
         * Set the module details in the detail pane view
         * @param result
         */
		private void detailComputed(final String result) {
            if (!fMonitor.isCanceled()) {
                WorkbenchJob setDetail = new WorkbenchJob("set details") { //$NON-NLS-1$
                    @Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (!fMonitor.isCanceled()) {
                            getDetailDocument().set(result);
                        }
                        return Status.OK_STATUS;
                    }
                };
                setDetail.setSystem(true);
                setDetail.schedule();
            }
        }

    }
    
    /**
     * To get the details of the given module selected in Modules View 
     * @param module
     * @return
     */
    private String getModuleDetail( IModuleDMData module ) {
        StringBuffer sb = new StringBuffer();
        
        // Type
        String type = null;
//        switch( module.getType() ) {
//            case ICModule.EXECUTABLE:
//                type = ModulesMessages.getString( "ModulesView.Executable" ); //$NON-NLS-1$
//                break;
//            case ICModule.SHARED_LIBRARY:
//                type = ModulesMessages.getString( "ModulesView.SharedLibrary" ); //$NON-NLS-1$
//                break;
//        }
        type = ModulesMessages.getString( "ModulesView.SharedLibrary" ); //$NON-NLS-1$
        if ( type != null ) {
            sb.append( ModulesMessages.getString( "ModulesView.Type" ) ); //$NON-NLS-1$
            sb.append( type );
            sb.append( '\n' );
        }
        
        // Symbols flag
        sb.append( ModulesMessages.getString( "ModulesView.Symbols" ) ); //$NON-NLS-1$
        sb.append( ( module.isSymbolsLoaded()) ? ModulesMessages.getString( "ModulesView.Loaded" ) : ModulesMessages.getString( "ModulesView.NotLoaded" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( '\n' );

        // Symbols file
        sb.append( ModulesMessages.getString( "ModulesView.SymbolsFile" ) ); //$NON-NLS-1$
        sb.append( module.getFile());
        sb.append( '\n' );
        
        // Base address
        String baseAddress = module.getBaseAddress();
        sb.append( ModulesMessages.getString( "ModulesView.BaseAddress" ) ); //$NON-NLS-1$
        sb.append( baseAddress );
        sb.append( '\n' );
            
        // Size
        long size = module.getSize();
        if ( size > 0 ) { 
            sb.append( ModulesMessages.getString( "ModulesView.Size" ) ); //$NON-NLS-1$
            sb.append( size );
            sb.append( '\n' );
        }

        return sb.toString();
    }

    
    public class GetModuleDetailsQuery extends Query<Object> {

        private IModuleDMContext fDmc;

        public GetModuleDetailsQuery(IModuleDMContext dmc) {
            super();
            fDmc = dmc;
        }

        @Override
        protected void execute(final DataRequestMonitor<Object> rm) {
            /*
             * We're in another dispatch, so we must guard against executor
             * shutdown again.
             */
            final DsfSession session = DsfSession.getSession(fDmc.getSessionId());
            if (session == null) {
                rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Debug session already shut down.", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            /*
             * Guard against a disposed service
             */
            DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), fDmc.getSessionId());
            IModules service = tracker.getService(IModules.class);
            tracker.dispose();
            if (service == null) {
                rm .setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Service unavailable", null)); //$NON-NLS-1$
                rm.done();
                return;
            }

            service.getModuleData(fDmc, new DataRequestMonitor<IModuleDMData>( session.getExecutor(), rm) {
                @Override
                protected void handleCompleted() {
                    /*
                     * We're in another dispatch, so we must guard against executor shutdown again.
                     */
                    if (!DsfSession.isSessionActive(session.getId())) {
                        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE, "Debug session already shut down.", null)); //$NON-NLS-1$
                        rm.done();
                        return;
                    }
                    super.handleCompleted();
                }

                @Override
                protected void handleSuccess() {
                    /*
                     * All good set return value.
                     */
                    rm.setData(getData());
                    rm.done();
                }
            });
        }
    }
}

