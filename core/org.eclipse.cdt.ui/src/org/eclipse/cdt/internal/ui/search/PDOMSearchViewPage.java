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

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.editor.ExternalSearchEditor;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchViewPage extends AbstractTextSearchViewPage {

	private PDOMSearchTreeContentProvider contentProvider;
	
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
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new PDOMSearchLabelProvider());
	}

	protected void configureTableViewer(TableViewer viewer) {
	}

	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
		if (!(match instanceof PDOMSearchMatch))
			return;
		
		IPath path = new Path(((PDOMSearchMatch)match).getFileName());
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
		if (files.length > 0) {
			IEditorPart editor = IDE.openEditor(CUIPlugin.getActivePage(), files[0]);
			try {
				IMarker marker = files[0].createMarker(NewSearchUI.SEARCH_MARKER);
				marker.setAttribute(IMarker.CHAR_START, currentOffset);
				marker.setAttribute(IMarker.CHAR_END, currentOffset + currentLength);
				IDE.gotoMarker(editor, marker);
				marker.delete();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		} else {
			// external file
			IEditorInput input = new ExternalEditorInput(new FileStorage(path));
			IEditorPart editor = CUIPlugin.getActivePage().openEditor(input, ExternalSearchEditor.EDITOR_ID);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor)editor;
				textEditor.selectAndReveal(currentOffset, currentLength);
			}
		}
	}
	
}
