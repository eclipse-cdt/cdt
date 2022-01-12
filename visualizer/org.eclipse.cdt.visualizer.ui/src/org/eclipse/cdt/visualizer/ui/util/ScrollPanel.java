/*******************************************************************************
 * Copyright (c) 2009, 2012 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

// ---------------------------------------------------------------------------
// ScrollPanel
// ---------------------------------------------------------------------------

/**
 * Container panel that adds scrollbar(s) to a content control.
 */
public class ScrollPanel extends ScrolledComposite {
	// --- constructors/destructors ---

	/** Constructor. */
	public ScrollPanel(Composite parent) {
		this(parent, null, true, true);
	}

	/** Constructor. */
	public ScrollPanel(Composite parent, boolean hscrollbar, boolean vscrollbar) {
		this(parent, null, hscrollbar, vscrollbar);
	}

	/** Constructor. */
	public ScrollPanel(Composite parent, Control contentControl, boolean hscrollbar, boolean vscrollbar) {
		super(parent, ((hscrollbar) ? SWT.H_SCROLL : SWT.NONE) | ((vscrollbar) ? SWT.V_SCROLL : SWT.NONE));
		initScrollPanel(contentControl, hscrollbar, vscrollbar);
	}

	/** Dispose method. */
	@Override
	public void dispose() {
		cleanupScrollPanel();
		super.dispose();
	}

	/** Overridden to permit subclassing */
	@Override
	protected void checkSubclass() {
		// do nothing -- superclass implementation throws a "Subclassing not allowed" exception
	}

	// --- init methods ---

	/** Initializes control */
	protected void initScrollPanel(Control contentControl, boolean hscrollbar, boolean vscrollbar) {
		setMinSize(0, 0);
		setShowFocusedControl(true);
		// If user doesn't want either scrollbar, we'll auto-size in that direction by default.
		setAutoResizeWidth(!hscrollbar);
		setAutoResizeHeight(!vscrollbar);
		if (contentControl != null)
			setContent(contentControl);
	}

	/** Cleans up control */
	protected void cleanupScrollPanel() {
	}

	// --- methods ---

	/** Sets whether ScrollPanel auto-resizes the width of its contained control
	 *  to match the size of the scrollpanel's content area, save when the control
	 *  is smaller than the minimum width. (This also implicitly disables/hides
	 *  the scrollbar when the control is auto-sized.)
	 */
	public void setAutoResizeWidth(boolean resizeWidth) {
		setExpandHorizontal(resizeWidth);
	}

	/** Sets whether ScrollPanel auto-resizes the height of its contained control
	 *  to match the size of the scrollpanel's content area, save when the control
	 *  is smaller than the minimum width. (This also implicitly disables/hides
	 *  the scrollbar when the control is auto-sized.)
	 */
	public void setAutoResizeHeight(boolean resizeHeight) {
		setExpandVertical(resizeHeight);
	}
}
