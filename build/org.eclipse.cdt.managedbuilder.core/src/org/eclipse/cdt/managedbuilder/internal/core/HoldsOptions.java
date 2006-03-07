/*******************************************************************************
 * Copyright (c) 2005 Symbian Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian Ltd - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implements the functionality that is needed to hold options and option
 * categories. In CDT 3.0, the functionality has been moved from ITool and 
 * Tool to this class.
 * 
 * This class is intended to be used as base class for all MBS grammar
 * elements that can hold Options and Option Categories. These are currently
 * Tool and ToolChain. 
 * 
 * Note that the member <code>superClass</code> must be shared with the 
 * derived class. This requires to wrap this member by access functions 
 * in the derived class or frequent casts, because the type of <code>superClass</code>
 * in <code>HoldsOptions</code> must be <code>IHoldOptions</code>. Further 
 * note that the member <code>resolved</code> must inherit the value of its
 * derived class. This achieved through the constructor.
 * 
 * @since 3.0
 */
public class HoldsOptions extends BuildObject implements IHoldsOptions {

	private static final IOptionCategory[] EMPTY_CATEGORIES = new IOptionCategory[0];

	//  Members that are to be shared with the derived class
	protected IHoldsOptions superClass;
	//  Members that must have the same values on creation as the derived class
	private boolean resolved;
	//  Parent and children
	private Vector categoryIds;
	private Map categoryMap;
	private List childOptionCategories;
	private Vector optionList;
	private Map optionMap;
	//  Miscellaneous
	private boolean isDirty = false;
	
	/*
	 *  C O N S T R U C T O R S
	 */
	
	private HoldsOptions() {
		// prevent accidental construction of class without setting up
		// resolved
	}
	
	protected HoldsOptions(boolean resolved) {
		this.resolved = resolved;
	}
	
	/**
	 * Copies children of <code>HoldsOptions</code>. Helper function for
	 * derived constructors.
	 * 
	 * @param source The children of the source will be cloned and added 
	 *               to the class itself.
	 */
	protected void copyChildren(HoldsOptions source) {

		//  Note: This function ignores OptionCategories since they should not be
		//        found on an non-extension tools
		if (source.optionList != null) {
			Iterator iter = source.getOptionList().listIterator();
			while (iter.hasNext()) {
				Option option = (Option) iter.next();
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId;
				String subName;
				if (option.getSuperClass() != null) {
					subId = option.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
					subName = option.getSuperClass().getName();
				} else {
					subId = option.getId() + "." + nnn;		//$NON-NLS-1$
					subName = option.getName();
				}
				Option newOption = new Option(this, subId, subName, option);
				addOption(newOption);
			}
		}		
	}
	
	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */

	/**
	 * Load child element from XML element if it is of the correct type
	 * 
	 * @param element which is loaded as child only iff it is of the correct type
	 * @return true when a child has been loaded, false otherwise 
	 */
	protected boolean loadChild(Node element) {
		if (element.getNodeName().equals(ITool.OPTION)) {
			Option option = new Option(this, (Element)element);
			addOption(option);
			return true;
		} else if (element.getNodeName().equals(ITool.OPTION_CAT)) {
			new OptionCategory(this, (Element)element);
			return true;
		}
		return false;
	}

	/**
	 * Load child element from configuration element if it is of the correct type
	 * 
	 * @param element which is loaded as child only iff it is of the correct type
	 * @return true when a child has been loaded, false otherwise 
	 */
	protected boolean loadChild(IManagedConfigElement element) {
		if (element.getName().equals(ITool.OPTION)) {
			Option option = new Option(this, element);
			addOption(option);
			return true;
		} else if (element.getName().equals(ITool.OPTION_CAT)) {
			new OptionCategory(this, element);
			return true;
		}
		return false;
	}
	
