/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view.search;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.IRSEViewPart;
import org.eclipse.rse.ui.view.ISystemRemoveElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableTreeViewProvider;
import org.eclipse.rse.ui.view.SystemView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;



/**
 * This class defines the Remote Search view.
 */
public class SystemSearchViewPart extends ViewPart implements ISystemResourceChangeListener, 
                                                              IMenuListener, ISelectionChangedListener, 
                                                              ISystemMessageLine, IRSEViewPart
{



	private PageBook pageBook;
	private StructuredViewer currentViewer;

	private IActionBars actionBars;
	private IMenuManager mMgr;
	private IToolBarManager tbMgr;
	private IStatusLineManager slMgr;
	
	private static final String MENU_HISTORY_GROUP_NAME = "historyGroup";
	private static final String MENU_CLEAR_HISTORY_GROUP_NAME = "clearHistoryGroup";

	private ArrayList viewers = new ArrayList();
	private ArrayList historyActions = new ArrayList();

	private CancelAction cancelAction;
	private SystemSearchClearHistoryAction clearHistoryAction;
	private SystemSearchRemoveSelectedMatchesAction removeSelectedAction;
	private SystemSearchRemoveAllMatchesAction removeAllAction;
	
	private SystemSearchCopyToClipboardAction copyAction;
	private SystemPasteFromClipboardAction pasteAction;

	//  for ISystemMessageLine
	private String _message, _errorMessage;
	private SystemMessage sysErrorMessage;
	private IStatusLineManager _statusLine = null;

	
	/**
	 * Double click listener.
	 */
	public class SystemSearchDoubleClickListener implements IDoubleClickListener {

		/**
		 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
		 */
		public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection selection = (IStructuredSelection) (event.getSelection());

			if (!selection.isEmpty()) {
				Object element = selection.getFirstElement();
				ISystemViewElementAdapter adapter = getAdapter(element);
				adapter.setViewer(currentViewer);
				adapter.handleDoubleClick(element);
			}
		}
	}

	class SelectAllAction extends Action {
		
		public SelectAllAction() {
			super(SystemResources.ACTION_SELECT_ALL_LABEL, null);
		}

		public void run() {
			
			if ((currentViewer != null) && (currentViewer instanceof TableViewer)) {
				TableViewer viewer = (TableViewer) currentViewer;
				viewer.getTable().selectAll();
				// force viewer selection change
				viewer.setSelection(viewer.getSelection());
			}
		}
	}

	public class CancelAction extends Action {
		
		public CancelAction() {
			super(SystemResources.ACTION_CANCEL_SEARCH_LABEL, SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_STOP_ID));
			setToolTipText(SystemResources.ACTION_CANCEL_SEARCH_TOOLTIP);
		}

		public void run() {
			
			if (currentViewer == null) {
				return;
			}
			
			Object input = currentViewer.getInput();
			
			if (input != null) {
				
				if (input instanceof IHostSearchResultSet) {
					IHostSearchResultSet resultSet = (IHostSearchResultSet)input;
					setEnabled(false);
					resultSet.cancel();
				}
			}
		}

		public void updateEnableState(IAdaptable input) {
			
			// no input yet, so disable it
			if (input == null) {
				setEnabled(false);
			}
			
			if (input instanceof IHostSearchResultSet) {
				IHostSearchResultSet set = (IHostSearchResultSet)input;
				
				// running, so enable it
				if (set.isRunning()) {
					setEnabled(true);
				}
				// otherwise, disable
				else {
					setEnabled(false);
				}
			}
			// some other input, disable it
			else {
				setEnabled(false);
			}
		}
	}

	/**
	 * Constructor for SystemSearchViewPart.
	 */
	public SystemSearchViewPart() {
		super();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		
		// create the page book
		pageBook = new PageBook(parent, SWT.NONE);
		
		// pageBook.showPage(createDummyControl());

		// get view site
		IViewSite site = getViewSite();
		
		// set a dummy selection provider
		// getSite().setSelectionProvider(createDummySelectionProvider());

		// get action bars
		actionBars = site.getActionBars();

		// get the menu manager
		mMgr = actionBars.getMenuManager();

		// get the tool bar manager
		tbMgr = actionBars.getToolBarManager();

		_statusLine = actionBars.getStatusLineManager();

		
		// initialize toolbar actions
		initToolBarActions(tbMgr);

		// get the status line manager
		slMgr = actionBars.getStatusLineManager();
		
		// update action bars
		actionBars.updateActionBars();

		// add view as a system listener
		SystemPlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);

		// set help
		SystemWidgetHelpers.setHelp(pageBook, SystemPlugin.HELPPREFIX + "srch0000");
	}

	private void initToolBarActions(IToolBarManager tbMgr) {
		
		// create cancel action
		if (cancelAction == null) {
			cancelAction = new CancelAction();
			
			if (currentViewer == null) {
				cancelAction.setEnabled(false);
			}
			else if (currentViewer.getInput() == null){
				cancelAction.setEnabled(false);
			}
			else {
				cancelAction.setEnabled(true);
			}
		}
		
		// create remove selected matches action
		if (removeSelectedAction == null) {
			removeSelectedAction = new SystemSearchRemoveSelectedMatchesAction(this, getShell());
			
			if (currentViewer == null) {
				removeSelectedAction.setEnabled(false);
			}
			else {
				removeSelectedAction.setEnabled(isRemoveSelectedEnabled());
			}
		}
		
		// create remove all matches action
		if (removeAllAction == null) {
			removeAllAction = new SystemSearchRemoveAllMatchesAction(this, getShell());
			
			if (currentViewer == null) {
				removeAllAction.setEnabled(false);
			}
			else {
				Object input = currentViewer.getInput();
				removeAllAction.setEnabled(isRemoveAllEnabled((IAdaptable)input));
			}
		}
		
		// add cancel action
		tbMgr.add(cancelAction);
		
		// add remove selected action
		tbMgr.add(removeSelectedAction);
		
		// add remove all action
		tbMgr.add(removeAllAction);

		// register global edit actions 		
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		
		// clipboard
		Clipboard clipboard = registry.getSystemClipboard();
		
		Shell shell = registry.getShell();
		
		copyAction = new SystemSearchCopyToClipboardAction(shell, clipboard);
		pasteAction = new SystemPasteFromClipboardAction(shell, clipboard);

		CellEditorActionHandler editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());

		editorActionHandler.setCopyAction(copyAction);
		editorActionHandler.setPasteAction(pasteAction);
		editorActionHandler.setDeleteAction(removeSelectedAction);
		// editorActionHandler.setSelectAllAction(new SelectAllAction());
	}
	
	/**
	 * Updates the remove selected action.
	 * @return <code>true</code> if remove selected action should be enabled, <code>false</code> otherwise.
	 */
	private boolean isRemoveSelectedEnabled() {
		
		ISelection selection = getSelection();
		
		if (selection == null) {
			return false;
		}
		else if (selection.isEmpty()) {
			return false;
		}
		else {
			
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection strSel = (IStructuredSelection)selection;
				
				// note that SystemSearchTableView returns the current input
				// if the actual selection is null
				// so we check for it and return null
				if (strSel.getFirstElement() == currentViewer.getInput()) {
					return false;
				}
				else {
					return true;
				}
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * Updates the remove all matches action.
	 * @param input the input to the current viewer, or <code>null</code> if there is currently no input.
	 * @return <code>true</code> if remove all action should be enabled, <code>false</code> otherwise.
	 */
	private boolean isRemoveAllEnabled(IAdaptable input) {
		
		if (input == null) {
			return false;
		}
		
		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)getAdapter(input);
		
		if (adapter == null) {
			return false;
		}
		else {
			return adapter.hasChildren(input);
		}
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		pageBook.setFocus();
	}

	/**
	 * @see org.eclipse.ui.IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
	}

	/**
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
	}

	/**
	 * Add a search result set.
	 * @param the search result set
	 */
	public void addSearchResult(IAdaptable resultSet) {
		
		// if the correct adapter is not registered, then return
		ISystemViewElementAdapter adapter = getAdapter(resultSet);
		
		if (adapter == null) {
			return;
		}
		
		if (resultSet instanceof IHostSearchResultSet) {
			currentViewer = createSearchResultsTable((IHostSearchResultSet)resultSet, adapter);			
		}
		else {
			currentViewer = createSearchResultsTree(resultSet, adapter);
			
			TreeViewer treeViewer = (TreeViewer)currentViewer;
			MenuManager menuMgr = new MenuManager("#PopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(this);
			Tree tree = (Tree)treeViewer.getControl();
			Menu menu = menuMgr.createContextMenu(tree);
			tree.setMenu(menu);	
		}
		
		// set input
		currentViewer.setInput(resultSet);
		
		// add as selection changed listener to current viewer
		currentViewer.addSelectionChangedListener(this);
		
		// set as selection provider
		getSite().setSelectionProvider(currentViewer);

		// add double click listener
		currentViewer.addDoubleClickListener(new SystemSearchDoubleClickListener());

		// set help for control
		SystemWidgetHelpers.setHelp(currentViewer.getControl(), SystemPlugin.HELPPREFIX + "srch0000");

		// add current viewer to viewer list
		viewers.add(currentViewer);

		// get title to use from adapter
		String title = adapter.getText(resultSet);

		// set the title of the view
		setContentDescription(title);
		
		int num = viewers.size()-1;

		// create history action
		SystemSearchHistoryAction historyAction = new SystemSearchHistoryAction(title, SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SEARCH_RESULT_ID), this, num);
		
		// add to list of history actions
		historyActions.add(historyAction);
		
		// if this is the first result set, add the clear history action
		if (viewers.size() == 1) {
			
			// create a group for history actions
			mMgr.add(new GroupMarker(MENU_HISTORY_GROUP_NAME));
			
			// create a separator with a group for clear history action
			mMgr.add(new Separator(MENU_CLEAR_HISTORY_GROUP_NAME));
			
			// add the clear history action to the group
			clearHistoryAction = new SystemSearchClearHistoryAction(this, getShell());
			mMgr.appendToGroup(MENU_CLEAR_HISTORY_GROUP_NAME, clearHistoryAction);
		}
		
		// add history action to the menu manager
		mMgr.appendToGroup(MENU_HISTORY_GROUP_NAME, historyAction);

		// add global actions
		// actionBars.setGlobalActionHandler(ActionFactory.DELETE, new SystemSearchDeleteAction(this));

		// update action bars
		actionBars.updateActionBars();
		
		// show the control
		pageBook.showPage(currentViewer.getControl());

		// enable/disable state for this input
		if (cancelAction != null) {
			cancelAction.updateEnableState(resultSet);
		}
		
		// enable/disable state
		if (removeSelectedAction != null) {
			removeSelectedAction.setEnabled(isRemoveSelectedEnabled());
		}
		
		// enable/disable state for this input
		if (removeAllAction != null) {
			removeAllAction.setEnabled(isRemoveAllEnabled(resultSet));
		}
	}

	private StructuredViewer createSearchResultsTree(IAdaptable resultSet, ISystemViewElementAdapter adapter)
	{

		// create the current tree
		Tree currentControl = new Tree(pageBook, SWT.MULTI);

		// create the current viewer
		TreeViewer currentViewer = new TreeViewer(currentControl);
		currentViewer.setUseHashlookup(true);
		currentViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

		// create a new content provider
		SystemSearchViewContentProvider contentProvider = new SystemSearchViewContentProvider();
		// save the viewpart to the provider
		contentProvider.setViewPart(this);
		// add the content provider to the viewer
		currentViewer.setContentProvider(contentProvider);

		// create a new label provider
		SystemSearchViewLabelProvider labelProvider = new SystemSearchViewLabelProvider();

		// add the label provider to the viewer
		currentViewer.setLabelProvider(labelProvider);

		return currentViewer;
	}

	private StructuredViewer createSearchResultsTable(IHostSearchResultSet resultSet, ISystemViewElementAdapter adapter) {
		
		// create table portion
		// TODO change to tabletree when eclipse fixes the swt widget
		//TableTree table = new TableTree(pageBook, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		Tree tabletree = new Tree(pageBook, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		SystemSearchTableView viewer = new SystemSearchTableView(tabletree, (IHostSearchResultSet)resultSet, this);
		viewer.setWorkbenchPart(this);
		
		getSite().registerContextMenu(viewer.getContextMenuManager(), viewer);
		return viewer;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		
		// remove as resource change listener
		SystemPlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
		
		// clear viewers
		clearViewers();
		
		// clear arrays
		viewers.clear();
		historyActions.clear();

		// call super as required
		super.dispose();
	}
	
	/**
	 * Remove current viewer as selection provider, removes children of all the inputs and disposes
	 * the controls if they haven't already been disposed.
	 */
	private void clearViewers() {
		
		// remove current viewer as selection provider if it exists
		if (currentViewer != null) {
			
			// remove as selection changed listener to current viewer
			currentViewer.removeSelectionChangedListener(this);
			
			if (getSite().getSelectionProvider() == currentViewer) {
				getSite().setSelectionProvider(null);
			}
		}

		for (int i = 0; i < viewers.size(); i++) {
			
			Object viewer = viewers.get(i);
			
			// if we're dealing with universal search
			if (viewer instanceof SystemSearchTableView) {
				
				SystemSearchTableView tableView = (SystemSearchTableView)viewer;
				
				Object input = tableView.getInput();
				
				// dispose the remote search result set
				// which cancels the search and removes contents from the input
				// (i.e. removes from model)
				if (input instanceof IHostSearchResultSet) {
					IHostSearchResultSet set = (IHostSearchResultSet)input;
					set.dispose();
				}
				
				// dispose viewer
				tableView.dispose();
			}
			// other search
			else if (viewer instanceof TreeViewer){
				
				TreeViewer treeView = (TreeViewer)viewer;
				
				Object input = treeView.getInput();
				
				ISystemViewElementAdapter adapter = getAdapter(input);
				
				if (adapter != null && adapter instanceof ISystemRemoveElementAdapter) {
					ISystemRemoveElementAdapter rmAdapter = (ISystemRemoveElementAdapter)adapter;
					rmAdapter.removeAllChildren(input);
					
					Control control = treeView.getControl();
					
					if (!control.isDisposed()) {
						control.dispose();
					} 
				}
			}
		}
	}

	/**
	 * Show search result with the given index.
	 * @param the index in the result history list
	 */
	public void showSearchResult(int index) {
		
		// remove as selection listener from current viewer
		if (currentViewer != null) {
			currentViewer.removeSelectionChangedListener(this);
		}

		// get viewer with this index and make it current
		currentViewer = (StructuredViewer)(viewers.get(index));
		
		// set as selection provider
		getSite().setSelectionProvider(currentViewer);
		
		// add as selection changed listener to current viewer
		currentViewer.addSelectionChangedListener(this);

		// get the input
		IAdaptable resultSet = (IAdaptable)(currentViewer.getInput());
		
		if (resultSet == null) {
			return;
		}
		
		ISystemViewElementAdapter adapter = getAdapter(resultSet);

		// if the correct adapter is not registered, then return
		if (adapter == null) {
			return;
		}

		// get title to use from adapter
		String title = adapter.getText(resultSet);

		// set the title of the view
		setContentDescription(title);

		// get the associated control
		Control currentControl = currentViewer.getControl();

		// show the control
		pageBook.showPage(currentControl);

		// enable/disable state for this input
		if (cancelAction != null) {
			cancelAction.updateEnableState(resultSet);
		}
		
		// enable/disable state
		if (removeSelectedAction != null) {
			removeSelectedAction.setEnabled(isRemoveSelectedEnabled());
		}
		
		// enable/disable state for this input
		if (removeAllAction != null) {
			removeAllAction.setEnabled(isRemoveAllEnabled(resultSet));
		}
	}

	/**
	 * Delete the selected object in the view.
	 * @return <code>true</code> if the selection has been deleted, <code>false</code> otherwise.
	 */
	public boolean deleteSelected() {
		
		if (currentViewer == null) {
			return false;
		}
		
		IStructuredSelection selection = (IStructuredSelection)(currentViewer.getSelection());
		
		if (selection == null || selection.isEmpty()) {
			return false;
		}
		
		Object input = currentViewer.getInput();
				
		ISystemViewElementAdapter adapter = getAdapter(input);
		
		// adapter should be an instance of ISystemRemoveElementAdapter
		if (adapter == null || !(adapter instanceof ISystemRemoveElementAdapter)) {
			return false;
		}
		
		Iterator elements = selection.iterator();
		
		ArrayList removeElements = new ArrayList();

		while (elements.hasNext()) {
			Object element = elements.next();
			((ISystemRemoveElementAdapter)adapter).remove(input, element);
			removeElements.add(element);
		}
		
		// current viewer should be an instance of tree viewer
		// remove the elements from it to update the view
		if (currentViewer instanceof TreeViewer) {
			((TreeViewer)currentViewer).remove(removeElements.toArray());
		}
		
		// get title to use from adapter
		String title = adapter.getText(input);

		// set the title of the view
		setContentDescription(title);

		// enable/disable state for this input
		if (cancelAction != null) {
			cancelAction.updateEnableState((IAdaptable)input);
		}
					
		// enable/disable state for this input
		if (removeSelectedAction != null) {
			removeSelectedAction.setEnabled(isRemoveSelectedEnabled());
		}
					
		// enable/disable state for this input
		if (removeAllAction != null) {
			removeAllAction.setEnabled(isRemoveAllEnabled((IAdaptable)input));
		}
		
		return true;
	}
	
	/**
	 * Deletes all the pages in the view.
	 * @return <code>true</code> if all pages have been deleted, <code>false</code> otherwise.
	 */
	public boolean deleteAllPages() {
		
		// first show a dummy control in page book
		// this must be done before viewers are cleared
		// reason is that current showing control in page book can not be disposed
		// SWT doesn't seem to like it
		// This is fixed in 3.0
		pageBook.showPage(createDummyControl());
		
		// clear viewers
		clearViewers();
		
		// current viewer is null again
		currentViewer = null;
		
		// clear the viewer list
		viewers.clear();
		
		// disable cancel action
		cancelAction.setEnabled(false);
		
		// disable remove all action
		removeSelectedAction.setEnabled(false);
		
		// disable remove all action
		removeAllAction.setEnabled(false);
		
		// clear the history action list
		historyActions.clear();
		
		// get rid of all menu manager actions
		mMgr.removeAll();
		
		// update action bars
		actionBars.updateActionBars();
		
		// clear the content description
		setContentDescription("");
				
		return true;
	}
	
	/**
	 * Creates a dummy control to show in the page book.
	 * @return a dummy control.
	 */
	private Control createDummyControl() {
		Control control = new Composite(pageBook, SWT.NONE);
		return control;
	}
	
	/**
	 * Deletes the current page.
	 * @return <code>true</code> if the current page has been deleted, <code>false</code> otherwise.
	 */
	public boolean deleteCurrentPage() {

		// remove current viewer as selection provider if it exists
		if (currentViewer != null) {
			
			// remove as selection changed listener to current viewer
			currentViewer.removeSelectionChangedListener(this);
			
			if (getSite().getSelectionProvider() == currentViewer) {
				getSite().setSelectionProvider(null);
			}
		}
		else {
			return false;
		}
		
		Object input = currentViewer.getInput();
		
		ISystemViewElementAdapter adapter = getAdapter(input);
		
		// universal search
		if (currentViewer instanceof SystemSearchTableView) 
		{
			
			SystemSearchTableView tableView = (SystemSearchTableView)currentViewer;
			
			// remove viewer as listener
			tableView.removeAsListener();
			
			// clear model
			if (input instanceof IHostSearchResultSet) {
				IHostSearchResultSet set = (IHostSearchResultSet)input;
				set.dispose();
			}
			
			// now refresh viewer
			// but flush cache of the provider first for an accurate refresh
			SystemTableTreeViewProvider provider = (SystemTableTreeViewProvider)(tableView.getContentProvider());
			provider.flushCache();
			tableView.refresh();
		}
		// other search
		else if (currentViewer instanceof TreeViewer){
				
			TreeViewer treeView = (TreeViewer)currentViewer;
				
			if (adapter != null && adapter instanceof ISystemRemoveElementAdapter) {
				ISystemRemoveElementAdapter rmAdapter = (ISystemRemoveElementAdapter)adapter;
				rmAdapter.removeAllChildren(input);
				treeView.refresh();
			}
		}
		
		// get title to use from adapter
		String title = adapter.getText(input);

		// set the title of the view
		setContentDescription(title);
		
		// disable cancel action
		cancelAction.setEnabled(false);
		
		// disable remove selected action
		removeSelectedAction.setEnabled(false);
		
		// disable remove all action
		removeAllAction.setEnabled(false);		
		
		return true;
	}

	/**
	 * Get the adapter for the given object.
	 * @param the object the object for which I want the adapter.
	 * @return the adapter for the object.
	 */
	public ISystemViewElementAdapter getAdapter(Object element) {
		return SystemAdapterHelpers.getAdapter(element);
	}

	/**
	 * Get the shell.
	 * @return the shell
	 */
	public Shell getShell() {
		return getSite().getShell();
	}

	public void systemResourceChanged(ISystemResourceChangeEvent event) {

		// need to introduce another event type for this....
		if (event.getType() == ISystemResourceChangeEvents.EVENT_SEARCH_FINISHED) {
			
			// the view is added as a system listener when the part is created
			// so the current viewer may not exist if the search results has not been added yet
			if (currentViewer == null) {
				return;
			}
			
			Object actualSource = event.getSource();

			if (actualSource instanceof IHostSearchResultConfiguration) {
				
				IHostSearchResultSet source = ((IHostSearchResultConfiguration)actualSource).getParentResultSet();
				
				// get title to use from adapter
				ISystemViewElementAdapter adapter = getAdapter(source);
				
				if (adapter == null) {
					return;
				}
				
				int index = -1;
				
				// if the source is the input to the current viewer
				// update view title and cancel action
				// also update the history action corresponding to the current view
				if (currentViewer.getInput() == source) {
					
					// end of a search
					String title = adapter.getText(source);

					// set the title of the view
					setContentDescription(title);

					// enable/disable state for this input
					if (cancelAction != null) {
						cancelAction.updateEnableState((IAdaptable)source);
					}
					
					// enable/disable state for this input
					if (removeSelectedAction != null) {
						removeSelectedAction.setEnabled(isRemoveSelectedEnabled());
					}
					
					// enable/disable state for this input
					if (removeAllAction != null) {
						removeAllAction.setEnabled(isRemoveAllEnabled((IAdaptable)source));
					}
						
					// find out where the current viewer is in the viewer list
					index = viewers.indexOf(currentViewer);
				}
				// if the source is not the input to the current view
				// we simply update the history action
				else {
					
					for (int i = 0; i < viewers.size(); i++) {
						
						SystemSearchTableView view = (SystemSearchTableView)viewers.get(i);
				
						if (view.getInput() == source) {
							index = i;
							break;
						} 
					}
				}
				
				// since the history actions list paralles the viewer list, use the index to
				// get the history action
				if (index >= 0) {
					SystemSearchHistoryAction historyAction = (SystemSearchHistoryAction)historyActions.get(index);
					historyAction.setText(adapter.getText(source));
				}
			}
		}
	}
	
	//------------------------------------------------------
	// Methods used by the tree view pop-up menu
	//------------------------------------------------------
	/**
	 * Fill context for the tree view pop-up menu.
	 * @param menu the menu manager.
	 */
	public void fillContextMenu(IMenuManager menu)
	{
		IStructuredSelection selection = (IStructuredSelection)currentViewer.getSelection();
		
		if (selection == null) {
			return;
		}
		
		int selectionCount = selection.size();
		
		if (selectionCount == 0) { // nothing selected
			return;
		}
		else {
			
			// if only one selection, check if selection is the input
			// if so add no actions
			if (selectionCount == 1) {
				
				if (selection.getFirstElement() == currentViewer.getInput()) {
					return;
				}
			}
			
			// if selection count is more than 1
			// check if all have same parent
			// if not, check if they have ancestor relationship
			// if so, add no actions
//			if (selectionCount > 1) {
//				boolean allSelectionsFromSameParent = sameParent();
//				
//				// if all selections do not have the same parent, do not show anything in the menu
//				if (!allSelectionsFromSameParent) {
//					
//					if (selectionHasAncestryRelationship()) {
//						// don't show the menu because actions with
//						//  multiple select on objects that are ancestors 
//						//  of each other is problematic
//						// still create the standard groups
//						SystemView.createStandardGroups(menu);
//						return;
//					}
//				}
//			}
			
			// partition into groups...
			SystemView.createStandardGroups(menu);
				
			// adapter actions
			SystemMenuManager ourMenu = new SystemMenuManager(menu);
			Object element = selection.getFirstElement();
			ISystemViewElementAdapter adapter = getAdapter(element);
			adapter.setViewer(currentViewer);	
			adapter.addActions(ourMenu, selection,
							   getShell(),
							   ISystemContextMenuConstants.GROUP_ADAPTERS);
		}
	}
	
	/**
	 * This is called to ensure all elements in a multiple-selection have the same parent in the
	 *  tree viewer. If they don't we automatically disable all actions. 
	 * <p>
	 * Designed to be as fast as possible by going directly to the SWT widgets
	 */
	public boolean sameParent()
	{
		boolean same = true;
		
		Tree tree = null;
		
		if (currentViewer instanceof AbstractTreeViewer) {
			tree = (Tree)(currentViewer.getControl());
		}
		else {
			return false;
		}
		
		TreeItem[] items = tree.getSelection();
		
		if ((items == null) || (items.length == 0))
			return true;
		
		TreeItem prevParent = null;
		TreeItem currParent = null;
		
		for (int idx=0; same && (idx<items.length); idx++)
		{
		   currParent = items[idx].getParentItem();           
		 
		   if ((idx>0) && (currParent != prevParent))
		   {
			 same = false;
		   }
		   else
		   {
			 prevParent = currParent;  
		   }
		}
		
		return same;
	}
	
	/**
	 * Called when the context menu is about to open.
	 * Calls {@link #fillContextMenu(IMenuManager)}
	 */
	public void menuAboutToShow(IMenuManager menu)
	{
	  fillContextMenu(menu);		
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		currentViewer.addSelectionChangedListener(listener);
	}
	
	/**
	* Returns the current selection for this provider.
	*
	* @return the current selection
	*/
	public ISelection getSelection()
	{
		if (currentViewer == null) {
			return null;
		}
		else {
			return currentViewer.getSelection();
		}
	}
	/**
	* Removes the given selection change listener from this selection provider.
	* Has no affect if an identical listener is not registered.
	*
	* @param listener a selection changed listener
	*/
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		currentViewer.removeSelectionChangedListener(listener);
	}
	/**
	* Sets the current selection for this selection provider.
	*
	* @param selection the new selection
	*/
	public void setSelection(ISelection selection)
	{
		currentViewer.setSelection(selection);
	}
	/**
	 * Set the title of this view part
	 * @param title
	 */
	public void setViewPartTitle(String title)
	{
	   setContentDescription(title);
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		
		if (event.getSelectionProvider() == currentViewer) {
			removeSelectedAction.setEnabled(isRemoveSelectedEnabled());
		}
		else {
			removeSelectedAction.setEnabled(false);
		}
		
		IStructuredSelection sel = (IStructuredSelection)(event.getSelection());
		
		Iterator iter = sel.iterator();
		
		// set viewer for adapters of the selected elements
		while (iter.hasNext()) {
		    ISystemViewElementAdapter adapter = getAdapter(iter.next());
		    adapter.setViewer(currentViewer);
		}
		
		copyAction.setEnabled(copyAction.updateSelection(sel));
		pasteAction.setEnabled(pasteAction.updateSelection(sel));
	}
	
	/**
	 * Gets the current viewer, i.e. the viewer whose control is being currently displayed in the view.
	 * @return the current viewer, or <code>null</code> if there is no current viewer.
	 */
	public StructuredViewer getCurrentViewer() {
		return currentViewer;
	}
	
	public Viewer getRSEViewer()
	{
		return currentViewer;
	}
	
//	 -------------------------------
	// ISystemMessageLine interface...
	// -------------------------------
	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage()
	{
		_errorMessage = null;
		sysErrorMessage = null;
		if (_statusLine != null)
			_statusLine.setErrorMessage(_errorMessage);
	}
	/**
	 * Clears the currently displayed message.
	 */
	public void clearMessage()
	{
		_message = null;
		if (_statusLine != null)
			_statusLine.setMessage(_message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage()
	{
		return _errorMessage;
	}
	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage()
	{
		return _message;
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message)
	{
		this._errorMessage = message;
		if (_statusLine != null)
			_statusLine.setErrorMessage(message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage()
	{
		return sysErrorMessage;
	}

	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage message)
	{
		sysErrorMessage = message;
		setErrorMessage(message.getLevelOneText());
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(Throwable exc)
	{
		setErrorMessage(exc.getMessage());
	}

	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message)
	{
		this._message = message;
		if (_statusLine != null)
			_statusLine.setMessage(message);
	}
	/** 
	 *If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message)
	{
		setMessage(message.getLevelOneText());
	}

}