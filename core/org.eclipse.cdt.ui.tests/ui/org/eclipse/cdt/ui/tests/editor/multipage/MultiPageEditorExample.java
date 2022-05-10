/*******************************************************************************
 * Copyright (c) 2022 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dominic Scharfe (COSEDA Technologies GmbH) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.editor.multipage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * Example for a {@link MultiPageEditorPart multi page editor} which contains one or multiple C/C++ editors.
 *
 */
public class MultiPageEditorExample extends MultiPageEditorPart {
	public static final String ID = "org.eclipse.cdt.ui.tests.multi_page_editor";

	private List<CEditor> cEditors;

	public MultiPageEditorExample() {
		super();
		cEditors = new ArrayList<>();
	}

	@Override
	protected void createPages() {
		createPage(getEditorInput());
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		cEditors.forEach(part -> part.doSave(monitor));
	}

	@Override
	public void doSaveAs() {
		// Not supported yet
	}

	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return cEditors.stream().filter(e -> e.isDirty()).findAny().isPresent();
	}

	@Override
	protected CTabItem createItem(int index, Control control) {
		CTabItem item = new CTabItem((CTabFolder) getContainer(), SWT.CLOSE, index);
		item.setControl(control);
		return item;
	}

	/**
	 * Creates a new page for the given editor input.
	 */
	void createPage(IEditorInput editorInput) {
		try {
			CEditor editor = new CEditor();
			cEditors.add(editor);
			int index = addPage(editor, editorInput);
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested CEditor", null, e.getStatus());
		}
	}

}