	/**
	 * Persist the tool to the project file. Intended to be called by derived
	 * class only, thus do not handle exceptions.
	 * 
	 * @param doc
	 * @param element
	 * @throws BuildException 
	 */
	protected void serialize(Document doc, Element element) throws BuildException {
		
		Iterator iter;
		
		if (childOptionCategories != null) {
			iter = childOptionCategories.listIterator();
			while (iter.hasNext()) {
				OptionCategory optCat = (OptionCategory)iter.next();
				Element optCatElement = doc.createElement(OPTION);
				element.appendChild(optCatElement);
				optCat.serialize(doc, optCatElement);
			}
		}
		
		List optionElements = getOptionList();
		iter = optionElements.listIterator();
		while (iter.hasNext()) {
			Option option = (Option) iter.next();
			Element optionElement = doc.createElement(OPTION);
			element.appendChild(optionElement);
			option.serialize(doc, optionElement);
		}
}
	
	/*
	 *  M E T H O D S   M O V E D   F R O M   I T O O L   I N   3 . 0
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#createOption(IOption, String, String, boolean)
	 */
	public IOption createOption(IOption superClass, String Id, String name, boolean isExtensionElement) {
		Option option = new Option(this, superClass, Id, name, isExtensionElement);
		addOption(option);
		if(!isExtensionElement)
			setDirty(true);
		return option;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#createOptions(IHoldsOptions)
	 */
	public void createOptions(IHoldsOptions superClass) {
		Iterator iter = ((HoldsOptions)superClass).getOptionList().listIterator();
		while (iter.hasNext()) {
			Option optionChild = (Option) iter.next();
			int nnn = ManagedBuildManager.getRandomNumber();
			String subId = optionChild.getId() + "." + nnn;		//$NON-NLS-1$
			createOption(optionChild, subId, optionChild.getName(), false);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#removeOption(IOption)
	 */
	public void removeOption(IOption option) {
		getOptionList().remove(option);
		getOptionMap().remove(option.getId());
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#getOptions()
	 */
	public IOption[] getOptions() {
		IOption[] options = null;
		// Merge our options with our superclass' options.
		if (superClass != null) {
			options = superClass.getOptions();
		}
		// Our options take precedence.
		Vector ourOpts = getOptionList();
		if (options != null) {
			for (int i = 0; i < ourOpts.size(); i++) {
				int j = options.length;
				IOption ourOpt = (IOption)ourOpts.get(i);
				if (ourOpt.getSuperClass() != null) {
					String matchId = ourOpt.getSuperClass().getId();
					search:
						for (j = 0; j < options.length; j++) {
							IOption superHolderOption = options[j];
							if (((Option)superHolderOption).wasOptRef()) {
								superHolderOption = superHolderOption.getSuperClass();
							}
							while (superHolderOption != null) {
								if (matchId.equals(superHolderOption.getId())) {
									options[j] = ourOpt;
									break search;
								}
								superHolderOption = superHolderOption.getSuperClass();
							}
						}
				}
				//  No Match?  Add it.
				if (j == options.length) {
					IOption[] newOptions = new IOption[options.length + 1];
					for (int k = 0; k < options.length; k++) {
						newOptions[k] = options[k];
					}						 
					newOptions[j] = ourOpt;
					options = newOptions;
				}
			}
  		} else {
			options = (IOption[])ourOpts.toArray(new IOption[ourOpts.size()]);
		}
		// Check for any invalid options.
		int numInvalidOptions = 0;
		int i;
		for (i=0; i < options.length; i++) {
			if (options[i].isValid() == false) {
				numInvalidOptions++;
			}
		}
		// Take invalid options out of the array, if there are any
		if (numInvalidOptions > 0) {
			int j = 0;
			IOption[] newOptions = new IOption[options.length - numInvalidOptions];
			for (i=0; i < options.length; i++) {
				if (options[i].isValid() == true) {
					newOptions[j] = options[i];
					j++;
				}
			}		
			options = newOptions;
		}
		return options;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#getOption(java.lang.String)
	 */
	public IOption getOption(String id) {
		return getOptionById(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#getOptionById(java.lang.String)
	 */
	public IOption getOptionById(String id) {
		IOption opt = (IOption)getOptionMap().get(id);
		if (opt == null) {
			if (superClass != null) {
				return superClass.getOptionById(id);
			}
		}
		if (opt == null) return null;
		return opt.isValid() ? opt : null;
	}

	/* (non-Javadoc)
	 * org.eclipse.cdt.managedbuilder.core.IHoldsOptions#getOptionBySuperClassId(java.lang.String)
	 */
	public IOption getOptionBySuperClassId(String optionId) {
		if (optionId == null) return null;
		
		//  Look for an option with this ID, or an option with a superclass with this id
		IOption[] options = getOptions();
		for (int i = 0; i < options.length; i++) {
			IOption targetOption = options[i];
			IOption option = targetOption;
			do {
				if (optionId.equals(option.getId())) {
					return targetOption.isValid() ? targetOption : null;
				}		
				option = option.getSuperClass();
			} while (option != null);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#getChildCategories()
	 */
	public IOptionCategory[] getChildCategories() {
		IOptionCategory[] superCats = EMPTY_CATEGORIES;
		IOptionCategory[] ourCats = EMPTY_CATEGORIES;
		// Merge our option categories with our superclass' option categories.
		// Note that these are two disjoint sets of categories because
		// categories do not use derivation AND object Id's are unique. Thus
		// they are merely sequentially added.
		if (superClass != null) {
			superCats = superClass.getChildCategories();
		}
		if ( childOptionCategories != null ) {
			ourCats = (IOptionCategory[])childOptionCategories.toArray(new IOptionCategory[childOptionCategories.size()]);
		}
		// Add the two arrays together;
		if (superCats.length > 0  ||  ourCats.length > 0) {
			IOptionCategory[] allCats = new IOptionCategory[superCats.length + ourCats.length];
			int j;
			for (j=0; j < superCats.length; j++)
				allCats[j] = superCats[j];
			for (j=0; j < ourCats.length; j++)
				allCats[j+superCats.length] = ourCats[j];
			return allCats;
		}
		// Nothing found, return EMPTY_CATEGORIES
		return EMPTY_CATEGORIES;
	}
	
	/*
	 *  M E T H O D S   M O V E D   F R O M   T O O L   I N   3 . 0
	 */

	/* (non-Javadoc)
	 * Memory-safe way to access the vector of category IDs
	 */
	private Vector getCategoryIds() {
		if (categoryIds == null) {
			categoryIds = new Vector();
		}
		return categoryIds;
	}
	
	/**
	 * @param category
	 */
	public void addChildCategory(IOptionCategory category) {
		if (childOptionCategories == null)
			childOptionCategories = new ArrayList();
		childOptionCategories.add(category);
	}
	
	/**
	 * @param option
	 */
	public void addOption(Option option) {
		getOptionList().add(option);
		getOptionMap().put(option.getId(), option);
	}
	/* (non-Javadoc)
	 * Memeory-safe way to access the map of category IDs to categories
	 */
	private Map getCategoryMap() {
		if (categoryMap == null) {
			categoryMap = new HashMap();
		}
		return categoryMap;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of options
	 */
	private Vector getOptionList() {
		if (optionList == null) {
			optionList = new Vector();
		}
		return optionList;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of IDs to options
	 */
	private Map getOptionMap() {
		if (optionMap == null) {
			optionMap = new HashMap();
		}
		return optionMap;
	}

	/* (non-Javadoc)
	 * org.eclipse.cdt.managedbuilder.core.IHoldsOptions#addOptionCategory()
	 */
	public void addOptionCategory(IOptionCategory category) {
		// To preserve the order of the categories, record the ids in the order they are read
		getCategoryIds().add(category.getId());
		// Map the categories by ID for resolution later
		getCategoryMap().put(category.getId(), category);
	}

	/* (non-Javadoc)
	 * org.eclipse.cdt.managedbuilder.core.IHoldsOptions#getOptionCategory()
	 */
	public IOptionCategory getOptionCategory(String id) {
		IOptionCategory cat = (IOptionCategory)getCategoryMap().get(id);
		if (cat == null  &&  superClass != null) {
			// Look up the holders superclasses to find the category
			return superClass.getOptionCategory(id);
		}
		return cat;
	}	
	
	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
		
	/* (non-Javadoc)
	 * Implements isDirty() for children of HoldsOptions. Intended to be
	 * called by derived class.
	 */
	protected boolean isDirty() {
		// If I need saving, just say yes
		if (isDirty) return true;
		
		// Otherwise see if any options need saving
		List optionElements = getOptionList();
		Iterator iter = optionElements.listIterator();
		while (iter.hasNext()) {
			Option option = (Option) iter.next();
			if (option.isDirty()) return true;
		}
		
		return isDirty;
	}

	/* (non-Javadoc)
	 * Implements setDirty() for children of HoldsOptions. Intended to be
	 * called by derived class.
	 */
	protected void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		// Propagate "false" to the children
		if (!isDirty) {
			List optionElements = getOptionList();
			Iterator iter = optionElements.listIterator();
			while (iter.hasNext()) {
				Option option = (Option) iter.next();
				if(!option.isExtensionElement())
					option.setDirty(false);
			}
		}
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references. Intended to be
	 *  called by derived class.
	 */
	protected void resolveReferences() {
		if (!resolved) {
			resolved = true;
			//  Call resolveReferences on our children
			Iterator optionIter = getOptionList().iterator();
			while (optionIter.hasNext()) {
				Option current = (Option)optionIter.next();
				current.resolveReferences();
			}
			// Somewhat wasteful, but use the vector to retrieve the categories in proper order
			Iterator catIter = getCategoryIds().iterator();
			while (catIter.hasNext()) {
				String id = (String)catIter.next();
				IOptionCategory current = (IOptionCategory)getCategoryMap().get(id);
				if (current instanceof Tool) {
					((Tool)current).resolveReferences();
				} else if (current instanceof ToolChain) {
					((ToolChain)current).resolveReferences();
				} else if (current instanceof OptionCategory) {
  					((OptionCategory)current).resolveReferences();
				}
			}
		}		
	}
	
	public IOption getOptionToSet(IOption option, boolean adjustExtension) throws BuildException{
		IOption setOption = null;
		if(option.getOptionHolder() != this)
			option = getOptionBySuperClassId(option.getId());
		
		if(adjustExtension){
			for(; option != null && !option.isExtensionElement(); option=option.getSuperClass()){}
			
			if(option != null){
				IHoldsOptions holder = option.getOptionHolder();
				if(holder == this)
					setOption = option;
				else {
					IOption newSuperClass = option;
					if (((Option)option).wasOptRef()) {
						newSuperClass = option.getSuperClass();
					}
					//  Create a new extension Option element
					String subId;
					String version = ManagedBuildManager.getVersionFromIdAndVersion(newSuperClass.getId());
					String baseId = ManagedBuildManager.getIdFromIdAndVersion(newSuperClass.getId());
					if ( version != null) {
						subId = baseId + ".adjusted." + new Integer(ManagedBuildManager.getRandomNumber()) + "_" + version; //$NON-NLS-1$ //$NON-NLS-2$ 
					} else {
						subId = baseId + ".adjusted." + new Integer(ManagedBuildManager.getRandomNumber()); //$NON-NLS-1$
					}
					setOption = createOption(newSuperClass, subId, null, true);
					((Option)setOption).setAdjusted(true);
					setOption.setValueType(option.getValueType());
				}
			}
		} else {
			if(option.getOptionHolder() == this && !option.isExtensionElement()){
				setOption = option;
			} else {
				IOption newSuperClass = option;
				for(;
					newSuperClass != null && !newSuperClass.isExtensionElement();
						newSuperClass = newSuperClass.getSuperClass()){}
				
				if (((Option)newSuperClass).wasOptRef()) {
					newSuperClass = newSuperClass.getSuperClass();
				}
				
				if(((Option)newSuperClass).isAdjustedExtension()){
					newSuperClass = newSuperClass.getSuperClass();
				}
				//  Create an Option element for the managed build project file (.CDTBUILD)
				String subId;
				subId = ManagedBuildManager.calculateChildId(newSuperClass.getId(), null);
				setOption = createOption(newSuperClass, subId, null, false);
				setOption.setValueType(option.getValueType());
			}
		}
		return setOption;
	}
}
