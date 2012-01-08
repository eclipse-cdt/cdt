/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.help;

import org.eclipse.help.IHelpResource;
import org.w3c.dom.Element;

public class CHelpTopic implements IHelpResource {
	private static final String ATTR_TITLE = "title"; //$NON-NLS-1$
	private static final String ATTR_HREF  = "href"; //$NON-NLS-1$

	private String href = null;
	private String title = null;
	
	public CHelpTopic(Element e, String defTitle) {
		href  = e.getAttribute(ATTR_HREF).trim();
		title =	e.getAttribute(ATTR_TITLE).trim();
		if (title == null || title.length() == 0)
			title = defTitle;
	}
	@Override
	public String getHref() {
		return href;
	}
	@Override
	public String getLabel() {
		return title;
	}
	@Override
	public String toString() {
		return "<topic href=\"" + href + "\" title=\"" + title + "\">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
