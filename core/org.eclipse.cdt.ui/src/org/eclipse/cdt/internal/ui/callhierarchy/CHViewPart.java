/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.ExtendedTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.TreeNavigator;
import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;

/**
 * The view part for the include browser.
 */
public class CHViewPart extends ViewPart {
	private static final int MAX_HISTORY_SIZE = 10;
    private static final String TRUE = String.valueOf(true);
    private static final String KEY_WORKING_SET_FILTER = "workingSetFilter"; //$NON-NLS-1$
    private static final String KEY_FILTER_VARIABLES = "variableFilter"; //$NON-NLS-1$
    private static final String KEY_FILTER_MACROS = "macroFilter"; //$NON-NLS-1$
    
    private IMemento fMemento;
    private boolean fShowsMessage;
    private CHNode fLastNavigationNode;
	private ArrayList fHistoryEntries= new ArrayList(MAX_HISTORY_SIZE);

    // widgets
    private PageBook fPagebook;
    private Composite fViewerPage;
    private Composite fInfoPage;
    private Text fInfoText;

    // treeviewer
    private CHContentProvider fContentProvider;
    private CHLabelProvider fLabelProvider;
    private ExtendedTreeViewer fTreeViewer;

    // filters, sorter
    private CHWorkingSetFilter fWorkingSetFilter;
    private ViewerFilter fVariableFilter;
    private ViewerFilter fMacroFilter;
    private ViewerComparator fSorterAlphaNumeric;
    private ViewerComparator fSorterReferencePosition;

    // actions
    private Action fReferencedByAction;
    private Action fMakesReferenceToAction;
    private Action fFilterVariablesAction;
    private Action fFilterMacrosAction;
    private Action fShowFilesInLabelsAction;
    private Action fNextAction;
    private Action fPreviousAction;
    private Action fRefreshAction;
	private Action fHistoryAction;

    
    public void setFocus() {
        fPagebook.setFocus();
    }

    public void setMessage(String msg) {
        fInfoText.setText(msg);
        fPagebook.showPage(fInfoPage);
        fShowsMessage= true;
        updateDescription();
        updateActionEnablement();
    }
    
    public void setInput(ICElement input) {
    	if (input == null) {
            setMessage(CHMessages.CHViewPart_emptyPageMessage);
            fTreeViewer.setInput(null);
            return;
    	}
        fShowsMessage= false;
//        boolean isHeader= false;
//        String contentType= input.getContentTypeId();
//        if (contentType.equals(CCorePlugin.CONTENT_TYPE_CXXHEADER) ||
//                contentType.equals(CCorePlugin.CONTENT_TYPE_CHEADER)) {
//            isHeader= true;
//        }
        fTreeViewer.setInput(null);
//        if (!isHeader) {
//        	fContentProvider.setComputeIncludedBy(isHeader);
//        	fIncludedByAction.setChecked(isHeader);
//        	fIncludesToAction.setChecked(!isHeader);
//        	updateSorter();
//        }
        fTreeViewer.setInput(input);
        fPagebook.showPage(fViewerPage);
        updateDescription();
    	updateHistory(input);
    	updateActionEnablement();
    }

	public void createPartControl(Composite parent) {
        fPagebook = new PageBook(parent, SWT.NULL);
        fPagebook.setLayoutData(new GridData(GridData.FILL_BOTH));
        createInfoPage();
        createViewerPage();
                
        initDragAndDrop();
        createActions();
        createContextMenu();

        getSite().setSelectionProvider(fTreeViewer);
        setMessage(CHMessages.CHViewPart_emptyPageMessage);
        
        initializeActionStates();
    }
    
    private void initializeActionStates() {
        boolean referencedBy= true;
        boolean filterVariables= false;
        boolean filterMacros= false;
        
        if (fMemento != null) {
            filterVariables= TRUE.equals(fMemento.getString(KEY_FILTER_VARIABLES));
            filterMacros= TRUE.equals(fMemento.getString(KEY_FILTER_MACROS));
        }
        
        fReferencedByAction.setChecked(referencedBy);
        fMakesReferenceToAction.setChecked(!referencedBy);
        fContentProvider.setComputeReferencedBy(referencedBy);
        
        fFilterMacrosAction.setChecked(filterMacros);
        fFilterMacrosAction.run();
        fFilterVariablesAction.setChecked(filterVariables);
        fFilterVariablesAction.run();
        updateSorter();
    }

