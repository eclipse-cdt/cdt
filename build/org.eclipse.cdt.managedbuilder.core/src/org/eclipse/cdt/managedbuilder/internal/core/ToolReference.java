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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
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
	 * Created a tool reference on the fly based on an existing tool.
	 * 
	 * @param owner The <code>Configuration</code> the receiver will be added to.
	 * @param parent The <code>ITool</code>tool the reference will be based on.
	 */
	public ToolReference(Configuration owner, ITool parent) {
		this.owner = owner;
		this.parent = parent;
		
		owner.addToolReference(this);
	}
	
	/**
	 * Created tool reference from an extension defined in a plugin manifest.
	 * 
	 * @param owner The <code>Configuration</code> the receiver will be added to.
	 * @param element The element containing build information for the reference.
	 */
	public ToolReference(Configuration owner, IConfigurationElement element) {
		this.owner = owner;
		
		parent = ((Target)owner.getTarget()).getTool(element.getAttribute("id"));

		owner.addToolReference(this);
		
		IConfigurationElement[] toolElements = element.getChildren();
		for (int m = 0; m < toolElements.length; ++m) {
			IConfigurationElement toolElement = toolElements[m];
			if (toolElement.getName().equals(ITool.OPTION_REF)) {
				new OptionReference(this, toolElement);
			}
		}
	}

	/**
	 * Create a new tool reference based on information contained in a project file.
	 * 
	 * @param owner The <code>Configuration</code> the receiver will be added to.
	 * @param element The element defined in the project file containing build information
	 * for the receiver.
	 */
	public ToolReference(Configuration owner, Element element) {
		this.owner = owner;
		
		Target parentTarget = (Target)owner.getTarget();
		parent = ((Target)parentTarget.getParent()).getTool(element.getAttribute("id"));

		owner.addToolReference(this);
	
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(ITool.OPTION_REF)) {
				new OptionReference(this, (Element)configElement);
			}
		}
	}

	/**
	 * Persist receiver to project file.
	 * 
	 * @param doc The persistent store for the reference information.
	 * @param element The root element in the store the receiver must use 
	 * to persist settings.
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(ITool.ID, parent.getId());
		
		if (optionReferences != null)
			for (int i = 0; i < optionReferences.size(); ++i) {
				OptionReference optionRef = (OptionReference)optionReferences.get(i);
				Element optionRefElement = doc.createElement(ITool.OPTION_REF);
				element.appendChild(optionRefElement);
				optionRef.serialize(doc, optionRefElement);
			}
	}

	public IConfiguration getConfiguration() {
		return owner;
	}
	
	public ITool getTool() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolCommand()
	 */
	public String getToolCommand() {
		return parent.getToolCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolFlags()
	 */
	public String getToolFlags() throws BuildException {
		// Get all of the options
		StringBuffer buf = new StringBuffer();
		IOption[] opts = getOptions();
		for (int index = 0; index < opts.length; index++) {
			IOption option = opts[index];
			switch (option.getValueType()) {
				case IOption.BOOLEAN :
					if (option.getBooleanValue()) {
						buf.append(option.getCommand() + WHITE_SPACE);
					}
					break;
				
				case IOption.ENUMERATED :
					String enum = option.getEnumCommand(option.getSelectedEnum());
					if (enum.length() > 0) {
						buf.append(enum + WHITE_SPACE);
					}
					break;
				
				case IOption.STRING :
					String val = option.getStringValue();
					if (val.length() > 0) { 
						buf.append(val + WHITE_SPACE);
					}
					break;
					
				case IOption.STRING_LIST :
					String cmd = option.getCommand();
					String[] list = option.getStringListValue();
					for (int j = 0; j < list.length; j++) {
						String temp = list[j];
						buf.append(cmd + temp + WHITE_SPACE);
					}
					break;
					
				case IOption.INCLUDE_PATH :
					String incCmd = option.getCommand();
					String[] paths = option.getIncludePaths();
					for (int j = 0; j < paths.length; j++) {
						String temp = paths[j];
						buf.append(incCmd + temp + WHITE_SPACE);
					}
					break;

				case IOption.PREPROCESSOR_SYMBOLS :
					String defCmd = option.getCommand();
					String[] symbols = option.getDefinedSymbols();
					for (int j = 0; j < symbols.length; j++) {
						String temp = symbols[j];
						buf.append(defCmd + temp + WHITE_SPACE);
					}
					break;

				default :
					break;
			}

		}

		return buf.toString().trim();
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
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputFlag()
	 */
	public String getOutputFlag() {
		return parent.getOutputFlag();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutputPrefix()
	 */
	public String getOutputPrefix() {
		return parent.getOutputPrefix();
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
	 * @see org.eclipse.cdt.core.build.managed.ITool#producesFileType(java.lang.String)
	 */
	public boolean producesFileType(String outputExtension) {
		return parent.producesFileType(outputExtension);
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
	 * @see org.eclipse.cdt.core.build.managed.ITool#handlesFileType(java.lang.String)
	 */
	public boolean buildsFileType(String extension) {
		return parent.buildsFileType(extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOutput(java.lang.String)
	 */
	public String getOutputExtension(String inputExtension) {
		return parent.getOutputExtension(inputExtension);
	}

}
