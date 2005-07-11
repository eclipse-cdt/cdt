package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.AbstractToggleLinkingAction;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.cview.SelectionTransferDragAdapter;
import org.eclipse.cdt.internal.ui.cview.SelectionTransferDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.CDTViewerDragAdapter;
import org.eclipse.cdt.internal.ui.dnd.DelegatingDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.TransferDragSourceListener;
import org.eclipse.cdt.internal.ui.dnd.TransferDropTargetListener;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.StandardCElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.CustomFiltersActionGroup;
import org.eclipse.cdt.ui.actions.MemberFilterActionGroup;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.cdt.ui.actions.RefactoringActionGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

public class CContentOutlinePage extends Page implements IContentOutlinePage, ISelectionChangedListener {
	private CEditor fEditor;
	private ITranslationUnit fInput;
	private ProblemTreeViewer treeViewer;
	private ListenerList selectionChangedListeners = new ListenerList();
	private TogglePresentationAction fTogglePresentation;
	private String fContextMenuId;
	
	protected OpenIncludeAction fOpenIncludeAction;
	private IncludeGroupingAction fIncludeGroupingAction;
	
	private MemberFilterActionGroup fMemberFilterActionGroup;

	private ActionGroup fSelectionSearchGroup;
	private ActionGroup fRefactoringActionGroup;
	private ActionGroup fOpenViewActionGroup;
	/**
	 * Custom filter action group.
	 * @since 3.0
	 */
	private CustomFiltersActionGroup fCustomFiltersActionGroup;

	public class IncludeGroupingAction extends Action {
		CContentOutlinePage outLine;

		public IncludeGroupingAction(CContentOutlinePage outlinePage) {
			super(ActionMessages.getString("IncludesGroupingAction.label")); //$NON-NLS-1$
			setDescription(ActionMessages.getString("IncludesGroupingAction.description")); //$NON-NLS-1$
			setToolTipText(ActionMessages.getString("IncludeGroupingAction.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "synced.gif"); //$NON-NLS-1$		
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.LINK_EDITOR_ACTION);

			boolean enabled= isIncludesGroupingEnabled();
			setChecked(enabled);
			outLine = outlinePage;
		}

		/**
		 * Runs the action.
		 */
		public void run() {
			boolean oldValue = isIncludesGroupingEnabled();
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_INCLUDES, isChecked());
			if (oldValue != isChecked()) {
				outLine.contentUpdated();
			}
		}

