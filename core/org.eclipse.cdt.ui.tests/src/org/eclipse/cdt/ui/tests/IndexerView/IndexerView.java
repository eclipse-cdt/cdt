/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.IndexerView;

import java.io.IOException;

import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.PropertySheet;

/**
 * @author dsteffle
 */
public class IndexerView extends ViewPart {
    private static final int DEFAULT_INDEXER_SIZE = 1;
    private static final String SWITCH_FULL_NAMES = "Switch Full Names"; //$NON-NLS-1$
    private static final String SORT_RESULTS = "Sort Results"; //$NON-NLS-1$
    private static final String SEARCH_LOCATIONS = "Search Locations"; //$NON-NLS-1$
    private static final String _TOTAL_IENTRYRESULTS = " total IEntryResults"; //$NON-NLS-1$
    private static final String _FILTERED_IENTRY_RESULTS_ = " filtered IEntry Results\n"; //$NON-NLS-1$
    private static final String INDEXER_STATS = "Indexer Stats"; //$NON-NLS-1$
    private static final String DISPLAY_INDEX_STATS = "Display Index Stats"; //$NON-NLS-1$
    private static final String INDEXER_VIEW___ = "Indexer View - "; //$NON-NLS-1$
    private static final String _INDEXER_MENU_MANAGER = "#Indexer_Menu_Manager"; //$NON-NLS-1$
    private static final String SET_FILTERS = "Set Filters"; //$NON-NLS-1$
    private static final String NEXT_PAGE = "Next Page"; //$NON-NLS-1$
    private static final String PREVIOUS_PAGE = "Previous Page"; //$NON-NLS-1$
    public static final String VIEW_ID = "org.eclipse.cdt.ui.tests.IndexerView"; //$NON-NLS-1$
    private static final String PROPERTIES_VIEW = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$
    protected static final String BLANK_STRING = ""; //$NON-NLS-1$
    static TableViewer viewer;
    protected Action searchLocationAction;
    protected Action previousPageAction;
    protected Action nextPageAction;
    protected Action singleClickAction;
    protected Action setFiltersAction;
    protected Action sortAction;
    protected Action displayFullNameAction;
    protected Action displayStatsAction;
    protected IIndexer[] indexers =  new IIndexer[DEFAULT_INDEXER_SIZE]; // support 1 indexer for now new IIndexer[CTestPlugin.getWorkspace().getRoot().getProjects().length];
    protected IProject project = null;
    
    protected static ViewContentProvider.StartInitializingIndexerView initializeIndexerViewJob = null;

