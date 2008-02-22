/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [180562][api] dont implement ISystemCompileXMLConstants
 * Martin Oberhuber (Wind River) - [219975] Fix implementations of clone()
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.compile;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;

/**
 * A compile command is represents a command that can be run on a remote system for the 
 *  explicit purpose of compiling selected source members.
 * <p>
 * The attributes of a compile command include:
 * <ul>
 *   <li>Nature. Either IBM-supplied or user defined. See {@link #setNature(String)} and {@link #getNature()}
 *   <li>Label. The visual name the user sees, representing this compile command. See {@link #setLabel(String)} and {@link #getLabel()}
 *   <li>DefaultString. The default IBM-supplied compile command (with parms) for support of restore defaults. See {@link #setDefaultString(String)} and {@link #getDefaultString()}
 *   <li>CurrentString. The current potentially user-edited compile command (with parms) for support of restore defaults. 
 *       See {@link #setCurrentString(String)} and {@link #getCurrentString()}
 *   <li>MenuOption. Identifies what compile popup menu this command appears in, if any. 
 *       See {@link #setMenuOption(String)} and {@link #getMenuOption()} and {@link #isPromptable()} and {@link #isNonPromptable()}.
 *   <li>Option. This compile commands position within the list of compile commands for a given compile type.
 *       See {@link #setOrder(int)} and {@link #getOrder()}
 *   <li>JobEnvironment. This is available for subsystems that need it, and they decide what to put in this attribute.
 * </ul>
 */
public class SystemCompileCommand implements Cloneable, IAdaptable {
	private SystemCompileType parentType; // reference to parent type
	private String nature;
	private String id;
	private String label;
	private String defaultString;
	private String currentString;
	private String menuOption;
	private String jobEnv;
	private int order;
	private boolean isLabelEditable = true;
	private static final String ID_IBM_PREFIX = "com.ibm"; //$NON-NLS-1$
	private static final String ID_USER_PREFIX = "user"; //$NON-NLS-1$

	/**
	 * Constructor for SystemCompileCommand
	 */
	public SystemCompileCommand(SystemCompileType parentType) {
		super();
		setParentType(parentType);
		setMenuOptionBoth();
	}

	/**
	 * Constructor for SystemCompileCommand. Id and label must be a unique value.
	 */
	public SystemCompileCommand(SystemCompileType parentType, String id, String label, String nature, String defaultString, String currentString, String menuOption, int order) {
		super();
		setParentType(parentType);
		setId(id);
		setLabel(label);
		setNature(nature);
		setDefaultString(defaultString);
		setCurrentString(currentString);
		setMenuOption(menuOption);
		setOrder(order);
		// if the given id is null, then try to configure it automatically.
		// This is only good for IBM and user supplied commands.
		// We assume ISV supplied commands have unique ids. 
		if (id == null) {
			configureId();
		}
	}

	/**
	 * Sets the parent type
	 * @param parentType the parent type
	 */
	public void setParentType(SystemCompileType parentType) {
		this.parentType = parentType;
	}

	/**
	 * Get the parent type
	 * @return the parent type
	 */
	public SystemCompileType getParentType() {
		return parentType;
	}

	/**
	 * Set the id. This is the unique id of the compile command.
	 * @param identifier the id
	 */
	public void setId(String identifier) {
		this.id = identifier;
	}

