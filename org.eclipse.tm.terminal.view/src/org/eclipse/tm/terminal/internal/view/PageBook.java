/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * A pagebook is a composite control where only a single control is visible
 * at a time. It is similar to a notebook, but without tabs.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 */
public class PageBook extends Composite {
	private Point minimumPageSize = new Point(0, 0);
	/**
	 * Layout for the page container.
	 *  
	 */
	private class PageLayout extends Layout {
		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			int x = minimumPageSize.x;
			int y = minimumPageSize.y;
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Point size = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}
			
			if (wHint != SWT.DEFAULT)
				x = wHint;
			if (hHint != SWT.DEFAULT)
				y = hHint;
			return new Point(x, y);
		}

		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				children[i].setSize(rect.width, rect.height);
			}
		}
	}
     /**
     * Creates a new empty pagebook.
     *
     * @param parent the parent composite
     * @param style the SWT style bits (use {@link SWT#NONE})
     */
    public PageBook(Composite parent, int style) {
        super(parent, style);
        setLayout(new PageLayout());
    }
    /**
     * The current control; <code>null</code> if none.
     */
    private Control currentPage = null;

    /**
     * Shows the given page. This method has no effect if the given page is not
     * contained in this pagebook.
     *
     * @param page the page to show
     */
    public void showPage(Control page) {

        if (page == currentPage)
            return;
        if (page.getParent() != this)
            return;

        currentPage = page;
 
        // show new page
        if (page != null) {
            if (!page.isDisposed()) {
                page.setVisible(true);
                layout(true);
                //				if (fRequestFocusOnShowPage)
                //					page.setFocus();
            }
        }
        
        // hide old *after* new page has been made visible in order to avoid flashing
        // we have to hide all other pages, because they might be visible
        // by some other means...
    	Control[] pages=getChildren();
    	for (int i = 0; i < pages.length; i++) {
    		if(pages[i]!=page && !pages[i].isDisposed()) {
    			pages[i].setVisible(false);
    		}
		}
    }
}
