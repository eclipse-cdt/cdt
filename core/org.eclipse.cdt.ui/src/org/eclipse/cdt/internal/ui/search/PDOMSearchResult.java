/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
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
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;

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
		} else if (input instanceof IStorageEditorInput) {
				try {
					IStorage storage = ((IStorageEditorInput)input).getStorage();
					if (storage.getFullPath() != null) {
						return storage.getFullPath().toOSString();
					}
				} catch (CoreException exc) {
					// ignore
				}
		} else if (input instanceof IPathEditorInput) {
			IPath path= ((IPathEditorInput)input).getPath();
			return path.toOSString();
		}
		ILocationProvider provider= (ILocationProvider) input.getAdapter(ILocationProvider.class);
		if (provider != null) {
			IPath path= provider.getPath(input);
			return path.toOSString();
		}
		return null;
	}
	
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		final String fileName= getFileName(editor);
		if (fileName != null && match instanceof PDOMSearchMatch) {
			final IPath filePath= new Path(fileName);
			return filePath.equals(IndexLocationFactory.getAbsolutePath(((PDOMSearchMatch)match).getLocation()));
		}
		return false;
	}
	
	private Match[] computeContainedMatches(AbstractTextSearchResult result, String filename) throws CoreException {
		IPath pfilename= new Path(filename);
		List list = new ArrayList(); 
		Object[] elements = result.getElements();
		for (int i = 0; i < elements.length; ++i) {
			if (pfilename.equals(IndexLocationFactory.getAbsolutePath(((PDOMSearchElement)elements[i]).getLocation()))) {
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
		if (element instanceof IIndexName) {
			IIndexName name = (IIndexName)element;
			try {
				IIndexFileLocation location = name.getFile().getLocation();
				if(location.getFullPath()!=null) {
					return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(location.getFullPath()));
				}
			} catch(CoreException ce) { /* fall-through to return null */ }
		}
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
