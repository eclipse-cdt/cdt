/*******************************************************************************
 * Copyright (c) 2005, 2010 Symbian Ltd and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildPropertiesRestriction;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

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
public abstract class HoldsOptions extends BuildObject implements IHoldsOptions, IBuildPropertiesRestriction, IBuildPropertyChangeListener {

	private static final IOptionCategory[] EMPTY_CATEGORIES = new IOptionCategory[0];

	//  Members that are to be shared with the derived class
	protected IHoldsOptions superClass;
	//  Members that must have the same values on creation as the derived class
	private boolean resolved;
	//  Parent and children
	private Vector<String> categoryIds;
	private Map<String, IOptionCategory> categoryMap;
	private List<IOptionCategory> childOptionCategories;
	private Map<String, Option> optionMap;
	//  Miscellaneous
	private boolean isDirty = false;
	private boolean rebuildState;
	
	/*
	 *  C O N S T R U C T O R S
	 */
	
	@SuppressWarnings("unused")
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
		
		boolean copyIds = id.equals(source.id);
		if (source.optionMap != null) {
			for (Option option : source.getOptionCollection()) {
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId;
				String subName;
				if (option.getSuperClass() != null) {
					subId = copyIds ? option.getId() : option.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
					subName = option.getSuperClass().getName();
				} else {
					subId = copyIds ? option.getId() : option.getId() + "." + nnn;		//$NON-NLS-1$
					subName = option.getName();
				}
				Option newOption = new Option(this, subId, subName, option);
				addOption(newOption);
			}
		}
		
		if(copyIds){
			isDirty = source.isDirty;
			rebuildState = source.rebuildState;
		}
	}
	
	void copyNonoverriddenSettings(HoldsOptions ho){
		if (ho.optionMap == null || ho.optionMap.size() == 0)
			return;
		
		IOption options[] = getOptions();
		for(int i = 0; i < options.length; i++){
			if(!options[i].getParent().equals(ho))
				continue;
			
			Option option = (Option)options[i];
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
	
	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */

	/**
	 * Load child element from XML element if it is of the correct type
	 * 
	 * @param element which is loaded as child only iff it is of the correct type
	 * @return true when a child has been loaded, false otherwise 
	 */
	protected boolean loadChild(ICStorageElement element) {
		if (element.getName().equals(IHoldsOptions.OPTION)) {
			Option option = new Option(this, element);
			addOption(option);
			return true;
		} else if (element.getName().equals(IHoldsOptions.OPTION_CAT)) {
			new OptionCategory(this, element);
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
		if (element.getName().equals(IHoldsOptions.OPTION)) {
			Option option = new Option(this, element);
			addOption(option);
			return true;
		} else if (element.getName().equals(IHoldsOptions.OPTION_CAT)) {
			new OptionCategory(this, element);
			return true;
		}
		return false;
	}
	
	/**
	 * Persist the tool to the XML storage element. Intended to be called by derived
	 * class only, thus do not handle exceptions.
	 * 
	 * @param element where to serialize the tool
	 * @throws BuildException 
	 */
	protected void serialize(ICStorageElement element) throws BuildException {
		if (childOptionCategories != null) {
			for (IOptionCategory optCat : childOptionCategories) {
				ICStorageElement optCatElement = element.createChild(OPTION);
				((OptionCategory)optCat).serialize(optCatElement);
			}
		}
		
		Collection<Option> optionElements = getOptionCollection();
		for (Option option : optionElements) {
			ICStorageElement optionElement = element.createChild(OPTION);
			option.serialize(optionElement);
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
		if(!isExtensionElement){
			setDirty(true);
			setRebuildState(true);
		}
		return option;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#createOptions(IHoldsOptions)
	 */
	public void createOptions(IHoldsOptions superClass) {
		for (Option optionChild : ((HoldsOptions)superClass).getOptionCollection()) {
			int nnn = ManagedBuildManager.getRandomNumber();
			String subId = optionChild.getId() + "." + nnn;		//$NON-NLS-1$
			createOption(optionChild, subId, optionChild.getName(), false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#removeOption(IOption)
	 */
	public void removeOption(IOption option) {
		if(option.getParent() != this)
			return;
//			throw new IllegalArgumentException();
		
		getOptionMap().remove(option.getId());
		setDirty(true);
		setRebuildState(true);
		
		if(!isExtensionElement()){
			NotificationManager.getInstance().optionRemoved(getParentResourceInfo(), this, option);
		}
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
		Collection<Option> ourOpts = getOptionCollection();
		if (options != null) {
			for (Option ourOpt : ourOpts) {
				int j = options.length;
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
			options = ourOpts.toArray(new IOption[ourOpts.size()]);
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
		IOption opt = getOptionMap().get(id);
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
		for (IOption targetOption : options) {
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
			ourCats = childOptionCategories.toArray(new IOptionCategory[childOptionCategories.size()]);
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
	private Vector<String> getCategoryIds() {
		if (categoryIds == null) {
			categoryIds = new Vector<String>();
		}
		return categoryIds;
	}
	
	/**
	 * @param category
	 */
	public void addChildCategory(IOptionCategory category) {
		if (childOptionCategories == null)
			childOptionCategories = new ArrayList<IOptionCategory>();
		childOptionCategories.add(category);
	}
	
	/**
	 * @param option
	 */
	public void addOption(Option option) {
		getOptionMap().put(option.getId(), option);
	}
	/* (non-Javadoc)
	 * Memeory-safe way to access the map of category IDs to categories
	 */
	private Map<String, IOptionCategory> getCategoryMap() {
		if (categoryMap == null) {
			categoryMap = new HashMap<String, IOptionCategory>();
		}
		return categoryMap;
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of options
	 */
	private Collection<Option> getOptionCollection() {
		// no need to store all the options twice, get them out of the map
		if(optionMap != null)
			return optionMap.values();
		else return Collections.emptyList();
	}
	
	/* (non-Javadoc)
	 * Memory-safe way to access the list of IDs to options
	 */
	private Map<String, Option> getOptionMap() {
		if (optionMap == null) {
			optionMap = new LinkedHashMap<String, Option>();
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
		IOptionCategory cat = getCategoryMap().get(id);
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
		if (isDirty) 
			return true;
		
		for (Option option : getOptionCollection())
			if (option.isDirty()) 
				return true;
		
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
			for (Option option : getOptionCollection())
				if(!option.isExtensionElement())
					option.setDirty(false);
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
			for (Option current : getOptionCollection()) {
				current.resolveReferences();
			}
			// Somewhat wasteful, but use the vector to retrieve the categories in proper order
			for (String id : getCategoryIds()) {
				IOptionCategory current = getCategoryMap().get(id);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#getOptionToSet(org.eclipse.cdt.managedbuilder.core.IOption, boolean)
	 */
	public IOption getOptionToSet(IOption option, boolean adjustExtension) throws BuildException{
		IOption setOption = null;
		// start changes
		if(option.getOptionHolder() != this) {
			// option = getOptionBySuperClassId(option.getId());
			IOption op = getOptionBySuperClassId(option.getId());
			if (op == null && option.getSuperClass() != null) {
				op = getOptionBySuperClassId(option.getSuperClass().getId());
				if (op == null) {
					ManagedBuilderCorePlugin.log(
						new Status(
							IStatus.ERROR, 
							ManagedBuilderCorePlugin.getUniqueIdentifier(), 
							IStatus.OK, 
							"Cannot get OptionToSet for option " +  //$NON-NLS-1$
								option.getId() + " @ holder " +  //$NON-NLS-1$
								option.getOptionHolder().getId() + "\nI'm holder " + //$NON-NLS-1$
								getId(),
							null)							
					);
				} else
					option = op;
			} else
				option = op;
		}
		// end changes
		
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#needsRebuild()
	 */
	public boolean needsRebuild() {
		if(rebuildState)
			return true;
		
		// Otherwise see if any options need saving
		for (Option option : getOptionCollection())
			if (option.needsRebuild()) 
				return true;

		// Bug 318331 If the parent needs a rebuild, then we do too as we may inherit options from our superClass...
		if (superClass != null && superClass.needsRebuild())
			return true;

		return rebuildState;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IHoldsOptions#setRebuildState(boolean)
	 */
	public void setRebuildState(boolean rebuild) {
		rebuildState = rebuild;
		
		// Propagate "false" to the children
		if (!rebuildState)
			for (Option option : getOptionCollection())
				if(!option.isExtensionElement())
					option.setRebuildState(false);
	}

	public void propertiesChanged() {
		if(isExtensionElement())
			return;
		adjustOptions(false);
	}
	
	public void adjustOptions(boolean extensions){
		IOption options[] = getOptions();
		
		for (IOption opt : options) {
			if (opt.isExtensionElement()) {
				Option option = (Option)opt;
				BooleanExpressionApplicabilityCalculator calc = 
					option.getBooleanExpressionCalculator(extensions);
					
				if(calc != null)
					calc.adjustOption(getParentResourceInfo(),this,option, extensions);
			}
		}
	}

	public boolean supportsType(String type) {
		IOption options[] = getOptions();
		boolean supports = false;
		for (IOption opt : options) {
			Option option = (Option)opt;
			if(option.supportsType(type)){
				supports = true;
				break;
			}
		}
		return supports;
	}

	public boolean supportsType(IBuildPropertyType type) {
		return supportsType(type.getId());
	}

	public boolean supportsValue(String type, String value){
		IOption options[] = getOptions();
		boolean supports = false;
		for (IOption opt : options) {
			Option option = (Option)opt;
			if(option.supportsValue(type, value)){
				supports = true;
				break;
			}
		}
		return supports;
	}

	public boolean supportsValue(IBuildPropertyType type,
			IBuildPropertyValue value) {
		return supportsValue(type.getId(), value.getId());
	}
	
	public abstract boolean isExtensionElement(); 
	
	protected abstract IResourceInfo getParentResourceInfo();

	public String[] getRequiredTypeIds() {
		List<String> list = new ArrayList<String>();
		for(IOption op : getOptions())
			list.addAll(Arrays.asList(((Option)op).getRequiredTypeIds()));
		return list.toArray(new String[list.size()]);
	}

	public String[] getSupportedTypeIds() {
		List<String> list = new ArrayList<String>();
		for(IOption op : getOptions())
			list.addAll(Arrays.asList(((Option)op).getSupportedTypeIds()));
		return list.toArray(new String[list.size()]);
	}

	public String[] getSupportedValueIds(String typeId) {
		List<String> list = new ArrayList<String>();
		for(IOption op : getOptions())
			list.addAll(Arrays.asList(((Option)op).getSupportedValueIds(typeId)));
		return list.toArray(new String[list.size()]);
	}

	public boolean requiresType(String typeId) {
		IOption options[] = getOptions();
		boolean requires = false;
		for (IOption opt : options) {
			Option option = (Option)opt;
			if(option.requiresType(typeId)){
				requires = true;
				break;
			}
		}
		return requires;
	}
	
	boolean hasCustomSettings(){
		if(superClass == null)
			return true;
		
		if(optionMap != null && optionMap.size() != 0){
			for(Option option : getOptionCollection())
				if(option.hasCustomSettings())
					return true;
		}
		
		return false;
	}
}