    public void init(IViewSite site, IMemento memento) throws PartInitException {
        fMemento= memento;
        super.init(site, memento);
    }


    public void saveState(IMemento memento) {
        if (fWorkingSetFilter != null) {
            fWorkingSetFilter.getUI().saveState(memento, KEY_WORKING_SET_FILTER);
        }
        memento.putString(KEY_FILTER_MACROS, String.valueOf(fFilterMacrosAction.isChecked()));
        memento.putString(KEY_FILTER_VARIABLES, String.valueOf(fFilterVariablesAction.isChecked()));
        super.saveState(memento);
    }

    private void createContextMenu() {
        MenuManager manager = new MenuManager();
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager m) {
                onContextMenuAboutToShow(m);
            }
        });
        Menu menu = manager.createContextMenu(fTreeViewer.getControl());
        fTreeViewer.getControl().setMenu(menu);
        IWorkbenchPartSite site = getSite();
        site.registerContextMenu(CUIPlugin.ID_CALL_HIERARCHY, manager, fTreeViewer); 
    }

    private void createViewerPage() {
        Display display= getSite().getShell().getDisplay();
        fViewerPage = new Composite(fPagebook, SWT.NULL);
        fViewerPage.setLayoutData(new GridData(GridData.FILL_BOTH));
        fViewerPage.setSize(100, 100);
        fViewerPage.setLayout(new FillLayout());

        fContentProvider= new CHContentProvider(display); 
        fLabelProvider= new CHLabelProvider(display, fContentProvider);
        fTreeViewer= new ExtendedTreeViewer(fViewerPage);
        fTreeViewer.setContentProvider(fContentProvider);
        fTreeViewer.setLabelProvider(fLabelProvider);
        fTreeViewer.setAutoExpandLevel(2);     
        fTreeViewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                onShowReference(event.getSelection());
            }
        });
    }
    
    private void createInfoPage() {
        fInfoPage = new Composite(fPagebook, SWT.NULL);
        fInfoPage.setLayoutData(new GridData(GridData.FILL_BOTH));
        fInfoPage.setSize(100, 100);
        fInfoPage.setLayout(new FillLayout());

        fInfoText= new Text(fInfoPage, SWT.WRAP | SWT.READ_ONLY); 
    }

    private void initDragAndDrop() {
        CHDropTargetListener dropListener= new CHDropTargetListener(this);
        Transfer[] localSelectionTransfer= new Transfer[] {
        		LocalSelectionTransfer.getTransfer()
        };
        DropTarget dropTarget = new DropTarget(fPagebook, DND.DROP_COPY);
        dropTarget.setTransfer(localSelectionTransfer);
        dropTarget.addDropListener(dropListener);
    }

    private void createActions() {
        WorkingSetFilterUI wsFilterUI= new WorkingSetFilterUI(this, fMemento, KEY_WORKING_SET_FILTER) {
            protected void onWorkingSetChange() {
                updateWorkingSetFilter(this);
            }
            protected void onWorkingSetNameChange() {
                updateDescription();
            }
        };

        fReferencedByAction= 
            new Action(CHMessages.CHViewPart_ShowCallers_label, IAction.AS_RADIO_BUTTON) { 
                public void run() {
                    if (isChecked()) {
                        onSetShowReferencedBy(true);
                    }
                }
        };
        fReferencedByAction.setToolTipText(CHMessages.CHViewPart_ShowCallers_tooltip);
        CPluginImages.setImageDescriptors(fReferencedByAction, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_REF_BY);       

        fMakesReferenceToAction= 
            new Action(CHMessages.CHViewPart_ShowCallees_label, IAction.AS_RADIO_BUTTON) { 
                public void run() {
                    if (isChecked()) {
                        onSetShowReferencedBy(false);
                    }
                }
        };
        fMakesReferenceToAction.setToolTipText(CHMessages.CHViewPart_ShowCallees_tooltip);
        CPluginImages.setImageDescriptors(fMakesReferenceToAction, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_RELATES_TO);       

        fVariableFilter= new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof CHNode) {
                	CHNode node= (CHNode) element;
                    return !node.isVariable();
                }
                return true;
            }
        };
        fFilterVariablesAction= new Action(CHMessages.CHViewPart_FilterVariables_label, IAction.AS_CHECK_BOX) {
            public void run() {
                if (isChecked()) {
                    fTreeViewer.addFilter(fVariableFilter);
                }
                else {
                    fTreeViewer.removeFilter(fVariableFilter);
                }
            }
        };
        fFilterVariablesAction.setToolTipText(CHMessages.CHViewPart_FilterVariables_tooltip);
        CPluginImages.setImageDescriptors(fFilterVariablesAction, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_HIDE_FIELDS);       

        fMacroFilter= new ViewerFilter() {
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof CHNode) {
                    CHNode node= (CHNode) element;
                    return !node.isMacro();
                }
                return true;
            }
        };
        fFilterMacrosAction= new Action(CHMessages.CHViewPart_HideMacros_label, IAction.AS_CHECK_BOX) {
            public void run() {
                if (isChecked()) {
                    fTreeViewer.addFilter(fMacroFilter);
                }
                else {
                    fTreeViewer.removeFilter(fMacroFilter);
                }
            }
        };
        fFilterMacrosAction.setToolTipText(CHMessages.CHViewPart_HideMacros_tooltip);
        CPluginImages.setImageDescriptors(fFilterMacrosAction, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_HIDE_MACROS);       
        
        fSorterAlphaNumeric= new ViewerComparator();
        fSorterReferencePosition= new ViewerComparator() {
            public int category(Object element) {
                if (element instanceof CHNode) {
                    return 0;
                }
                return 1;
            }
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (!(e1 instanceof CHNode)) {
                    if (!(e2 instanceof CHNode)) {
                        return 0;
                    }
                    return -1;
                }
                if (!(e2 instanceof CHNode)) {
                    return 1;
                }
                CHNode n1= (CHNode) e1;
                CHNode n2= (CHNode) e2;
                int offset1= n1.getFirstReferenceOffset();
                int offset2= n2.getFirstReferenceOffset();
                return CoreUtility.compare(offset1, offset2);
            }
        };
        
        fShowFilesInLabelsAction= new Action(CHMessages.CHViewPart_ShowFiles_label, IAction.AS_CHECK_BOX) {
            public void run() {
                onShowFilesInLabels(isChecked());
            }
        };
        fShowFilesInLabelsAction.setToolTipText(CHMessages.CHViewPart_ShowFiles_tooltip);
        fNextAction = new Action(CHMessages.CHViewPart_NextReference_label) {
            public void run() {
                onNextOrPrevious(true);
            }
        };
        fNextAction.setToolTipText(CHMessages.CHViewPart_NextReference_tooltip); 
        CPluginImages.setImageDescriptors(fNextAction, CPluginImages.T_LCL, CPluginImages.IMG_SHOW_NEXT);       

        fPreviousAction = new Action(CHMessages.CHViewPart_PreviousReference_label) {
            public void run() {
                onNextOrPrevious(false);
            }
        };
        fPreviousAction.setToolTipText(CHMessages.CHViewPart_PreviousReference_tooltip); 
        CPluginImages.setImageDescriptors(fPreviousAction, CPluginImages.T_LCL, CPluginImages.IMG_SHOW_PREV);       

        fRefreshAction = new Action(CHMessages.CHViewPart_Refresh_label) {
            public void run() {
                onRefresh();
            }
        };
        fRefreshAction.setToolTipText(CHMessages.CHViewPart_Refresh_tooltip); 
        CPluginImages.setImageDescriptors(fRefreshAction, CPluginImages.T_LCL, CPluginImages.IMG_REFRESH);       

        fHistoryAction = new CHHistoryDropDownAction(this);

        // setup action bar
        // global action hooks
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAction);
        actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAction);
        actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
        actionBars.updateActionBars();
        
        // local toolbar
        IToolBarManager tm = actionBars.getToolBarManager();
        tm.add(fNextAction);
        tm.add(fPreviousAction);
        tm.add(new Separator());
        tm.add(fFilterMacrosAction);
        tm.add(fFilterVariablesAction);
        tm.add(new Separator());
        tm.add(fReferencedByAction);
        tm.add(fMakesReferenceToAction);
		tm.add(fHistoryAction);
        tm.add(fRefreshAction);

        // local menu
        IMenuManager mm = actionBars.getMenuManager();

