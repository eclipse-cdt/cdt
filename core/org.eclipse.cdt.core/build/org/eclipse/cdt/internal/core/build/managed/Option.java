/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.build.managed;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * 
 */
public class Option extends BuildObject implements IOption {

	private ITool tool;
	private IOptionCategory category;
	private List enumValues;
	
	private int valueType;
	private Object value;
	
	private static final String[] emptyStrings = new String[0];
	 
	public Option(ITool tool) {
		this.tool = tool;
	}
	
	public Option(Tool tool, IConfigurationElement element) {
		this(tool);
		
		// id
		setId(element.getAttribute("id"));
		
		// hook me up
		tool.addOption(this);
		
		// name
		setName(element.getAttribute("name"));

		// category
		String categoryId = element.getAttribute("category");
		if (categoryId != null)
			setCategory(tool.getOptionCategory(categoryId));
		
		// valueType
		String valueTypeStr = element.getAttribute("valueType");
		if (valueTypeStr == null || valueTypeStr.equals("string"))
			valueType = IOption.STRING;
		else if (valueTypeStr.equals("stringList"))
			valueType = IOption.STRING_LIST;
		
		// value
		switch (valueType) {
			case IOption.STRING:
				value = element.getAttribute("value");
				break;
			case IOption.STRING_LIST:
				List valueList = new ArrayList();
				value = valueList;
				IConfigurationElement[] valueElements = element.getChildren("optionValue");
				for (int i = 0; i < valueElements.length; ++i) {
					valueList.add(valueElements[i].getAttribute("value"));
				}
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getApplicableValues()
	 */
	public String[] getApplicableValues() {
		return enumValues != null
			? (String[])enumValues.toArray(new String[enumValues.size()])
			: emptyStrings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCategory()
	 */
	public IOptionCategory getCategory() {
		return category != null ? category : getTool().getTopOptionCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	public String[] getStringListValue() {
		List v = (List)value;
		return v != null
			? (String[])v.toArray(new String[v.size()])
			: emptyStrings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringValue()
	 */
	public String getStringValue() {
		return (String)value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getTool()
	 */
	public ITool getTool() {
		return tool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueType()
	 */
	public int getValueType() {
		return valueType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setStringValue(org.eclipse.cdt.core.build.managed.IConfiguration, java.lang.String)
	 */
	public IOption setValue(IConfiguration config, String value)
		throws BuildException
	{
		if (valueType != IOption.STRING)
			throw new BuildException("Bad value for type");

		if (config == null) {
			this.value = value;
			return this;
		} else {
			
			// Magic time
			
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setStringValue(org.eclipse.cdt.core.build.managed.IConfiguration, java.lang.String[])
	 */
	public IOption setValue(IConfiguration config, String[] value)
		throws BuildException
	{
		if (valueType != IOption.STRING_LIST)
			throw new BuildException("Bad value for type");
		
		if (config == null) {
			this.value = value;
			return this;
		} else {
			// More magic
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#setCategory(org.eclipse.cdt.core.build.managed.IOptionCategory)
	 */
	public void setCategory(IOptionCategory category) {
		this.category = category;
	}

}