    public class ViewContentProvider implements IStructuredContentProvider,
            ITreeContentProvider {

        private static final String POPULATING_INDEXER_VIEW = "populating indexer view"; //$NON-NLS-1$
        protected IndexerNodeParent invisibleRoot;
        
        protected boolean displayForwards=false;
        protected boolean displayBackwards=false;
        
        private class InitializeView extends Job {
            private static final String NULL_PROJECT_SELECTED = "A null project was selected."; //$NON-NLS-1$
            private static final String ALL_NAME_SEARCH = "*"; //$NON-NLS-1$
            private static final String INDEXER_VIEW = "Indexer View"; //$NON-NLS-1$
            TableViewer theViewer = null;
            
            public InitializeView(String name, TableViewer viewer) {
                super(name);
                this.theViewer = viewer;
            }

            protected IStatus run(IProgressMonitor monitor) {
                
                for(int i=0; i<indexers.length; i++) {
                    if (indexers[i] instanceof ICDTIndexer) {
                        if (project == null) {
                            CTestPlugin.getStandardDisplay().asyncExec(new Runnable() {
                                public void run() {
                                    MessageDialog.openInformation(theViewer.getControl().getShell(), INDEXER_VIEW,
                                    NULL_PROJECT_SELECTED);        
                                }
                            });
                            
                            return Status.CANCEL_STATUS;
                        }
                        
                        IIndex index = ((ICDTIndexer)indexers[i]).getIndex(project.getFullPath(), true, true);
                        
                        if (index==null) return Status.CANCEL_STATUS;
                        
                        try {                         
                            IEntryResult[] results = index.getEntries(IIndex.ANY, IIndex.ANY, IIndex.ANY );
                            if (results == null) return Status.CANCEL_STATUS;
                            
                            int size = results.length; 
                            IndexerNodeLeaf[] children = new IndexerNodeLeaf[size];
                            for(int j=0; j<size; j++) {
                                children[j] = new IndexerNodeLeaf(results[j], index.getIndexFile());
                                children[j].setParent(invisibleRoot);
                            }
                            
                            invisibleRoot.setChildren(children);
                            
                            invisibleRoot.setIsForward(true); // initial display direction is forward
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                return Status.OK_STATUS;
            }
        }
        
        public class InitializeRunnable implements Runnable {
            TableViewer view = null;
            boolean updateView = true;
            
            public InitializeRunnable(TableViewer view, boolean updateView) {
                this.view = view;
                this.updateView = updateView;
            }
            
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            public void run() {
                if (!updateView) return; 
                
                enableButtons(false);
                view.refresh();
                
                if (view.getTable().getItems().length > 0) {
                    TableItem[] selection = new TableItem[1];
                    selection[0] = view.getTable().getItems()[0];
                    
                    // select the first item to prevent it from being selected accidentally (and possibly switching editors accidentally)
                    view.getTable().setSelection(selection);
                }
                
                enableButtons(true);
                previousPageAction.setEnabled(displayBackwards);
                nextPageAction.setEnabled(displayForwards);
            }
        }
        
        private class StartInitializingIndexerView extends Job {
            private static final String INITIALIZE_INDEXER_VIEW = "initialize Indexer View"; //$NON-NLS-1$
            InitializeView job = null;
            boolean updateView=true;
            
            public StartInitializingIndexerView(InitializeView job, boolean updateView) {
                super(INITIALIZE_INDEXER_VIEW);
                this.job = job;
                this.updateView = updateView;
            }
            
            protected IStatus run(IProgressMonitor monitor) {
                job.schedule();
                
                try {
                    job.join();
                } catch (InterruptedException ie) {
                    return Status.CANCEL_STATUS;
                }
                    
                CTestPlugin.getStandardDisplay().asyncExec(new InitializeRunnable(viewer, updateView)); // update the view from the Display thread
                
                updateView=true;
                
                return job.getResult();
            }
        }
        
        public ViewContentProvider() {
            this(null, false, false);
        }
        
        public void setDisplayForwards(boolean displayForwards) {
            this.displayForwards = displayForwards;
        }

        public void setDisplayBackwards(boolean displayBackwards) {
            this.displayBackwards = displayBackwards;
        }
        
        public ViewContentProvider(IndexerNodeParent parent, boolean displayForwards, boolean displayBackwards) {
            if (parent == null) {
                invisibleRoot = new IndexerNodeParent(null, null, this);
                initializeIndexerViewJob = new StartInitializingIndexerView(new InitializeView(POPULATING_INDEXER_VIEW, viewer), true);
                initializeIndexerViewJob.schedule();    
            } else {
                invisibleRoot = parent;
                initializeIndexerViewJob = new StartInitializingIndexerView(new InitializeView(POPULATING_INDEXER_VIEW, viewer), false);
                initializeIndexerViewJob.schedule();
            }
            
            invisibleRoot.reset();
            this.displayForwards=displayForwards;
            this.displayBackwards=displayBackwards;
        }
        
        public Object[] getElements(Object inputElement) {
            if (inputElement.equals(getViewSite())) {
                return getChildren(invisibleRoot);
             }
             return getChildren(inputElement);
        }

        public void dispose() {}

        public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IndexerNodeParent) {
                return ((IndexerNodeParent) parentElement).getChildren();
             }
             return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof IndexerNodeLeaf) {
                return ((IndexerNodeLeaf) element).getParent();
             }
             return null;
        }

        public boolean hasChildren(Object element) {
            if (element instanceof IndexerNodeParent)
                return ((IndexerNodeParent) element).hasChildren();
             return false;
        }
        
        public IndexerNodeParent getInvisibleRoot() {
            return invisibleRoot;
        }

        public boolean isDisplayForwards() {
            return displayForwards;
        }

        public boolean isDisplayBackwards() {
            return displayBackwards;
        }
        
        public String getProjectName() {
            if (project == null) return BLANK_STRING;
            
            return project.getName();
        }
    }
    
