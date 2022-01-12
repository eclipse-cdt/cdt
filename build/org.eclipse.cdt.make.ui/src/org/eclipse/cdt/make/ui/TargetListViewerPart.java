/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.make.ui;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.part.StructuredViewerPart;
import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class TargetListViewerPart extends StructuredViewerPart {

	private TableViewer tableViewer;
	private IMakeTarget fSelectedTarget;
	private final int ADD_TARGET = 0;
	private final int REMOVE_TARGET = 1;
	private final int EDIT_TARGET = 2;
	private IContainer fContainer;
	private boolean recursive;

	/**
	 * @param container
	 * @param recursive {@code true} if to search recursively for target
	 *
	 * @since 7.0
	 */
	public TargetListViewerPart(IContainer container, boolean recursive) {
		super(new String[] { MakeUIPlugin.getResourceString("TargetListViewer.button.add"), //$NON-NLS-1$
				MakeUIPlugin.getResourceString("TargetListViewer.button.remove"), //$NON-NLS-1$
				MakeUIPlugin.getResourceString("TargetListViewer.button.edit") }); //$NON-NLS-1$
		this.fContainer = container;
		this.recursive = recursive;
	}

	public TargetListViewerPart(IContainer container) {
		this(container, true);
	}

	@Override
	protected StructuredViewer createStructuredViewer(Composite parent, int style) {
		tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER);
		Table table = (Table) tableViewer.getControl();
		TableLayout layout = new TableLayout();

		layout.addColumnData(new ColumnWeightData(50));
		TableColumn tc = new TableColumn(table, SWT.NONE, 0);
		tc.setText(MakeUIPlugin.getResourceString("TargetListViewer.lable.target")); //$NON-NLS-1$

		layout.addColumnData(new ColumnWeightData(50));
		tc = new TableColumn(table, SWT.NONE, 1);
		tc.setText(MakeUIPlugin.getResourceString("TargetListViewer.lable.location")); //$NON-NLS-1$

		table.setLayout(layout);
		table.setHeaderVisible(true);

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				TargetListViewerPart.this.selectionChanged((IStructuredSelection) e.getSelection());
			}
		});
		tableViewer.setContentProvider(new MakeContentProvider(recursive));
		tableViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return (element instanceof IMakeTarget);
			}
		});
		tableViewer.setLabelProvider(new MakeLabelProvider(fContainer.getProjectRelativePath()));
		tableViewer.setInput(fContainer);

		return tableViewer;
	}

	@Override
	protected void buttonSelected(Button button, int index) {
		try {
			switch (index) {
			case ADD_TARGET: {
				MakeTargetDialog dialog = new MakeTargetDialog(getControl().getShell(), fContainer);
				dialog.open();
			}
				break;
			case REMOVE_TARGET:
				IMakeTargetManager manager = MakeCorePlugin.getDefault().getTargetManager();
				manager.removeTarget(
						(IMakeTarget) ((IStructuredSelection) getViewer().getSelection()).getFirstElement());
				break;
			case EDIT_TARGET: {
				MakeTargetDialog dialog = new MakeTargetDialog(getControl().getShell(),
						(IMakeTarget) ((IStructuredSelection) getViewer().getSelection()).getFirstElement());
				dialog.open();
			}

				break;
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(getControl().getShell(),
					MakeUIPlugin.getResourceString("TargetListViewer.exception.error"), //$NON-NLS-1$
					MakeUIPlugin.getResourceString("TargetListViewer.exception.message"), //$NON-NLS-1$
					e);
		}
	}

	protected void selectionChanged(IStructuredSelection selection) {
		fSelectedTarget = (IMakeTarget) selection.getFirstElement();
		if (getViewer() != null) {
			updateEnabledState();
		}
	}

	public void setSelectedTarget(IMakeTarget target) {
		fSelectedTarget = target;
		if (tableViewer != null) {
			tableViewer.setSelection(new StructuredSelection(fSelectedTarget), false);
		}
	}

	public IMakeTarget getSelectedTarget() {
		return fSelectedTarget;
	}

	@Override
	protected void updateEnabledState() {
		super.updateEnabledState();
		setButtonEnabled(REMOVE_TARGET, fSelectedTarget != null && isEnabled());
		setButtonEnabled(EDIT_TARGET, fSelectedTarget != null && isEnabled());
	}

	@Override
	public void createControl(Composite parent, int style, int span) {
		super.createControl(parent, style, span);
		updateEnabledState();
		if (getViewer() != null && fSelectedTarget != null) {
			getViewer().setSelection(new StructuredSelection(fSelectedTarget));
		}
	}

}
