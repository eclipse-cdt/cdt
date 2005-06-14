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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ILineLocatable;
import org.eclipse.cdt.core.search.IMatchLocatable;
import org.eclipse.cdt.core.search.IOffsetLocatable;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.ExternalSearchFile;
import org.eclipse.cdt.internal.ui.search.actions.GroupAction;
import org.eclipse.cdt.internal.ui.search.actions.SortAction;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
	
	public void createControl(Composite parent) {
		super.createControl( parent );
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ICHelpContextIds.C_SEARCH_VIEW);	
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

	protected void showMatch(Match match,  int currentOffset, int currentLength, boolean activateEditor)
			throws PartInitException {
	
		IEditorPart editor= null;
		if (match instanceof CSearchMatch){
			BasicSearchMatch searchMatch = ((CSearchMatch) match).getSearchMatch();
			if (searchMatch.getResource() != null){
				editor = IDE.openEditor(CUIPlugin.getActivePage(), getCanonicalFile((IFile) searchMatch.getResource()), false);
				IMatchLocatable searchLocatable = searchMatch.getLocatable();
				showWithMarker(editor, getCanonicalFile((IFile) searchMatch.getResource()), searchLocatable, currentOffset, currentLength);
			}
			else {
				//Match is outside of the workspace
				try {
					IEditorInput input =EditorUtility.getEditorInput(new ExternalSearchFile(searchMatch.getPath(), searchMatch));
					IWorkbenchPage p= CUIPlugin.getActivePage();
					IEditorPart editorPart= p.openEditor(input, "org.eclipse.cdt.ui.editor.ExternalSearchEditor"); //$NON-NLS-1$
					if (editorPart instanceof ITextEditor) {
						ITextEditor textEditor= (ITextEditor) editorPart;
						IMatchLocatable searchLocatable = searchMatch.getLocatable();
						int startOffset=0;
						int length=0;
						if (searchLocatable instanceof IOffsetLocatable){
						   startOffset = ((IOffsetLocatable)searchLocatable).getNameStartOffset();
						   length = ((IOffsetLocatable)searchLocatable).getNameEndOffset() - startOffset;
						} else if (searchLocatable instanceof ILineLocatable){
							int tempstartLine = ((ILineLocatable)searchLocatable).getStartLine();
							int tempendLine = ((ILineLocatable)searchLocatable).getEndLine();
							
							//Convert the given line number into an offset in order to be able to use
							//the text editor to open 
							IDocument doc =textEditor.getDocumentProvider().getDocument(input);
							if (doc == null)
								return;
							
							try {
//								NOTE: Subtract 1 from the passed in line number because, even though the editor is 1 based, the line
								//resolver doesn't take this into account and is still 0 based
								startOffset = doc.getLineOffset(tempstartLine-1);
								length=doc.getLineLength(tempstartLine-1);
							} catch (BadLocationException e) {}
							
							//See if an end line number has been provided - if so
							//use it to calculate the length of the reveal...
							//Make sure that an end offset exists that is greater than 0
							//and that is greater than the start line number
							if (tempendLine>0 && tempendLine > tempstartLine){
								int endOffset;
								try {
									//See NOTE above
									endOffset = doc.getLineOffset(tempendLine-1);
									length = endOffset - startOffset;
								} catch (BadLocationException e) {}
								
							}
						}
						textEditor.selectAndReveal(startOffset,length);
					}
					
				//TODO: Put in once we have marker support for External Translation Units
				/*	//Get all the CProjects off the model
					ICProject[] cprojects = CoreModel.getDefault().getCModel().getCProjects();
					
					ICProject containingProject=null;
					ICElement celem = null;
					//Find the CProject that the element belongs to
					for (int i=0; i<cprojects.length; i++){
						celem = cprojects[i].findElement(searchMatch.referringElement);
						containingProject=celem.getCProject();
						if (containingProject != null)
							break;
					}
					//Create a translation unit, open in editor
					ITranslationUnit unit = CoreModel.getDefault().createTranslationUnitFrom(containingProject, searchMatch.path);
					IEditorPart editorPart = null;
					if (unit != null) {
						editorPart = EditorUtility.openInEditor(unit);
					}
					//Show with marker
					if (editorPart instanceof ITextEditor) {
						ITextEditor textEditor= (ITextEditor) editorPart;
						showWithMarker(textEditor,(IFile) celem.getUnderlyingResource(),searchMatch.startOffset, searchMatch.endOffset - searchMatch.startOffset);
					}*/
				} catch (CModelException e) {}
				  catch (CoreException e) {}
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
		viewer.setLabelProvider(new CountLabelProvider(this, labelProvider));
		_contentProvider= new LevelTreeContentProvider(viewer, _currentGrouping);
		viewer.setContentProvider(_contentProvider);
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
	 */
	protected void configureTableViewer(TableViewer viewer) {
		viewer.setLabelProvider(new CountLabelProvider(this, new CSearchResultLabelProvider()));
		_contentProvider=new CSearchTableContentProvider(viewer);
		viewer.setContentProvider(_contentProvider);
		setSortOrder(_currentSortOrder);
	}
	
	private void showWithMarker(IEditorPart editor, IFile file,IMatchLocatable searchLocatable, int offset, int length) throws PartInitException {
		try {
			IMarker marker= file.createMarker(NewSearchUI.SEARCH_MARKER);
			HashMap attributes= new HashMap(4);
			if (searchLocatable instanceof IOffsetLocatable){
				attributes.put(IMarker.CHAR_START, new Integer(offset));
				attributes.put(IMarker.CHAR_END, new Integer(offset + length));
			} else if (searchLocatable instanceof ILineLocatable){
			   attributes.put(IMarker.LINE_NUMBER, new Integer(offset));	
			}
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
		
	private IFile getCanonicalFile(IFile originalFile){
		
		if (originalFile == null)
			return null;
		
		File tempFile = originalFile.getRawLocation().toFile();
		String canonicalPath = null;
		try {
			canonicalPath = tempFile.getCanonicalPath();
		} catch (IOException e1) {}
		
		if (canonicalPath != null && (!(originalFile.isLinked()))){
			IPath path = new Path(canonicalPath);
			
			IFile[] matches = CUIPlugin.getWorkspace().getRoot().findFilesForLocation(path);
			if (matches.length > 0)
				originalFile = matches[0];
		}
		return originalFile;
	}
	
}
