/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.core.OptionCategory;

/**
 * This class represent the elements in the TreeViewer that displays the tools
 * and categories in the tool options property pages.  The reason for these
 * elements is illustrated by bugzilla #123461.  We used to use the ToolChain,
 * Tool and OptionCategory objects themselves as the elements in the TreeViewer,
 * but the same OptionCategory can appear more than once in the list of Tree
 * Viewer items, and this caused problems.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ToolListElement {

	/*
	 * Bookeeping variables
	 */
	private ToolListElement parent = null;
	private List<ToolListElement> childElements = null;

	private IHoldsOptions optionHolder = null;
	private IOptionCategory optionCategory = null;
	private ITool tool = null;

	/*
	 * Constructor for an element tha represents an option category
	 */
	public ToolListElement(ToolListElement parent, IHoldsOptions optionHolder, IOptionCategory optionCategory) {
		this.parent = parent;
		this.optionHolder = optionHolder;
		this.optionCategory = optionCategory;
	}

	/*
	 * Constructor for an element tha represents a tool
	 */
	public ToolListElement(ITool tool) {
		this.tool = tool;
	}

	public boolean isEquivalentTo(ToolListElement e) {

		if (tool != null) {
			//  Look for a matching tool
			ITool matchTool = e.getTool();
			if (matchTool == tool)
				return true;
			if (matchTool == null)
				return false;
			if (matchTool.getName().equals(tool.getUniqueRealName()))
				return true;
			return false;
		}

		if (optionCategory != null) {
			IOptionCategory matchCategory = e.getOptionCategory();
			IHoldsOptions matchHolder = e.getHoldOptions();
			if (matchCategory == optionCategory && matchHolder == optionHolder)
				return true;
			if (matchCategory == null)
				return false;

			//String matchCategoryName = matchCategory.getName();
			//String optionCategoryName = optionCategory.getName();
			String matchCategoryName = OptionCategory.makeMatchName(matchCategory);
			String optionCategoryName = OptionCategory.makeMatchName(optionCategory);
			if (matchHolder.getName().equals(optionHolder.getName()) && matchCategoryName.equals(optionCategoryName))
				return true;

			return false;
		}
		return false;
	}

	/*
	 * Field accessors
	 */
	public ToolListElement getParent() {
		return parent;
	}

	public IHoldsOptions getHoldOptions() {
		return optionHolder;
	}

	public IOptionCategory getOptionCategory() {
		return optionCategory;
	}

	public ITool getTool() {
		return tool;
	}

	/*
	 * Children handling
	 */
	public ToolListElement[] getChildElements() {
		if (childElements != null)
			return childElements.toArray(new ToolListElement[childElements.size()]);
		else
			return new ToolListElement[0];
	}

	public void addChildElement(ToolListElement element) {
		if (childElements == null)
			childElements = new ArrayList<>();
		childElements.add(element);
	}
}
