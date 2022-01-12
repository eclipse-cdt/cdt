/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationV2;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolReference;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @deprecated This class is deprecated in 2.1
 */
@Deprecated
public class ConfigurationV2 extends BuildObject implements IConfigurationV2 {
	private boolean isDirty = false;
	private IConfigurationV2 parent;
	private boolean rebuildNeeded = false;
	private boolean resolved = true;
	private ITarget target;
	private List<IToolReference> toolReferences;
	private IConfiguration createdConfig;

	/**
	 * Build a configuration from the project manifest file.
	 *
	 * @param target The <code>Target</code> the configuration belongs to.
	 * @param element The element from the manifest that contains the overridden configuration information.
	 */
	public ConfigurationV2(Target target, Element element) {
		this.target = target;

		// id
		setId(element.getAttribute(IConfigurationV2.ID));

		// hook me up
		target.addConfiguration(this);

		// name
		if (element.hasAttribute(IConfigurationV2.NAME))
			setName(element.getAttribute(IConfigurationV2.NAME));

		if (element.hasAttribute(IConfigurationV2.PARENT)) {
			// See if the target has a parent
			ITarget targetParent = target.getParent();
			// If so, then get my parent from it
			if (targetParent != null) {
				parent = targetParent.getConfiguration(element.getAttribute(IConfigurationV2.PARENT));
			} else {
				parent = null;
			}
		}

		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(IConfigurationV2.TOOLREF_ELEMENT_NAME)) {
				new ToolReference(this, (Element) configElement);
			}
		}

	}

	/**
	 * Create a new configuration based on one already defined.
	 *
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param parentConfig The <code>IConfigurationV2</code> to copy the settings from.
	 * @param id A unique ID for the configuration.
	 */
	public ConfigurationV2(Target target, IConfigurationV2 parentConfig, String id) {
		this.id = id;
		this.name = parentConfig.getName();
		this.target = target;

		// If this contructor is called to clone an existing
		// configuration, the parent of the parent should be stored.
		// As of 2.0, there is still one single level of inheritence to
		// worry about
		parent = parentConfig.getParent() == null ? parentConfig : parentConfig.getParent();

		// Check that the tool and the project match
		IProject project = (IProject) target.getOwner();

		// Get the tool references from the target and parent
		List<IToolReference> allToolRefs = new Vector<>(target.getLocalToolReferences());
		allToolRefs.addAll(((ConfigurationV2) parentConfig).getLocalToolReferences());
		for (IToolReference toolRef : allToolRefs) {
			// Make a new ToolReference based on the tool in the ref
			ITool parentTool = toolRef.getTool();
			ToolReference newRef = new ToolReference(this, parentTool);

			// The reference may have a different command than the parent tool
			String refCmd = toolRef.getToolCommand();
			if (!refCmd.equals(parentTool.getToolCommand())) {
				newRef.setToolCommand(refCmd);
			}

			List<OptionReference> optRefs = toolRef.getOptionReferenceList();
			for (OptionReference optRef : optRefs) {
				IOption opt = optRef.getOption();
				try {
					switch (opt.getValueType()) {
					case IOption.BOOLEAN:
						new OptionReference(newRef, opt).setValue(optRef.getBooleanValue());
						break;
					case IOption.STRING:
					case IOption.TREE:
						new OptionReference(newRef, opt).setValue(optRef.getStringValue());
						break;
					case IOption.ENUMERATED:
						new OptionReference(newRef, opt).setValue(optRef.getSelectedEnum());
						break;
					case IOption.STRING_LIST:
						new OptionReference(newRef, opt).setValue(optRef.getStringListValue());
						break;
					case IOption.INCLUDE_PATH:
						new OptionReference(newRef, opt).setValue(optRef.getIncludePaths());
						break;
					case IOption.PREPROCESSOR_SYMBOLS:
						new OptionReference(newRef, opt).setValue(optRef.getDefinedSymbols());
						break;
					case IOption.LIBRARIES:
						new OptionReference(newRef, opt).setValue(optRef.getLibraries());
						break;
					case IOption.OBJECTS:
						new OptionReference(newRef, opt).setValue(optRef.getUserObjects());
						break;
					}
				} catch (BuildException e) {
					continue;
				}
			}
		}

		target.addConfiguration(this);
	}

	/**
	 * Create a new <code>ConfigurationV2</code> based on the specification in the plugin manifest.
	 *
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param element The element from the manifest that contains the default configuration settings.
	 */
	public ConfigurationV2(Target target, IManagedConfigElement element) {
		this.target = target;

		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;

		// id
		setId(element.getAttribute(IConfigurationV2.ID));

		// hook me up
		target.addConfiguration(this);

		// name
		setName(element.getAttribute(IConfigurationV2.NAME));

		IManagedConfigElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IManagedConfigElement configElement = configElements[l];
			if (configElement.getName().equals(IConfigurationV2.TOOLREF_ELEMENT_NAME)) {
				new ToolReference(this, configElement);
			}
		}
	}

	/**
	 * A fresh new configuration for a target.
	 */
	public ConfigurationV2(Target target, String id) {
		this.id = id;
		this.target = target;

		target.addConfiguration(this);
	}

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			//			IManagedConfigElement element = ManagedBuildManager.getConfigElement(this);
			List<IToolReference> localToolReferences = getLocalToolReferences();
			for (IToolReference ref : localToolReferences) {
				((ToolReference) ref).resolveReferences();
			}
		}
	}

	/**
	 * Adds a tool reference to the receiver.
	 */
	public void addToolReference(ToolReference toolRef) {
		getLocalToolReferences().add(toolRef);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#getToolReferences()
	 */
	@Override
	public IToolReference[] getToolReferences() {
		List<IToolReference> list = getLocalToolReferences();
		IToolReference[] tools = new IToolReference[list.size()];
		list.toArray(tools);
		return tools;
	}

	/* (non-Javadoc)
	 * @param option
	 * @return
	 */
	private OptionReference createOptionReference(IOption option) {
		ToolReference searchRef = null;
		IToolReference answer = null;
		// The option may already be a reference created to hold user settings
		if (option instanceof OptionReference) {
			// The option reference belongs to an existing tool reference
			OptionReference optionRef = (OptionReference) option;
			searchRef = optionRef.getToolReference();

			// That tool reference may belong to a target or to the configuration
			if (searchRef.ownedByConfiguration(this))
				return optionRef;
			else {
				// All this means is that the tool ref does not belong to the receiver.
				// The receiver may also have a reference to the tool
				if ((answer = findLocalReference(searchRef)) == null) {
					// Otherwise, create one and save the option setting in it
					answer = new ToolReference(this, searchRef);
				}
				return answer.createOptionReference(option);
			}
		} else {
			// Find out if a tool reference already exists.
			// Note: as in MBS 2.0 the ITool == IHoldsOptions was always
			// true, just up-cast the pointers.
			searchRef = (ToolReference) getToolReference((ITool) option.getOptionHolder());
			if (searchRef == null) {
				answer = new ToolReference(this, (ITool) option.getOptionHolder());
			} else {
				// The reference may belong to the target
				if (!searchRef.ownedByConfiguration(this)) {
					answer = new ToolReference(this, searchRef);
				} else {
					answer = searchRef;
				}
			}
			return answer.createOptionReference(option);
		}
	}

	/* (non-Javadoc)
	 * @param toolRef
	 * @return
	 */
	private IToolReference findLocalReference(ToolReference toolRef) {
		List<IToolReference> localToolReferences = getLocalToolReferences();
		for (IToolReference ref : localToolReferences) {
			if (toolRef.getTool().equals(ref.getTool())) {
				return ref;
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#getFilteredTools(org.eclipse.core.resources.IProject)
	 */
	@Override
	public ITool[] getFilteredTools(IProject project) {
		ITool[] localTools = getTools();
		Vector<ITool> tools = new Vector<>(localTools.length);
		for (ITool tool : localTools) {
			try {
				// Make sure the tool is right for the project
				switch (tool.getNatureFilter()) {
				case ITool.FILTER_C:
					if (project.hasNature(CProjectNature.C_NATURE_ID)
							&& !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
						tools.add(tool);
					}
					break;
				case ITool.FILTER_CC:
					if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
						tools.add(tool);
					}
					break;
				case ITool.FILTER_BOTH:
					tools.add(tool);
					break;
				default:
					break;
				}
			} catch (CoreException e) {
				continue;
			}
		}

		// Answer the filtered tools as an array
		return tools.toArray(new ITool[tools.size()]);
	}

	/* (non-javadoc)
	 * A safety method to avoid NPEs. It answers the tool reference list in the
	 * receiver. It does not look at the tool references defined in the parent.
	 *
	 * @return List
	 */
	protected List<IToolReference> getLocalToolReferences() {
		if (toolReferences == null) {
			toolReferences = new ArrayList<>();
		}
		return toolReferences;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#getName()
	 */
	@Override
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#getTools()
	 */
	@Override
	public ITool[] getTools() {
		ITool[] tools = parent != null ? parent.getTools() : target.getTools();

		// Validate that the tools correspond to the nature
		IProject project = (IProject) target.getOwner();
		if (project != null) {
			List<ITool> validTools = new ArrayList<>();

			// The target is associated with a real project
			for (int i = 0; i < tools.length; ++i) {
				ITool tool = tools[i];
				// Make sure the tool filter and project nature agree
				switch (tool.getNatureFilter()) {
				case ITool.FILTER_C:
					try {
						if (project.hasNature(CProjectNature.C_NATURE_ID)
								&& !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							validTools.add(tool);
						}
					} catch (CoreException e) {
						continue;
					}
					break;
				case ITool.FILTER_CC:
					try {
						if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
							validTools.add(tool);
						}
					} catch (CoreException e) {
						continue;
					}
					break;
				case ITool.FILTER_BOTH:
					validTools.add(tool);
					break;
				}
			}
			// Now put the valid tools back into the array
			tools = validTools.toArray(new ITool[validTools.size()]);
		}

		// Replace tools with local overrides
		for (int i = 0; i < tools.length; ++i) {
			IToolReference ref = getToolReference(tools[i]);
			if (ref != null)
				tools[i] = ref;
		}

		return tools;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#isDirty()
	 */
	@Override
	public boolean isDirty() {
		// If I need saving, just say yes
		if (isDirty)
			return true;

		// Otherwise see if any tool references need saving
		List<IToolReference> localToolReferences = getLocalToolReferences();
		for (IToolReference ref : localToolReferences) {
			if (ref.isDirty())
				return true;
		}

		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#needsRebuild()
	 */
	@Override
	public boolean needsRebuild() {
		return rebuildNeeded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#getParent()
	 */
	@Override
	public IConfigurationV2 getParent() {
		return parent;
	}

	/* (non-javadoc)
	 *
	 * @param tool
	 * @return List
	 */
	protected List<OptionReference> getOptionReferences(ITool tool) {
		List<OptionReference> references = new ArrayList<>();

		// Get all the option references I add for this tool
		IToolReference toolRef = getToolReference(tool);
		if (toolRef != null) {
			references.addAll(toolRef.getOptionReferenceList());
		}

		// See if there is anything that my parents add that I don't
		if (parent != null) {
			List<OptionReference> temp = ((ConfigurationV2) parent).getOptionReferences(tool);
			for (OptionReference ref : temp) {
				if (!references.contains(ref)) {
					references.add(ref);
				}
			}
		}

		return references;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#getToolById(java.lang.String)
	 */
	@Override
	public ITool getToolById(String id) {
		ITool[] tools = parent != null ? parent.getTools() : target.getTools();

		// Replace tools with local overrides
		for (int i = 0; i < tools.length; ++i) {
			IToolReference ref = getToolReference(tools[i]);
			if (ref != null)
				tools[i] = ref;
		}

		// Search the result for the ID
		for (int index = tools.length - 1; index >= 0; --index) {
			if (tools[index].getId().equals(id)) {
				return tools[index];
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#getTarget()
	 */
	@Override
	public ITarget getTarget() {
		return (target == null && parent != null) ? parent.getTarget() : target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#getOwner()
	 */
	@Override
	public IResource getOwner() {
		return getTarget().getOwner();
	}

	/* (non-Javadoc)
	 * Returns the reference for a given tool or <code>null</code> if one is not
	 * found.
	 *
	 * @param tool
	 * @return ToolReference
	 */
	private IToolReference getToolReference(ITool tool) {
		// Sanity
		if (tool == null)
			return null;

		// See if the receiver has a reference to the tool
		List<IToolReference> localToolReferences = getLocalToolReferences();
		for (IToolReference temp : localToolReferences) {
			if (temp.references(tool)) {
				return temp;
			}
		}

		// See if the target that the receiver belongs to has a reference to the tool
		ITool[] targetTools = target.getTools();
		for (int index = targetTools.length - 1; index >= 0; --index) {
			ITool targetTool = targetTools[index];
			if (targetTool instanceof ToolReference) {
				if (((ToolReference) targetTool).references(tool)) {
					return (ToolReference) targetTool;
				}
			}
		}
		return null;
	}

	public void reset(IManagedConfigElement element) {
		// I just need to reset the tool references
		getLocalToolReferences().clear();
		IManagedConfigElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IManagedConfigElement configElement = configElements[l];
			if (configElement.getName().equals(IConfigurationV2.TOOLREF_ELEMENT_NAME)) {
				ToolReference ref = new ToolReference(this, configElement);
				ref.resolveReferences();
			}
		}
		isDirty = true;
	}

	/**
	 * Persist receiver to project file.
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(IConfigurationV2.ID, id);

		if (name != null)
			element.setAttribute(IConfigurationV2.NAME, name);

		if (parent != null)
			element.setAttribute(IConfigurationV2.PARENT, parent.getId());

		// Serialize only the tool references defined in the configuration
		List<IToolReference> localToolReferences = getLocalToolReferences();
		for (IToolReference toolRef : localToolReferences) {
			Element toolRefElement = doc.createElement(IConfigurationV2.TOOLREF_ELEMENT_NAME);
			element.appendChild(toolRefElement);
			((ToolReference) toolRef).serialize(doc, toolRefElement);
		}

		// I am clean now
		isDirty = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#setDirty(boolean)
	 */
	@Override
	public void setDirty(boolean isDirty) {
		// Override the dirty flag
		this.isDirty = isDirty;
		// And do the same for the tool references
		List<IToolReference> localToolReferences = getLocalToolReferences();
		for (IToolReference toolRef : localToolReferences) {
			((ToolReference) toolRef).setDirty(isDirty);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#setOption(org.eclipse.cdt.core.build.managed.IOption, boolean)
	 */
	@Override
	public void setOption(IOption option, boolean value) throws BuildException {
		// Is there a delta
		if (option.getBooleanValue() != value) {
			createOptionReference(option).setValue(value);
			isDirty = true;
			rebuildNeeded = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String)
	 */
	@Override
	public void setOption(IOption option, String value) throws BuildException {
		String oldValue;
		// Check whether this is an enumerated option
		if (option.getValueType() == IOption.ENUMERATED) {
			oldValue = option.getSelectedEnum();
		} else {
			oldValue = option.getStringValue();
		}
		if (oldValue != null && !oldValue.equals(value)) {
			createOptionReference(option).setValue(value);
			isDirty = true;
			rebuildNeeded = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfigurationV2#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String[])
	 */
	@Override
	public void setOption(IOption option, String[] value) throws BuildException {
		// Is there a delta
		String[] oldValue;
		switch (option.getValueType()) {
		case IOption.STRING_LIST:
			oldValue = option.getStringListValue();
			break;
		case IOption.INCLUDE_PATH:
			oldValue = option.getIncludePaths();
			break;
		case IOption.PREPROCESSOR_SYMBOLS:
			oldValue = option.getDefinedSymbols();
			break;
		case IOption.LIBRARIES:
			oldValue = option.getLibraries();
			break;
		case IOption.OBJECTS:
			oldValue = option.getUserObjects();
			break;
		default:
			oldValue = new String[0];
			break;
		}
		if (!Arrays.equals(value, oldValue)) {
			createOptionReference(option).setValue(value);
			isDirty = true;
			rebuildNeeded = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#setRebuildState(boolean)
	 */
	@Override
	public void setRebuildState(boolean rebuild) {
		rebuildNeeded = rebuild;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	@Override
	public void setToolCommand(ITool tool, String command) {
		// Make sure the command is different
		if (command != null) {
			// Does this config have a ref to the tool
			IToolReference ref = getToolReference(tool);
			if (ref == null) {
				// Then make one
				ref = new ToolReference(this, tool);
			}
			// Set the ref's command
			isDirty = ref.setToolCommand(command);
			rebuildNeeded = isDirty;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#setCreatedConfig(IConfiguration)
	 */
	@Override
	public void setCreatedConfig(IConfiguration config) {
		createdConfig = config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationV2#getCreatedConfig()
	 */
	@Override
	public IConfiguration getCreatedConfig() {
		return createdConfig;
	}

}
