/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {

	private PDOMSearchQuery query;
	
	public PDOMSearchResult(PDOMSearchQuery query) {
		super();
		this.query = query;
	}

	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	private String getFileName(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (input instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput)input;
			return fileInput.getFile().getLocation().toOSString();
		} else if (input instanceof ExternalEditorInput) {
			ExternalEditorInput extInput = (ExternalEditorInput)input;
			return extInput.getStorage().getFullPath().toOSString();
		} else {
			return null;
		}
	}
	
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		try {
			String filename = getFileName(editor);
			if (filename != null && match instanceof PDOMSearchMatch)
				return filename.equals(((PDOMSearchMatch)match).getFileName());
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return false;
	}
	
	private Match[] computeContainedMatches(AbstractTextSearchResult result, String filename) throws CoreException {
		List list = new ArrayList(); 
		Object[] elements = result.getElements();
		for (int i = 0; i < elements.length; ++i) {
			if (((PDOMSearchElement) elements[i]).getFileName()
					.equals(filename)) {
				Match[] matches = result.getMatches(elements[i]);
				for (int j = 0; j < matches.length; ++j) {
					if (matches[j] instanceof PDOMSearchMatch) {
						list.add(matches[j]);
					}
				}
			}
		}
		return (Match[])list.toArray(new Match[list.size()]);
	}
	
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		try {
			String filename = getFileName(editor);
			if (filename != null)
				return computeContainedMatches(result, filename);
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return new Match[0];
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		try {
			String filename = file.getLocation().toOSString();
			return computeContainedMatches(result, filename);
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return new Match[0];
	}

	public IFile getFile(Object element) {
		if (element instanceof PDOMName) {
			PDOMName name = (PDOMName)element;
			IASTFileLocation loc = name.getFileLocation();
			IPath path = new Path(loc.getFileName());
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
			if (files.length > 0)
				return files[0];
			else
				return null;
		} else
			return null;
	}

	public String getLabel() {
		return query.getLabel();
	}

	public String getTooltip() {
		return null;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public ISearchQuery getQuery() {
		return query;
	}

}
