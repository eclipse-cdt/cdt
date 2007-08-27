/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;
import org.eclipse.cdt.refactoring.actions.CRefactoringActionGroup;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.internal.ui.util.CoreUtility;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.AdaptingSelectionProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.EditorOpener;
import org.eclipse.cdt.internal.ui.viewsupport.ExtendedTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
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
//    private static final String KEY_FILTER_MACROS = "macroFilter"; //$NON-NLS-1$
    private static final String KEY_SHOW_FILES= "showFilesInLabels"; //$NON-NLS-1$
    
    private IMemento fMemento;
    private boolean fShowsMessage;
    private CHNode fNavigationNode;
    private int fNavigationDetail;
    
	private ArrayList fHistoryEntries= new ArrayList(MAX_HISTORY_SIZE);

    // widgets
    private PageBook fPagebook;
    private Composite fViewerPage;
    private Label fInfoText;

    // treeviewer
    private CHContentProvider fContentProvider;
    private CHLabelProvider fLabelProvider;
    private ExtendedTreeViewer fTreeViewer;

    // filters, sorter
    private ViewerFilter fVariableFilter;
//    private ViewerFilter fMacroFilter;
    private ViewerComparator fSorterAlphaNumeric;
    private ViewerComparator fSorterReferencePosition;
	private WorkingSetFilterUI fWorkingSetFilterUI;

    // actions
    private Action fReferencedByAction;
    private Action fMakesReferenceToAction;
    private Action fFilterVariablesAction;
