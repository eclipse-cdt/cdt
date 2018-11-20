/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.dialogs;

import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.refactoring.changes.CreateFileChange;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.util.ViewerPane;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Emanuel Graf
 */
public class CreateFileChangePreview implements IChangePreviewViewer {

	private static class CreateFileChangePane extends ViewerPane {
		public CreateFileChangePane(Composite parent, int style) {
			super(parent, style);
		}
	}

	private CreateFileChangePane control;
	private SourceViewer srcViewer;
	private CTextTools textTools;

	@Override
	public void createControl(Composite parent) {
		control = new CreateFileChangePane(parent, SWT.BORDER | SWT.FLAT);
		Dialog.applyDialogFont(control);
		srcViewer = new CSourceViewer(control, null, null, false,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION,
				CUIPlugin.getDefault().getPreferenceStore());
		textTools = CUIPlugin.getDefault().getTextTools();
		IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
		CSourceViewerConfiguration configuration = new CSourceViewerConfiguration(textTools.getColorManager(), store,
				null, textTools.getDocumentPartitioning());
		srcViewer.configure(configuration);
		srcViewer.setEditable(false);
		control.setContent(srcViewer.getControl());
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setInput(ChangePreviewViewerInput input) {
		Assert.isNotNull(input);
		if (control != null) {
			Change change = input.getChange();
			if (change instanceof CreateFileChange) {
				CreateFileChange createFileChange = (CreateFileChange) change;
				control.setText(createFileChange.getName());
				Document document = new Document(createFileChange.getSource());
				textTools.setupCDocument(document);
				srcViewer.setDocument(document);
			}
		}
	}
}
