/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.HashMap;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.search.actions.GroupAction;
import org.eclipse.cdt.internal.ui.search.actions.SortAction;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class CSearchResultPage extends AbstractTextSearchViewPage {
	private CSearchContentProvider _contentProvider;
	private int _currentSortOrder;
	private int _currentGrouping;
	
	private SortAction _parentSortAction;
	private SortAction _pathSortAction;
	private SortAction _elementNameSortAction;
	
	private GroupAction _groupFileAction;
	private GroupAction _groupFoldersAction;
	private GroupAction _groupProjectAction;
	private GroupAction _groupClassAction;
	
	private static final String KEY_GROUPING= "org.eclipse.cdt.search.resultpage.grouping"; //$NON-NLS-1$
	
	
	public CSearchResultPage(){
	   _parentSortAction = new SortAction(CSearchMessages.getString("CSearchResultPage.parent_name"),this,CSearchResultLabelProvider.SHOW_CONTAINER_ELEMENT); //$NON-NLS-1$
	   _pathSortAction = new SortAction(CSearchMessages.getString("CSearchResultPage.path_name"),this,CSearchResultLabelProvider.SHOW_PATH); //$NON-NLS-1$
	   _elementNameSortAction = new SortAction(CSearchMessages.getString("CSearchResultPage.element_name"),this, CSearchResultLabelProvider.SHOW_ELEMENT_CONTAINER); //$NON-NLS-1$
	   _currentSortOrder=  CSearchResultLabelProvider.SHOW_ELEMENT_CONTAINER;

		initGroupingActions();
	}
	
	
	/**
	 * 
	 */
	private void initGroupingActions() {
		_groupProjectAction= new GroupAction(CSearchMessages.getString("CSearchResultPage.groupby_project"),CSearchMessages.getString("CSearchResultPage.groupby_project.tooltip"), this, LevelTreeContentProvider.LEVEL_PROJECT); //$NON-NLS-1$ //$NON-NLS-2$
		_groupProjectAction.setImageDescriptor(CPluginImages.DESC_OBJS_SEARCHHIERPROJECT);
		
		_groupFoldersAction= new GroupAction(CSearchMessages.getString("CSearchResultPage.groupby_folder"), CSearchMessages.getString("CSearchResultPage.groupby_folder.tooltip"), this, LevelTreeContentProvider.LEVEL_FOLDER); //$NON-NLS-1$ //$NON-NLS-2$
		_groupFoldersAction.setImageDescriptor(CPluginImages.DESC_OBJS_SEARCHHIERFODLER);
		
		_groupFileAction= new GroupAction(CSearchMessages.getString("CSearchResultPage.groupby_file"), CSearchMessages.getString("CSearchResultPage.groupby_file.tooltip"), this, LevelTreeContentProvider.LEVEL_FILE); //$NON-NLS-1$ //$NON-NLS-2$
		_groupFileAction.setImageDescriptor(CPluginImages.DESC_OBJS_TUNIT);
		
		_groupClassAction = new GroupAction(CSearchMessages.getString("CSearchResultPage.groupby_class"), CSearchMessages.getString("CSearchResultPage.groupby_class.tooltip"),this, LevelTreeContentProvider.LEVEL_CLASS);  //$NON-NLS-1$//$NON-NLS-2$
		_groupClassAction.setImageDescriptor(CPluginImages.DESC_OBJS_CLASS);
		
		try {
			_currentGrouping= getSettings().getInt(KEY_GROUPING);
		} catch (NumberFormatException e) {
			_currentGrouping= LevelTreeContentProvider.LEVEL_PROJECT;
		}
	}

	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activateEditor)
			throws PartInitException {
		// TODO Auto-generated method stub
		IEditorPart editor= null;
		Object element= match.getElement();
		if (element instanceof ICElement) {
			ICElement cElement= (ICElement) element;
			try {
				editor= EditorUtility.openInEditor(cElement, false);
			} catch (PartInitException e1) {
				return;
			} catch (CModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} else if (element instanceof IFile) {
			editor= IDE.openEditor(CUIPlugin.getActivePage(), (IFile) element, false);
		} else if (element instanceof BasicSearchMatch){
			BasicSearchMatch searchMatch = (BasicSearchMatch) element;
			if (searchMatch.resource != null){
				editor = IDE.openEditor(CUIPlugin.getActivePage(), (IFile) searchMatch.resource, false);
				showWithMarker(editor, (IFile) searchMatch.resource, currentOffset, currentLength);
			}}
		if (editor instanceof ITextEditor) {
		ITextEditor textEditor= (ITextEditor) editor;
			textEditor.selectAndReveal(currentOffset, currentLength);
		} else if (editor != null){
			if (element instanceof IFile) {
				IFile file= (IFile) element;
				showWithMarker(editor, file, currentOffset, currentLength);
			}
		} 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#elementsChanged(java.lang.Object[])
	 */
	protected void elementsChanged(Object[] objects) {
		// TODO Auto-generated method stub
		if (_contentProvider != null)
			_contentProvider.elementsChanged(objects);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#clear()
	 */
	protected void clear() {
		if (_contentProvider!=null)
			_contentProvider.clear();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setSorter(new ViewerSorter());
		CSearchResultLabelProvider labelProvider = new CSearchResultLabelProvider();
		labelProvider.setOrder(CSearchResultLabelProvider.SHOW_NAME_ONLY);
		viewer.setLabelProvider(labelProvider);
		_contentProvider= new LevelTreeContentProvider(viewer, _currentGrouping);
		viewer.setContentProvider(_contentProvider);
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
	 */
	protected void configureTableViewer(TableViewer viewer) {
		viewer.setLabelProvider(new CountLabelProvider(this, new CSearchResultLabelProvider(this)));
		_contentProvider=new CSearchTableContentProvider(viewer);
		viewer.setContentProvider(_contentProvider);
		setSortOrder(_currentSortOrder);
	}
	
	private void showWithMarker(IEditorPart editor, IFile file, int offset, int length) throws PartInitException {
		try {
			IMarker marker= file.createMarker(SearchUI.SEARCH_MARKER);
			HashMap attributes= new HashMap(4);
			attributes.put(IMarker.CHAR_START, new Integer(offset));
			attributes.put(IMarker.CHAR_END, new Integer(offset + length));
			marker.setAttributes(attributes);
			IDE.gotoMarker(editor, marker);
			marker.delete();
		} catch (CoreException e) {
			throw new PartInitException("Search Result Error", e); //$NON-NLS-1$
		}
	}
	/**
	 * @param sortOrder
	 */
	public void setSortOrder(int sortOrder) {
		_currentSortOrder= sortOrder;
		StructuredViewer viewer= getViewer();
		CountLabelProvider lpWrapper= (CountLabelProvider) viewer.getLabelProvider();
		((CSearchResultLabelProvider)lpWrapper.getLabelProvider()).setOrder(sortOrder);
		
		if (sortOrder == CSearchResultLabelProvider.SHOW_ELEMENT_CONTAINER) {
			viewer.setSorter(new ElementNameSorter());
		} else if (sortOrder == CSearchResultLabelProvider.SHOW_PATH) {
			viewer.setSorter(new PathNameSorter());
		} else
			viewer.setSorter(new ParentNameSorter());
		
	}
	
	protected void fillContextMenu(IMenuManager mgr) {
		super.fillContextMenu(mgr);
		addSortActions(mgr);
		//fActionGroup.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		//fActionGroup.fillContextMenu(mgr);
	}
	
	private void addSortActions(IMenuManager mgr) {
		if (getLayout() != FLAG_LAYOUT_FLAT)
			return;
		MenuManager sortMenu= new MenuManager(CSearchMessages.getString("CSearchResultPage.sort")); //$NON-NLS-1$
		sortMenu.add(_elementNameSortAction);
		sortMenu.add(_pathSortAction);
		sortMenu.add(_parentSortAction);
		
		_elementNameSortAction.setChecked(_currentSortOrder == _elementNameSortAction.getSortOrder());
		_pathSortAction.setChecked(_currentSortOrder == _pathSortAction.getSortOrder());
		_parentSortAction.setChecked(_currentSortOrder == _parentSortAction.getSortOrder());
		
		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, sortMenu);
	}
	
	private void addGroupActions(IToolBarManager mgr) {
		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, _groupProjectAction);
		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, _groupFoldersAction);
		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, _groupFileAction);
		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, _groupClassAction);
		
		updateGroupingActions();
	}


	/**
	 * @param _grouping
	 */
	public void setGrouping(int groupOrder) {
		_currentGrouping = groupOrder;
		StructuredViewer viewer= getViewer();
		LevelTreeContentProvider cp= (LevelTreeContentProvider) viewer.getContentProvider();
		cp.setLevel(groupOrder);
		updateGroupingActions();
		getSettings().put(KEY_GROUPING, _currentGrouping);
	}
	

	private void updateGroupingActions() {
		_groupProjectAction.setChecked(_currentGrouping == LevelTreeContentProvider.LEVEL_PROJECT);
		_groupFoldersAction.setChecked(_currentGrouping == LevelTreeContentProvider.LEVEL_FOLDER);
		_groupFileAction.setChecked(_currentGrouping == LevelTreeContentProvider.LEVEL_FILE);
		_groupClassAction.setChecked(_currentGrouping == LevelTreeContentProvider.LEVEL_CLASS);
	}
	
	protected void fillToolbar(IToolBarManager tbm) {
		super.fillToolbar(tbm);
		if (getLayout() != FLAG_LAYOUT_FLAT)
			addGroupActions(tbm);
	}
	
	
		
	
}