//    private Action fFilterMacrosAction;
    private Action fShowFilesInLabelsAction;
    private Action fNextAction;
    private Action fPreviousAction;
    private Action fRefreshAction;
	private Action fHistoryAction;
	private Action fShowReference;
	private Action fOpenElement;
	
	// action groups
	private OpenViewActionGroup fOpenViewActionGroup;
	private SelectionSearchGroup fSelectionSearchGroup;
	private CRefactoringActionGroup fRefactoringActionGroup;
	private IContextActivation fContextActivation;

    
    public void setFocus() {
        fPagebook.setFocus();
    }

    public void setMessage(String msg) {
        fInfoText.setText(msg);
        fPagebook.showPage(fInfoText);
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
        boolean allowsRefTo= allowsRefTo(input);
        fTreeViewer.setInput(null);
        if (!allowsRefTo && !fContentProvider.getComputeReferencedBy()) {
        	fContentProvider.setComputeReferencedBy(true);
        	fReferencedByAction.setChecked(true);
        	fMakesReferenceToAction.setChecked(false);
        	updateSorter();
        }
    	fMakesReferenceToAction.setEnabled(allowsRefTo);
        fTreeViewer.setInput(input);
        fPagebook.showPage(fViewerPage);
        updateDescription();
    	updateHistory(input);
    	updateActionEnablement();
    }

	public void reportNotIndexed(ICElement input) {
		if (input != null && getInput() == input) {
			setMessage(IndexUI.getFleNotIndexedMessage(input));
		}
	}

	public void reportInputReplacement(ICElement input, ICElement inputHandle) {
		if (input == getInput()) {
			fTreeViewer.setInput(inputHandle);
			fTreeViewer.setExpandedState(inputHandle, true);
		}
	}

	private boolean allowsRefTo(ICElement element) {
		if (element instanceof IFunction || element instanceof IMethod) {
			return true;
		}
		
		return false;
	}

	public void createPartControl(Composite parent) {
        fPagebook = new PageBook(parent, SWT.NULL);
        fPagebook.setLayoutData(new GridData(GridData.FILL_BOTH));
        createInfoPage();
        createViewerPage();
                
        getSite().setSelectionProvider(new AdaptingSelectionProvider(ICElement.class, fTreeViewer));

        initDragAndDrop();
        createActions();
        createContextMenu();

        setMessage(CHMessages.CHViewPart_emptyPageMessage);
        
        initializeActionStates();
        
    	IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
    	if (ctxService != null) {
    		fContextActivation= ctxService.activateContext(CUIPlugin.CVIEWS_SCOPE);
    	}
    }
	
	public void dispose() {
		if (fContextActivation != null) {
			IContextService ctxService = (IContextService)getSite().getService(IContextService.class);
	    	if (ctxService != null) {
	    		ctxService.deactivateContext(fContextActivation);
	    	}
		}

		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.dispose();
			fOpenViewActionGroup= null;
		}
		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.dispose();
			fSelectionSearchGroup= null;
		}
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.dispose();
			fRefactoringActionGroup= null;
		}
		if (fWorkingSetFilterUI != null) {
			fWorkingSetFilterUI.dispose();
			fWorkingSetFilterUI= null;
		}
		super.dispose();
	}
	
    private void initializeActionStates() {
        boolean referencedBy= true;
        boolean filterVariables= false;
//        boolean filterMacros= false;
        boolean showFiles= false;
        
        if (fMemento != null) {
            filterVariables= TRUE.equals(fMemento.getString(KEY_FILTER_VARIABLES));
//            filterMacros= TRUE.equals(fMemento.getString(KEY_FILTER_MACROS));
            showFiles= TRUE.equals(fMemento.getString(KEY_SHOW_FILES));
        }
        
        fLabelProvider.setShowFiles(showFiles);
        fShowFilesInLabelsAction.setChecked(showFiles);
        
        fReferencedByAction.setChecked(referencedBy);
        fMakesReferenceToAction.setChecked(!referencedBy);
        fContentProvider.setComputeReferencedBy(referencedBy);
        
//        fFilterMacrosAction.setChecked(filterMacros);
//        fFilterMacrosAction.run();
        fFilterVariablesAction.setChecked(filterVariables);
        fFilterVariablesAction.run();
        updateSorter();
    }

    public void init(IViewSite site, IMemento memento) throws PartInitException {
        fMemento= memento;
        super.init(site, memento);
    }


    public void saveState(IMemento memento) {
        if (fWorkingSetFilterUI != null) {
        	fWorkingSetFilterUI.saveState(memento, KEY_WORKING_SET_FILTER);
        }
//        memento.putString(KEY_FILTER_MACROS, String.valueOf(fFilterMacrosAction.isChecked()));
        memento.putString(KEY_FILTER_VARIABLES, String.valueOf(fFilterVariablesAction.isChecked()));
        memento.putString(KEY_SHOW_FILES, String.valueOf(fShowFilesInLabelsAction.isChecked()));
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

        fContentProvider= new CHContentProvider(this, display); 
        fLabelProvider= new CHLabelProvider(display, fContentProvider);
        fTreeViewer= new ExtendedTreeViewer(fViewerPage);
        fTreeViewer.setContentProvider(fContentProvider);
        fTreeViewer.setLabelProvider(fLabelProvider);
        fTreeViewer.setAutoExpandLevel(2);     
        fTreeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
            	onShowSelectedReference(event.getSelection());
            }
        });
    }
    
    private void createInfoPage() {
    	fInfoText = new Label(fPagebook, SWT.TOP | SWT.LEFT | SWT.WRAP);
    }

    private void initDragAndDrop() {
        CHDropTargetListener dropListener= new CHDropTargetListener(this);
        Transfer[] localSelectionTransfer= new Transfer[] {
        		LocalSelectionTransfer.getTransfer()
        };
        DropTarget dropTarget = new DropTarget(fPagebook, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT);
        dropTarget.setTransfer(localSelectionTransfer);
        dropTarget.addDropListener(dropListener);
    }

    private void createActions() {
    	// action gruops
    	fOpenViewActionGroup= new OpenViewActionGroup(this);
    	fOpenViewActionGroup.setSuppressCallHierarchy(true);
    	fOpenViewActionGroup.setSuppressProperties(true);
    	fSelectionSearchGroup= new SelectionSearchGroup(getSite());
    	fRefactoringActionGroup= new CRefactoringActionGroup(this);
    	
    	fWorkingSetFilterUI= new WorkingSetFilterUI(this, fMemento, KEY_WORKING_SET_FILTER) {
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
                    return !node.isVariableOrEnumerator();
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

//        fMacroFilter= new ViewerFilter() {
//            public boolean select(Viewer viewer, Object parentElement, Object element) {
//                if (element instanceof CHNode) {
//                    CHNode node= (CHNode) element;
//                    return !node.isMacro();
//                }
//                return true;
//            }
//        };
//        fFilterMacrosAction= new Action(CHMessages.CHViewPart_HideMacros_label, IAction.AS_CHECK_BOX) {
//            public void run() {
//                if (isChecked()) {
//                    fTreeViewer.addFilter(fMacroFilter);
//                }
//                else {
//                    fTreeViewer.removeFilter(fMacroFilter);
//                }
//            }
//        };
//        fFilterMacrosAction.setToolTipText(CHMessages.CHViewPart_HideMacros_tooltip);
//        CPluginImages.setImageDescriptors(fFilterMacrosAction, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_HIDE_MACROS);       
        
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
        
        fShowReference= new Action(CHMessages.CHViewPart_ShowReference_label) {
        	public void run() {
        		onShowSelectedReference(fTreeViewer.getSelection());
        	}
        };
        fShowReference.setToolTipText(CHMessages.CHViewPart_ShowReference_tooltip);	
        fOpenElement= new Action(CHMessages.CHViewPart_Open_label) {
        	public void run() {
        		onOpenElement(fTreeViewer.getSelection());
        	}
        };
        fOpenElement.setToolTipText(CHMessages.CHViewPart_Open_tooltip);
        
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
        fRefactoringActionGroup.fillActionBars(actionBars);
        fOpenViewActionGroup.fillActionBars(actionBars);
        fSelectionSearchGroup.fillActionBars(actionBars);
        
        actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAction);
        actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAction);
        actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), fRefreshAction);
        actionBars.updateActionBars();
        
        // local toolbar
        IToolBarManager tm = actionBars.getToolBarManager();
        tm.add(fNextAction);
        tm.add(fPreviousAction);
        tm.add(new Separator());
