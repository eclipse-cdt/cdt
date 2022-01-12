/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Export tab for C/C++ Project Paths page for 3.X projects.
 *
 * @deprecated as of CDT 4.0. This tab was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
public class CPathOrderExportPage extends CPathBasePage {

	private ListDialogField<?> fCPathList;

	public CPathOrderExportPage(ListDialogField<?> cPathList) {
		super(CPathEntryMessages.OrderExportsPage_title);
		setDescription(CPathEntryMessages.OrderExportsPage_description);
		fCPathList = cPathList;
	}

	@Override
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fCPathList }, true);
		LayoutUtil.setHorizontalGrabbing(fCPathList.getListControl(null), true);

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fCPathList.setButtonsMinWidth(buttonBarWidth);
	}

	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_ORDER);
	}

	/*
	 * @see BuildPathBasePage#getSelection
	 */
	@Override
	public List<?> getSelection() {
		return fCPathList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */
	@Override
	public void setSelection(List<?> selElements) {
		fCPathList.selectElements(new StructuredSelection(selElements));
	}

	@Override
	public boolean isEntryKind(int kind) {
		return true;
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {

	}

	@Override
	public void performDefaults() {
	}

}
