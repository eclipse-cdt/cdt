/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

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
import org.eclipse.cdt.internal.ui.dnd.*;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.StandardCElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.*;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
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
	private ProblemTreeViewer fTreeViewer;
	private ListenerList selectionChangedListeners = new ListenerList(ListenerList.IDENTITY);
	private TogglePresentationAction fTogglePresentation;
	private String fContextMenuId;
	private Menu fMenu;
	
	protected OpenIncludeAction fOpenIncludeAction;
	private IncludeGroupingAction fIncludeGroupingAction;
	
	private MemberFilterActionGroup fMemberFilterActionGroup;

	private ActionGroup fSelectionSearchGroup;
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
    * Returns the CEditor corresponding to this CContentOutlinePage.
    * @param return
    */
	public CEditor getEditor() {
		return fEditor;
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
	}

	protected CContentOutlinerProvider createContentProvider(TreeViewer viewer) {
		IWorkbenchPart part= getSite().getPage().getActivePart();
		if (part == null) {
			return new CContentOutlinerProvider(viewer);
		}
		return new CContentOutlinerProvider(viewer, part.getSite());
	}

	protected ProblemTreeViewer createTreeViewer(Composite parent) {
		fTreeViewer = new ProblemTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fTreeViewer.setContentProvider(createContentProvider(fTreeViewer));
		fTreeViewer.setLabelProvider(new DecoratingCLabelProvider(new StandardCElementLabelProvider(), true));
		fTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		fTreeViewer.setUseHashlookup(true);
		fTreeViewer.addSelectionChangedListener(this);
		return fTreeViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fTreeViewer = createTreeViewer(parent);
		initDragAndDrop();

		MenuManager manager= new MenuManager(fContextMenuId);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		Control control= fTreeViewer.getControl();
		fMenu= manager.createContextMenu(control);
		control.setMenu(fMenu);
	
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
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
		site.registerContextMenu(fContextMenuId, manager, fTreeViewer);
		site.setSelectionProvider(fTreeViewer);
		
		IActionBars bars= site.getActionBars();		
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY, fTogglePresentation);

		fSelectionSearchGroup = new SelectionSearchGroup(this);
		fOpenViewActionGroup = new OpenViewActionGroup(this);
		// Custom filter group
		fCustomFiltersActionGroup= new CustomFiltersActionGroup("org.eclipse.cdt.ui.COutlinePage", getTreeViewer()); //$NON-NLS-1$

		// Do this before setting input but after the initializations of the fields filtering
		registerActionBars(bars);

		fTreeViewer.setInput(fInput);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, ICHelpContextIds.COUTLINE_VIEW);
	}
	
	public void dispose() {
		if (fTreeViewer != null) {
			fTreeViewer.removeSelectionChangedListener(this);
		}
		
		if (fTogglePresentation != null) {
			fTogglePresentation.setEditor(null);
			fTogglePresentation= null;
		}
		
		if (fMemberFilterActionGroup != null) {
			fMemberFilterActionGroup.dispose();
			fMemberFilterActionGroup= null;
		}
		
		if (fOpenViewActionGroup != null) {
		    fOpenViewActionGroup.dispose();
		    fOpenViewActionGroup= null;
		}
		
		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.dispose();
			fSelectionSearchGroup= null;
		}

		if (fCustomFiltersActionGroup != null) {
			fCustomFiltersActionGroup.dispose();
			fCustomFiltersActionGroup= null;
		}

		if (selectionChangedListeners != null) {
			selectionChangedListeners.clear();
			selectionChangedListeners= null;
		}

		if (fMenu != null && !fMenu.isDisposed()) {
			fMenu.dispose();
			fMenu= null;
		}

		fInput= null;
		
		super.dispose();
	}

	private void registerActionBars(IActionBars actionBars) {
		IToolBarManager toolBarManager= actionBars.getToolBarManager();
		
		LexicalSortingAction action= new LexicalSortingAction(getTreeViewer());
		toolBarManager.add(action);

		fMemberFilterActionGroup= new MemberFilterActionGroup(fTreeViewer, "COutlineViewer"); //$NON-NLS-1$
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
		if (fTreeViewer == null)
			return null;
		return fTreeViewer.getControl();
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionProvider.
	 */
	public ISelection getSelection() {
		if (fTreeViewer == null)
			return StructuredSelection.EMPTY;
		return fTreeViewer.getSelection();
	}

	/**
	 * Returns this page's tree viewer.
	 *
	 * @return this page's tree viewer, or <code>null</code> if 
	 *   <code>createControl</code> has not been called yet
	 */
	protected TreeViewer getTreeViewer() {
		return fTreeViewer;
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
		fTreeViewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * Method declared on ISelectionProvider.
	 */
	public void setSelection(ISelection selection) {
		if (fTreeViewer != null) 
			fTreeViewer.setSelection(selection);
	}

	/**
	 * Set the current input to the content provider.  
	 * @param unit
	 */
	public void setInput(ITranslationUnit unit) {
		fInput = unit;
		if (fTreeViewer != null) {
			fTreeViewer.setInput (fInput);
		}
	}

	private void initDragAndDrop() {
		int ops= DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transfers= new Transfer[] {
			LocalSelectionTransfer.getInstance()
		};
		
		// Drop Adapter
		TransferDropTargetListener[] dropListeners= new TransferDropTargetListener[] {
			new SelectionTransferDropAdapter(fTreeViewer)
		};
		fTreeViewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, new DelegatingDropAdapter(dropListeners));
		
		// Drag Adapter
		TransferDragSourceListener[] dragListeners= new TransferDragSourceListener[] {
			new SelectionTransferDragAdapter(fTreeViewer)
		};
		fTreeViewer.addDragSupport(ops, transfers, new CDTViewerDragAdapter(fTreeViewer, dragListeners));
	}

}
