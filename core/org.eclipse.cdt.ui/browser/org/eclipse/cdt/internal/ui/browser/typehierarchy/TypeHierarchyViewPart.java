/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.cdt.internal.ui.actions.SelectAllAction;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.viewsupport.StatusBarUpdater;
import org.eclipse.cdt.internal.ui.workingsets.WorkingSetFilterActionGroup;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

/**
 * view showing the super types/sub types of its input.
 */
public class TypeHierarchyViewPart extends ViewPart implements ITypeHierarchyViewPart, IViewPartInputProvider {

	public static final int VIEW_ID_SUPER= 0;
	public static final int VIEW_ID_SUB= 1;
	
	public static final int VIEW_ORIENTATION_VERTICAL= 0;
	public static final int VIEW_ORIENTATION_HORIZONTAL= 1;
	public static final int VIEW_ORIENTATION_SINGLE= 2;
	public static final int VIEW_ORIENTATION_AUTOMATIC= 3;
	
	private static final String DIALOGSTORE_HIERARCHYVIEW= "TypeHierarchyViewPart.hierarchyview";	 //$NON-NLS-1$
	private static final String DIALOGSTORE_VIEWORIENTATION= "TypeHierarchyViewPart.orientation";	 //$NON-NLS-1$

	//private static final String TAG_INPUT= "input"; //$NON-NLS-1$
	private static final String TAG_VIEW= "view"; //$NON-NLS-1$
	private static final String TAG_ORIENTATION= "orientation"; //$NON-NLS-1$
	private static final String TAG_RATIO= "ratio"; //$NON-NLS-1$
	//private static final String TAG_SELECTION= "selection"; //$NON-NLS-1$
	private static final String TAG_VERTICAL_SCROLL= "vertical_scroll"; //$NON-NLS-1$
	
	private static final String GROUP_FOCUS= "group.focus"; //$NON-NLS-1$
	

	// the selected type in the hierarchy view
	private ICElement fSelectedType;
	// input element or null
	private ICElement fInputElement;
	
	// history of input elements. No duplicates
	private ArrayList fInputHistory;
	
	private IMemento fMemento;
	private IDialogSettings fDialogSettings;
	
	protected TypeHierarchyLifeCycle fHierarchyLifeCycle;
	private ITypeHierarchyLifeCycleListener fTypeHierarchyLifeCycleListener;
	
	private IPropertyChangeListener fPropertyChangeListener;
		
	private SelectionProviderMediator fSelectionProviderMediator;
	private ISelectionChangedListener fSelectionChangedListener;
	private IPartListener2 fPartListener;

	private int fCurrentOrientation;
	int fOrientation= VIEW_ORIENTATION_AUTOMATIC;
	boolean fInComputeOrientation= false;
	
	private boolean fLinkingEnabled;
	private boolean fSelectInEditor;
	
	private boolean fIsVisible;
	private boolean fNeedRefresh;	
	private boolean fIsEnableMemberFilter;
	protected boolean fIsRefreshRunnablePosted;
	
	private int fCurrentViewerIndex;
	private TypeHierarchyViewer[] fAllViewers;
	
	protected MethodsViewer fMethodsViewer;	
	
	private SashForm fTypeMethodsSplitter;
	private PageBook fViewerbook;
	protected PageBook fPagebook;
	
	private Label fNoHierarchyShownLabel;
	private Label fEmptyTypesViewer;
	
	private ViewForm fTypeViewerViewForm;
	private ViewForm fMethodViewerViewForm;
	
	private CLabel fMethodViewerPaneLabel;
	private CUILabelProvider fPaneLabelProvider;
	private Composite fParent;
	
	private ToggleViewAction[] fViewActions;
	private ToggleLinkingAction fToggleLinkingAction;
	private HistoryDropDownAction fHistoryDropDownAction;
	private ToggleOrientationAction[] fToggleOrientationActions;
	private EnableMemberFilterAction fEnableMemberFilterAction;
	private ShowQualifiedTypeNamesAction fShowQualifiedTypeNamesAction;
//	private AddMethodStubAction fAddStubAction;
	private FocusOnTypeAction fFocusOnTypeAction;
	private FocusOnSelectionAction fFocusOnSelectionAction;
	private CompositeActionGroup fActionGroups;
	protected SelectAllAction fSelectAllAction;
	
	private WorkingSetFilterActionGroup fWorkingSetActionGroup;
	private Job fRestoreStateJob;
	
	public TypeHierarchyViewPart() {
		fSelectedType= null;
		fInputElement= null;
		fIsVisible= false;
		fIsRefreshRunnablePosted= false;
		fSelectInEditor= true;
		fRestoreStateJob= null;
		
		fHierarchyLifeCycle= new TypeHierarchyLifeCycle();
		fTypeHierarchyLifeCycleListener= new ITypeHierarchyLifeCycleListener() {
			public void typeHierarchyChanged(TypeHierarchyLifeCycle typeHierarchy, ICElement[] changedTypes) {
				doTypeHierarchyChanged(typeHierarchy, changedTypes);
			}
		};
		fHierarchyLifeCycle.addChangedListener(fTypeHierarchyLifeCycleListener);
		
		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				doPropertyChange(event);
			}
		};
		PreferenceConstants.getPreferenceStore().addPropertyChangeListener(fPropertyChangeListener);

		fIsEnableMemberFilter= false;
		
		fInputHistory= new ArrayList();
		fAllViewers= null;
				
		fViewActions= new ToggleViewAction[] {
			new ToggleViewAction(this, VIEW_ID_SUPER),
			new ToggleViewAction(this, VIEW_ID_SUB)
		};
		
		fDialogSettings= CUIPlugin.getDefault().getDialogSettings();
		
		fHistoryDropDownAction= new HistoryDropDownAction(this);
		fHistoryDropDownAction.setEnabled(false);
		
		fToggleOrientationActions= new ToggleOrientationAction[] {
			new ToggleOrientationAction(this, VIEW_ORIENTATION_VERTICAL),
			new ToggleOrientationAction(this, VIEW_ORIENTATION_HORIZONTAL),
			new ToggleOrientationAction(this, VIEW_ORIENTATION_AUTOMATIC),
			new ToggleOrientationAction(this, VIEW_ORIENTATION_SINGLE)
		};
			
		fEnableMemberFilterAction= new EnableMemberFilterAction(this, false);
		fShowQualifiedTypeNamesAction= new ShowQualifiedTypeNamesAction(this, false);
		
		fFocusOnTypeAction= new FocusOnTypeAction(this);
		
		fPaneLabelProvider= new CUILabelProvider();
