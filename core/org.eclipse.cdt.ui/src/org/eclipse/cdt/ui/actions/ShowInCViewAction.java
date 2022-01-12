/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.ui.actions;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class will open the C/C++ Projects view and highlight the
 * selected resource matching the current resouce being edited in
 * the C/C++ Editor.  It uses the IShowInSource/IShowInTarget to
 * accomplish this task so as to provide some additional portability
 * and future proofing.
 *
 * @deprecated Use generic IShowInTarget support instead.
 */
@Deprecated
public class ShowInCViewAction extends SelectionProviderAction {

	private IWorkbenchPage page;
	private ITextEditor fEditor;

	public ShowInCViewAction(IWorkbenchSite site) {
		this(site.getPage(), site.getSelectionProvider());
	}

	public ShowInCViewAction(ITextEditor editor) {
		this(editor.getEditorSite().getWorkbenchWindow().getActivePage(), editor.getSelectionProvider());
		fEditor = editor;
	}

	public ShowInCViewAction(IWorkbenchPage page, ISelectionProvider viewer) {
		super(viewer, CEditorMessages.ShowInCView_label);
		setToolTipText(CEditorMessages.ShowInCView_tooltip);
		setDescription(CEditorMessages.ShowInCView_description);
		this.page = page;
		setDescription(CEditorMessages.ShowInCView_tooltip);
		//WorkbenchHelp.setHelp(this, ICHelpContextIds.SHOW_IN_CVIEW_ACTION);
	}

	@Override
	public void run() {
		ISelection selection = getSelection();
		if (selection instanceof ITextSelection) {
			run(fEditor);
		} else if (selection instanceof IStructuredSelection) {
			run((IStructuredSelection) selection);
		}

	}

	public void run(IStructuredSelection selection) {
		if (page == null) {
			page = CUIPlugin.getActivePage();
			if (page == null) {
				return;
			}
		}

		//Locate a source and a target for us to use
		try {
			IWorkbenchPart part = page.showView(CUIPlugin.CVIEW_ID);
			if (part instanceof ISetSelectionTarget) {
				((ISetSelectionTarget) part).selectReveal(selection);
			}
		} catch (PartInitException ex) {
		}
	}

	public void run(ITextEditor editor) {
		if (editor != null) {
			try {
				ICElement celement = SelectionConverter.getElementAtOffset(editor);
				if (celement != null) {
					run(new StructuredSelection(celement));
				}
			} catch (CModelException e) {
			}
		}
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(!getSelection().isEmpty());
	}

}
