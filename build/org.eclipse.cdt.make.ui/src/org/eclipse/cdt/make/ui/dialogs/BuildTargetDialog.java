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

package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.cdt.make.ui.TargetListViewerPart;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BuildTargetDialog extends Dialog {
	TargetListViewerPart targetPart;
	IContainer fContainer;

	public BuildTargetDialog(Shell shell, IContainer container) {
		super(shell);
		fContainer = container;
		targetPart = new TargetListViewerPart(fContainer);
	}

	public void setTarget(IMakeTarget target) {
		targetPart.setSelectedTarget(target);
	}

	public IMakeTarget getTarget() {
		return targetPart.getSelectedTarget();
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MakeUIPlugin.getResourceString("BuildTargetDialog.title.buildTarget")); //$NON-NLS-1$
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create Build and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, MakeUIPlugin.getResourceString("BuildTargetDialog.button.build"), true); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		getButton(IDialogConstants.OK_ID).setEnabled(targetPart.getSelectedTarget() != null);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 2;
		Label title = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		title.setLayoutData(gd);
		title.setText(MakeUIPlugin.getResourceString("BuildTargetDialog.title.makeTargetsFor") + fContainer.getFullPath().toString().substring(1)); //$NON-NLS-1$
		targetPart.createControl(composite, SWT.NULL, 2);

		gd = (GridData) targetPart.getControl().getLayoutData();
		gd.heightHint = convertHeightInCharsToPixels(15);
		gd.widthHint = convertWidthInCharsToPixels(50);
		targetPart.getControl().setLayoutData(gd);
		targetPart.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		targetPart.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getButton(IDialogConstants.OK_ID).setEnabled(targetPart.getSelectedTarget() != null);
			}
		});
		return composite;
	}

	protected void okPressed() {
		IMakeTarget selected = targetPart.getSelectedTarget();
		super.okPressed();
		if (selected != null) {
			TargetBuild.buildTargets(getParentShell(), new IMakeTarget[] { selected });
		}
	}

}
