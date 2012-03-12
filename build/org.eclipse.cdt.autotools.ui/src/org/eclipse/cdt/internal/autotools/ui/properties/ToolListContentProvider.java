/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		ArrayList<ToolListElement> toolList = new ArrayList<ToolListElement>();
		AutotoolsConfiguration.Option[] options = AutotoolsConfiguration.getTools();
		for (int i = 0; i < options.length; ++i) {
			AutotoolsConfiguration.Option opt = options[i];
			String optName = opt.getName();
			ToolListElement tool = new ToolListElement(optName, IConfigureOption.TOOL);
			toolList.add(tool);
			AutotoolsConfiguration.Option[] categories = 
				AutotoolsConfiguration.getChildOptions(optName);
			for (int j = 0; j < categories.length; ++j) {
				String catName = categories[j].getName();
				ToolListElement newItem = new ToolListElement(catName, IConfigureOption.CATEGORY);
				tool.addChild(newItem);
			}
		}
		return toolList.toArray(new ToolListElement[toolList.size()]);
	}
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof AutotoolsConfiguration) {
			return elements.clone();
		}
		return ((ToolListElement)parentElement).getChildren();	
	}

	public Object getParent(Object element) {
		return ((ToolListElement)element).getParent();
	}

	public boolean hasChildren(Object element) {
		return ((ToolListElement)element).hasChildren();
	}

	public Object[] getElements(Object inputElement) {
		if (elements != null)
			return elements.clone();
		return null;
	}

	public void dispose() {
		// nothing needed
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		elements = createElements();
	}

}
