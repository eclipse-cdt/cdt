/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ToolReference implements IToolReference {
	private static final String DEFAULT_SEPARATOR = ","; //$NON-NLS-1$	

	private String command;
	private boolean isDirty = false;
	private List optionReferences;
	private IBuildObject owner;
	private String outputExtensions;
	private String outputFlag;
	private String outputPrefix;
	protected ITool parent;
	private boolean resolved = true;
	
	/**
	 * Create a new tool reference based on information contained in 
	 * a project file.
	 * 
	 * @param owner The <code>Configuration</code> the receiver will be added to.
	 * @param element The element defined in the project file containing build information
	 * for the receiver.
	 */
	public ToolReference(BuildObject owner, Element element) {
		this.owner = owner;
		
		if (owner instanceof Configuration) {
			if (parent == null) {
				Target parentTarget = (Target) ((Configuration)owner).getTarget();
				try {
					parent = ((Target)parentTarget.getParent()).getTool(element.getAttribute(ID));
				} catch (NullPointerException e) {
					parent = null;
				}
			}
			((Configuration)owner).addToolReference(this);
		} else if (owner instanceof Target) {
   			if (parent == null) {
   				try {
   					parent = ((Target)((Target)owner).getParent()).getTool(element.getAttribute(ID));
   				} catch (NullPointerException e) {
   					parent = null;
   				}
   			}
			((Target)owner).addToolReference(this);
		}

		// Get the overridden tool command (if any)
		if (element.hasAttribute(ITool.COMMAND)) {
			command = element.getAttribute(ITool.COMMAND);
		}
	
		// Get the overridden output prefix (if any)
		if (element.hasAttribute(ITool.OUTPUT_PREFIX)) {
			outputPrefix = element.getAttribute(ITool.OUTPUT_PREFIX);
		}
		
		// Get the output extensions the reference produces 
		if (element.hasAttribute(ITool.OUTPUTS)) {
			outputExtensions = element.getAttribute(ITool.OUTPUTS);
		}
		// Get the flag to control output
		if (element.hasAttribute(ITool.OUTPUT_FLAG))
			outputFlag = element.getAttribute(ITool.OUTPUT_FLAG);

		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(ITool.OPTION_REF)) {
				new OptionReference(this, (Element)configElement);
			}
		}
	}

	/**
	 * Created tool reference from an extension defined in a plugin manifest.
	 * 
	 * @param owner The <code>BuildObject</code> the receiver will be added to.
	 * @param element The element containing build information for the reference.
	 */
	public ToolReference(BuildObject owner, IManagedConfigElement element) {
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;

		this.owner = owner;

		// hook me up
		if (owner instanceof Configuration) {
			((Configuration)owner).addToolReference(this);
		} else if (owner instanceof Target) {
			((Target)owner).addToolReference(this);
		}

		// Get the overridden tool command (if any)
		command = element.getAttribute(ITool.COMMAND);

		// Get the overridden output prefix, if any
		outputPrefix = element.getAttribute(ITool.OUTPUT_PREFIX);
		
		// Get the overridden output extensions (if any)
		String output = element.getAttribute(ITool.OUTPUTS);
		if (output != null) {
			outputExtensions = output;
		}

		// Get the flag to control output
		outputFlag = element.getAttribute(ITool.OUTPUT_FLAG);
	
		IManagedConfigElement[] toolElements = element.getChildren();
		for (int m = 0; m < toolElements.length; ++m) {
			IManagedConfigElement toolElement = toolElements[m];
			if (toolElement.getName().equals(ITool.OPTION_REF)) {
				new OptionReference(this, toolElement);
			}
		}
	}

	/**
	 * Created a tool reference on the fly based on an existing tool or tool reference.
	 * 
	 * @param owner The <code>BuildObject</code> the receiver will be added to.
	 * @param tool The <code>ITool</code>tool the reference will be based on.
	 */
	public ToolReference(BuildObject owner, ITool tool) {
		this.owner = owner;
		if (tool instanceof ToolReference) {
			parent = ((ToolReference)tool).getTool();
		} else {
			parent = tool;
		}
		command = tool.getToolCommand();
		outputFlag = tool.getOutputFlag();
		outputPrefix = tool.getOutputPrefix();
		String[] extensions = tool.getOutputExtensions();
		outputExtensions = new String();
		if (extensions != null) {
			for (int index = 0; index < extensions.length; ++index) {
				if (extensions[index] == null) continue;
				outputExtensions += extensions[index];
				if (index < extensions.length - 1) {
					outputExtensions += DEFAULT_SEPARATOR;
				}
			}
		}
		
		// Create a copy of the option references of the parent in the receiver
		if (tool instanceof ToolReference) {
			List parentRefs = ((ToolReference)tool).getOptionReferenceList();
			Iterator iter = parentRefs.iterator();
			while (iter.hasNext()) {
				IOption parent = (IOption)iter.next();
				OptionReference clone = createOptionReference(parent);
				try {
					switch (parent.getValueType()) {
						case IOption.BOOLEAN:
							clone.setValue(parent.getBooleanValue());
							break;
						case IOption.STRING:
							clone.setValue(parent.getStringValue());
						case IOption.ENUMERATED:
							clone.setValue(parent.getSelectedEnum());
							break;
						default:
							clone.setValue(parent.getStringListValue());
							break;
					}
				} catch (BuildException e) {
					// Likely a mismatch between the value and option type
					continue;
				}
			}
		}
		
		if (owner instanceof Configuration) {
			((Configuration)owner).addToolReference(this);
		} else if (owner instanceof Target) {
			((Target)owner).addToolReference(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolReference#references(org.eclipse.cdt.managedbuilder.core.ITool)
	 */
	public boolean references(ITool target) {
		if (equals(target)) {
			// we are the target
			return true;
		} else if (parent == null) {
			// basic sanity before proceeding
			return false;
		} else if (parent instanceof IToolReference) {
			// check the reference we are overriding
			return ((IToolReference)parent).references(target);
		} else if (target instanceof IToolReference) {
			return parent.equals(((IToolReference)target).getTool()); 
		} else {
			// the real reference
			return parent.equals(target);
		}
	}

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);
			// resolve my parent
			if (owner instanceof Configuration) {
				Target target = (Target) ((Configuration)owner).getTarget();
				parent = target.getTool(element.getAttribute(ID));
			} else if (owner instanceof Target) {
				parent = ((Target)owner).getTool(element.getAttribute(ID));
			}
			// recursively resolve my parent
			if (parent instanceof Tool) {
				((Tool)parent).resolveReferences();
			} else if (parent instanceof ToolReference) {
				((ToolReference)parent).resolveReferences();
			}

			Iterator it = getOptionReferenceList().iterator();
			while (it.hasNext()) {
				OptionReference optRef = (OptionReference)it.next();
				optRef.resolveReferences();
			}
		}		
	}	
	
	/**
	 * Adds the option reference specified in the argument to the receiver.
	 * 
	 * @param optionRef
	 */
	public void addOptionReference(OptionReference optionRef) {
		getOptionReferenceList().add(optionRef);
		isDirty = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#buildsFileType(java.lang.String)
	 */
	public boolean buildsFileType(String extension) {
		if (parent == null) {
			// bad reference
			return false;
		}
		return parent.buildsFileType(extension);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolReference#createOptionReference(org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	public OptionReference createOptionReference(IOption option) {
		// Check if the option reference already exists
		OptionReference ref = getOptionReference(option);
		// It is possible that the search will return an option reference
		// that is supplied by another element of the build model, not the caller. 
		// For example, if the search is starated by a configuration and the target 
		// the caller  belongs to has an option reference for the option, it 
		// will be returned. While this is correct behaviour for a search, the 
		// caller will need to create a clone for itself, so make sure the tool 
		// reference of the search result is owned by the caller
		if (ref == null || !ref.getToolReference().owner.equals(this.owner)) {
			ref = new OptionReference(this, option);
		}
		return ref;
	}
	
	/* (non-Javadoc)
	 * @return
	 */
	protected List getAllOptionRefs() {
		// First get all the option references this tool reference contains
		if (owner instanceof Configuration) {
			return ((Configuration)owner).getOptionReferences(parent);
		} else if (owner instanceof Target) {
			return ((Target)owner).getOptionReferences(parent);
		} else {
			// this shouldn't happen
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getId()
	 */
	public String getId() {
		if (parent == null) {
			// bad reference
			return new String();
		}
		return parent.getId();
	}
	
 	/* (non-Javadoc)
 	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getInputExtensions()
 	 */
 	public List getInputExtensions() {
 		return getTool().getInputExtensions();
 	}
	
	 	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getName()
	 */
	public String getName() {
		if (parent == null) {
			// bad reference
			return new String();
		}
		return parent.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getNatureFilter()
	 */
	public int getNatureFilter() {
		if (parent == null) {
			// bad reference
			return ITool.FILTER_BOTH;
		}
		return parent.getNatureFilter();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		return getOptionById(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getOption(java.lang.String)
	 */
	public IOption getOptionById(String id) {
		IOption[] options = getOptions();
		for (int i = 0; i < options.length; i++) {
			IOption current = options[i];
			if (current.getId().equals(id)) {
				return current;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#producesFileType(java.lang.String)
	 */
	public boolean producesFileType(String outputExtension) {
		// Check if the reference produces this type of file
		if (!getOutputsList().contains(outputExtension)) {
			return parent.producesFileType(outputExtension);
		} else {
			return true;
		}
		
	}

	/* (non-Javadoc)
	 * @return
	 */
	private List getOutputsList() {
		ArrayList answer = new ArrayList();
		if (outputExtensions != null) {
			String[] exts = outputExtensions.split(DEFAULT_SEPARATOR);
			answer.addAll(Arrays.asList(exts));
		}
		return answer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolReference#getTool()
	 */
	public ITool getTool() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.ITool#getToolCommand()
	 */
	public String getToolCommand() {
		if (command == null) {
			// see if the parent has one
			if (parent != null) {
				return parent.getToolCommand();
			}
			return new String();	// bad reference
		}
		return command;
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
					String boolCmd;
					if (option.getBooleanValue()) {
						boolCmd = option.getCommand();
					} else {
						// Note: getCommandFalse is new with CDT 2.0
						boolCmd = option.getCommandFalse();
					}
					if (boolCmd != null && boolCmd.length() > 0) {
						buf.append(boolCmd + WHITE_SPACE);
					}
					break;
				
				case IOption.ENUMERATED :
					String enum = option.getEnumCommand(option.getSelectedEnum());
					if (enum.length() > 0) {
						buf.append(enum + WHITE_SPACE);
					}
					break;
				
				case IOption.STRING :
					String strCmd = option.getCommand();
					String val = option.getStringValue();
					if (val.length() > 0) { 
						if (strCmd != null) buf.append(strCmd);
						buf.append(val + WHITE_SPACE);
					}
					break;
					
				case IOption.STRING_LIST :
					String cmd = option.getCommand();
					String[] list = option.getStringListValue();
					for (int j = 0; j < list.length; j++) {
						String temp = list[j];
						if (cmd != null) buf.append(cmd);
						buf.append(temp + WHITE_SPACE);
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getTopOptionCategory()
	 */
	public IOptionCategory getTopOptionCategory() {
		try {
			return parent.getTopOptionCategory();
		} catch (NullPointerException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * Answers an option reference that overrides the option, or <code>null</code>
	 * 
	 * @param option
	 * @return OptionReference
	 */
	private OptionReference getOptionReference(IOption option) {
		// Get all the option references for this option
		Iterator iter = getAllOptionRefs().listIterator();
		while (iter.hasNext()) {
			OptionReference optionRef = (OptionReference) iter.next();
			if (optionRef.references(option))
				return optionRef;
		}

		return null;
	}
	
	/* (non-Javadoc)
	 * 
	 * @param id
	 * @return
	 */
	private OptionReference getOptionReference(String id) {
		Iterator it = getOptionReferenceList().iterator();
		while (it.hasNext()) {
			OptionReference current = (OptionReference)it.next();
			if (current.getId().equals(id)) {
				return current;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolReference#getOptionReferenceList()
	 */
	public List getOptionReferenceList() {
		if (optionReferences == null) {
			optionReferences = new ArrayList();
		}
		return optionReferences;
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
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputExtensions()
	 */
	public String[] getOutputExtensions() {
		if (outputExtensions == null) outputExtensions = new String();
		return outputExtensions.split(DEFAULT_SEPARATOR);
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputExtension(java.lang.String)
	 */
	public String getOutputExtension(String inputExtension) {
		if (parent == null) {
			// bad reference
			return new String();
		}
		return parent.getOutputExtension(inputExtension);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputFlag()
	 */
	public String getOutputFlag() {
		if (outputFlag == null) {
			if (parent != null) {
				return parent.getOutputFlag();
			} else {
				// We never should be here
				return new String();
			}
		}
		return outputFlag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#getOutputPrefix()
	 */
	public String getOutputPrefix() {
		if (outputPrefix == null) {
			if (parent != null) {
				return parent.getOutputPrefix();
			}
			return new String();	// bad reference
		}
		return outputPrefix;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolReference#isDirty()
	 */
	public boolean isDirty() {
		return isDirty;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.ITool#isHeaderFile(java.lang.String)
	 */
	public boolean isHeaderFile(String ext) {
		if (parent == null) {
			// bad reference
			return false;
		}
		return parent.isHeaderFile(ext);
	}

	/**
	 * Answers <code>true</code> if the owner of the receiver matches 
	 * the argument.
	 * 
	 * @param config
	 * @return
	 */
	public boolean ownedByConfiguration(IConfiguration config) {
		if (owner instanceof Configuration) {
			return ((IConfiguration)owner).equals(config);
		}
		return false;
	}
	
	/**
	 * Persist receiver to project file.
	 * 
	 * @param doc The persistent store for the reference information.
	 * @param element The root element in the store the receiver must use 
	 * to persist settings.
	 */
	public void serialize(Document doc, Element element) {
		if (parent == null) return;	// This is a bad reference
		element.setAttribute(ITool.ID, parent.getId());

		// Output the command 
		if (command != null) {
			element.setAttribute(ITool.COMMAND, getToolCommand());
		}
		
		// Save output prefix
		if (outputPrefix != null) {
			element.setAttribute(ITool.OUTPUT_PREFIX, getOutputPrefix());
		}
		
		// Save the output flag
		if (outputFlag != null) {
			element.setAttribute(ITool.OUTPUT_FLAG, getOutputFlag());
		}
		
		// Save the outputs
		if (outputExtensions != null) {
			element.setAttribute(ITool.OUTPUTS, outputExtensions);
		}
		
		// Output the option references
		Iterator iter = getOptionReferenceList().listIterator();
		while (iter.hasNext()) {
			OptionReference optionRef = (OptionReference) iter.next();
			Element optionRefElement = doc.createElement(ITool.OPTION_REF);
			element.appendChild(optionRefElement);
			optionRef.serialize(doc, optionRefElement);
		}
		
		// Set the reference to clean
		isDirty = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolReference#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		// Override the local flag 
		this.isDirty = isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IToolReference#setToolCommand(java.lang.String)
	 */
	public boolean setToolCommand(String cmd) {
		if (cmd != null && !cmd.equals(command)) {
			command = cmd;
			isDirty = true;
			return true;
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String answer = new String();	
		if (parent != null) {
			answer += "Reference to " + parent.getName();	//$NON-NLS-1$ 
		}
		
		if (answer.length() > 0) {
			return answer;
		} else {
			return super.toString();			
		}
	}

}
