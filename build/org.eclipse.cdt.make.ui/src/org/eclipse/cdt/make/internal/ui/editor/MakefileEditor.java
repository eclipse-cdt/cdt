/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class MakefileEditor extends TextEditor {
	public final static String MAKE_COMMENT = "make_comment"; //$NON-NLS-1$
	public final static String MAKE_KEYWORD = "make_keyword"; //$NON-NLS-1$
	public final static String MAKE_MACRO_VAR = "macro_var"; //$NON-NLS-1$
	public final static String MAKE_META_DATA = "meta_data"; //$NON-NLS-1$

	/**
	 * The page that shows the outline.
	 */
	protected MakefileContentOutlinePage page;



	public MakefileEditor() {
		super();
		initializeEditor();
	}

	/**
	 * @see AbstractTextEditor#init(IEditorSite, IEditorInput)
	 */
	protected void initializeEditor() {

		setSourceViewerConfiguration(new MakefileEditorConfiguration(new MakefileColorManager()));
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#MakefileEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#MakefileRulerContext"); //$NON-NLS-1$
		setDocumentProvider(new MakefileDocumentProvider());
	}

	/* (non-Javadoc)
	 * Method declared on IAdaptable
	 */
	public Object getAdapter(Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return getOutlinePage();
		}
		return super.getAdapter(key);
	}

	private MakefileContentOutlinePage getOutlinePage() {
		if (page == null) {
			page= new MakefileContentOutlinePage(getDocumentProvider(), this);
			//page.addPostSelectionChangedListener(selectionChangedListener);
			page.setInput(getEditorInput());
		}
		return page;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (page != null) {
			page.update();
		}
	}

}
