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
import java.util.Map;

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class ToolReference implements ITool {

	private ITool parent;
	private IConfiguration owner;
	private List optionReferences;
	private Map optionRefMap;
	
	/**
	 * Created on the fly.
	 * 
	 * @param owner
	 * @param parent
	 */
	public ToolReference(Configuration owner, ITool parent) {
		this.owner = owner;
		this.parent = parent;
		
		owner.addToolReference(this);
	}
	
	/**
	 * Created from extension.
	 * 
	 * @param owner
	 * @param element
	 */
	public ToolReference(Configuration owner, IConfigurationElement element) {
		this.owner = owner;
		
		parent = ((Target)owner.getTarget()).getTool(element.getAttribute("id"));

		owner.addToolReference(this);
		
		IConfigurationElement[] toolElements = element.getChildren();
		for (int m = 0; m < toolElements.length; ++m) {
			IConfigurationElement toolElement = toolElements[m];
			if (toolElement.getName().equals("optionRef")) {
				new OptionReference(this, toolElement);
			}
		}
	}

	public ToolReference(Configuration owner, Element element) {
		this.owner = owner;
		
		Target parentTarget = (Target)owner.getTarget();
		parent = ((Target)parentTarget.getParent()).getTool(element.getAttribute("id"));

		owner.addToolReference(this);
	
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals("optionRef")) {
				new OptionReference(this, (Element)configElement);
			}
		}
	}

	public void serealize(Document doc, Element element) {
		element.setAttribute("id", parent.getId());
		
		if (optionReferences != null)
			for (int i = 0; i < optionReferences.size(); ++i) {
				OptionReference optionRef = (OptionReference)optionReferences.get(i);
				Element optionRefElement = doc.createElement("optionRef");
				element.appendChild(optionRefElement);
				optionRef.serealize(doc, optionRefElement);
			}
	}

	public IConfiguration getConfiguration() {
		return owner;
	}
	
	public ITool getTool() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#createOption()
	 */
	public IOption createOption() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOptions()
	 */
	public IOption[] getOptions() {
		IOption[] options = parent.getOptions();
		
		// Replace with our references
		for (int i = 0; i < options.length; ++i) {
			OptionReference ref = getOptionReference(options[i]);
			if (ref != null)
				options[i] = ref;
		}
			
		return options;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getTarget()
	 */
	public ITarget getTarget() {
		return owner.getTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getTopOptionCategory()
	 */
	public IOptionCategory getTopOptionCategory() {
		return parent.getTopOptionCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getId()
	 */
	public String getId() {
		return parent.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#getName()
	 */
	public String getName() {
		return parent.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#setId(java.lang.String)
	 */
	public void setId(String id) {
		// Not allowed
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IBuildObject#setName(java.lang.String)
	 */
	public void setName(String name) {
		// Not allowed
	}

	public boolean references(ITool target) {
		if (equals(target))
			// we are the target
			return true;
		else if (parent instanceof ToolReference)
			// check the reference we are overriding
			return ((ToolReference)parent).references(target);
		else
			// the real reference
			return parent.equals(target);
	}
	
	private OptionReference getOptionReference(IOption option) {
		if (optionReferences != null)
			for (int i = 0; i < optionReferences.size(); ++i) {
				OptionReference optionRef = (OptionReference)optionReferences.get(i);
				if (optionRef.references(option))
					return optionRef;
			}
		return null;
	}
	
	public OptionReference createOptionReference(IOption option) {
		return new OptionReference(this, option);
	}
	
	public void addOptionReference(OptionReference optionRef) {
		if (optionReferences == null)
			optionReferences = new ArrayList();
		optionReferences.add(optionRef);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