//        tm.add(fNext);
//        tm.add(fPrevious);
//        tm.add(new Separator());
        wsFilterUI.fillActionBars(actionBars);
        mm.add(fReferencedByAction);
        mm.add(fMakesReferenceToAction);
        mm.add(new Separator());
        mm.add(fShowFilesInLabelsAction);
        mm.add(new Separator());
        mm.add(fFilterMacrosAction);
        mm.add(fFilterVariablesAction);
    }
    
    private CHNode getNextNode(boolean forward) {
    	TreeNavigator navigator= new TreeNavigator(fTreeViewer.getTree(), CHNode.class);
    	TreeItem selectedItem= navigator.getSelectedItemOrFirstOnLevel(1, forward);
    	if (selectedItem == null) {
    		return null;
    	}
    	
        if (selectedItem.getData().equals(fLastNavigationNode)) {
        	selectedItem= navigator.getNextSibbling(selectedItem, forward);
        }
        
        return selectedItem == null ? null : (CHNode) selectedItem.getData();
    }
        
    protected void onNextOrPrevious(boolean forward) {
    	CHNode nextItem= getNextNode(forward);
        if (nextItem != null) {
            StructuredSelection sel= new StructuredSelection(nextItem);
            fTreeViewer.setSelection(sel);
            onShowReference(sel);
        }
    }

    protected void onRefresh() {
        fContentProvider.recompute();
    }
    
    protected void onShowFilesInLabels(boolean show) {
        fLabelProvider.setShowFiles(show);
        fTreeViewer.refresh();
    }

    private void updateHistory(ICElement input) {
    	if (input != null) {
    		fHistoryEntries.remove(input);
    		fHistoryEntries.add(0, input);
    		if (fHistoryEntries.size() > MAX_HISTORY_SIZE) {
    			fHistoryEntries.remove(MAX_HISTORY_SIZE-1);
    		}
    	}
	}

    private void updateSorter() {
        if (fReferencedByAction.isChecked()) {
            fTreeViewer.setComparator(fSorterAlphaNumeric);
        }
        else {
            fTreeViewer.setComparator(fSorterReferencePosition);
        }
    }
    
    private void updateDescription() {
        String message= ""; //$NON-NLS-1$
        if (!fShowsMessage) {
        	ITranslationUnit tu= getInput();
            if (tu != null) {
                IPath path= tu.getPath();
                if (path != null) {
                    String format, file, scope;
                    
                    file= path.lastSegment() + "(" + path.removeLastSegments(1) + ")";  //$NON-NLS-1$//$NON-NLS-2$
                    if (fWorkingSetFilter == null) {
                        scope= CHMessages.CHViewPart_WorkspaceScope;
                    }
                    else {
                        scope= fWorkingSetFilter.getLabel();
                    }
                    
                    if (fReferencedByAction.isChecked()) {
                        format= CHMessages.CHViewPart_Title_callers;
                    }
                    else {
                        format= CHMessages.CHViewPart_Title_callees;
                    }
                    message= Messages.format(format, file, scope);
                }
            }
        }
        message= "The Call Hierarchy is work in progress! - " + message; //$NON-NLS-1$
        setContentDescription(message);
    }
    
	private void updateActionEnablement() {
		fHistoryAction.setEnabled(!fHistoryEntries.isEmpty());
		fNextAction.setEnabled(!fShowsMessage);
		fPreviousAction.setEnabled(!fShowsMessage);
		fRefreshAction.setEnabled(!fShowsMessage);
	}

    private void updateWorkingSetFilter(WorkingSetFilterUI filterUI) {
        if (filterUI.getWorkingSet() == null) {
            if (fWorkingSetFilter != null) {
                fTreeViewer.removeFilter(fWorkingSetFilter);
                fWorkingSetFilter= null;
            }
        }
        else {
            if (fWorkingSetFilter != null) {
                fTreeViewer.refresh();
            }
            else {
                fWorkingSetFilter= new CHWorkingSetFilter(filterUI);
                fTreeViewer.addFilter(fWorkingSetFilter);
            }
        }
    }
    
    protected void onSetShowReferencedBy(boolean showReferencedBy) {
        if (showReferencedBy != fContentProvider.getComputeReferencedBy()) {
            Object input= fTreeViewer.getInput();
            fTreeViewer.setInput(null);
            fContentProvider.setComputeReferencedBy(showReferencedBy);
            updateSorter();
            fTreeViewer.setInput(input);
            updateDescription();
        }
    }

    protected void onContextMenuAboutToShow(IMenuManager m) {
//        final IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
//        final CHNode node= IBConversions.selectionToNode(selection);
//        
//        if (node != null) {
//            // open reference
//            if (node.getParent() != null && node.getDirectiveFile() != null) {
//                m.add(new Action(CHMessages.CHViewPart_OpenReference_label) {
//                    public void run() {
//                        onShowReference(selection);
//                    }
//                });
//            }

            // support for opening the function/method
//            final ITranslationUnit tu= node.getRepresentedTranslationUnit();
//            if (tu != null) {
//                // open
//                OpenFileAction ofa= new OpenFileAction(page);
//                ofa.selectionChanged(selection);
//                m.add(ofa);
//
//                // open with
//                // keep the menu shorter, no open with support
////                final IResource r= tu.getResource();
////                if (r != null) {
////                    IMenuManager submenu= new MenuManager(IBMessages.IBViewPart_OpenWithMenu_label); 
////                    submenu.add(new OpenWithMenu(page, r));
////                    m.add(submenu);
////                }
//            }
//        }
        m.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
    }
    
    protected void onShowReference(ISelection selection) {
//        CHNode node= IBConversions.selectionToNode(selection);
//        if (node != null) {
//            IWorkbenchPage page= getSite().getPage();
//            IBFile ibf= node.getDirectiveFile();
//            long timestamp= node.getTimestamp();
//            if (ibf != null) {
//                IEditorPart editor= null;
//                IPath filebufferKey= null;
//                IFile f= ibf.getResource();
//                if (f != null) {
//                    if (timestamp == 0) {
//                    	timestamp= f.getLocalTimeStamp();
//                    }
//                	fLastNavigationNode= node;
//                	try {
//                		editor= IDE.openEditor(page, f, false);
//                		filebufferKey= f.getFullPath();
//                	} catch (PartInitException e) {
//                		CUIPlugin.getDefault().log(e);
//                	}
//                }
//                else {
//                    IPath location= ibf.getLocation();
//                    if (location != null) {
//                        if (timestamp == 0) {
//                        	timestamp= location.toFile().lastModified();
//                        }
//                        fLastNavigationNode= node;
//                    	ExternalEditorInput ei= new ExternalEditorInput(new FileStorage(null, location));
//						try {
//                            IEditorDescriptor descriptor = IDE.getEditorDescriptor(location.lastSegment());
//                            editor= IDE.openEditor(page, ei, descriptor.getId(), false);
//                            filebufferKey= location;
//						} catch (PartInitException e) {
//                            CUIPlugin.getDefault().log(e);
//						}
//                    }
//                }
//                if (editor instanceof ITextEditor) {
//                    ITextEditor te= (ITextEditor) editor;
//                    Position pos= new Position(node.getDirectiveCharacterOffset(),
//                    		node.getDirectiveName().length() + 2);
//                    if (filebufferKey != null) {
//                    	IPositionConverter pc= CCorePlugin.getPositionTrackerManager().findPositionConverter(filebufferKey, timestamp);
//                    	if (pc != null) {
//                    		pos= pc.historicToActual(pos);
//                    	}
//                    }
//                    
//                    te.selectAndReveal(pos.getOffset(), pos.getLength());
//                }
//            }
//            else {
//            	ITranslationUnit tu= IBConversions.selectionToTU(selection);
//            	if (tu != null) {
//            		IResource r= tu.getResource();
//            		if (r != null) {
//            			OpenFileAction ofa= new OpenFileAction(page);
//            			ofa.selectionChanged((IStructuredSelection) selection);
//            			ofa.run();
//            		}
//            	}
//            }
//        }
    }

    public ShowInContext getShowInContext() {
        return new ShowInContext(null, fTreeViewer.getSelection());
    }
    
	public Control getPageBook() {
		return fPagebook;
	}

	public ICElement[] getHistoryEntries() {
		return (ICElement[]) fHistoryEntries.toArray(new ICElement[fHistoryEntries.size()]);
	}

	public void setHistoryEntries(ICElement[] remaining) {
		fHistoryEntries.clear();
		fHistoryEntries.addAll(Arrays.asList(remaining));
	}

	public ITranslationUnit getInput() {
        Object input= fTreeViewer.getInput();
        if (input instanceof ITranslationUnit) {
        	return (ITranslationUnit) input;
        }
        return null;
	}
}
