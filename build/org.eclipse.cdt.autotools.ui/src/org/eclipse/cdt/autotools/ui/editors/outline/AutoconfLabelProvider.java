/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc., (c) 2008 NOKIA Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Ed Swartz (NOKIA) - refactoring
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.outline;

import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfCaseConditionElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfCaseElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElifElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElseElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfForElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfIfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroArgumentElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfSelectElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfUntilElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfWhileElement;
import org.eclipse.cdt.internal.autotools.ui.AutotoolsUIPluginImages;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;


public class AutoconfLabelProvider implements ILabelProvider {

	public AutoconfLabelProvider() {
		super();
	}
	
	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public Image getImage(Object element) {
		if (element instanceof AutoconfIfElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_IF);
		else if (element instanceof AutoconfElseElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_ELSE);
		else if (element instanceof AutoconfElifElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_ELIF);
		else if (element instanceof AutoconfCaseElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_CASE);
		else if (element instanceof AutoconfCaseConditionElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_CONDITION);
		else if (element instanceof AutoconfForElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_FOR);
		else if (element instanceof AutoconfWhileElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_WHILE);
		else if (element instanceof AutoconfUntilElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_WHILE);	// TODO
		else if (element instanceof AutoconfSelectElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_WHILE);	// TODO
		else if (element instanceof AutoconfMacroElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_ACMACRO);
		else if (element instanceof AutoconfMacroArgumentElement)
			return AutotoolsUIPluginImages.get(AutotoolsUIPluginImages.IMG_OBJS_ACMACRO_ARG); // TODO
		return null;
	}

	public String getText(Object element) {
		if (element instanceof AutoconfElement) {
			AutoconfElement e = (AutoconfElement)element;
			String result;
			String name = e.getName();
			if (name.length() > 31)
				name = name.substring(0, 31) + "...";
			String var = e.getVar();
			if (var != null) {
				if (var.length() > 15)
					var = var.substring(0, 15) + "...";
				var = " " + var; //$NON-NLS-1$
			} else {
				var = "";
			}
			result = (name + var).replaceAll("(\r|\n| |\t|\f)+", " ");
			return result;
		} else if (element instanceof String) {
			return (String) element;
		}
		return "";
	}

}
