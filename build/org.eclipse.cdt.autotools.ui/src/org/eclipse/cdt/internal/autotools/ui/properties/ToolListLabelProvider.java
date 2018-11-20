/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Red Hat Inc. - Modification for Autotools usage
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IConfigureOption;
import org.eclipse.cdt.internal.autotools.ui.AutotoolsUIPluginImages;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ToolListLabelProvider extends LabelProvider {
	private final Image IMG_TOOL = AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_CFG_TOOL);
	private final Image IMG_CAT = AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_CFG_CATEGORY);
	private static final String ERROR_UNKNOWN_ELEMENT = "ConfigurePropertyPage.error.Unknown_tree_element"; //$NON-NLS-1$

	private ImageDescriptor descriptor = null;
	private ResourceManager manager = null;
	private IAConfiguration cfg = null;

	public ToolListLabelProvider(IAConfiguration cfg) {
		this.cfg = cfg;
	}

	public IAConfiguration getCfg() {
		return cfg;
	}

	public void setCfg(IAConfiguration cfg) {
		this.cfg = cfg;
	}

	@Override
	public Image getImage(Object element) {
		if (!(element instanceof ToolListElement)) {
			throw unknownElement(element);
		}
		Image defaultImage = IMG_CAT;
		ToolListElement toolListElement = (ToolListElement) element;
		IConfigureOption cat = cfg.getOption(toolListElement.getName());

		if (cat == null) {
			defaultImage = IMG_TOOL;
		}

		// Use default icon for display
		return defaultImage;
	}

	@Override
	public String getText(Object element) {
		if (!(element instanceof ToolListElement)) {
			throw unknownElement(element);
		}
		ToolListElement toolListElement = (ToolListElement) element;
		IConfigureOption cat = cfg.getOption(toolListElement.getName());

		if (cat == null) {
			return toolListElement.getName();
		} else {
			return cat.getDescription();
		}
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException(UIMessages.getFormattedString(ERROR_UNKNOWN_ELEMENT, element.getClass().getName()));
	}

	/**
	 * Disposing any images that were allocated for it.
	 *
	 * @since 3.0
	 */
	@Override
	public void dispose() {
		if (descriptor != null && manager != null) {
			manager.destroyImage(descriptor);
		}
	}
}
