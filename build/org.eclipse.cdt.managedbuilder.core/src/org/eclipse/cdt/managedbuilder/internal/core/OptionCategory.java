/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
// import org.eclipse.core.runtime.PluginVersionIdentifier;  // uncomment this line after 'parent' is available
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class OptionCategory extends BuildObject implements IOptionCategory {

	private static final IOptionCategory[] emtpyCategories = new IOptionCategory[0];

	//  Parent and children
	private Tool tool;
	private List children;			// Note: These are logical Option Category children, not "model" children
	//  Managed Build model attributes
	private IOptionCategory owner;	// The logical Option Category parent
	private String ownerId;
	//  Miscellaneous
	private boolean isExtensionOptionCategory = false;
	private boolean isDirty = false;
	private boolean resolved = true;
	
	/*
	 *  C O N S T R U C T O R S
	 */

	public OptionCategory(IOptionCategory owner) {
		this.owner = owner;
	}
	
	/**
	 * This constructor is called to create an option category defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IToolChain parent of this builder, or <code>null</code> if
	 *                defined at the top level
	 * @param element The builder definition from the manifest file or a dynamic element
	 *                provider
	 */
	public OptionCategory(Tool parent, IManagedConfigElement element) {
		this.tool = parent;
		isExtensionOptionCategory = true;

		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionOptionCategory(this);
		
		// Add the category to the tool
		tool.addOptionCategory(this);
	}

	/**
	 * Create an <codeOptionCategory</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>Tool</code> the OptionCategory will be added to. 
	 * @param element The XML element that contains the OptionCategory settings.
	 */
	public OptionCategory(Tool parent, Element element) {
		tool = parent;
		isExtensionOptionCategory = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
		
		// Add the category to the tool
		tool.addOptionCategory(this);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */

	public void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IOptionCategory.ID));
		
		// name
		setName(element.getAttribute(IOptionCategory.NAME));
		
		// owner
		ownerId = element.getAttribute(IOptionCategory.OWNER);
	}
	
	/* (non-Javadoc)
	 * Initialize the OptionCategory information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the OptionCategory information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// owner
		if (element.hasAttribute(IOptionCategory.OWNER)) {
			ownerId = element.getAttribute(IOptionCategory.OWNER);
		}
		if (ownerId != null) {
			owner = tool.getOptionCategory(ownerId);
		} else {
			owner = tool;
		}		
		
		// Hook me in
		if (owner instanceof Tool)
			((Tool)owner).addChildCategory(this);
		else
			((OptionCategory)owner).addChildCategory(this);
	}

	/**
	 * Persist the OptionCategory to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}

		if (owner != null)
			element.setAttribute(IOptionCategory.OWNER, owner.getId());
		
		// I am clean now
		isDirty = false;
	}
	
	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getChildCategories()
	 */
	public IOptionCategory[] getChildCategories() {
		if (children != null)
			return (IOptionCategory[])children.toArray(new IOptionCategory[children.size()]);
		else
			return emtpyCategories;
	}

	public void addChildCategory(OptionCategory category) {
		if (children == null)
			children = new ArrayList();
		children.add(category);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOptions(org.eclipse.cdt.core.build.managed.ITool)
	 */
	public Object[][] getOptions(IConfiguration configuration) {
		ITool[] tools = null;
		if (configuration != null) {
			tools = configuration.getTools();
		}
		return getOptions(tools, FILTER_PROJECT);
	}

	public Object[][] getOptions(IResourceConfiguration resConfig) {
		ITool[] tools = null;
		if (resConfig != null) {
			tools = resConfig.getTools();
		}
		return getOptions(tools, FILTER_FILE);
	}
	
	private Object[][] getOptions(ITool[] tools, int filterValue) {
		ITool catTool = getTool();
		ITool tool = null;

		if (tools != null) {
			// Find the child of the configuration/resource configuration that represents the same tool.
			// It could the tool itself, or a "sub-class" of the tool.
			for (int i = 0; i < tools.length; ++i) {
				ITool current = tools[i];
				do {
					if (catTool == current) {
						tool = tools[i];
						break;
					}
				} while ((current = current.getSuperClass()) != null);
				if (tool != null) break;
			}
		}
		if (tool == null) {
			tool = catTool;
		}
		
		// Get all of the tool's options and see which ones are part of
		// this category.
		IOption[] allOptions = tool.getOptions();
		Object[][] myOptions = new Object[allOptions.length][2];
		int index = 0;
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			if (option.getCategory().equals(this)) {
				
				// Check whether this option can be displayed for a specific resource type.
				if( (option.getResourceFilter() == FILTER_ALL) || (option.getResourceFilter() == filterValue) ) {
					myOptions[index] = new Object[2];
					myOptions[index][0] = tool;
					myOptions[index][1] = option;
					index++;	
				}
			}
		}

		return myOptions;
	}
	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOwner()
	 */
	public IOptionCategory getOwner() {
		return owner;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getTool()
	 */
	public ITool getTool() {
		// This will stop at the Tool's top category
		return owner.getTool();
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionCategory#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionOptionCategory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionCategory#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension OptionCategory
 		if (isExtensionOptionCategory) return false;
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IIOptionCategory#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			if (ownerId != null) {
				owner = tool.getOptionCategory(ownerId);
				if (owner == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"owner",	//$NON-NLS-1$
							ownerId,
							"optionCategory",	//$NON-NLS-1$
							getId());
				}
			}
			if (owner == null) {
				owner = tool;
			}
			
			// Hook me in
			if (owner instanceof Tool)
				((Tool)owner).addChildCategory(this);
			else
				((OptionCategory)owner).addChildCategory(this);
		}
	}

	// Uncomment this code after the 'parent' is available
	/**
	 * @return Returns the managedBuildRevision.
	 *
	public String getManagedBuildRevision() {
		if ( managedBuildRevision == null) {
			if ( getParent() != null) {
				return getParent().getManagedBuildRevision();
			}
		}
		return managedBuildRevision;
	}

	/**
	 * @return Returns the version.
	 *
	public PluginVersionIdentifier getVersion() {
		if ( version == null) {
			if ( getParent() != null) {
				return getParent().getVersion();
			}
		}
		return version;
	}
	
	public void setVersion(PluginVersionIdentifier version) {
		// Do nothing
	}
*/

}
