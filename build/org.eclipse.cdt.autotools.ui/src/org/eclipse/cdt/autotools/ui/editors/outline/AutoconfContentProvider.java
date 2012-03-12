/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.outline;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class AutoconfContentProvider implements ITreeContentProvider {

	private IDocumentProvider documentProvider;
	private AutoconfEditor editor;
	protected final static String SECTION_POSITIONS = "section_positions";
	protected IPositionUpdater positionUpdater = new DefaultPositionUpdater(SECTION_POSITIONS);
	
	public AutoconfContentProvider(ITextEditor editor) {
		if (editor instanceof AutoconfEditor) {
			this.editor = (AutoconfEditor) editor;
		}
		this.documentProvider = editor.getDocumentProvider();
	}
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != null)
		{
			IDocument document = documentProvider.getDocument(oldInput);
			if (document != null)
			{
				try
				{
					document.removePositionCategory(SECTION_POSITIONS);
				}
				catch (BadPositionCategoryException x)
				{
				}
				document.removePositionUpdater(positionUpdater);
			}
		}

		if (newInput != null)
		{
			IDocument document = documentProvider.getDocument(newInput);
			if (document != null)
			{
				document.addPositionCategory(SECTION_POSITIONS);
				document.addPositionUpdater(positionUpdater);
			}
		}
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof AutoconfElement) {
			AutoconfElement element = (AutoconfElement)parentElement;
			return element.getChildren();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof AutoconfElement) {
			return ((AutoconfElement)element).hasChildren();
		} 
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return this.getChildren(editor.getRootElement());
	}
}
