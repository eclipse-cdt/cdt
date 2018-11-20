/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.outline;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.cdt.internal.autotools.ui.editors.LexicalSortingAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class AutoconfContentOutlinePage extends ContentOutlinePage {

	private ITextEditor editor;
	private IEditorInput input;
	private LexicalSortingAction sortAction;

	public AutoconfContentOutlinePage(AutoconfEditor editor) {
		super();
		this.editor = editor;
	}

	public void setInput(IEditorInput editorInput) {
		this.input = editorInput;
		update();
	}

	protected ISelection updateSelection(ISelection sel) {
		ArrayList<AutoconfElement> newSelection = new ArrayList<>();
		if (sel instanceof IStructuredSelection) {
			Iterator<?> iter = ((IStructuredSelection) sel).iterator();
			for (; iter.hasNext();) {
				//ICElement elem= fInput.findEqualMember((ICElement)iter.next());
				Object o = iter.next();
				if (o instanceof AutoconfElement) {
					newSelection.add((AutoconfElement) o);
				}
			}
		}
		return new StructuredSelection(newSelection);
	}

	public void update() {
		//set the input so that the outlines parse can be called
		//update the tree viewer state
		final TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			final Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().asyncExec(() -> {
					if (!control.isDisposed()) {
						ISelection sel = viewer.getSelection();
						viewer.setSelection(updateSelection(sel));
						viewer.refresh();
					}
				});
			}
		}
	}

	@Override
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new AutoconfContentProvider(editor));
		viewer.setLabelProvider(new AutoconfLabelProvider());
		viewer.addSelectionChangedListener(this);

		if (input != null) {
			viewer.setInput(input);
		}
		sortAction.setTreeViewer(viewer);
	}

	/*
	 * Change in selection
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);

		//find out which item in tree viewer we have selected, and set highlight range accordingly
		ISelection selection = event.getSelection();
		if (selection.isEmpty()) {
			editor.resetHighlightRange();
		} else {
			AutoconfElement element = (AutoconfElement) ((IStructuredSelection) selection).getFirstElement();

			try {
				int offset = element.getStartOffset();
				int length = element.getEndOffset() - offset;
				editor.setHighlightRange(offset, length, true);
				editor.selectAndReveal(offset, length);
			} catch (IllegalArgumentException x) {
				editor.resetHighlightRange();
			}
		}
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		IToolBarManager toolBarManager = pageSite.getActionBars().getToolBarManager();
		sortAction = new LexicalSortingAction();
		toolBarManager.add(sortAction);
	}

}
