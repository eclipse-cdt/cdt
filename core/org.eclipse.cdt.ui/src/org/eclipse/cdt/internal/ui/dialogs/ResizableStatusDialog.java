/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * A resizable {@link StatusDialog} that preserves its dimensions between invocations.
 */
public abstract class ResizableStatusDialog extends StatusDialog {
	public ResizableStatusDialog(Shell parent) {
		super(parent);
	}

	@Override
	protected Point getInitialSize() {
		Point defaultSize= getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point restoredSize= super.getInitialSize();
		if (restoredSize.x < defaultSize.x)
			restoredSize.x= defaultSize.x;
		if (restoredSize.y < defaultSize.y)
			restoredSize.y= defaultSize.y;
		return restoredSize;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		String sectionName= getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= settings.getSection(sectionName);
		if (section == null)
			section= settings.addNewSection(sectionName);
		return section;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}

