/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 26, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.ui.search;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
/**
 * @author bog
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CSearchResult extends AbstractTextSearchResult {
	CSearchQuery cQuery;
	
	public CSearchResult(CSearchQuery query){
		cQuery = query;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#findContainedMatches(org.eclipse.core.resources.IFile)
	 */
	public Match[] findContainedMatches(IFile file) {
		ICElement cElement= create(file);
		Set matches= new HashSet();
		collectMatches(matches, cElement);
		return (Match[]) matches.toArray(new Match[matches.size()]);
	}
	
	private ICElement create(IFile file){
		IProject project = file.getProject();
		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
		return cProject;
	}
	
	private void collectMatches(Set matches, ICElement element) {
		Match[] m= getMatches(element);
		if (m.length != 0) {
			for (int i= 0; i < m.length; i++) {
				matches.add(m[i]);
			}
		}
		if (element instanceof IParent) {
			IParent parent= (IParent) element;
			try {
				ICElement[] children= parent.getChildren();
				for (int i= 0; i < children.length; i++) {
					collectMatches(matches, children[i]);
				}
			} catch (CModelException e) {
				// we will not be tracking these results
			}
			
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getFile(java.lang.Object)
	 */
	public IFile getFile(Object element) {
		if (element instanceof ICElement) {
			ICElement cElement= (ICElement) element;
			element= cElement.getUnderlyingResource();
		}
		if (element instanceof IFile)
			return (IFile)element;
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#isShownInEditor(org.eclipse.search.ui.text.Match, org.eclipse.ui.IEditorPart)
	 */
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#findContainedMatches(org.eclipse.ui.IEditorPart)
	 */
	public Match[] findContainedMatches(IEditorPart editor) {
		IEditorInput editorInput= editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput)  {
			IFileEditorInput fileEditorInput= (IFileEditorInput) editorInput;
			return findContainedMatches(fileEditorInput.getFile());
		} /*else if (editorInput instanceof IClassFileEditorInput) {
			IClassFileEditorInput classFileEditorInput= (IClassFileEditorInput) editorInput;
			Set matches= new HashSet();
			collectMatches(matches, classFileEditorInput.getClassFile());
			return (Match[]) matches.toArray(new Match[matches.size()]);
		}*/
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getText()
	 */
	public String getText() {
		int matchCount= getMatchCount();
		String format= null;
		if (matchCount == 1)
			format= cQuery.getSingularLabel();
		else 
			format= cQuery.getPluralLabelPattern();
		return MessageFormat.format(format, new Object[] { new Integer(matchCount) });
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getTooltip()
	 */
	public String getTooltip() {
		// TODO Auto-generated method stub
		return getText();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return cQuery.getImageDescriptor();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getQuery()
	 */
	public ISearchQuery getQuery() {
		return cQuery;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResult#getLabel()
	 */
	public String getLabel() {
		int matches = getMatchCount();
		String label = null;
		if (matches == 1)
			return cQuery.getSingularLabel();
		else
			label = cQuery.getPluralLabelPattern();
		
		return MessageFormat.format(label, new Object[]{new Integer(matches)});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getEditorMatchAdapter()
	 */
	public IEditorMatchAdapter getEditorMatchAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getFileMatchAdapter()
	 */
	public IFileMatchAdapter getFileMatchAdapter() {
		// TODO Auto-generated method stub
		return null;
	}
}
