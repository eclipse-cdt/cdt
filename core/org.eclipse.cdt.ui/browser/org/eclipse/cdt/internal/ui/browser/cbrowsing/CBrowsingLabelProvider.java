/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.swt.graphics.Image;

class CBrowsingLabelProvider extends AppearanceAwareLabelProvider {

	private static final int TEXTFLAGS = DEFAULT_TEXTFLAGS | CElementLabels.F_APP_TYPE_SIGNATURE;
    private static final int IMAGEFLAGS = DEFAULT_IMAGEFLAGS | CElementImageProvider.SMALL_ICONS;

    CBrowsingLabelProvider() {
        super(TEXTFLAGS, IMAGEFLAGS);
    }

    public Image getImage(Object element) {
		if (element == CBrowsingContentProvider.CONTENT_ERROR) {
            return null;
	    } else if (element == CBrowsingContentProvider.CONTENT_CANCELLED) {
	        return null;
	    }
        return super.getImage(element);
    }

    public String getText(Object element) {
		if (element == CBrowsingContentProvider.CONTENT_ERROR) {
            return CBrowsingMessages.getString("CBrowsingLabelProvider.errorNoItems"); //$NON-NLS-1$
	    } else if (element == CBrowsingContentProvider.CONTENT_CANCELLED) {
            return CBrowsingMessages.getString("CBrowsingLabelProvider.errorCancelled"); //$NON-NLS-1$
	    }
        return super.getText(element);
    }
}
