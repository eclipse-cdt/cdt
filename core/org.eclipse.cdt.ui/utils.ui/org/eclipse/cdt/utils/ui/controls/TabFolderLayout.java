/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.ui.controls;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class TabFolderLayout extends Layout {

	protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
		if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
			return new Point(wHint, hHint);
			
		Control [] children = composite.getChildren ();
		int count = children.length;
		int maxWidth = 0, maxHeight = 0;
		for (int i=0; i<count; i++) {
			Control child = children [i];
			Point pt = child.computeSize (SWT.DEFAULT, SWT.DEFAULT, flushCache);
			maxWidth = Math.max (maxWidth, pt.x);
			maxHeight = Math.max (maxHeight, pt.y);
		}
		
		if (wHint != SWT.DEFAULT)
			maxWidth= wHint;
		if (hHint != SWT.DEFAULT)
			maxHeight= hHint;
		
		return new Point(maxWidth, maxHeight);	
		
	}
	protected void layout (Composite composite, boolean flushCache) {
		Rectangle rect= composite.getClientArea();
	
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].setBounds(rect);
		}
	}
}