		public boolean isIncludesGroupingEnabled () {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES);
		}

	}

	/**
	 * This action toggles whether this C Outline page links
	 * its selection to the active editor.
	 * 
	 * @since 3.0
	 */
	public class ToggleLinkingAction extends AbstractToggleLinkingAction {
	
	    CContentOutlinePage fOutlinePage;
	
		/**
		 * Constructs a new action.
		 * 
		 * @param outlinePage the Java outline page
		 */
		public ToggleLinkingAction(CContentOutlinePage outlinePage) {
			//boolean isLinkingEnabled= PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE);
			boolean isLinkingEnabled= true;
			setChecked(isLinkingEnabled);
			fOutlinePage= outlinePage;
		}

		/**
		 * Runs the action.
		 */
		public void run() {
			//TODO synchronize selection with editor
			//PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, isChecked());
			//if (isChecked() && fEditor != null)
			//	fEditor.synchronizeOutlinePage(fEditor.computeHighlightRangeSourceReference(), false);
		}

	}
	
	public CContentOutlinePage(CEditor editor) {
		this("#TranslationUnitOutlinerContext", editor); //$NON-NLS-1$
	}
	
	public CContentOutlinePage(String contextMenuID, CEditor editor) {
		super();
		fEditor= editor;
		fInput= null;
		fContextMenuId = contextMenuID;

		fTogglePresentation= new TogglePresentationAction();
		fTogglePresentation.setEditor(editor);
		
		fOpenIncludeAction= new OpenIncludeAction(this);
	}
	
	public ICElement getRoot() {
		return fInput;
	}
	
	/**
	 * Called by the editor to signal that the content has updated.
	 */
	public void contentUpdated() {
		if (fInput != null) {				
			final TreeViewer treeViewer= getTreeViewer();
			if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!treeViewer.getControl().isDisposed()) {
							ISelection sel= treeViewer.getSelection();
							treeViewer.setSelection(updateSelection(sel));		
							treeViewer.refresh();
						}
					}
				});
			}
		}
	}
	
	protected ISelection updateSelection(ISelection sel) {
		ArrayList newSelection= new ArrayList();
		if (sel instanceof IStructuredSelection) {
			Iterator iter= ((IStructuredSelection)sel).iterator();
			for (;iter.hasNext();) {
				//ICElement elem= fInput.findEqualMember((ICElement)iter.next());
				Object o = iter.next();
				if (o instanceof ICElement) {
					newSelection.add(o);
				}
			}
		}
		return new StructuredSelection(newSelection);
	}
	
	/**
	 * called to create the context menu of the outline
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		CUIPlugin.createStandardGroups(menu);
		
		if (OpenViewActionGroup.canActionBeAdded(getSelection())){
			fOpenViewActionGroup.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
			fOpenViewActionGroup.fillContextMenu(menu);
			fOpenViewActionGroup.setContext(null);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}

		if (OpenIncludeAction.canActionBeAdded(getSelection())) {
			menu.add(fOpenIncludeAction);
		}
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
		
		if (SelectionSearchGroup.canActionBeAdded(getSelection())){
			fSelectionSearchGroup.fillContextMenu(menu);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
		
		fRefactoringActionGroup.fillContextMenu(menu);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		treeViewer = new ProblemTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		//treeViewer.setContentProvider(new CElementContentProvider(true, true));
		treeViewer.setContentProvider(new CContentOutlinerProvider(treeViewer));
		treeViewer.setLabelProvider(new DecoratingCLabelProvider(new StandardCElementLabelProvider(), true));
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.setUseHashlookup(true);
		treeViewer.addSelectionChangedListener(this);

		initDragAndDrop();

		MenuManager manager= new MenuManager(fContextMenuId);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		Control control= treeViewer.getControl();
		Menu menu= manager.createContextMenu(control);
		control.setMenu(menu);
	
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
			 */
			public void doubleClick(DoubleClickEvent event) {
				if (fOpenIncludeAction != null) {
					fOpenIncludeAction.run();
				}
			}
		});
		// register global actions
		IPageSite site= getSite();
		site.registerContextMenu(fContextMenuId, manager, treeViewer);
		site.setSelectionProvider(treeViewer);
		
		IActionBars bars= site.getActionBars();		
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY, fTogglePresentation);

		fSelectionSearchGroup = new SelectionSearchGroup(this);
		fRefactoringActionGroup = new RefactoringActionGroup(this, null);
		fOpenViewActionGroup = new OpenViewActionGroup(this);
		// Custom filter group
		fCustomFiltersActionGroup= new CustomFiltersActionGroup("org.eclipse.cdt.ui.COutlinePage", getTreeViewer()); //$NON-NLS-1$

		treeViewer.setInput(fInput);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, ICHelpContextIds.COUTLINE_VIEW);
	}
	
	public void dispose() {
		if (treeViewer != null) {
			treeViewer.removeSelectionChangedListener(this);
		}
		
		if (fTogglePresentation != null) {
			fTogglePresentation.setEditor(null);
			fTogglePresentation= null;
		}
		
		if (fMemberFilterActionGroup != null) {
			fMemberFilterActionGroup.dispose();
			fMemberFilterActionGroup= null;
		}
		
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.dispose();
			fRefactoringActionGroup= null;
		}
		if (fOpenViewActionGroup != null) {
		    fOpenViewActionGroup.dispose();
		    fOpenViewActionGroup= null;
		}
		
		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.dispose();
			fSelectionSearchGroup= null;
		}
				
		if (selectionChangedListeners != null) {
			selectionChangedListeners.clear();
			selectionChangedListeners= null;
		}
		
		fInput= null;
		
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		IToolBarManager toolBarManager= actionBars.getToolBarManager();
		
		LexicalSortingAction action= new LexicalSortingAction(getTreeViewer());
		toolBarManager.add(action);

		fMemberFilterActionGroup= new MemberFilterActionGroup(treeViewer, "COutlineViewer"); //$NON-NLS-1$
		fMemberFilterActionGroup.fillActionBars(actionBars);

		fCustomFiltersActionGroup.fillActionBars(actionBars);

		IMenuManager menu= actionBars.getMenuManager();
		menu.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
		
		//fToggleLinkingAction= new ToggleLinkingAction(this);
		//menu.add(fToggleLinkingAction);
		fIncludeGroupingAction= new IncludeGroupingAction(this);
		menu.add(fIncludeGroupingAction);
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionProvider.
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	/**
	 * Fires a selection changed event.
	 *
	 * @param selction the new selection
	 */
	protected void fireSelectionChanged(ISelection selection) {
		// create an event
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
	
		// fire the event
		Object[] listeners = selectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((ISelectionChangedListener) listeners[i]).selectionChanged(event);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IPage (and Page).
	 */
	public Control getControl() {
		if (treeViewer == null)
			return null;
		return treeViewer.getControl();
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionProvider.
	 */
	public ISelection getSelection() {
		if (treeViewer == null)
			return StructuredSelection.EMPTY;
		return treeViewer.getSelection();
	}

	/**
	 * Returns this page's tree viewer.
	 *
	 * @return this page's tree viewer, or <code>null</code> if 
	 *   <code>createControl</code> has not been called yet
	 */
	protected TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionProvider.
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionChangeListener.
	 * Gives notification that the tree selection has changed.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		fireSelectionChanged(event.getSelection());
	}

	/**
	 * Sets focus to a part in the page.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionProvider.
	 */
	public void setSelection(ISelection selection) {
		if (treeViewer != null) 
			treeViewer.setSelection(selection);
	}

	/**
	 * Set the current input to the content provider.  
	 * @param unit
	 */
	public void setInput(ITranslationUnit unit) {
		fInput = unit;
		if (treeViewer != null) {
			treeViewer.setInput (fInput);
		}
	}

	private void initDragAndDrop() {
		int ops= DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transfers= new Transfer[] {
			LocalSelectionTransfer.getInstance()
		};
		
		// Drop Adapter
		TransferDropTargetListener[] dropListeners= new TransferDropTargetListener[] {
			new SelectionTransferDropAdapter(treeViewer)
		};
		treeViewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, new DelegatingDropAdapter(dropListeners));
		
		// Drag Adapter
		TransferDragSourceListener[] dragListeners= new TransferDragSourceListener[] {
			new SelectionTransferDragAdapter(treeViewer)
		};
		treeViewer.addDragSupport(ops, transfers, new CDTViewerDragAdapter(treeViewer, dragListeners));
	}

}
