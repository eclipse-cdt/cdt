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

import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class OptionReference implements IOption {

	private IOption option;
	private ToolReference owner;
	private Object value;

	/**
	 * Created internally.
	 * 
	 * @param owner
	 * @param option
	 */
	public OptionReference(ToolReference owner, IOption option) {
		this.owner = owner;
		this.option = option;
		
		owner.addOptionReference(this);
	}

	/**
	 * Created from extension.
	 * 
	 * @param owner
	 * @param element
	 */
	public OptionReference(ToolReference owner, IConfigurationElement element) {
		this.owner = owner;
		option = owner.getTool().getOption(element.getAttribute("id"));
		
		owner.addOptionReference(this);
	}

	/**
	 * Created from project file.
	 * 
	 * @param owner
	 * @param element
	 */
	public OptionReference(ToolReference owner, Element element) {
		this.owner = owner;	
		option = owner.getTool().getOption(element.getAttribute("id"));
		
		owner.addOptionReference(this);
	}
	
	/**
	 * Write out to project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serealize(Document doc, Element element) {
		element.setAttribute("id", option.getId());
		option = owner.getOption(element.getAttribute("id"));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getApplicableValues()
	 */
	public String[] getApplicableValues() {
		return option.getApplicableValues();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getCategory()
	 */
	public IOptionCategory getCategory() {
		return option.getCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getName()
	 */
	public String getName() {
		return option.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringListValue()
	 */
	public String[] getStringListValue() throws BuildException {
		if (value == null)
			return option.getStringListValue();
		else if (getValueType() == IOption.STRING_LIST)
			return (String[])value;
		else
			throw new BuildException("bad value type");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getStringValue()
	 */
	public String getStringValue() throws BuildException {
		if (value == null)
			return option.getStringValue();
		else if (getValueType() == IOption.STRING)
			return (String)value;
		else
			throw new BuildException("bad value type");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getTool()
	 */
	public ITool getTool() {
		return owner;
	}

	public ToolReference getToolReference() {
		return owner;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOption#getValueType()
	 */
	public int getValueType() {
		return option.getValueType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getId()
	 */
	public String getId() {
		return option.getId();
	}

	public boolean references(IOption target) {
		if (equals(target))
			// we are the target
			return true;
		else if (option instanceof OptionReference)
			// check the reference we are overriding
			return ((OptionReference)option).references(target);
		else
			// the real reference
			return option.equals(target);
	}

	public void setValue(String value) throws BuildException {
		if (getValueType() == IOption.STRING)
			this.value = value;
		else
			throw new BuildException("bad value type");
	}
	
	public void setValue(String [] value) throws BuildException {
		if (getValueType() == IOption.STRING_LIST)
			this.value = value;
		else
			throw new BuildException("bad value type");
	}
}
