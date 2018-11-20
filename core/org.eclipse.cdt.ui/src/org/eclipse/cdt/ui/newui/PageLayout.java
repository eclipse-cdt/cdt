/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Layout for the page container.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PageLayout extends Layout {

	/**
	 * The minimum page size; 200 by 200 by default.
	 *
	 * @see #setMinimumPageSize
	 */
	private Point minimumPageSize = new Point(200, 200);

	@Override
	public void layout(Composite composite, boolean force) {
		Rectangle rect = composite.getClientArea();
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setSize(rect.width, rect.height);
		}
	}

	@Override
	public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
		if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
			return new Point(wHint, hHint);
		}
		int x = minimumPageSize.x;
		int y = minimumPageSize.y;

		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			Point size = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
			x = Math.max(x, size.x);
			y = Math.max(y, size.y);
		}
		if (wHint != SWT.DEFAULT) {
			x = wHint;
		}
		if (hHint != SWT.DEFAULT) {
			y = hHint;
		}
		return new Point(x, y);
	}
}
