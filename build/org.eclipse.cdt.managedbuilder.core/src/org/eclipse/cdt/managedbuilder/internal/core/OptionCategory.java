/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class OptionCategory extends BuildObject implements IOptionCategory {

	private static final IOptionCategory[] emtpyCategories = new IOptionCategory[0];

	//  Parent and children
	private IHoldsOptions holder;
	private List children;			// Note: These are logical Option Category children, not "model" children
	//  Managed Build model attributes
	private IOptionCategory owner;	// The logical Option Category parent
	private String ownerId;
	private URL    iconPathURL;
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
	 * @param parent  The IHoldsOptions parent of this catgeory, or <code>null</code> if
	 *                defined at the top level
	 * @param element The category definition from the manifest file or a dynamic element
	 *                provider
	 */
	public OptionCategory(IHoldsOptions parent, IManagedConfigElement element) {
		this.holder = parent;
		isExtensionOptionCategory = true;

		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionOptionCategory(this);
		
		// Add the category to the parent
		parent.addOptionCategory(this);
	}

	/**
	 * Create an <codeOptionCategory</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>IHoldsOptions</code> object the OptionCategory will be added to. 
	 * @param element The XML element that contains the OptionCategory settings.
	 */
	public OptionCategory(IHoldsOptions parent, Element element) {
		this.holder = parent;
		isExtensionOptionCategory = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);
		
		// Add the category to the parent
		parent.addOptionCategory(this);
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

		// icon
		if ( element.getAttribute(IOptionCategory.ICON) != null  && element instanceof DefaultManagedConfigElement)
		{
		    String icon = element.getAttribute(IOptionCategory.ICON);
			iconPathURL = ManagedBuildManager.getURLInBuildDefinitions( (DefaultManagedConfigElement)element, new Path(icon) );
		}
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
			owner = holder.getOptionCategory(ownerId);
		} else {
			owner = getNullOptionCategory();
		}		
		
		// icon - was saved as URL in string form
		if (element.hasAttribute(IOptionCategory.ICON)) {
			String iconPath = element.getAttribute(IOptionCategory.ICON);
			try {
				iconPathURL = new URL(iconPath);
			} catch (MalformedURLException e) {
				// Print a warning
				ManagedBuildManager.OutputIconError(iconPath);
				iconPathURL = null;
			}
		}
		
		// Hook me in
		if (owner == null)
			((HoldsOptions)holder).addChildCategory(this);
		else if (owner instanceof Tool)
			((Tool)owner).addChildCategory(this);
		else
			((OptionCategory)owner).addChildCategory(this);
	}

	private IOptionCategory getNullOptionCategory() {
		// Handle difference between Tool and others by using
		// the fact that Tool implements IOptionCategory. If so,
		// the holder is in fact a parent category to this category.
		if (holder instanceof IOptionCategory) {
			return (IOptionCategory)holder;
		}
		return null;
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
		
		if (iconPathURL != null) {
			// Save as URL in string form
			element.setAttribute(IOptionCategory.ICON, iconPathURL.toString());
		}
		
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
		IHoldsOptions[] optionHolders = null;
		if (configuration != null) {
			IHoldsOptions optionHolder = getOptionHolder();
			if (optionHolder instanceof ITool) {
				optionHolders = configuration.getTools();
			} else if (optionHolder instanceof IToolChain) {
				// Get the toolchain of this configuration, which is
				// the holder equivalent for this option
				optionHolders = new IHoldsOptions[1];
				optionHolders[0] = configuration.getToolChain();
			}
			// TODO: if further option holders were to be added in future,
			// this function needs to be extended
		}
		return getOptions(optionHolders, FILTER_PROJECT);
	}

	public Object[][] getOptions(IResourceConfiguration resConfig) {
		IHoldsOptions[] optionHolders = null;
		if (resConfig != null) {
			IHoldsOptions optionHolder = getOptionHolder();
			if (optionHolder instanceof ITool) {
				optionHolders = resConfig.getTools();
			} else if (optionHolder instanceof IToolChain) {
				// Resource configurations do not support categories that are children 
				// of toolchains. The reason for this is that options in such categories 
				// are intended to be global. Thus return nothing.
				// TODO: Remove this restriction in future? 
				optionHolders = new IHoldsOptions[1];
				optionHolders[0] = null;
			}
			// TODO: if further option holders were to be added in future,
			// this function needs to be extended
		}
		return getOptions(optionHolders, FILTER_FILE);
	}
	
	private IHoldsOptions getOptionHoldersSuperClass(IHoldsOptions optionHolder) {
		if (optionHolder instanceof ITool) 
			return ((ITool)optionHolder).getSuperClass();
		else if (optionHolder instanceof IToolChain) 
			return ((IToolChain)optionHolder).getSuperClass();
		return null;
	}
	
	private Object[][] getOptions(IHoldsOptions[] optionHolders, int filterValue) {
		IHoldsOptions catHolder = getOptionHolder();
		IHoldsOptions optionHolder = null;

		if (optionHolders != null) {
			// Find the child of the configuration/resource configuration that represents the same tool.
			// It could the tool itself, or a "sub-class" of the tool.
			for (int i = 0; i < optionHolders.length; ++i) {
				IHoldsOptions current = optionHolders[i];
				do {
					if (catHolder == current) {
						optionHolder = optionHolders[i];
						break;
					}
				} while ((current = getOptionHoldersSuperClass(current)) != null);
				if (optionHolder != null) break;
			}
		}
		if (optionHolder == null) {
			optionHolder = catHolder;
		}
		
		// Get all of the tool's options and see which ones are part of
		// this category.
		IOption[] allOptions = optionHolder.getOptions();
		Object[][] myOptions = new Object[allOptions.length][2];
		int index = 0;
		for (int i = 0; i < allOptions.length; ++i) {
			IOption option = allOptions[i];
			if (option.getCategory().equals(this)) {
				
				// Check whether this option can be displayed for a specific resource type.
				if( (option.getResourceFilter() == FILTER_ALL) || (option.getResourceFilter() == filterValue) ) {
					myOptions[index] = new Object[2];
					myOptions[index][0] = optionHolder;
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
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getOptionHolder()
	 */
	public IHoldsOptions getOptionHolder() {
		// This will stop at the parent's top category
		if (owner != null)
			return owner.getOptionHolder();
		return holder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getTool()
	 */
	public ITool getTool() {
		// This will stop at the tool's top category
		IHoldsOptions parent = owner.getOptionHolder();
		if (parent instanceof ITool)
		{
			return (ITool)parent;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IOptionCategory#getIconPath()
	 */
	public URL getIconPath() {
		return iconPathURL;
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
		boolean error = false;
		if (!resolved) {
			resolved = true;
			if (ownerId != null) {
				owner = holder.getOptionCategory(ownerId);
				if (owner == null) {
					if (holder instanceof IOptionCategory) {
						// Report error, only if the parent is a tool and thus also
						// an option category. 
						ManagedBuildManager.OutputResolveError(
								"owner",	//$NON-NLS-1$
								ownerId,
								"optionCategory",	//$NON-NLS-1$
								getId());
						error = true;
					} else if ( false == holder.getId().equals(ownerId) ) {
						// Report error, if the holder ID does not match the owner's ID.
						ManagedBuildManager.OutputResolveError(								
								"owner",	//$NON-NLS-1$
								ownerId,
								"optionCategory",	//$NON-NLS-1$
								getId());
						error = true;
					}
				}
			}
			if (owner == null) {
				owner = getNullOptionCategory();
			}
			
			// Hook me in
			if (owner == null  &&  error == false)
				((HoldsOptions)holder).addChildCategory(this);
			else if (owner instanceof Tool)
				((Tool)owner).addChildCategory(this);
			else
				((OptionCategory)owner).addChildCategory(this);
		}
	}

	/**
	 * @return Returns the managedBuildRevision.
	 */
	public String getManagedBuildRevision() {
		if ( managedBuildRevision == null) {
			if ( getOptionHolder() != null) {
				return getOptionHolder().getManagedBuildRevision();
			}
		}
		return managedBuildRevision;
	}

	/**
	 * @return Returns the version.
	 */
	public PluginVersionIdentifier getVersion() {
		if ( version == null) {
			if ( getOptionHolder() != null) {
				return getOptionHolder().getVersion();
			}
		}
		return version;
	}
	
	public void setVersion(PluginVersionIdentifier version) {
		// Do nothing
	}

}
