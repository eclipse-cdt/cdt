/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

public class ExtendedTreeViewer extends TreeViewer {

	private boolean fPreservingSelection = false;

	public ExtendedTreeViewer(Composite parent) {
		super(parent);
	}

	public ExtendedTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	public void refresh(final Object[] elements) {
		preservingSelection(() -> {
			for (int i = 0; i < elements.length; i++) {
				refresh(elements[i]);
			}
		});
	}

	@Override
	protected void preservingSelection(Runnable updateCode) {
		if (fPreservingSelection) {
			updateCode.run();
		} else {
			fPreservingSelection = true;
			try {
				super.preservingSelection(updateCode);
			} finally {
				fPreservingSelection = false;
			}
		}
	}
}
