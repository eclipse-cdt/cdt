/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.ui.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Template {

	private final TemplateExtension parent;
	private final IConfigurationElement element;
	private Map<String, Tag> tags;

	public Template(TemplateExtension parent, IConfigurationElement element) {
		this.parent = parent;
		this.element = element;
	}

	public String getId() {
		return element.getAttribute("id"); //$NON-NLS-1$
	}

	public String getLabel() {
		return element.getAttribute("label"); //$NON-NLS-1$
	}

	public String getDescription() {
		IConfigurationElement[] descs = element.getChildren("description"); //$NON-NLS-1$
		return descs.length > 0 ? descs[0].getValue() : null;
	}

	public ImageDescriptor getIcon() {
		String iconPath = element.getAttribute("icon"); //$NON-NLS-1$

		if (iconPath == null) {
			return null;
		}

		return AbstractUIPlugin.imageDescriptorFromPlugin(element.getNamespaceIdentifier(), iconPath);
	}

	private void initTags() {
		if (tags == null) {
			tags = new HashMap<>();
			for (IConfigurationElement ref : element.getChildren("tagReference")) { //$NON-NLS-1$
				String id = ref.getAttribute("id"); //$NON-NLS-1$
				Tag tag = parent.getTag(id);
				if (tag != null) {
					tags.put(tag.getId(), tag);
				}
			}
		}
	}

	public void addTag(Tag tag) {
		initTags();
		tags.put(tag.getId(), tag);
	}

	public boolean hasTag(String tagId) {
		if (tagId.equals(Tag.ALL_ID)) {
			// All means all
			return true;
		}

		initTags();
		return tags.containsKey(tagId);
	}

	public Collection<Tag> getTags() {
		return tags.values();
	}

	public IWorkbenchWizard getWizard() throws CoreException {
		return (IWorkbenchWizard) element.createExecutableExtension("wizard"); //$NON-NLS-1$
	}

}