	/**
	 * Get the id.
	 * @return the unique id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id automatically. This only works for IBM supplied or user supplied commands. It does
	 * not work with ISV supplied commands. ISVs should set their own unique id.
	 */
	private void configureId() {
		if (nature != null && label != null) {
			if (isIBMSupplied()) {
				setId(ID_IBM_PREFIX + "." + label); //$NON-NLS-1$
			} else if (isUserSupplied()) {
				setId(ID_USER_PREFIX + "." + label); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Set the label. This is the visual name the user sees in the compile command list.
	 * @param name the label
	 */
	public void setLabel(String name) {
		this.label = name;
		configureId(); // Id may change as a result
	}

	/**
	 * Get the label.  This is the visual name the user sees in the compile command list.
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the nature: either IBM-supplied or user defined.
	 * @param nature Typically one of {#link ISystemCompileXMLConstants#NATURE_IBM_VALUE} or {#link ISystemCompileXMLConstants#NATURE_USER_VALUE}
	 * @see #setIsIBMSupplied()
	 * @see #setIsUserSupplied()
	 * @see #setIsISVSupplied()
	 */
	public void setNature(String nature) {
		this.nature = nature;
		configureId(); // Id may change as a result
		// For IBM and User supplied commands, we set editability parameters. We do this here because it's convenient.
		// During a copy and paste of an IBM supplied command, nature of newly created command is user supplied,
		// and so label becomes editable.
		if (isIBMSupplied()) {
			setLabelEditable(false);
			setCommandStringEditable(true);
		} else if (isUserSupplied()) {
			setLabelEditable(true);
			setCommandStringEditable(true);
		}
	}

	/**
	 * Indicate this is IBM supplied. This sets the nature to {#link ISystemCompileXMLConstants#NATURE_IBM_VALUE}
	 */
	public void setIsIBMSupplied() {
		setNature(ISystemCompileXMLConstants.NATURE_IBM_VALUE);
	}

	/**
	 * Indicate this is user supplied. This sets the nature to {#link ISystemCompileXMLConstants#NATURE_USER_VALUE}
	 */
	public void setIsUserSupplied() {
		setNature(ISystemCompileXMLConstants.NATURE_USER_VALUE);
	}

	/**
	 * Indicate this is ISV supplied. This sets the nature to {#link ISystemCompileXMLConstants#NATURE_ISV_VALUE}
	 */
	public void setIsISVSupplied() {
		setNature(ISystemCompileXMLConstants.NATURE_ISV_VALUE);
	}

	/**
	 * Get the nature: either IBM-supplied or user defined.
	 * @return the nature. One of {#link ISystemCompileXMLConstants#NATURE_IBM_VALUE} or {#link ISystemCompileXMLConstants#NATURE_USER_VALUE}
	 * @see #isIBMSupplied()
	 * @see #isUserSupplied()
	 */
	public String getNature() {
		return nature;
	}

	/**
	 * Return true if this is an IBM-supplied type. If false it is user or ISV supplied.
	 */
	public boolean isIBMSupplied() {
		return nature.equals(ISystemCompileXMLConstants.NATURE_IBM_VALUE);
	}

	/**
	 * Return true if this is an user-supplied type. If false it is IBM or ISV supplied.
	 */
	public boolean isUserSupplied() {
		return nature.equals(ISystemCompileXMLConstants.NATURE_USER_VALUE);
	}

	/**
	 * Return true if this is an ISV-supplied type. If false it is IBM or user supplied.
	 */
	public boolean isISVSupplied() {
		return nature.equals(ISystemCompileXMLConstants.NATURE_ISV_VALUE);
	}

	/**
	 * Set the default string. This is the IBM-supplied compile command (with parameters) that is restored when "Restore Defaults" is pressed.
	 * @param defaultString the default string
	 */
	public void setDefaultString(String defaultString) {
		//this.defaultString = defaultString.toUpperCase(); // now leave it up to GUI to do massaging
		this.defaultString = defaultString;
	}

	/**
	 * Get the default string. This is the IBM-supplied compile command (with parameters) that is restored when "Restore Defaults" is pressed.
	 * @return the default string
	 */
	public String getDefaultString() {
		return defaultString;
	}

	/**
	 * Set the current string. This is the current value of the compile command (with parameters). 
	 * @param currentString the current string
	 */
	public void setCurrentString(String currentString) {
		//this.currentString = currentString.toUpperCase(); now leave it up to GUI to massage command string
		this.currentString = currentString;
	}

	/**
	 * Get the current string. This is the current value of the compile command (with parameters). 
	 * @return the current string
	 */
	public String getCurrentString() {
		return currentString;
	}

	/**
	 * Set the menu option. Dictates in what popup menu, if any, this compile command appears in.
	 * @param menuOption the menu option. 
	 *  One of {#link ISystemCompileXMLConstants#MENU_PROMPTABLE_VALUE} 
	 *      or {#link ISystemCompileXMLConstants#MENU_NON_PROMPTABLE_VALUE} 
	 *      or {#link ISystemCompileXMLConstants#MENU_BOTH_VALUE} 
	 *      or {#link ISystemCompileXMLConstants#MENU_NONE_VALUE} 
	 */
	public void setMenuOption(String menuOption) {
		this.menuOption = menuOption;
	}

	/**
	 * Fastpath to setting the menu option to both, which is the typical case
	 */
	public void setMenuOptionBoth() {
		setMenuOption(ISystemCompileXMLConstants.MENU_BOTH_VALUE);
	}

	/**
	 * Get the menu option. Dictates in what popup menu, if any, this compile command appears in.
	 * @return the menu option:
	 *  One of {#link ISystemCompileXMLConstants#MENU_PROMPTABLE_VALUE} 
	 *      or {#link ISystemCompileXMLConstants#MENU_NON_PROMPTABLE_VALUE} 
	 *      or {#link ISystemCompileXMLConstants#MENU_BOTH_VALUE} 
	 *      or {#link ISystemCompileXMLConstants#MENU_NONE_VALUE}
	 */
	public String getMenuOption() {
		return menuOption;
	}

	/**
	 * Set the order. That is, this compile commands position within the list of compile commands for a given compile type.
	 * @returns the compile command's order or position.
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Get the order. That is, this compile commands position within the list of compile commands for a given compile type.
	 * @return the order or position.
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Returns if it is promptable. Queries the value of the menuOption attribute.
	 * 
	 * @return true if promptable, false otherwise
	 */
	public boolean isPromptable() {
		if (menuOption.equals(ISystemCompileXMLConstants.MENU_BOTH_VALUE) || menuOption.equals(ISystemCompileXMLConstants.MENU_PROMPTABLE_VALUE))
			return true;
		else
			return false;
	}

	/**
	 * Returns if it is non-promptable. Queries the value of the menuOption attribute.
	 */
	public boolean isNonPromptable() {
		if (menuOption.equals(ISystemCompileXMLConstants.MENU_BOTH_VALUE) || menuOption.equals(ISystemCompileXMLConstants.MENU_NON_PROMPTABLE_VALUE))
			return true;
		else
			return false;
	}

	/**
	 * Set the job environment property. This is subsystem specific, and not used by all subsystems.
	 */
	public void setJobEnvironment(String jobenv) {
		this.jobEnv = jobenv;
	}

	/**
	 * Get the job environment property.
	 */
	public String getJobEnvironment() {
		return jobEnv;
	}

	/**
	 * Sets whether the label is editable in the Work With Compile Commands dialog.
	 */
	public void setLabelEditable(boolean editable) {
		isLabelEditable = editable;
	}

	/**
	 * Gets whether the label is editable in the Work With Compile Commands dialog.
	 */
	public boolean isLabelEditable() {
		return isLabelEditable;
	}

	/**
	 * Sets whether the command string is editable in the Work With Compile Commands dialog.
	 */
	public void setCommandStringEditable(boolean editable) {
	}

	/**
	 * Gets whether the command string is editable in the Work With Compile Commands dialog.
	 */
	public boolean isCommandStringEditable() {
		// return isCommandStringEditable;
		return true; // for 5.1, all command strings are editable
		// TODO: For V6, think about the scenario when it's false
		// how do we handle that in the various dialogs?
	}

	/**
	 * Clone the object: creates a new compile command and copies all its attributes.
	 * 
	 * During the process of cloning, the Nature is always set to be 
	 * User-supplied - so even if an IBM-Supplied compile command is cloned,
	 * the result will be treated as User-supplied. 
     * 
	 * Subclasses must ensure that such a deep copy operation is always
	 * possible, so their state must always be cloneable. Which should 
	 * always be possible to achieve, since this Object also needs to be
	 * serializable. If a subclass adds additional complex attributes, 
	 * this method should be subclassed to clone those attributes.
	 */
	public Object clone() {
		////Old invalid method of cloning does not maintain runtime type
		//SystemCompileCommand clone = new SystemCompileCommand(getParentType(), getId(), getLabel(), ISystemCompileXMLConstants.NATURE_USER_VALUE, null, getCurrentString(), getMenuOption(), getOrder());
		SystemCompileCommand clone = null;
		try {
			clone = (SystemCompileCommand)super.clone();
		} catch(CloneNotSupportedException e) {
			//assert false; //can never happen
			throw new RuntimeException(e);
		}
		clone.setNature(ISystemCompileXMLConstants.NATURE_USER_VALUE);
		clone.setDefaultString(null);
		clone.configureId();
		if (jobEnv != null) clone.setJobEnvironment(jobEnv);
		return clone;
	}

	/**
	 * Print the full command string to standard out, for debugging purposes
	 */
	public void printCommand(String indent) {
		System.out.println(indent + "Label: '" + label + "', Cmd: '" + currentString + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Do substitution of any variables found in the compile command, using the given 
	 *  remote source object. It is the responsibility of the caller of this method to 
	 *  supply a "substitutor" that has knowledge of the supported substitution variables
	 *  for these compile commands. Each implementation will override the substitutor 
	 *  interface to support the necessary getters for resolving their supported variables.
	 * <p>
	 * This method retrieves the substitution variable list from the compile manager, and
	 *  then calls doSubstitutions in it. This in turn will call back to the supplied substitutor
	 *  for each match it finds in compile string, of a variable in its list. 
	 */
	public String doVariableSubstitution(Object remoteObject, ISystemCompileCommandSubstitutor substitutor) {
		SystemCompileManager mgr = parentType.getParentProfile().getParentManager();
		mgr.setCurrentCompileCommand(this); // defect 47808
		SystemCmdSubstVarList substVarList = mgr.getSubstitutionVariableList();
		String substitutedString = substVarList.doSubstitutions(getCurrentString(), remoteObject, substitutor);
		mgr.setCurrentCompileCommand(null); // defect 47808
		//System.out.println("mgr         class = " + mgr.getClass().getName());
		//System.out.println("substVL     class = " + substVarList.getClass().getName());
		//System.out.println("substitutor class = " + substitutor.getClass().getName());
		return substitutedString;
	}

	/**
	 * Return this object as a string.
	 */
	public String toString() {
		return getCurrentString();
	}

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType) {
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}
}