//        tm.add(fFilterMacrosAction);
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
        fWorkingSetFilterUI.fillActionBars(actionBars);
        mm.add(fReferencedByAction);
        mm.add(fMakesReferenceToAction);
        mm.add(new Separator());
        mm.add(fShowFilesInLabelsAction);
        mm.add(new Separator());
//        mm.add(fFilterMacrosAction);
        mm.add(fFilterVariablesAction);
    }
    
    private void setNextNode(boolean forward) {
    	TreeNavigator navigator= new TreeNavigator(fTreeViewer.getTree(), CHNode.class);
    	TreeItem selectedItem= navigator.getSelectedItemOrFirstOnLevel(1, forward);
    	if (selectedItem == null) {
    		fNavigationNode= null;
    		return;
    	}
    	
    	
        if (selectedItem.getData().equals(fNavigationNode)) {
        	if (forward && fNavigationDetail < getReferenceCount(fNavigationNode)-1) {
        		fNavigationDetail++;
        	}
        	else if (!forward && fNavigationDetail > 0) {
        		fNavigationDetail--;
        	}
        	else {
        		selectedItem= navigator.getNextSibbling(selectedItem, forward);
                fNavigationNode= selectedItem == null ? null : (CHNode) selectedItem.getData();
            	initNavigationDetail(forward);
        	}
        }
        else {
        	fNavigationNode= (CHNode) selectedItem.getData();
        	initNavigationDetail(forward);
        }
    }

	private void initNavigationDetail(boolean forward) {
		if (!forward && fNavigationNode != null) {
			fNavigationDetail= Math.max(0, getReferenceCount(fNavigationNode) -1);
		}
		else {
			fNavigationDetail= 0;
		}
	}
        
	protected void onShowSelectedReference(ISelection selection) {
		fNavigationDetail= 0;
    	fNavigationNode= selectionToNode(selection);
        showReference();
	}

	protected void onOpenElement(ISelection selection) {
    	CHNode node= selectionToNode(selection);
    	openElement(node);
	}

	private void openElement(CHNode node) {
		if (node != null && !node.isMultiDef()) {
    		ICElement elem= node.getRepresentedDeclaration();
    		if (elem != null) {
    			IWorkbenchPage page= getSite().getPage();
    			try {
					EditorOpener.open(page, elem);
				} catch (CModelException e) {
					CUIPlugin.getDefault().log(e);
				}
    		}
    	}
	}

    protected void onNextOrPrevious(boolean forward) {
    	setNextNode(forward);
        if (fNavigationNode != null) {
            StructuredSelection sel= new StructuredSelection(fNavigationNode);
            fTreeViewer.setSelection(sel);
            showReference();
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
        	ICElement elem= getInput();
            if (elem != null) {
                String format, scope, label;
            	
                // label
                label= CElementBaseLabels.getElementLabel(elem, CHHistoryAction.LABEL_OPTIONS);
            	
                // scope
                IWorkingSet workingSet= fWorkingSetFilterUI.getWorkingSet();
            	if (workingSet == null) {	
            		scope= CHMessages.CHViewPart_WorkspaceScope;
            	}
            	else {
            		scope= workingSet.getLabel();
            	}
                
            	// format
            	if (fReferencedByAction.isChecked()) {
            		format= CHMessages.CHViewPart_Title_callers;
            	}
            	else {
            		format= CHMessages.CHViewPart_Title_callees;
            	}
            	
            	// message
            	message= Messages.format(format, label, scope);
            }
        }
        setContentDescription(message);
    }
    
	private void updateActionEnablement() {
		fHistoryAction.setEnabled(!fHistoryEntries.isEmpty());
		fNextAction.setEnabled(!fShowsMessage);
		fPreviousAction.setEnabled(!fShowsMessage);
		fRefreshAction.setEnabled(!fShowsMessage);
	}

    private void updateWorkingSetFilter(WorkingSetFilterUI filterUI) {
    	fContentProvider.setWorkingSetFilter(filterUI);
    }
    
    public void onSetShowReferencedBy(boolean showReferencedBy) {
        if (showReferencedBy != fContentProvider.getComputeReferencedBy()) {
            Object input= fTreeViewer.getInput();
            fTreeViewer.setInput(null);
            fContentProvider.setComputeReferencedBy(showReferencedBy);
            updateSorter();
            fTreeViewer.setInput(input);
            updateDescription();
        }
    }

    protected void onContextMenuAboutToShow(IMenuManager menu) {
		CUIPlugin.createStandardGroups(menu);
		
		CHNode node= selectionToNode(fTreeViewer.getSelection());
		if (node != null) {
			if (getReferenceCount(node) > 0) {
				menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, fShowReference);
			}
			if (!node.isMultiDef()) {
				menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, fOpenElement);
			}
			if (node.getParent() != null) {
				final ICElement element= node.getRepresentedDeclaration();
				if (element != null) {
					String label= Messages.format(CHMessages.CHViewPart_FocusOn_label, 
							CElementLabels.getTextLabel(element, CElementBaseLabels.ALL_FULLY_QUALIFIED | CElementBaseLabels.M_PARAMETER_TYPES));
					menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, new Action(label) {
						public void run() {
							setInput(element);
						}
					});
				}
			}
		}
		
		// action groups
		ISelection selection = getSite().getSelectionProvider().getSelection();
		if (OpenViewActionGroup.canActionBeAdded(selection)){
			fOpenViewActionGroup.fillContextMenu(menu);
		}

		if (SelectionSearchGroup.canActionBeAdded(selection)){
			fSelectionSearchGroup.fillContextMenu(menu);
		}
		fRefactoringActionGroup.fillContextMenu(menu);
    }
    	    
    private void showReference() {
    	CHNode node= getReferenceNode();
        if (node != null) {
        	ITranslationUnit file= node.getFileOfReferences();
        	if (file != null) {
        		IWorkbenchPage page= getSite().getPage();
        		if (node.getReferenceCount() > 0) {
        			long timestamp= node.getTimestamp();
        			if (fNavigationDetail < 0) {
        				fNavigationDetail= 0;
        			}
        			else if (fNavigationDetail >= node.getReferenceCount()-1) {
        				fNavigationDetail= node.getReferenceCount()-1;
        			}

        			CHReferenceInfo ref= node.getReference(fNavigationDetail);
        			Region region= new Region(ref.getOffset(), ref.getLength());
        			EditorOpener.open(page, file, region, timestamp);
        		}
        		else {
        			try {
        				EditorOpener.open(page, node.getRepresentedDeclaration());
        			} catch (CModelException e) {
        				CUIPlugin.getDefault().log(e);
        			}
        		}
        	}
        }
    }
    
	private CHNode getReferenceNode() {
		if (fNavigationNode != null) {
			CHNode parent = fNavigationNode.getParent();
			if (parent instanceof CHMultiDefNode) {
				return parent;
			}
    	}
		return fNavigationNode;
	}
	
	private int getReferenceCount(CHNode node) {
		if (node != null) {
			CHNode parent = node.getParent();
			if (parent instanceof CHMultiDefNode) {
				return parent.getReferenceCount();
			}
			return node.getReferenceCount();
    	}
		return 0;
	}
		

	private CHNode selectionToNode(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			for (Iterator iter = ss.iterator(); iter.hasNext(); ) {
				Object cand= iter.next();
				if (cand instanceof CHNode) {
					return (CHNode) cand;
				}
			}
		}
		return null;
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

	public ICElement getInput() {
        Object input= fTreeViewer.getInput();
        if (input instanceof ICElement) {
        	return (ICElement) input;
        }
        return null;
	}

	public TreeViewer getTreeViewer() {
		return fTreeViewer;
	}
}
