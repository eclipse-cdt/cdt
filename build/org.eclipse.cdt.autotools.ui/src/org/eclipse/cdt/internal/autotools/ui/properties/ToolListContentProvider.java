/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import java.util.ArrayList;

import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IConfigureOption;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ToolListContentProvider implements ITreeContentProvider {

	private ToolListElement[] elements;

	private ToolListElement[] createElements() {
		ArrayList<ToolListElement> toolList = new ArrayList<>();
		AutotoolsConfiguration.Option[] options = AutotoolsConfiguration.getTools();
		for (int i = 0; i < options.length; ++i) {
			AutotoolsConfiguration.Option opt = options[i];
			String optName = opt.getName();
			ToolListElement tool = new ToolListElement(optName, IConfigureOption.TOOL);
			toolList.add(tool);
			AutotoolsConfiguration.Option[] categories = AutotoolsConfiguration.getChildOptions(optName);
			for (int j = 0; j < categories.length; ++j) {
				String catName = categories[j].getName();
				ToolListElement newItem = new ToolListElement(catName, IConfigureOption.CATEGORY);
				tool.addChild(newItem);
			}
		}
		return toolList.toArray(new ToolListElement[toolList.size()]);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof AutotoolsConfiguration) {
			return elements.clone();
		}
		return ((ToolListElement) parentElement).getChildren();
	}

	@Override
	public Object getParent(Object element) {
		return ((ToolListElement) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((ToolListElement) element).hasChildren();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (elements != null)
			return elements.clone();
		return null;
	}

	@Override
	public void dispose() {
		// nothing needed
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		elements = createElements();
	}

}