    class ViewLabelProvider extends LabelProvider {

        public String getText(Object obj) {
          if (obj == null) return BLANK_STRING;
           return obj.toString();
        }

        public Image getImage(Object obj) {
           if (obj instanceof IndexerNodeLeaf) {
        	   IEntryResult result = ((IndexerNodeLeaf)obj).getResult();
        	   int index = getKey(result.getMetaKind(), result.getKind(), result.getRefKind());
        	   if (index > -1)
        		   return IndexerViewPluginImages.get(index);
           }
           
           return IndexerViewPluginImages.get(IndexerViewPluginImages.IMG_WARNING);
        }
     }

    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

        viewer.setContentProvider(new ViewContentProvider());

        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setInput(getViewSite());
        
        makeActions();
        hookContextMenu();
        hookSingleClickAction();

        contributeToActionBars();
    }

    protected void enableButtons(boolean value) {
        setFiltersAction.setEnabled(value);
        setFiltersAction.setEnabled(value);
        sortAction.setEnabled(value);
        displayFullNameAction.setEnabled(value);
    }
    
    private void makeActions() {
        searchLocationAction = new SearchLocationsAction();
        searchLocationAction.setText(SEARCH_LOCATIONS);
        searchLocationAction.setToolTipText(SEARCH_LOCATIONS);
        searchLocationAction.setImageDescriptor(IndexerViewPluginImages.DESC_SEARCH_LOCATION);
        
        previousPageAction = new Action() {
            public void run() {
                if (viewer.getContentProvider() instanceof ViewContentProvider) {
                    IndexerNodeParent root = ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot();
                    root.setIsForward(false);
                    root.setNavigate(true);
                }
                viewer.refresh();
                
                setEnabled(((ViewContentProvider)viewer.getContentProvider()).isDisplayBackwards());
                nextPageAction.setEnabled(((ViewContentProvider)viewer.getContentProvider()).isDisplayForwards());
            }
        };
        previousPageAction.setText(PREVIOUS_PAGE);
        previousPageAction.setToolTipText(PREVIOUS_PAGE);
        previousPageAction.setImageDescriptor(IndexerViewPluginImages.DESC_BACK);
        
        nextPageAction = new Action() {
            public void run() {
                if (viewer.getContentProvider() instanceof ViewContentProvider) {
                    IndexerNodeParent root = ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot();
                    root.setIsForward(true);
                    root.setNavigate(true);
                }
                viewer.refresh();
                
                previousPageAction.setEnabled(((ViewContentProvider)viewer.getContentProvider()).isDisplayBackwards());
                setEnabled(((ViewContentProvider)viewer.getContentProvider()).isDisplayForwards());
            }
        };
        nextPageAction.setText(NEXT_PAGE);
        nextPageAction.setToolTipText(NEXT_PAGE);
        nextPageAction.setImageDescriptor(IndexerViewPluginImages.DESC_NEXT);
        
        setFiltersAction = new Action() {
            public void run() {
                if (!(viewer.getContentProvider() instanceof ViewContentProvider)) return;

                FilterIndexerViewDialog dialog = new FilterIndexerViewDialog(getSite().getShell(), ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot(), (project==null?BLANK_STRING:project.getName()));
                int result = dialog.open();
                
                if (result == IDialogConstants.OK_ID) {
                    // reset the view but remember the buttons being displayed from the old content provider
                    ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().reset();
                    viewer.refresh();
                }
            }
        };
        setFiltersAction.setText(SET_FILTERS);
        setFiltersAction.setToolTipText(SET_FILTERS);
        setFiltersAction.setImageDescriptor(IndexerViewPluginImages.DESC_FILTER_BUTTON);
        
        sortAction = new Action() {
            public void run() {
                if (viewer.getContentProvider() instanceof ViewContentProvider) {
                    enableButtons(false);
                    if (((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().isSort()) {
                        ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().setSort(false);
                        ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().reset();
                        viewer.refresh();
                        this.setImageDescriptor(IndexerViewPluginImages.DESC_SORT);
                    } else {
                        ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().setSort(true);
                        ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().reset();
                        viewer.refresh();
                        this.setImageDescriptor(IndexerViewPluginImages.DESC_SORTED);
                    }
                    enableButtons(true);
                }
            }
        };
        sortAction.setText(SORT_RESULTS);
        sortAction.setToolTipText(SORT_RESULTS);
        sortAction.setImageDescriptor(IndexerViewPluginImages.DESC_SORTED);
        
        displayFullNameAction = new Action() {
            public void run() {
                if (viewer.getContentProvider() instanceof ViewContentProvider) {
                    if (((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().isDisplayFullName()) {
                        ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().setDisplayFullName(false);
                        viewer.refresh();
                        this.setImageDescriptor(IndexerViewPluginImages.DESC_DISPLAY_FULL_NAME);
                    } else {
                        ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot().setDisplayFullName(true);
                        viewer.refresh();
                        this.setImageDescriptor(IndexerViewPluginImages.DESC_FULL_NAME_DISPLAYED);
                    }
                }
            }
        };
        displayFullNameAction.setText(SWITCH_FULL_NAMES);
        displayFullNameAction.setToolTipText(SWITCH_FULL_NAMES);
        displayFullNameAction.setImageDescriptor(IndexerViewPluginImages.DESC_FULL_NAME_DISPLAYED);
        
        displayStatsAction = new Action() {
            public void run() {
                CTestPlugin.getStandardDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if (viewer.getContentProvider() instanceof ViewContentProvider) {
                            IndexerNodeParent root = ((ViewContentProvider)viewer.getContentProvider()).getInvisibleRoot();
                            
                            MessageDialog.openInformation(getSite().getShell(), INDEXER_STATS,
                                    root.getFilteredCount() + _FILTERED_IENTRY_RESULTS_ + root.getFullLength() + _TOTAL_IENTRYRESULTS);
                        }
                    }
                });
            }
        };
        displayStatsAction.setText(DISPLAY_INDEX_STATS);
        displayStatsAction.setToolTipText(DISPLAY_INDEX_STATS);
        displayStatsAction.setImageDescriptor(IndexerViewPluginImages.DESC_STATS);
        
        singleClickAction = new IndexerHighlighterAction();
    }
    
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager(_INDEXER_MENU_MANAGER);
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            private void hideMenuItems(IMenuManager manager) {
            }

            public void menuAboutToShow(IMenuManager manager) {
                IndexerView.this.fillContextMenu(manager);
                hideMenuItems(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    void fillContextMenu(IMenuManager manager) {
        manager.add(searchLocationAction);
        manager.add(new Separator());
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private class IndexerHighlighterAction extends Action {
        public void run() {
            ISelection selection = viewer.getSelection();
            
            IViewPart part = getSite().getPage().findView(PROPERTIES_VIEW);
            if (part instanceof PropertySheet) {
                ((PropertySheet)part).selectionChanged(getSite().getPart(), selection); 
            }
        }
    }
    
    private void hookSingleClickAction() {
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                singleClickAction.run();
            }
        });
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {}

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(previousPageAction);
        manager.add(nextPageAction);
        manager.add(new Separator());
        manager.add(sortAction);
        manager.add(displayFullNameAction);
        manager.add(setFiltersAction);
        manager.add(new Separator());
        manager.add(displayStatsAction);
        manager.add(new Separator());
    }

    public void setFocus() {
        IViewPart part = getSite().getPage().findView(PROPERTIES_VIEW);
        if (part instanceof PropertySheet) {
            ((PropertySheet)part).selectionChanged(getSite().getPart(), viewer.getSelection());
        }
    }

    public void appendIndexer(IIndexer indexer) {
//        indexers = (IIndexer[])ArrayUtil.append(IIndexer.class, indexers, indexer);
        // only support 1 indexer for now
        indexers[0] = indexer;
    }
    
    public void clearIndexers() {
        // for now only support 1 indexer at a time
        indexers = new IIndexer[1];
    }
    
    public void setContentProvider(ViewContentProvider provider) {
        viewer.setContentProvider(provider);
    }
    
    public void setProject(IProject project) {
        this.setPartName(INDEXER_VIEW___ + project.getName());
        this.project=project;
    }
    
    public static ViewerFilter[] getViewerFilters() {
        return viewer.getFilters();
    }

    public String getProjectName() {
        if (project == null) return BLANK_STRING;
        
        return project.getName();
    }
    
    private class SearchLocationsAction extends Action {
        private static final String LOCATIONS = "Locations"; //$NON-NLS-1$
        private static final String INDEX = "Index"; //$NON-NLS-1$
        protected void displayLocations(IndexerNodeLeaf leaf, String queryLabel, String pattern) {
            IndexerQuery job = new IndexerQuery(leaf, queryLabel, pattern);
            NewSearchUI.activateSearchResultView();
            NewSearchUI.runQueryInBackground(job);
         }
                
        public void run() {
            if (viewer.getSelection() instanceof IStructuredSelection &&
                    ((IStructuredSelection)viewer.getSelection()).getFirstElement() instanceof IndexerNodeLeaf) {
                displayLocations((IndexerNodeLeaf)((IStructuredSelection)viewer.getSelection()).getFirstElement(), 
                        INDEX, LOCATIONS);
            }
        }
  }

    public static int getKey(int meta, int kind, int ref) {   	             
            switch (ref) {
            case IIndex.REFERENCE :
            	switch (meta) {
            	case IIndex.TYPE      : return FilterIndexerViewDialog.ENTRY_TYPE_REF;
            	case IIndex.FUNCTION  : return FilterIndexerViewDialog.ENTRY_FUNCTION_REF;
            	case IIndex.METHOD    : return FilterIndexerViewDialog.ENTRY_METHOD_REF;
            	case IIndex.FIELD     : return FilterIndexerViewDialog.ENTRY_FIELD_REF;
            	case IIndex.MACRO     : return -1;
            	case IIndex.NAMESPACE : return FilterIndexerViewDialog.ENTRY_NAMESPACE_REF;
            	case IIndex.ENUMTOR   : return FilterIndexerViewDialog.ENTRY_ENUMTOR_REF;
            	case IIndex.INCLUDE   : return FilterIndexerViewDialog.ENTRY_INCLUDE_REF;
            	}
            	break;
            case IIndex.DECLARATION :
            	switch (meta) {
            	case IIndex.TYPE :
            		switch (kind) {
            		case IIndex.TYPE_CLASS      : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_CLASS;
            		case IIndex.TYPE_STRUCT     : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_STRUCT;
            		case IIndex.TYPE_UNION      : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_UNION;
            		case IIndex.TYPE_ENUM       : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_ENUM;
            		case IIndex.TYPE_VAR        : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_VAR;
            		case IIndex.TYPE_TYPEDEF    : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_TYPEDEF;
            		case IIndex.TYPE_DERIVED    : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_DERIVED;
            		case IIndex.TYPE_FRIEND     : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_FRIEND;
            		case IIndex.TYPE_FWD_CLASS  : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_FWD_CLASS;
            		case IIndex.TYPE_FWD_STRUCT : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_FWD_STRUCT;
            		case IIndex.TYPE_FWD_UNION  : return FilterIndexerViewDialog.ENTRY_TYPE_DECL_FWD_UNION;
            		}              
            	case IIndex.FUNCTION  : return FilterIndexerViewDialog.ENTRY_FUNCTION_DECL;
            	case IIndex.METHOD    : return FilterIndexerViewDialog.ENTRY_METHOD_DECL;
            	case IIndex.FIELD     : return FilterIndexerViewDialog.ENTRY_FIELD_DECL;
            	case IIndex.MACRO     : return FilterIndexerViewDialog.ENTRY_MACRO_DECL; 
            	case IIndex.NAMESPACE : return FilterIndexerViewDialog.ENTRY_NAMESPACE_DECL;
            	case IIndex.ENUMTOR   : return FilterIndexerViewDialog.ENTRY_ENUMTOR_DECL;
            	case IIndex.INCLUDE   : return -1;
            	}
            	break;
            }
            return 0;
        }
}
