package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.IDocumentProvider;

import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CFileElementWorkingCopy;
import org.eclipse.cdt.internal.ui.StandardCElementLabelProvider;
import org.eclipse.cdt.internal.ui.CContentProvider;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;

public class CContentOutlinePage extends Page implements IContentOutlinePage, ISelectionChangedListener {
	private CEditor fEditor;
	private CFileElementWorkingCopy fInput;
	private ProblemTreeViewer treeViewer;
	private ListenerList selectionChangedListeners = new ListenerList();
	
	private OpenIncludeAction fOpenIncludeAction;
	private SearchForReferencesAction fSearchForReferencesAction;

	public CContentOutlinePage(CEditor editor) {
		super();
		fEditor= editor;
		fInput= null;
		
		fOpenIncludeAction= new OpenIncludeAction(this);
		fSearchForReferencesAction= new SearchForReferencesAction(this);
	}
	
	public ICElement getRoot() {
		return fInput;
	}
	
	/**
	 * Called by the editor to signal that the content has updated.
	 */
	public void contentUpdated() {
		if (fInput != null) {
			try {
				fInput.update();
			} catch (CoreException e) {
				CPlugin.log(e.getStatus());
				fInput= null;
				return;
			}
				
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
	
	private ISelection updateSelection(ISelection sel) {
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
	private void contextMenuAboutToShow(IMenuManager menu) {		
		if (OpenIncludeAction.canActionBeAdded(getSelection())) {
			menu.add(fOpenIncludeAction);
		}
		if (SearchForReferencesAction.canActionBeAdded(getSelection())) {
			menu.add(fSearchForReferencesAction);
		}
	}
	
	/**
	 * @see ContentOutlinePage#createControl
	 */
	public void createControl(Composite parent) {
		treeViewer = new ProblemTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.addSelectionChangedListener(this);
		
		//treeViewer.setContentProvider(new CModelContentProvider());
		treeViewer.setContentProvider(new CContentProvider(true, true));
		treeViewer.setLabelProvider(new StandardCElementLabelProvider());
		treeViewer.setAutoExpandLevel(treeViewer.ALL_LEVELS);
		treeViewer.addSelectionChangedListener(this);
		
		CPlugin.getDefault().getProblemMarkerManager().addListener(treeViewer);
				
		MenuManager manager= new MenuManager("#PopUp");
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		Control control= treeViewer.getControl();
		Menu menu= manager.createContextMenu(control);
		control.setMenu(menu);
		
		//IFileEditorInput editorInput= (IFileEditorInput)fEditor.getEditorInput();
		IEditorInput editorInput= (IEditorInput)fEditor.getEditorInput();
		IDocumentProvider provider= fEditor.getDocumentProvider();
		try {
			if (editorInput instanceof IFileEditorInput)
				//fInput = ((CPlugin.ElementFactory)plugin.getCCore()).createWorkingCopy((IFileEditorInput)editorInput, provider);
				fInput = new CFileElementWorkingCopy((IFileEditorInput)editorInput, provider);
			else if (editorInput instanceof IStorageEditorInput)
				//fInput = ((CPlugin.ElementFactory)plugin.getCCore()).createWorkingCopy((IStorageEditorInput)editorInput, provider);
				fInput = new CFileElementWorkingCopy((IStorageEditorInput)editorInput, provider);
			else
				throw new CoreException(new Status(IStatus.ERROR, CPlugin.PLUGIN_ID, 0, "no Editor Input", null));

			treeViewer.setInput(fInput);
		} catch (CoreException e) {
			CPlugin.log(e.getStatus());
			fInput= null;
		}
	}
	
	public void dispose() {
		CPlugin.getDefault().getProblemMarkerManager().removeListener(treeViewer);
		super.dispose();
	}

	/**
	 * @see IPage#setActionBars(IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		IToolBarManager toolBarManager= actionBars.getToolBarManager();
		
		LexicalSortingAction action= new LexicalSortingAction(getTreeViewer());
		toolBarManager.add(action);
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
}
