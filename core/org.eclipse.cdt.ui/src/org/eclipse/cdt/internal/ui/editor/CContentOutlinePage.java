package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.StandardCElementLabelProvider;
import org.eclipse.cdt.internal.ui.cview.CViewMessages;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.MemberFilterActionGroup;
import org.eclipse.cdt.ui.actions.RefactoringActionGroup;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class CContentOutlinePage extends Page implements IContentOutlinePage, ISelectionChangedListener {
	private CEditor fEditor;
	private ITranslationUnit fInput;
	private ProblemTreeViewer treeViewer;
	private ListenerList selectionChangedListeners = new ListenerList();
	private TogglePresentationAction fTogglePresentation;
	private String fContextMenuId;
	
	private OpenIncludeAction fOpenIncludeAction;
	
	private MemberFilterActionGroup fMemberFilterActionGroup;

	private ActionGroup fSelectionSearchGroup;
	private ActionGroup fRefactoringActionGroup;
	
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
							treeViewer.getControl().setRedraw(false);
							treeViewer.refresh();
							treeViewer.setSelection(updateSelection(sel));		
							treeViewer.getControl().setRedraw(true);
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
				ICElement elem = (ICElement)iter.next();
				if (elem != null) {
					newSelection.add(elem);
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
		
		if (OpenIncludeAction.canActionBeAdded(getSelection())) {
			menu.add(fOpenIncludeAction);
		}
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
		
		if (SelectionSearchGroup.canActionBeAdded(getSelection())){
			MenuManager search = new MenuManager(CViewMessages.getString("SearchAction.label"), IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$	
			fSelectionSearchGroup.fillContextMenu(search);
			menu.add(search);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
		
		fRefactoringActionGroup.fillContextMenu(menu);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		treeViewer = new ProblemTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		treeViewer.setContentProvider(new CElementContentProvider(true, true));
		treeViewer.setLabelProvider(new StandardCElementLabelProvider());
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.addSelectionChangedListener(this);
		
		CUIPlugin.getDefault().getProblemMarkerManager().addListener(treeViewer);
				
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
		bars.setGlobalActionHandler(ICEditorActionDefinitionIds.TOGGLE_PRESENTATION, fTogglePresentation);

		fSelectionSearchGroup = new SelectionSearchGroup(this);
		fRefactoringActionGroup = new RefactoringActionGroup(this, null);
		
		treeViewer.setInput(fInput);
		WorkbenchHelp.setHelp(control, ICHelpContextIds.COUTLINE_VIEW);	
	}
	
	public void dispose() {
		CUIPlugin.getDefault().getProblemMarkerManager().removeListener(treeViewer);
		
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
		contentUpdated();		
	}


}
