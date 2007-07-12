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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchViewPage extends AbstractTextSearchViewPage {

	private IPDOMSearchContentProvider contentProvider;
	
	public PDOMSearchViewPage(int supportedLayouts) {
		super(supportedLayouts);
	}

	public PDOMSearchViewPage() {
		super();
	}

	protected void elementsChanged(Object[] objects) {
		if (contentProvider != null)
			contentProvider.elementsChanged(objects);
	}

	protected void clear() {
		if (contentProvider != null)
			contentProvider.clear();
	}

	protected void configureTreeViewer(TreeViewer viewer) {
		contentProvider = new PDOMSearchTreeContentProvider();
		viewer.setContentProvider((PDOMSearchTreeContentProvider)contentProvider);
		viewer.setLabelProvider(new PDOMSearchTreeLabelProvider(this));
	}

	protected void configureTableViewer(TableViewer viewer) {
		contentProvider = new PDOMSearchListContentProvider();
		viewer.setContentProvider((PDOMSearchListContentProvider)contentProvider);
		viewer.setLabelProvider(new PDOMSearchListLabelProvider(this));
	}

	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
		if (!(match instanceof PDOMSearchMatch))
			return;
		
		try {
			Object element= ((PDOMSearchMatch)match).getElement();
			IIndexFileLocation ifl= ((PDOMSearchElement)element).getLocation();
			IPath path = IndexLocationFactory.getPath(ifl);
			IEditorPart editor = EditorUtility.openInEditor(path, null);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor)editor;
				textEditor.selectAndReveal(currentOffset, currentLength);
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	public StructuredViewer getViewer() {
		return super.getViewer();
	}
}
