package org.eclipse.tools.templates.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;

public class Tag {

	public static final String ALL_ID = "all"; //$NON-NLS-1$

	public String id;
	public String label;

	public Tag(IConfigurationElement element) {
		id = element.getAttribute("id"); //$NON-NLS-1$
		label = element.getAttribute("label"); //$NON-NLS-1$
	}

	public Tag(String id, String label) {
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tag) {
			return id.equals(((Tag) obj).id);
		} else {
			return false;
		}
	}

}
