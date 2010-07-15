/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *     Wind River Systems - adapted to work with platform Modules view (bug 210558)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.views.variables.details.AbstractDetailPane;
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
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * 
 */
public class ModuleDetailPane extends AbstractDetailPane implements IAdaptable, IPropertyChangeListener{

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
    public static final String NAME = "Module Viewer";
    public static final String DESCRIPTION = "A detail pane that is based on a source viewer.  Displays as text and has actions for assigning values, content assist and text modifications.";
    
    
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
        protected IStatus run(IProgressMonitor monitor) {
            fMonitor = monitor;
            
            String detail = ""; //$NON-NLS-1$
            if ( fElement instanceof ICModule ) {
                detail = getModuleDetail( ((ICModule)fElement) );
            }
            if ( fElement instanceof ICElement ) {
                detail = fElement.toString();
            }
            
            detailComputed(detail);
            return Status.OK_STATUS;
        }
        
        private void detailComputed(final String result) {
            if (!fMonitor.isCanceled()) {
                WorkbenchJob setDetail = new WorkbenchJob("set details") { //$NON-NLS-1$
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
    
    private String getModuleDetail( ICModule module ) {
        StringBuffer sb = new StringBuffer();
        
        // Type
        String type = null;
        switch( module.getType() ) {
            case ICModule.EXECUTABLE:
                type = ModulesMessages.getString( "ModulesView.1" ); //$NON-NLS-1$
                break;
            case ICModule.SHARED_LIBRARY:
                type = ModulesMessages.getString( "ModulesView.2" ); //$NON-NLS-1$
                break;
        }
        if ( type != null ) {
            sb.append( ModulesMessages.getString( "ModulesView.3" ) ); //$NON-NLS-1$
            sb.append( type );
            sb.append( '\n' );
        }
        
        // Symbols flag
        sb.append( ModulesMessages.getString( "ModulesView.4" ) ); //$NON-NLS-1$
        sb.append( ( module.areSymbolsLoaded() ) ? ModulesMessages.getString( "ModulesView.5" ) : ModulesMessages.getString( "ModulesView.6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append( '\n' );

        // Symbols file
        sb.append( ModulesMessages.getString( "ModulesView.7" ) ); //$NON-NLS-1$
        sb.append( module.getSymbolsFileName().toOSString() );
        sb.append( '\n' );

        // CPU
        String cpu = module.getCPU();
        if ( cpu != null ) {
            sb.append( ModulesMessages.getString( "ModulesView.8" ) ); //$NON-NLS-1$
            sb.append( cpu );
            sb.append( '\n' );
        }

        // Base address
        IAddress baseAddress = module.getBaseAddress();
        if ( !baseAddress.isZero() ) {
            sb.append( ModulesMessages.getString( "ModulesView.9" ) ); //$NON-NLS-1$
            sb.append( baseAddress.toHexAddressString() );
            sb.append( '\n' );
        }
        
        // Size
        long size = module.getSize();
        if ( size > 0 ) { 
            sb.append( ModulesMessages.getString( "ModulesView.10" ) ); //$NON-NLS-1$
            sb.append( size );
            sb.append( '\n' );
        }

        return sb.toString();
    }

    
    /**
     * The source viewer in which the computed string detail
     * of selected modules will be displayed.
     */
    private SourceViewer fSourceViewer;
    
    /**
     * Variables used to create the detailed information for a selection
     */
    private IDocument fDetailDocument;
    private DetailJob fDetailJob = null;
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
     */
    public Control createControl(Composite parent) {
        
        createSourceViewer(parent);
        
        if (isInView()){
            createViewSpecificComponents();
            createActions();
            CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
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
        fSourceViewer.getTextWidget().setFont(JFaceResources.getFont(IInternalCDebugUIConstants.DETAIL_PANE_FONT));
        fSourceViewer.setEditable(false);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fSourceViewer.getTextWidget(), ICDebugHelpContextIds.MODULES_DETAIL_PANE);
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
            public void focusGained(FocusEvent e) {
                
                getViewSite().setSelectionProvider(fSourceViewer.getSelectionProvider());
                
                setGlobalAction(IDebugView.SELECT_ALL_ACTION, getAction(DETAIL_SELECT_ALL_ACTION));
                setGlobalAction(IDebugView.COPY_ACTION, getAction(DETAIL_COPY_ACTION));
                
                getViewSite().getActionBars().updateActionBars();
            }
            
            public void focusLost(FocusEvent e) {
                
                getViewSite().setSelectionProvider(null);
                
                setGlobalAction(IDebugView.SELECT_ALL_ACTION, null);
                setGlobalAction(IDebugView.CUT_ACTION, null);
                setGlobalAction(IDebugView.COPY_ACTION, null);
                setGlobalAction(IDebugView.PASTE_ACTION, null);
                setGlobalAction(IDebugView.FIND_ACTION, null);
                
                getViewSite().getActionBars().updateActionBars();
                
            }
        });
        
        // Add a context menu to the detail area
        createDetailContextMenu(fSourceViewer.getTextWidget()); 
    }
    
    /**
     * Creates the actions to add to the context menu
     */
    private void createActions() {
        
        TextViewerAction textAction= new TextViewerAction(fSourceViewer, ITextOperationTarget.SELECT_ALL);
        textAction.configureAction("Select &All", "", "");  //$NON-NLS-2$ //$NON-NLS-3$
        textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.SELECT_ALL);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, ICDebugHelpContextIds.MODULE_DETAIL_PANE_SELECT_ALL_ACTION);
        setAction(DETAIL_SELECT_ALL_ACTION, textAction);
        
        textAction= new TextViewerAction(fSourceViewer, ITextOperationTarget.COPY);
        textAction.configureAction("&Copy", "", "");  //$NON-NLS-2$ //$NON-NLS-3$
        textAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.COPY);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(textAction, ICDebugHelpContextIds.MODULE_DETAIL_PANE_COPY_ACTION);
        setAction(DETAIL_COPY_ACTION, textAction);
        
        setSelectionDependantAction(DETAIL_COPY_ACTION);
        
        updateSelectionDependentActions();
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
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#display(org.eclipse.jface.viewers.IStructuredSelection)
     */
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
        
        Object firstElement = selection.getFirstElement();
        if (firstElement != null && firstElement instanceof IDebugElement) {
            String modelID = ((IDebugElement)firstElement).getModelIdentifier();
        }
        
        synchronized (this) {
            if (fDetailJob != null) {
                fDetailJob.cancel();
            }
            fDetailJob = new DetailJob(selection.getFirstElement());
            fDetailJob.schedule();
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
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.details.AbstractDetailPane#dispose()
     */
    public void dispose(){
        super.dispose();
        
        if (fDetailJob != null) fDetailJob.cancel();
        if (fSourceViewer != null && fSourceViewer.getControl() != null) fSourceViewer.getControl().dispose();
        
        if (isInView()){
            CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
            JFaceResources.getFontRegistry().removeListener(this);  
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPane#getDescription()
     */
    public String getDescription() {
        return DESCRIPTION;
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
        return NAME;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class required) {
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
        fDetailDocument.set(""); //$NON-NLS-1$
        fSourceViewer.setEditable(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName= event.getProperty();
        if (propertyName.equals(IInternalCDebugUIConstants.DETAIL_PANE_FONT)) {
            fSourceViewer.getTextWidget().setFont(JFaceResources.getFont(IInternalCDebugUIConstants.DETAIL_PANE_FONT));
        } 
        
    }

    /**
     * Wrapper class that wraps around an IFindReplaceTarget.  Allows the detail pane to scroll
     * to text selected by the find/replace action.  The source viewer treats the text as a single
     * line, even when the text is wrapped onto several lines so the viewer will not scroll properly
     * on it's own.  See bug 178106.
     */
    class FindReplaceTargetWrapper implements IFindReplaceTarget{
        
        private IFindReplaceTarget fTarget;
        
        /**
         * Constructor
         * 
         * @param target find/replace target this class will wrap around.
         */
        public FindReplaceTargetWrapper(IFindReplaceTarget target){
            fTarget = target;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IFindReplaceTarget#canPerformFind()
         */
        public boolean canPerformFind() {
            return fTarget.canPerformFind();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IFindReplaceTarget#findAndSelect(int, java.lang.String, boolean, boolean, boolean)
         */
        public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
            int position = fTarget.findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord);
            // Explicitly tell the widget to show the selection because the viewer thinks the text is all on one line, even if wrapping is turned on.
            if (fSourceViewer != null){
                StyledText text = fSourceViewer.getTextWidget();
                if(text != null && !text.isDisposed()) {
                    text.showSelection();
                }
            }
            return position;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IFindReplaceTarget#getSelection()
         */
        public Point getSelection() {
            return fTarget.getSelection();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IFindReplaceTarget#getSelectionText()
         */
        public String getSelectionText() {
            return fTarget.getSelectionText();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IFindReplaceTarget#isEditable()
         */
        public boolean isEditable() {
            return fTarget.isEditable();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.text.IFindReplaceTarget#replaceSelection(java.lang.String)
         */
        public void replaceSelection(String text) {
            fTarget.replaceSelection(text);
        }
    }
    
}

