/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Sergey Prigogin (Google)
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
import org.eclipse.search.ui.text.MatchFilter;
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
 */
public class CSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {
	private static final String KEY_SHOW_POLYMORPHIC_CALLS = "ShowPolymorphicCalls"; //$NON-NLS-1$
	final static MatchFilter[] ALL_FILTERS = new MatchFilter[] {HidePolymorphicCalls.FILTER};
	final static MatchFilter[] NO_FILTERS = {};

	private CSearchQuery query;
	private boolean indexerBusy;
	
	public CSearchResult(CSearchQuery query) {
		super();
		this.query = query;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	private String getFileName(IEditorPart editor) {
		final IEditorInput input= editor.getEditorInput();
		IPath path= null;
		if (input instanceof FileEditorInput) {
			final FileEditorInput fileInput = (FileEditorInput)input;
			path= fileInput.getFile().getLocation();
		} else if (input instanceof ExternalEditorInput) {
			final ExternalEditorInput extInput = (ExternalEditorInput)input;
			path= extInput.getPath();
		} else if (input instanceof IStorageEditorInput) {
			try {
				final IStorage storage= ((IStorageEditorInput)input).getStorage();
				path= storage.getFullPath();
			} catch (CoreException exc) {
				// ignore
			}
		} else if (input instanceof IPathEditorInput) {
			path= ((IPathEditorInput)input).getPath();
		} else {
			ILocationProvider provider= (ILocationProvider) input.getAdapter(ILocationProvider.class);
			if (provider != null) {
				path= provider.getPath(input);
			}
		}		
		if (path != null)
			return path.toOSString();
		
		return null;
	}
	
	@Override
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		final String fileName= getFileName(editor);
		if (fileName != null && match instanceof CSearchMatch) {
			final IPath filePath= new Path(fileName);
			return filePath.equals(IndexLocationFactory.getAbsolutePath(((CSearchMatch)match).getLocation()));
		}
		return false;
	}
	
	private Match[] computeContainedMatches(AbstractTextSearchResult result, String filename) throws CoreException {
		IPath pfilename= new Path(filename);
		List<Match> list = new ArrayList<Match>(); 
		Object[] elements = result.getElements();
		for (int i = 0; i < elements.length; ++i) {
			if (pfilename.equals(IndexLocationFactory.getAbsolutePath(((CSearchElement)elements[i]).getLocation()))) {
				Match[] matches = result.getMatches(elements[i]);
				for (int j = 0; j < matches.length; ++j) {
					if (matches[j] instanceof CSearchMatch) {
						list.add(matches[j]);
					}
				}
			}
		}
		return list.toArray(new Match[list.size()]);
	}
	
	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		try {
			String filename = getFileName(editor);
			if (filename != null)
				return computeContainedMatches(result, filename);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return new Match[0];
	}

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		try {
			String filename = file.getLocation().toOSString();
			return computeContainedMatches(result, filename);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return new Match[0];
	}

	@Override
	public IFile getFile(Object element) {
		if (element instanceof IIndexName) {
			IIndexName name = (IIndexName)element;
			try {
				IIndexFileLocation location = name.getFile().getLocation();
				if(location.getFullPath()!=null) {
					return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(location.getFullPath()));
				}
			} catch(CoreException ce) { /* fall-through to return null */ }
		} else if (element instanceof CSearchElement) {
			CSearchElement searchElement = (CSearchElement)element;
			IIndexFileLocation location = searchElement.getLocation();
			if(location.getFullPath()!=null) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(location.getFullPath()));
			}
		}
		return null;
	}

	@Override
	public String getLabel() {
		// Report pattern and number of matches
		return query.getResultLabel(getMatchCount());
	}

	@Override
	public String getTooltip() {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public ISearchQuery getQuery() {
		return query;
	}

	/**
	 * Remember whether the indexer was busy when the search was performed.
	 * @param b
	 */
	public void setIndexerBusy(boolean b) {
		this.indexerBusy = b;
	}
	
	/**
	 * Tell if the indexer was busy when search results were gathered.
	 */
	public boolean wasIndexerBusy() {
		return indexerBusy;
	}

	@Override
	public MatchFilter[] getAllMatchFilters() {
		return ALL_FILTERS;
	}

	@Override
	public MatchFilter[] getActiveMatchFilters() {	
		MatchFilter[] result = super.getActiveMatchFilters();
		if (result == null) {
			if (CUIPlugin.getDefault().getDialogSettings().getBoolean(KEY_SHOW_POLYMORPHIC_CALLS)) {
				return ALL_FILTERS;
			}
			return NO_FILTERS;
		}
		return result;
	}

	@Override
	public void setActiveMatchFilters(MatchFilter[] filters) {
		boolean showPoly= false;
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] == HidePolymorphicCalls.FILTER) {
				showPoly= true;
			}
		}
		CUIPlugin.getDefault().getDialogSettings().put(KEY_SHOW_POLYMORPHIC_CALLS, showPoly);
		super.setActiveMatchFilters(filters);
	}
}
