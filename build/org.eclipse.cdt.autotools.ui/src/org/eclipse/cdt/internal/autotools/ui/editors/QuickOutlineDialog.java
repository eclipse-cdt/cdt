/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors;

import org.eclipse.cdt.autotools.ui.editors.AutoconfEditor;
import org.eclipse.cdt.autotools.ui.editors.outline.AutoconfContentProvider;
import org.eclipse.cdt.autotools.ui.editors.outline.AutoconfLabelProvider;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class QuickOutlineDialog extends PopupDialog {

	TreeViewer treeViewer;
	private Text filterText;

	private TextEditor editor;
	private ContainsFilter treeViewerFilter;

	public QuickOutlineDialog(Shell parent, int shellStyle, AutoconfEditor editor) {
		super(parent, shellStyle, true, true, true, true, true, null, null);
		this.editor = editor;
		create();
	}

	public void setVisible(boolean visible) {
		if (visible) {
			open();
			filterText.setFocus();
		} else {
			saveDialogBounds(getShell());
			getShell().setVisible(false);
		}
	}

	public void dispose() {
		close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		createUIWidgetTreeViewer(parent);
		createUIListenersTreeViewer();
		return treeViewer.getControl();
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		filterText = new Text(parent, SWT.NONE);
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
				.hint(SWT.DEFAULT, Dialog.convertHeightInCharsToPixels(fontMetrics, 1)).applyTo(filterText);

		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) { // Enter pressed
					gotoSelectedElement();
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					treeViewer.getTree().setFocus();
				} else if (e.keyCode == SWT.ARROW_UP) {
					treeViewer.getTree().setFocus();
				} else if (e.character == 0x1B) { // Escape pressed
					dispose();
				}
			}
		});
		filterText.addModifyListener(e -> {
			String filterTextInput = ((Text) e.widget).getText().toLowerCase();
			treeViewerFilter.setLookFor(filterTextInput);
			stringMatcherUpdated();
		});
		return filterText;
	}

	private void stringMatcherUpdated() {
		treeViewer.getControl().setRedraw(false);
		treeViewer.refresh();
		treeViewer.expandAll();
		if (treeViewer.getTree().getTopItem() != null && treeViewer.getTree().getTopItem().getItemCount() > 0) {
			treeViewer.getTree().select(treeViewer.getTree().getTopItem().getItem(0));
		} else if (treeViewer.getTree().getItemCount() > 0) {
			treeViewer.getTree().select(treeViewer.getTree().getItem(0));
		}
		treeViewer.getControl().setRedraw(true);
	}

	private void createUIWidgetTreeViewer(Composite parent) {
		final int style = SWT.H_SCROLL | SWT.V_SCROLL;
		final Tree widget = new Tree(parent, style);
		final GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = widget.getItemHeight() * 12;
		widget.setLayoutData(data);
		treeViewer = new TreeViewer(widget);
		treeViewerFilter = new ContainsFilter();
		treeViewer.addFilter(treeViewerFilter);

		ITreeContentProvider fOutlineContentProvider = new AutoconfContentProvider(editor);
		ILabelProvider fTreeLabelProvider = new AutoconfLabelProvider();
		treeViewer.setContentProvider(fOutlineContentProvider);
		treeViewer.setLabelProvider(fTreeLabelProvider);
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		treeViewer.setUseHashlookup(true);
		treeViewer.setInput(editor.getAdapter(IContentOutlinePage.class));
	}

	private void createUIListenersTreeViewer() {
		final Tree tree = treeViewer.getTree();
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
			}
		});
	}

	private void gotoSelectedElement() {
		if (editor instanceof AutoconfEditor) {
			final AutoconfElement curElement = (AutoconfElement) getSelectedElement();
			if (curElement == null) {
				return;
			}
			dispose();
			editor.setHighlightRange(curElement.getStartOffset(), curElement.getLength(), true);
		}
	}

	private Object getSelectedElement() {
		if (treeViewer == null) {
			return null;
		}
		return ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
	}
}
