/*******************************************************************************
 * Copyright (c) 2005, 2010 Symbian Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Symbian Ltd - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

/**
 * Implements the functionality that is needed to hold options and option
 * categories. The functionality has been moved from ITool to here in CDT 3.0.
 * Backwards compatibility of interfaces has been maintained because ITool 
 * extends IHoldOptions.
 * 
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IHoldsOptions extends IBuildObject {

	public static final String OPTION = "option"; //$NON-NLS-1$
	public static final String OPTION_CAT = "optionCategory"; //$NON-NLS-1$
	public static final String OPTION_REF = "optionReference"; //$NON-NLS-1$
	/*
	 *  M E T H O D S   M O V E D   F R O M   I T O O L   I N   3 . 0
	 */

	/**
	 * Creates a child Option
	 * 
	 * @param superClass The superClass, if any
	 * @param Id The id for the new option 
	 * @param name The name for the new option
	 * @param isExtensionElement Indicates whether this is an extension element or a managed project element
	 * 
	 * @return IOption
	 */
	public IOption createOption(IOption superClass, String Id, String name, boolean isExtensionElement);

	/**
	 * Removes an option.
	 */
	public void removeOption(IOption option);

	/**
	 * This is a deprecated method for retrieving an <code>IOption</code> from 
	 * the receiver based on an ID. It is preferred that you use the newer method 
	 * <code>getOptionById</code>
	 * @see IHoldsOptions#getOptionById(java.lang.String)
	 *  
	 * @param id unique identifier of the option to search for
	 * @return <code>IOption</code>
	 * @deprecated use getOptionById() instead
	 */
	@Deprecated
	public IOption getOption(String id);

	/**
	 * Get the <code>IOption</code> in the receiver with the specified 
	 * ID. This is an efficient search in the receiver.
	 * 
	 * <p>If the receiver does not have an option with that ID, the method 
	 * returns <code>null</code>. It is the responsibility of the caller to 
	 * verify the return value.  
	 * 
	 * @param id unique identifier of the option to search for
	 * @return <code>IOption</code>
	 * @since 2.0
	 */
	public IOption getOptionById(String id);

	/**
	 * Get the <code>IOption</code> in the receiver with the specified 
	 * ID, or an option with a superclass with this id. 
	 * 
	 * <p>If the receiver does not have an option with that ID, the method 
	 * returns <code>null</code>. It is the responsibility of the caller to 
	 * verify the return value.  
	 * 
	 * @param id unique identifier of the option to search for
	 * @return <code>IOption</code>
	 * @since 3.0
	 */
	public IOption getOptionBySuperClassId(String id);

	/**
	 * Returns the complete list of options that are available for this object.
	 * The list is a merging of the options specified for this object with the 
	 * options of its superclasses. The lowest option instance in the hierarchy
	 * takes precedence.  
	 * 
	 * @return IOption[]
	 */
	public IOption[] getOptions();

	/**
	 * Returns the option category children of this tool.
	 * 
	 * @return IOptionCategory[]
	 */
	public IOptionCategory[] getChildCategories();

	/*
	 *  M E T H O D S   M O V E D   F R O M   T O O L   I N   3 . 0
	 */

	/**
	 * Adds the <code>IOptionCategory</code> to this Option Holder's 
	 * list of Option Categories. 
	 * 
	 * @param category  The option category to be added
	 */
	public void addOptionCategory(IOptionCategory category);

	/*
	 *  N E W   M E T H O D S   A D D E D   I N   3 . 0
	 */

	/**
	 * Answers the <code>IOptionCategory</code> that has the unique identifier 
	 * specified in the argument. 
	 * 
	 * @param id The unique identifier of the option category
	 * @return <code>IOptionCategory</code> with the id specified in the argument
	 * @since 3.0
	 */
	public IOptionCategory getOptionCategory(String id);

	/**
	 * Creates options from the superclass and adds it to this class.
	 * Each individual option in superclass, will become the superclass for
	 * the new option. 
	 *
	 * @param superClass The superClass 
	 * @since 3.0
	 */
	public void createOptions(IHoldsOptions superClass);

	/**
	
	* This method should be called in order to obtain the option whose value and attributes could be directly changed/adjusted
	* 
	* @param option -the option to be modified
	* @param adjustExtension - if false, modifications are to be made for the non-extension element 
	* (only for some particular configuration associated with some eclipse project)
	* This is the most common use of this method.
	* 
	* True is allowed only while while handling the LOAD value handler event.
	* In this case modifications are to be made for the extension element. 
	* This could be used for adjusting extension options 
	* Note: changing this option will affect all non-extension configurations using this option!
	*/
	IOption getOptionToSet(IOption option, boolean adjustExtension) throws BuildException;

	/**
	 * specifies whether the option holder is modified and needs rebuild
	 * 
	 * @return boolean
	 */
	public boolean needsRebuild();

	/**
	 * sets the holder rebuild state
	 */
	public void setRebuildState(boolean rebuild);
}
