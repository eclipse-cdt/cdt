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
	public String getHref() {
		return href;
	}
	public String getLabel() {
		return title;
	}
	public String toString() {
		return "<topic href=\"" + href + "\" title=\"" + title + "\">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
