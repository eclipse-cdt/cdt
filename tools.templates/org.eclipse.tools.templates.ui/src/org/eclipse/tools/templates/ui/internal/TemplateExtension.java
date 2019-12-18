/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.ui.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class TemplateExtension {

	private Map<String, Template> templates;
	private Map<String, Tag> tags;

	private void init() {
		if (templates != null)
			return;

		templates = new HashMap<>();
		tags = new HashMap<>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(Activator.getId(), "templates"); //$NON-NLS-1$

		// tags
		Tag allTag = new Tag(Tag.ALL_ID, "All");
		tags.put(allTag.getId(), allTag);

		for (IConfigurationElement element : point.getConfigurationElements()) {
			if (element.getName().equals("tag")) { //$NON-NLS-1$
				Tag tag = new Tag(element);
				tags.put(tag.getId(), tag);
			}
		}

		// templates
		for (IConfigurationElement element : point.getConfigurationElements()) {
			if (element.getName().equals("template")) { //$NON-NLS-1$
				Template template = new Template(this, element);
				templates.put(template.getId(), template);
				template.addTag(allTag);
			}
		}

		// template extensions
		for (IConfigurationElement element : point.getConfigurationElements()) {
			if (element.getName().equals("templateExtension")) { //$NON-NLS-1$
				String templateId = element.getAttribute("templateId"); //$NON-NLS-1$
				Template template = templates.get(templateId);
				if (template != null) {
					for (IConfigurationElement tagRef : element.getChildren("tagReference")) { //$NON-NLS-1$
						String tagId = tagRef.getAttribute("id"); //$NON-NLS-1$
						Tag tag = tags.get(tagId);
						if (tag != null) {
							template.addTag(tag);
						}
					}
				}
			}
		}
	}

	public Collection<Template> getTemplates() {
		init();
		return templates.values();
	}

	public Tag getTag(String id) {
		init();
		return tags.get(id);
	}

}
