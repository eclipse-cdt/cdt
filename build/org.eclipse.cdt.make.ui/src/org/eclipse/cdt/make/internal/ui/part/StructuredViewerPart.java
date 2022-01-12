/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.part;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class StructuredViewerPart extends SharedPartWithButtons {
	private StructuredViewer viewer;
	private Point minSize = null;

	public StructuredViewerPart(String[] buttonLabels) {
		super(buttonLabels);
	}

	public StructuredViewer getViewer() {
		return viewer;
	}

	public Control getControl() {
		return viewer.getControl();
	}

	@Override
	protected void createMainControl(Composite parent, int style, int span) {
		viewer = createStructuredViewer(parent, style);
		Control control = viewer.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = span;
		control.setLayoutData(gd);
		applyMinimumSize();
	}

	public void setMinimumSize(int width, int height) {
		minSize = new Point(width, height);
		if (viewer != null)
			applyMinimumSize();
	}

	private void applyMinimumSize() {
		if (minSize != null) {
			GridData gd = (GridData) viewer.getControl().getLayoutData();
			gd.widthHint = minSize.x;
			gd.heightHint = minSize.y;
		}
	}

	@Override
	protected void updateEnabledState() {
		getControl().setEnabled(isEnabled());
		super.updateEnabledState();
	}

	protected abstract StructuredViewer createStructuredViewer(Composite parent, int style);
}
