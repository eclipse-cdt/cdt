/*******************************************************************************
 * Copyright (c) 2006, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * The content in the tree and list views may be either:
 * <p>
 * IStatus - warnings or errors from the search<br>
 * ICElement - for C/C++ elements, including TUs, folders, projects<br>
 * IPath - directory container, full path<br>
 * 		IIndexFileLocation - for file entries inside IPath directory containers<br>
 * {@link IPDOMSearchContentProvider#URI_CONTAINER} - container for URIs
 * 		URI - from IIndexFileLocations not resolvable to the local filesystem, under URI_CONTAINER<br>
 * @author Doug Schaefer
 * @author Ed Swartz
 *
 */
public interface IPDOMSearchContentProvider {

	/** This node encapsulates results in the search tree for results not resolvable to files. */
	static Object URI_CONTAINER = new Object();

	/** This node appears in the tree when the indexer was running during the search
	 * to warn the user that the results are suspicious.
	 * <p>
	 * TODO: it would be better if IIndexManager told us which projects specifically
	 * were being indexed at the time, so we could annotate per-project whose results are suspicious
	 * (which may be none at all for a given search).
	 * See the handling of {@link CSearchResult#wasIndexerBusy()}.
	 */
	static IStatus INCOMPLETE_RESULTS_NODE = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID,
			CSearchMessages.CSearchMessages_IndexRunningIncompleteWarning);

	public void elementsChanged(Object[] elements);

	public void clear();

}