//		
//		fAddStubAction= new AddMethodStubAction();
		fFocusOnSelectionAction= new FocusOnSelectionAction(this);	
	
		fPartListener= new IPartListener2() {
			public void partVisible(IWorkbenchPartReference ref) {
				IWorkbenchPart part= ref.getPart(false);
				if (part == TypeHierarchyViewPart.this) {
					visibilityChanged(true);
				}
			}

			public void partHidden(IWorkbenchPartReference ref) {
				IWorkbenchPart part= ref.getPart(false);
				if (part == TypeHierarchyViewPart.this) {
					visibilityChanged(false);
				}
			}

			public void partActivated(IWorkbenchPartReference ref) {
				IWorkbenchPart part= ref.getPart(false);
				if (part instanceof IEditorPart)
					editorActivated((IEditorPart) part);
			}

		 	public void partInputChanged(IWorkbenchPartReference ref) {
				IWorkbenchPart part= ref.getPart(false);
				if (part instanceof IEditorPart)
					editorActivated((IEditorPart) part);
		 	}

			public void partBroughtToTop(IWorkbenchPartReference ref) {}
			public void partClosed(IWorkbenchPartReference ref) {}
			public void partDeactivated(IWorkbenchPartReference ref) {}
			public void partOpened(IWorkbenchPartReference ref) {}
		};
		
		fSelectionChangedListener= new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				doSelectionChanged(event);
			}
		};
		
		fLinkingEnabled= PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.LINK_TYPEHIERARCHY_TO_EDITOR);
	}

	/**
	 * Method doPropertyChange.
	 * @param event
	 */
	protected void doPropertyChange(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (fMethodsViewer != null) {
//			if (MembersOrderPreferenceCache.isMemberOrderProperty(event.getProperty())) {
//				fMethodsViewer.refresh();
//			}
		}
		if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property)) {
			updateHierarchyViewer(true);
		}
	}
		
	/**
	 * Adds the entry if new. Inserted at the beginning of the history entries list.
	 */		
	private void addHistoryEntry(ICElement entry) {
		if (fInputHistory.contains(entry)) {
			fInputHistory.remove(entry);
		}
		fInputHistory.add(0, entry);
		fHistoryDropDownAction.setEnabled(true);
	}
	
	private void updateHistoryEntries() {
		for (int i= fInputHistory.size() - 1; i >= 0; i--) {
			ICElement type= (ICElement) fInputHistory.get(i);
			if (!type.exists()) {
				fInputHistory.remove(i);
			}
		}
		fHistoryDropDownAction.setEnabled(!fInputHistory.isEmpty());
	}
	
	/**
	 * Goes to the selected entry, without updating the order of history entries.
	 */	
	public void gotoHistoryEntry(ICElement entry) {
		if (fInputHistory.contains(entry)) {
			updateInput(entry);
		}
	}	
	
	/**
	 * Gets all history entries.
	 */
	public ICElement[] getHistoryEntries() {
		if (fInputHistory.size() > 0) {
			updateHistoryEntries();
		}
		return (ICElement[]) fInputHistory.toArray(new ICElement[fInputHistory.size()]);
	}
	
	/**
	 * Sets the history entries
	 */
	public void setHistoryEntries(ICElement[] elems) {
		fInputHistory.clear();
		for (int i= 0; i < elems.length; i++) {
			fInputHistory.add(elems[i]);
		}
		updateHistoryEntries();
	}
	
	/**
	 * Selects an member in the methods list or in the current hierarchy.
	 */	
	public void selectMember(ICElement member) {
		fSelectInEditor= false;
		if (!TypeUtil.isClassOrStruct(member)) {
			Control methodControl= fMethodsViewer.getControl();
			if (methodControl != null && !methodControl.isDisposed()) {
				methodControl.setFocus();
			}

			fMethodsViewer.setSelection(new StructuredSelection(member), true);
		} else {
			Control viewerControl= getCurrentViewer().getControl();
			if (viewerControl != null && !viewerControl.isDisposed()) {
				viewerControl.setFocus();
			}
			
			if (!member.equals(fSelectedType)) {
				getCurrentViewer().setSelection(new StructuredSelection(member), true);
			}
		}
		fSelectInEditor= true;
	}

	/**
	 * @deprecated
	 */	
	public ICElement getInput() {
	    return fInputElement;
	}
	
	/**
	 * Sets the input to a new type
	 * @deprecated 
	 */
	public void setInput(ICElement type) {
		setInputElement(type);
	}	
	
	/**
	 * Returns the input element of the type hierarchy.
	 * Can be of type <code>ICElement</code> or <code>IPackageFragment</code>
	 */	
	public ICElement getInputElement() {
		return fInputElement;
	}			
		

	/**
	 * Sets the input to a new element.
	 */	
	public void setInputElement(ICElement element) {
		if (element != null) {
		    if (!(element instanceof IStructure)) {
		        element = TypeUtil.getDeclaringClass(element);
			}
			if (element == null || !element.exists()) {
				MessageDialog.openError(getSite().getShell(), TypeHierarchyMessages.getString("TypeHierarchyViewPart.error.title"), TypeHierarchyMessages.getString("TypeHierarchyViewPart.error.message")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
		} else {
			CUIPlugin.getDefault().logErrorMessage("Invalid type hierarchy input type.");//$NON-NLS-1$
		}
		if (element != null && !element.equals(fInputElement)) {
			addHistoryEntry(element);
		}
			
		updateInput(element);
	}
	
	/**
	 * Changes the input to a new type
	 */
	private void updateInput(ICElement inputElement) {
		ICElement prevInput= fInputElement;
		
		synchronized (this) {
			if (fRestoreStateJob != null) {
				fRestoreStateJob.cancel();
				try {
					fRestoreStateJob.join();
				} catch (InterruptedException e) {
					// ignore
				} finally {
					fRestoreStateJob= null;
				}
			}
		}
		
		// Make sure the UI got repainted before we execute a long running
		// operation. This can be removed if we refresh the hierarchy in a 
		// separate thread.
		// Work-araound for http://dev.eclipse.org/bugs/show_bug.cgi?id=30881
		processOutstandingEvents();
		if (inputElement == null) {	
			clearInput();
		} else {
			fInputElement= inputElement;
			try {
				fHierarchyLifeCycle.ensureRefreshedTypeHierarchy(inputElement, CUIPlugin.getActiveWorkbenchWindow());
				// fHierarchyLifeCycle.ensureRefreshedTypeHierarchy(inputElement, getSite().getWorkbenchWindow());
			} catch (InvocationTargetException e) {
				ExceptionHandler.handle(e, getSite().getShell(), TypeHierarchyMessages.getString("TypeHierarchyViewPart.exception.title"), TypeHierarchyMessages.getString("TypeHierarchyViewPart.exception.message")); //$NON-NLS-1$ //$NON-NLS-2$
				clearInput();
				return;
			} catch (InterruptedException e) {
				return;				
			}

			if (!TypeUtil.isClassOrStruct(inputElement)) {
				setView(VIEW_ID_SUPER);
			}
			// turn off member filtering
			fSelectInEditor= false;
			setMemberFilter(null);
			internalSelectType(null, false); // clear selection
			fIsEnableMemberFilter= false;
			if (!inputElement.equals(prevInput)) {
				updateHierarchyViewer(true);
			}
			ICElement root= getSelectableType(inputElement);
			internalSelectType(root, true);
			updateMethodViewer(root);
			updateToolbarButtons();
			updateTitle();
			enableMemberFilter(false);
			fPagebook.showPage(fTypeMethodsSplitter);
			fSelectInEditor= true;
		}
	}
	
	private void processOutstandingEvents() {
		Display display= getDisplay();
		if (display != null && !display.isDisposed())
			display.update();
	}	
	
	private void clearInput() {
		fInputElement= null;
		fHierarchyLifeCycle.freeHierarchy();
		
		updateHierarchyViewer(false);
		updateToolbarButtons();
	}

	/*
	 * @see IWorbenchPart#setFocus
	 */	
	public void setFocus() {
		fPagebook.setFocus();
	}

	/*
	 * @see IWorkbenchPart#dispose
	 */	
	public void dispose() {
		fHierarchyLifeCycle.freeHierarchy();
		fHierarchyLifeCycle.removeChangedListener(fTypeHierarchyLifeCycleListener);
		fPaneLabelProvider.dispose();
		
		if (fMethodsViewer != null) {
			fMethodsViewer.dispose();
		}
		
		if (fPropertyChangeListener != null) {
			CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
			fPropertyChangeListener= null;
		}
		
		getSite().getPage().removePartListener(fPartListener);

		if (fActionGroups != null)
			fActionGroups.dispose();
		
		if (fWorkingSetActionGroup != null) {
			fWorkingSetActionGroup.dispose();
		}
		
		super.dispose();
	}
		
	/**
	 * Answer the property defined by key.
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInSource.class) {
			return getShowInSource();
		}
		if (key == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { CUIPlugin.CVIEW_ID, IPageLayout.ID_RES_NAV  };
				}

			};
		}
		return super.getAdapter(key);
	}
	
	private Control createTypeViewerControl(Composite parent) {
		fViewerbook= new PageBook(parent, SWT.NULL);
				
		KeyListener keyListener= createKeyListener();
						
		// Create the viewers
		TypeHierarchyViewer superTypesViewer= new SuperTypeHierarchyViewer(fViewerbook, fHierarchyLifeCycle, this);
		initializeTypesViewer(superTypesViewer, keyListener, IContextMenuConstants.TARGET_ID_SUPERTYPES_VIEW);
		
		TypeHierarchyViewer subTypesViewer= new SubTypeHierarchyViewer(fViewerbook, fHierarchyLifeCycle, this);
		initializeTypesViewer(subTypesViewer, keyListener, IContextMenuConstants.TARGET_ID_SUBTYPES_VIEW);
		
		fAllViewers= new TypeHierarchyViewer[2];
		fAllViewers[VIEW_ID_SUPER]= superTypesViewer;
		fAllViewers[VIEW_ID_SUB]= subTypesViewer;
		
		int currViewerIndex;
		try {
			currViewerIndex= fDialogSettings.getInt(DIALOGSTORE_HIERARCHYVIEW);
			if (currViewerIndex < 0 || currViewerIndex > 2) {
				currViewerIndex= VIEW_ID_SUPER;
			}
		} catch (NumberFormatException e) {
			currViewerIndex= VIEW_ID_SUPER;
		}
			
		fEmptyTypesViewer= new Label(fViewerbook, SWT.LEFT);
		
		for (int i= 0; i < fAllViewers.length; i++) {
			fAllViewers[i].setInput(fAllViewers[i]);
		}
		
		// force the update
		fCurrentViewerIndex= -1;
		setView(currViewerIndex);
				
		return fViewerbook;
	}
	
	private KeyListener createKeyListener() {
		return new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.stateMask == 0) {
					if (event.keyCode == SWT.F5) {
						ITypeHierarchy hierarchy= fHierarchyLifeCycle.getHierarchy();
						if (hierarchy != null) {
							fHierarchyLifeCycle.typeHierarchyChanged(hierarchy);
							doTypeHierarchyChangedOnViewers(null);
						}
						updateHierarchyViewer(false);
						return;
					}
 				}
			}
		};		
	}
	

	private void initializeTypesViewer(final TypeHierarchyViewer typesViewer, KeyListener keyListener, String cotextHelpId) {
		typesViewer.getControl().setVisible(false);
		typesViewer.getControl().addKeyListener(keyListener);
		typesViewer.initContextMenu(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menu) {
				fillTypesViewerContextMenu(typesViewer, menu);
			}
		}, cotextHelpId,	getSite());
		typesViewer.addPostSelectionChangedListener(fSelectionChangedListener);
		typesViewer.setQualifiedTypeName(isShowQualifiedTypeNames());
		typesViewer.setWorkingSetFilter(fWorkingSetActionGroup.getWorkingSetFilter());
	}
	
	private Control createMethodViewerControl(Composite parent) {
		fMethodsViewer= new MethodsViewer(parent, fHierarchyLifeCycle, this);
		fMethodsViewer.initContextMenu(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menu) {
				fillMethodsViewerContextMenu(menu);
			}
		}, IContextMenuConstants.TARGET_ID_MEMBERS_VIEW, getSite());
		fMethodsViewer.addPostSelectionChangedListener(fSelectionChangedListener);
		
		Control control= fMethodsViewer.getTable();
		control.addKeyListener(createKeyListener());
		control.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				fSelectAllAction.setEnabled(true);
			}

			public void focusLost(FocusEvent e) {
				fSelectAllAction.setEnabled(false);
			}
		});
		
		return control;
	}
	
	private void initDragAndDrop() {
//		Transfer[] transfers= new Transfer[] { LocalSelectionTransfer.getInstance() };
//		int ops= DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
//
//		for (int i= 0; i < fAllViewers.length; i++) {
//			addDragAdapters(fAllViewers[i], ops, transfers);
//			addDropAdapters(fAllViewers[i], ops | DND.DROP_DEFAULT, transfers);
//		}	
//		addDragAdapters(fMethodsViewer, ops, transfers);
//
//		//DND on empty hierarchy
//		DropTarget dropTarget = new DropTarget(fNoHierarchyShownLabel, ops | DND.DROP_DEFAULT);
//		dropTarget.setTransfer(transfers);
//		dropTarget.addDropListener(new TypeHierarchyTransferDropAdapter(this, fAllViewers[0]));
	}
	
//	private void addDropAdapters(AbstractTreeViewer viewer, int ops, Transfer[] transfers){
//		TransferDropTargetListener[] dropListeners= new TransferDropTargetListener[] {
//			new TypeHierarchyTransferDropAdapter(this, viewer)
//		};
//		viewer.addDropSupport(ops, transfers, new DelegatingDropAdapter(dropListeners));
//	}

//	private void addDragAdapters(StructuredViewer viewer, int ops, Transfer[] transfers) {
//		TransferDragSourceListener[] dragListeners= new TransferDragSourceListener[] {
//			new SelectionTransferDragAdapter(viewer)
//		};
//		viewer.addDragSupport(ops, transfers, new JdtViewerDragAdapter(viewer, dragListeners));
//	}	
			
	/**
	 * Returns the inner component in a workbench part.
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite container) {
		fParent= container;
    	addResizeListener(container);

		fPagebook= new PageBook(container, SWT.NONE);
		fWorkingSetActionGroup= new WorkingSetFilterActionGroup(CUIPlugin.ID_TYPE_HIERARCHY, container.getShell(), fPropertyChangeListener);

		// page 1 of page book (no hierarchy label)
		
		fNoHierarchyShownLabel= new Label(fPagebook, SWT.TOP + SWT.LEFT + SWT.WRAP);
		fNoHierarchyShownLabel.setText(TypeHierarchyMessages.getString("TypeHierarchyViewPart.empty")); //$NON-NLS-1$	
		
		// page 2 of page book (viewers)

		fTypeMethodsSplitter= new SashForm(fPagebook, SWT.VERTICAL);
		fTypeMethodsSplitter.setVisible(false);

		fTypeViewerViewForm= new ViewForm(fTypeMethodsSplitter, SWT.NONE);
						
		Control typeViewerControl= createTypeViewerControl(fTypeViewerViewForm);
		fTypeViewerViewForm.setContent(typeViewerControl);
				
		fMethodViewerViewForm= new ViewForm(fTypeMethodsSplitter, SWT.NONE);
		fTypeMethodsSplitter.setWeights(new int[] {35, 65});
		
		Control methodViewerPart= createMethodViewerControl(fMethodViewerViewForm);
		fMethodViewerViewForm.setContent(methodViewerPart);
		
		fMethodViewerPaneLabel= new CLabel(fMethodViewerViewForm, SWT.NONE);
		fMethodViewerViewForm.setTopLeft(fMethodViewerPaneLabel);
				
		ToolBar methodViewerToolBar= new ToolBar(fMethodViewerViewForm, SWT.FLAT | SWT.WRAP);
		fMethodViewerViewForm.setTopCenter(methodViewerToolBar);

		initDragAndDrop();
		
		MenuManager menu= new MenuManager();
		menu.add(fFocusOnTypeAction);
		fNoHierarchyShownLabel.setMenu(menu.createContextMenu(fNoHierarchyShownLabel));
		
		fPagebook.showPage(fNoHierarchyShownLabel);

		try {
			fOrientation= fDialogSettings.getInt(DIALOGSTORE_VIEWORIENTATION);
			if (fOrientation < 0 || fOrientation > 3) {
				fOrientation= VIEW_ORIENTATION_VERTICAL;
			}
		} catch (NumberFormatException e) {
			fOrientation= VIEW_ORIENTATION_AUTOMATIC;
		}

		// force the update
		fCurrentOrientation= -1;
		// will fill the main tool bar
		setOrientation(fOrientation);
		
		if (fMemento != null) { // restore state before creating action
			restoreLinkingEnabled(fMemento);
		}
		fToggleLinkingAction= new ToggleLinkingAction(this);

		// set the filter menu items
		IActionBars actionBars= getViewSite().getActionBars();
		IMenuManager viewMenu= actionBars.getMenuManager();
		for (int i= 0; i < fViewActions.length; i++) {
			ToggleViewAction action= fViewActions[i];
			viewMenu.add(action);
			action.setEnabled(false);
		}
		viewMenu.add(new Separator());
		
		fWorkingSetActionGroup.contributeToMenu(viewMenu);
		
		viewMenu.add(new Separator());
		
		IMenuManager layoutSubMenu= new MenuManager(TypeHierarchyMessages.getString("TypeHierarchyViewPart.layout.submenu")); //$NON-NLS-1$
		viewMenu.add(layoutSubMenu);
		for (int i= 0; i < fToggleOrientationActions.length; i++) {
			layoutSubMenu.add(fToggleOrientationActions[i]);
		}
		viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		viewMenu.add(fShowQualifiedTypeNamesAction);
		viewMenu.add(fToggleLinkingAction);
		
	
		// fill the method viewer tool bar
		ToolBarManager lowertbmanager= new ToolBarManager(methodViewerToolBar);
		lowertbmanager.add(fEnableMemberFilterAction);			
		lowertbmanager.add(new Separator());
		fMethodsViewer.contributeToToolBar(lowertbmanager);
		lowertbmanager.update(true);
							
		// selection provider
		int nHierarchyViewers= fAllViewers.length; 
		StructuredViewer[] trackedViewers= new StructuredViewer[nHierarchyViewers + 1];
		for (int i= 0; i < nHierarchyViewers; i++) {
			trackedViewers[i]= fAllViewers[i];
		}
		trackedViewers[nHierarchyViewers]= fMethodsViewer;
		fSelectionProviderMediator= new SelectionProviderMediator(trackedViewers);
		IStatusLineManager slManager= getViewSite().getActionBars().getStatusLineManager();
		fSelectionProviderMediator.addSelectionChangedListener(new StatusBarUpdater(slManager));
		
		getSite().setSelectionProvider(fSelectionProviderMediator);
		getSite().getPage().addPartListener(fPartListener);
		
		// see http://bugs.eclipse.org/bugs/show_bug.cgi?id=33657
		ICElement input= null; //determineInputElement();
		if (fMemento != null) {
			restoreState(fMemento, input);
		} else if (input != null) {
			setInputElement(input);
		} else {
			setViewerVisibility(false);
		}

		WorkbenchHelp.setHelp(fPagebook, ICHelpContextIds.TYPE_HIERARCHY_VIEW);
		
		
		fActionGroups= new CompositeActionGroup(new ActionGroup[] {
//				new NewWizardsActionGroup(this.getSite()),
//				new OpenEditorActionGroup(this), 
				new OpenViewActionGroup(this), 
//				new CCPActionGroup(this), 
//				new GenerateActionGroup(this),
//				new RefactorActionGroup(this),
//				new CSearchActionGroup(this)
		});
		
		fActionGroups.fillActionBars(actionBars);
		fSelectAllAction= new SelectAllAction(fMethodsViewer);
		
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fSelectAllAction);
	}

	private void addResizeListener(Composite parent) {
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				computeOrientation();
			}
		});
	}

	void computeOrientation() {
		// fix for bug: 63268 error while activating view 
		// avoid recursive calls of compute orientation 
		if (fInComputeOrientation) {
			return;
		}
		fInComputeOrientation= true;
		try {
			if (fOrientation != VIEW_ORIENTATION_AUTOMATIC) {
				setOrientation(fOrientation);
			}
			else {
				if (fOrientation == VIEW_ORIENTATION_SINGLE)
					return;
				Point size= fParent.getSize();
				if (size.x != 0 && size.y != 0) {
					if (size.x > size.y) 
						setOrientation(VIEW_ORIENTATION_HORIZONTAL);
					else 
						setOrientation(VIEW_ORIENTATION_VERTICAL);
				}
			}
		} finally {
			fInComputeOrientation= false;
		}
	}

	/**
	 * called from ToggleOrientationAction.
	 * @param orientation VIEW_ORIENTATION_SINGLE, VIEW_ORIENTATION_HORIZONTAL or VIEW_ORIENTATION_VERTICAL
	 */	
	public void setOrientation(int orientation) {
		if (fCurrentOrientation != orientation) {
			boolean methodViewerNeedsUpdate= false;
			
			if (fMethodViewerViewForm != null && !fMethodViewerViewForm.isDisposed()
					&& fTypeMethodsSplitter != null && !fTypeMethodsSplitter.isDisposed()) {
				if (orientation == VIEW_ORIENTATION_SINGLE) {
					fMethodViewerViewForm.setVisible(false);
					enableMemberFilter(false);
					updateMethodViewer(null);
				} else {
					if (fCurrentOrientation == VIEW_ORIENTATION_SINGLE) {
						fMethodViewerViewForm.setVisible(true);
						methodViewerNeedsUpdate= true;
					}
					boolean horizontal= orientation == VIEW_ORIENTATION_HORIZONTAL;
					fTypeMethodsSplitter.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
				}
				updateMainToolbar(orientation);
				fTypeMethodsSplitter.layout();
			}
			updateCheckedState();
			if (methodViewerNeedsUpdate) {
				updateMethodViewer(fSelectedType);
			}
			fDialogSettings.put(DIALOGSTORE_VIEWORIENTATION, orientation);
			fCurrentOrientation= orientation;
		}
	}
	
		
	private void updateCheckedState() {
		for (int i= 0; i < fToggleOrientationActions.length; i++) {
			fToggleOrientationActions[i].setChecked(fOrientation == fToggleOrientationActions[i].getOrientation());
		}
	}

	private void updateMainToolbar(int orientation) {
		IActionBars actionBars= getViewSite().getActionBars();
		IToolBarManager tbmanager= actionBars.getToolBarManager();	
				
		if (orientation == VIEW_ORIENTATION_HORIZONTAL) {
			clearMainToolBar(tbmanager);
			ToolBar typeViewerToolBar= new ToolBar(fTypeViewerViewForm, SWT.FLAT | SWT.WRAP);
			fillMainToolBar(new ToolBarManager(typeViewerToolBar));
			fTypeViewerViewForm.setTopLeft(typeViewerToolBar);
		} else {
			fTypeViewerViewForm.setTopLeft(null);
			fillMainToolBar(tbmanager);
		}
	}
	
	private void fillMainToolBar(IToolBarManager tbmanager) {
		tbmanager.removeAll();
		for (int i= 0; i < fViewActions.length; i++) {
			tbmanager.add(fViewActions[i]);
		}
		tbmanager.add(fHistoryDropDownAction);
		tbmanager.update(false);	
	}

	private void clearMainToolBar(IToolBarManager tbmanager) {
		tbmanager.removeAll();
		tbmanager.update(false);		
	}	
	
	
	/**
	 * Creates the context menu for the hierarchy viewers
	 */
	protected void fillTypesViewerContextMenu(TypeHierarchyViewer viewer, IMenuManager menu) {
		CUIPlugin.createStandardGroups(menu);
		
		menu.appendToGroup(IContextMenuConstants.GROUP_SHOW, new Separator(GROUP_FOCUS));
		// viewer entries
		viewer.contributeToContextMenu(menu);
		
		if (fFocusOnSelectionAction.canActionBeAdded())
			menu.appendToGroup(GROUP_FOCUS, fFocusOnSelectionAction);
		menu.appendToGroup(GROUP_FOCUS, fFocusOnTypeAction);

		fActionGroups.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		fActionGroups.fillContextMenu(menu);
		fActionGroups.setContext(null);
	}

	/**
	 * Creates the context menu for the method viewer
	 */	
	protected void fillMethodsViewerContextMenu(IMenuManager menu) {
		CUIPlugin.createStandardGroups(menu);
		// viewer entries
		fMethodsViewer.contributeToContextMenu(menu);
		fActionGroups.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		fActionGroups.fillContextMenu(menu);
		fActionGroups.setContext(null);
//		if (fSelectedType != null && fAddStubAction.init(fSelectedType, fMethodsViewer.getSelection())) {
//			IMenuManager source= menu.findMenuUsingPath(GenerateActionGroup.MENU_ID);
//			if (source != null)
//				source.appendToGroup(GenerateActionGroup.GROUP_GENERATE, fAddStubAction);
//		}
	}
	
	/**
	 * Toggles between the empty viewer page and the hierarchy
	 */
	private void setViewerVisibility(boolean showHierarchy) {
		if (showHierarchy) {
			fViewerbook.showPage(getCurrentViewer().getControl());
		} else {
			fViewerbook.showPage(fEmptyTypesViewer);
		}
	}
	
	/**
	 * Sets the member filter. <code>null</code> disables member filtering.
	 */	
	private void setMemberFilter(IMember[] memberFilter) {
		Assert.isNotNull(fAllViewers);
		for (int i= 0; i < fAllViewers.length; i++) {
			fAllViewers[i].setMemberFilter(memberFilter);
		}
	}	
	
	private ICElement getSelectableType(ICElement elem) {
	    if (!TypeUtil.isClassOrStruct(elem)) {
			return getCurrentViewer().getTreeRootType();
		}
		return elem;
	}
	
	private void internalSelectType(ICElement elem, boolean reveal) {	
		TypeHierarchyViewer viewer= getCurrentViewer();
		if (viewer != null) {
			viewer.removePostSelectionChangedListener(fSelectionChangedListener);
			viewer.setSelection(elem != null ? new StructuredSelection(elem) : StructuredSelection.EMPTY, reveal);
			viewer.addPostSelectionChangedListener(fSelectionChangedListener);
		}
	}
		
	/**
	 * When the input changed or the hierarchy pane becomes visible,
	 * <code>updateHierarchyViewer<code> brings up the correct view and refreshes
	 * the current tree
	 */
	protected void updateHierarchyViewer(final boolean doExpand) {
		if (fInputElement == null) {
			fNoHierarchyShownLabel.setText(TypeHierarchyMessages.getString("TypeHierarchyViewPart.empty")); //$NON-NLS-1$	
			fPagebook.showPage(fNoHierarchyShownLabel);
		} else {
		    final TypeHierarchyViewer viewer = getCurrentViewer();
			if (viewer != null && viewer.containsElements() != null) {
				Runnable runnable= new Runnable() {
					public void run() {
						viewer.updateContent(doExpand); // refresh
					}
				};
				BusyIndicator.showWhile(getDisplay(), runnable);
				if (!isChildVisible(fViewerbook, viewer.getControl())) {
					setViewerVisibility(true);
				}	
			} else {							
				fEmptyTypesViewer.setText(TypeHierarchyMessages.getFormattedString("TypeHierarchyViewPart.nodecl", fInputElement.getElementName()));				 //$NON-NLS-1$
				setViewerVisibility(false);
			}
		}
	}
	
	private void updateMethodViewer(final ICElement input) {
		if (!fIsEnableMemberFilter && fCurrentOrientation != VIEW_ORIENTATION_SINGLE) {
			if (input == fMethodsViewer.getInput()) {
				if (input != null) {
					Runnable runnable= new Runnable() {
						public void run() {
							fMethodsViewer.refresh(); // refresh
						}
					};
					BusyIndicator.showWhile(getDisplay(), runnable);					
				}
			} else {
				if (input != null) {
					fMethodViewerPaneLabel.setText(fPaneLabelProvider.getText(input));
					fMethodViewerPaneLabel.setImage(fPaneLabelProvider.getImage(input));
				} else {
					fMethodViewerPaneLabel.setText(""); //$NON-NLS-1$
					fMethodViewerPaneLabel.setImage(null);
				}
				Runnable runnable= new Runnable() {
					public void run() {
						fMethodsViewer.setInput(input); // refresh
					}
				};
				BusyIndicator.showWhile(getDisplay(), runnable);				
			}
		}
	}
	
	protected void doSelectionChanged(SelectionChangedEvent e) {
		if (e.getSelectionProvider() == fMethodsViewer) {
			methodSelectionChanged(e.getSelection());
		} else {
			typeSelectionChanged(e.getSelection());
		}
	}
	
	
	
	private void methodSelectionChanged(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			List selected= ((IStructuredSelection)sel).toList();
			int nSelected= selected.size();
			if (fIsEnableMemberFilter) {
				IMember[] memberFilter= null;
				if (nSelected > 0) {
					memberFilter= new IMember[nSelected];
					selected.toArray(memberFilter);
				}
				setMemberFilter(memberFilter);
				updateHierarchyViewer(true);
				updateTitle();
				internalSelectType(fSelectedType, true);	
			}
			if (nSelected == 1 && fSelectInEditor) {
				revealElementInEditor(selected.get(0), fMethodsViewer);
			}
		}
	}
	
	private void typeSelectionChanged(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			List selected= ((IStructuredSelection)sel).toList();
			int nSelected= selected.size();
			if (nSelected != 0) {
				List types= new ArrayList(nSelected);
				for (int i= nSelected-1; i >= 0; i--) {
					Object elem= selected.get(i);
					if (elem instanceof ICElement && !types.contains(elem)) {
						types.add(elem);
					}
				}
				if (types.size() == 1) {
					fSelectedType= (ICElement) types.get(0);
					updateMethodViewer(fSelectedType);
				} else if (types.size() == 0) {
					// method selected, no change
				}
				if (nSelected == 1 && fSelectInEditor) {
					revealElementInEditor(selected.get(0), getCurrentViewer());
				}
			} else {
				fSelectedType= null;
				updateMethodViewer(null);
			}
		}
	}
	
	private void revealElementInEditor(Object elem, StructuredViewer originViewer) {
		// only allow revealing when the type hierarchy is the active page
		// no revealing after selection events due to model changes
		
		if (getSite().getPage().getActivePart() != this) {
			return;
		}
		
		if (fSelectionProviderMediator.getViewerInFocus() != originViewer) {
			return;
		}
		
		IEditorPart editorPart= EditorUtility.isOpenInEditor(elem);
		if (editorPart != null && (elem instanceof ICElement)) {
			getSite().getPage().removePartListener(fPartListener);
			getSite().getPage().bringToTop(editorPart);
			EditorUtility.revealInEditor(editorPart, (ICElement) elem);
			getSite().getPage().addPartListener(fPartListener);
		}
	}
	
	private Display getDisplay() {
		if (fPagebook != null && !fPagebook.isDisposed()) {
			return fPagebook.getDisplay();
		}
		return null;
	}		
	
	private boolean isChildVisible(Composite pb, Control child) {
		Control[] children= pb.getChildren();
		for (int i= 0; i < children.length; i++) {
			if (children[i] == child && children[i].isVisible())
				return true;
		}
		return false;
	}
	
	private void updateTitle() {
		String tooltip;
		String title;
		String viewerTitle;
	    TypeHierarchyViewer viewer = getCurrentViewer();
	    if (viewer != null) {
			viewerTitle= viewer.getTitle();
	    } else {
	        viewerTitle = TypeHierarchyMessages.getString("TraditionalHierarchyViewer.title"); //$NON-NLS-1$
	    }
			
		if (fInputElement != null) {
			String[] args= new String[] { viewerTitle, CElementLabels.getElementLabel(fInputElement, CElementLabels.ALL_DEFAULT) };
			title= TypeHierarchyMessages.getFormattedString("TypeHierarchyViewPart.title", args); //$NON-NLS-1$
			tooltip= TypeHierarchyMessages.getFormattedString("TypeHierarchyViewPart.tooltip", args); //$NON-NLS-1$
		} else {
			title= viewerTitle;
			tooltip= viewerTitle;
		}
		setPartName(title);
		setContentDescription(""); //$NON-NLS-1$
		setTitleToolTip(tooltip);
	}
	
	private void updateToolbarButtons() {
		for (int i= 0; i < fViewActions.length; i++) {
			ToggleViewAction action= fViewActions[i];
			action.setEnabled(true);
		}
	}
		
	/**
	 * Sets the current view (see view id)
	 * called from ToggleViewAction. Must be called after creation of the view part.
	 */	
	public void setView(int viewerIndex) {
		Assert.isNotNull(fAllViewers);
		if (viewerIndex < fAllViewers.length && fCurrentViewerIndex != viewerIndex) {			
			fCurrentViewerIndex= viewerIndex;
			
			updateHierarchyViewer(true);
			if (fInputElement != null) {
				ISelection currSelection= getCurrentViewer().getSelection();
				if (currSelection == null || currSelection.isEmpty()) {
					internalSelectType(getSelectableType(fInputElement), false);
					currSelection= getCurrentViewer().getSelection();
				}
				if (!fIsEnableMemberFilter) {
					typeSelectionChanged(currSelection);
				}
			}		
			updateTitle();
					
			fDialogSettings.put(DIALOGSTORE_HIERARCHYVIEW, viewerIndex);
			getCurrentViewer().getTree().setFocus();
		}
		for (int i= 0; i < fViewActions.length; i++) {
			ToggleViewAction action= fViewActions[i];
			action.setChecked(fCurrentViewerIndex == action.getViewerIndex());
		}
	}

	/**
	 * Gets the current active view index.
	 */		
	public int getViewIndex() {
		return fCurrentViewerIndex;
	}
	
	private TypeHierarchyViewer getCurrentViewer() {
	    if (fCurrentViewerIndex != -1)
	        return fAllViewers[fCurrentViewerIndex];
	    return null;
	}

	/**
	 * called from EnableMemberFilterAction.
	 * Must be called after creation of the view part.
	 */	
	public void enableMemberFilter(boolean on) {
		if (on != fIsEnableMemberFilter) {
			fIsEnableMemberFilter= on;
			if (!on) {
				ICElement methodViewerInput= (ICElement) fMethodsViewer.getInput();
				setMemberFilter(null);
				updateHierarchyViewer(true);
				updateTitle();
			
				if (methodViewerInput != null && getCurrentViewer().isElementShown(methodViewerInput)) {
					// avoid that the method view changes content by selecting the previous input
					internalSelectType(methodViewerInput, true);
				} else if (fSelectedType != null) {
					// choose a input that exists
					internalSelectType(fSelectedType, true);
					updateMethodViewer(fSelectedType);
				}
			} else {
				methodSelectionChanged(fMethodsViewer.getSelection());
			}
		}
		fEnableMemberFilterAction.setChecked(on);
	}
	
	/**
	 * called from ShowQualifiedTypeNamesAction. Must be called after creation
	 * of the view part.
	 */	
	public void showQualifiedTypeNames(boolean on) {
		if (fAllViewers == null) {
			return;
		}
		for (int i= 0; i < fAllViewers.length; i++) {
			fAllViewers[i].setQualifiedTypeName(on);
		}
	}
	
	private boolean isShowQualifiedTypeNames() {
		return fShowQualifiedTypeNamesAction.isChecked();
	}
	
	/**
	 * Called from ITypeHierarchyLifeCycleListener.
	 * Can be called from any thread
	 */
	protected void doTypeHierarchyChanged(final TypeHierarchyLifeCycle typeHierarchy, final ICElement[] changedTypes) {
		if (!fIsVisible) {
			fNeedRefresh= true;
			return;
		}
		if (fIsRefreshRunnablePosted) {
			return;
		}
		
		Display display= getDisplay();
		if (display != null) {
			fIsRefreshRunnablePosted= true;
			display.asyncExec(new Runnable() {
				public void run() {
					try {
						if (fPagebook != null && !fPagebook.isDisposed()) {
							doTypeHierarchyChangedOnViewers(changedTypes);
						}
					} finally {
						fIsRefreshRunnablePosted= false;
					}
				}
			});
		}
	}
	
	protected void doTypeHierarchyChangedOnViewers(ICElement[] changedTypes) {
		if (fHierarchyLifeCycle.getHierarchy() == null || !fHierarchyLifeCycle.getHierarchy().exists()) {
			clearInput();
		} else {
			if (changedTypes == null) {
				// hierarchy change
				try { 
					fHierarchyLifeCycle.ensureRefreshedTypeHierarchy(fInputElement, getSite().getWorkbenchWindow());
				} catch (InvocationTargetException e) {
					ExceptionHandler.handle(e, getSite().getShell(), TypeHierarchyMessages.getString("TypeHierarchyViewPart.exception.title"), TypeHierarchyMessages.getString("TypeHierarchyViewPart.exception.message")); //$NON-NLS-1$ //$NON-NLS-2$
					clearInput();
					return;
				} catch (InterruptedException e) {
					return;
				}
				fMethodsViewer.refresh();
				updateHierarchyViewer(false);
			} else {
				// elements in hierarchy modified
				Object methodViewerInput= fMethodsViewer.getInput();
				fMethodsViewer.refresh();
				fMethodViewerPaneLabel.setText(fPaneLabelProvider.getText(methodViewerInput));
				fMethodViewerPaneLabel.setImage(fPaneLabelProvider.getImage(methodViewerInput));				
				if (getCurrentViewer().isMethodFiltering()) {
					if (changedTypes.length == 1) {
						getCurrentViewer().refresh(changedTypes[0]);
					} else {
						updateHierarchyViewer(false);
					}
				} else {
					getCurrentViewer().update(changedTypes, new String[] { IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE } );
				}
			}
		}
	}	
	
	/*
	 * @see IViewPart#init
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fMemento= memento;
	}	
	
	/*
	 * @see ViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		if (fPagebook == null) {
			// part has not been created
			if (fMemento != null) { //Keep the old state;
				memento.putMemento(fMemento);
			}
			return;
		}
//		if (fInputElement != null) {
//			String handleIndentifier=  fInputElement.getHandleIdentifier();
//			memento.putString(TAG_INPUT, handleIndentifier);
//		}		
		memento.putInteger(TAG_VIEW, getViewIndex());
		memento.putInteger(TAG_ORIENTATION, fOrientation);	
		int weigths[]= fTypeMethodsSplitter.getWeights();
		int ratio= (weigths[0] * 1000) / (weigths[0] + weigths[1]);
		memento.putInteger(TAG_RATIO, ratio);
		
		ScrollBar bar= getCurrentViewer().getTree().getVerticalBar();
		int position= bar != null ? bar.getSelection() : 0;
		memento.putInteger(TAG_VERTICAL_SCROLL, position);

//		ICElement selection= (ICElement)((IStructuredSelection) getCurrentViewer().getSelection()).getFirstElement();
//		if (selection != null) {
//			memento.putString(TAG_SELECTION, selection.getHandleIdentifier());
//		}
		
		fWorkingSetActionGroup.saveState(memento);
			
		fMethodsViewer.saveState(memento);
		
		saveLinkingEnabled(memento);
	}
	
	private void saveLinkingEnabled(IMemento memento) {
		memento.putInteger(PreferenceConstants.LINK_TYPEHIERARCHY_TO_EDITOR, fLinkingEnabled ? 1 : 0);
	}

	/**
	 * Restores the type hierarchy settings from a memento.
	 */
	private void restoreState(final IMemento memento, ICElement defaultInput) {
		ICElement input= defaultInput;
//		String elementId= memento.getString(TAG_INPUT);
//		if (elementId != null) {
//			input= CoreModel.create(elementId);
//			if (input != null && !input.exists()) {
//				input= null;
//			}
//		}
		if (input == null) {
			doRestoreState(memento, input);
		} else {
			final ICElement hierarchyInput= input;
			
			synchronized (this) {
				String label= TypeHierarchyMessages.getFormattedString("TypeHierarchyViewPart.restoreinput", hierarchyInput.getElementName()); //$NON-NLS-1$
				fNoHierarchyShownLabel.setText(label); 

				fRestoreStateJob= new Job(label) {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							doRestoreInBackground(memento, hierarchyInput, monitor);
						} catch (CModelException e) {
							return e.getStatus();
						} catch (OperationCanceledException e) {
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				fRestoreStateJob.schedule();
			}
		}
	}
	
	protected void doRestoreInBackground(final IMemento memento, final ICElement hierarchyInput, IProgressMonitor monitor) throws CModelException {
		fHierarchyLifeCycle.doHierarchyRefresh(hierarchyInput, monitor);	
		if (!monitor.isCanceled()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					// running async: check first if view still exists
					if (fPagebook != null && !fPagebook.isDisposed()) {
						doRestoreState(memento, hierarchyInput);
					}
				}
			});
		}
	}
	
		
	final void doRestoreState(IMemento memento, ICElement input) {
		synchronized (this) {
			if (fRestoreStateJob == null) {
				return;
			}
			fRestoreStateJob= null;
		}
		
		fWorkingSetActionGroup.restoreState(memento);
		setInputElement(input);

		Integer viewerIndex= memento.getInteger(TAG_VIEW);
		if (viewerIndex != null) {
			setView(viewerIndex.intValue());
		}
		Integer orientation= memento.getInteger(TAG_ORIENTATION);
		if (orientation != null) {
			fOrientation= orientation.intValue();
		}
		computeOrientation();
		updateCheckedState();

		Integer ratio= memento.getInteger(TAG_RATIO);
		if (ratio != null) {
			fTypeMethodsSplitter.setWeights(new int[] { ratio.intValue(), 1000 - ratio.intValue() });
		}
		ScrollBar bar= getCurrentViewer().getTree().getVerticalBar();
		if (bar != null) {
			Integer vScroll= memento.getInteger(TAG_VERTICAL_SCROLL);
			if (vScroll != null) {
				bar.setSelection(vScroll.intValue());
			}
		}
		fMethodsViewer.restoreState(memento);
	}
	
	private void restoreLinkingEnabled(IMemento memento) {
		Integer val= memento.getInteger(PreferenceConstants.LINK_TYPEHIERARCHY_TO_EDITOR);
		if (val != null) {
			fLinkingEnabled= val.intValue() != 0;
		}
	}
	
	
	/**
	 * view part becomes visible
	 */
	protected void visibilityChanged(boolean isVisible) {
		fIsVisible= isVisible;
		if (isVisible && fNeedRefresh) {
			doTypeHierarchyChangedOnViewers(null);
		}
		fNeedRefresh= false;
	}
	
	
	/**
	 * Link selection to active editor.
	 */
	protected void editorActivated(IEditorPart editor) {
		if (!isLinkingEnabled()) {
			return;
		}
		if (fInputElement == null) {
			// no type hierarchy shown
			return;
		}
		
		ICElement elem= (ICElement)editor.getEditorInput().getAdapter(ICElement.class);
		TypeHierarchyViewer currentViewer= getCurrentViewer();
		if (elem instanceof ITranslationUnit) {
		    try {
			    ICElement[] allTypes= TypeUtil.getAllTypes((ITranslationUnit)elem);
				for (int i= 0; i < allTypes.length; i++) {
					if (currentViewer.isElementShown(allTypes[i])) {
						internalSelectType(allTypes[i], true);
						updateMethodViewer(allTypes[i]);
						return;
					}
				}
		    } catch (CModelException e) {
		    }
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider#getViewPartInput()
	 */
	public Object getViewPartInput() {
		return fInputElement;
	}
	
	
	/**
	 * Returns the <code>IShowInSource</code> for this view.
	 */
	protected IShowInSource getShowInSource() {
		return new IShowInSource() {
			public ShowInContext getShowInContext() {
				return new ShowInContext(
					null,
				getSite().getSelectionProvider().getSelection());
			}
		};
	}
	
	boolean isLinkingEnabled() {
		return fLinkingEnabled;
	}
	
	public void setLinkingEnabled(boolean enabled) {
		fLinkingEnabled= enabled;
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.LINK_TYPEHIERARCHY_TO_EDITOR, enabled);

		if (enabled) {
			IEditorPart editor = getSite().getPage().getActiveEditor();
			if (editor != null) {
				editorActivated(editor);
			}
		}
	}
	
	public void clearNeededRefresh() {
		fNeedRefresh= false;
	}

}
