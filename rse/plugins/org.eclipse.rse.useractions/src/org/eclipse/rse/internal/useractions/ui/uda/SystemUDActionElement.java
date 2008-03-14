package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.StringTokenizer;

import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.UserActionsIcon;
import org.eclipse.swt.graphics.Image;

/**
 * Represents a single user defined action, as an adaptable
 *  eclipse-friendly object
 */
public class SystemUDActionElement extends SystemXMLElementWrapper {
	private final static String ACTION_TAG = "Action"; //$NON-NLS-1$
	private final static String COMMENT_TAG = "Comment"; //$NON-NLS-1$
	private final static String COMMAND_TAG = "Command"; //$NON-NLS-1$
	private final static String FILETYPES_TAG = "FileTypes"; //$NON-NLS-1$
	private final static String PROMPT_ATTR = "Prompt"; //$NON-NLS-1$
	private final static String REFRESH_ATTR = "Refresh"; //$NON-NLS-1$
	private final static String COLLECT_ATTR = "Collect"; //$NON-NLS-1$
	private final static String SINGLESEL_ATTR = "SingleSelection"; //$NON-NLS-1$
	private final static String SHOW_ATTR = "Enable"; //$NON-NLS-1$
	//for reseting
	private String initCommand;
	private Object data;

	/**
	 * Constructor
	 * @param e - The actual xml document element for this action
	 * @param am - The subsystemFactory-specific manager of actions
	 * @param profile - The system profile which owns this action
	 * @param domainType - The integer representation of the domain this is in (or this is, for a domain element)
	 */
	public SystemUDActionElement(IPropertySet e, SystemUDActionManager am, ISystemProfile profile, int domainType) {
		super(e, am, profile, domainType);
	}

	/**
	 * Return the value of this node's "Name" attribute, but with "..." appended if
	 *  the action is a prompting action. This is used for popup menu labels.
	 */
	public String getLabel() {
		String name = getName();
		if (getPrompt()) name = name + "..."; //$NON-NLS-1$
		return name;
	}

	/**
	 * Return image to use for this item, in tree views
	 */
	public Image getImage() {
		if (isIBM()) {
			if (isUserChanged())
				return UserActionsIcon.USERACTION_IBMUSR.getImage();
			else
				return UserActionsIcon.USERACTION_IBM.getImage();
		} else
			return UserActionsIcon.USERACTION_USR.getImage();
	}

	/**
	 * Return our tag name
	 */
	public String getTagName() {
		return ACTION_TAG;
	}

	/**
	 * Return value of the "Comment" sub-tag
	 */
	
	public String getComment() {
		//Get the property for this 
		IProperty commentProperty = elm.getProperty(COMMENT_TAG);
		if (commentProperty != null)
		{
			return commentProperty.getValue();
		}
		return "";  //$NON-NLS-1$
	}

	/**
	 * Return value of the "Command" sub-tag, which is the current command value
	 */
	public String getCommand() {
		//Get the property for this 
		IProperty commentProperty = elm.getProperty(COMMAND_TAG);
		if (commentProperty != null)
		{
			return commentProperty.getValue();
		}
		return "";  //$NON-NLS-1$
	}

	/**
	 * Return value of the "Prompt" attribute
	 */
	public boolean getPrompt() {
		return getBooleanAttribute(PROMPT_ATTR, false);
	}

	/**
	 * Return value of the "Refresh" attribute
	 */
	public boolean getRefresh() {
		return getBooleanAttribute(REFRESH_ATTR, false);
	}

	/**
	 * Return value of the "Show" attribute
	 */
	public boolean getShow() {
		return getBooleanAttribute(SHOW_ATTR, true);
	}

	/**
	 * Return value of the "Collect" attribute
	 */
	public boolean getCollect() {
		return getBooleanAttribute(COLLECT_ATTR, false);
	}

	/**
	 * Return value of the "Single Selection" attribute
	 */
	public boolean getSingleSelection() {
		return getBooleanAttribute(SINGLESEL_ATTR, false);
	}

	/**
	 * Return value of the "FileTypes" sub-tag
	 */
	public String[] getFileTypes() {
		//Get the property for this 
		IProperty fileTypeProperty = elm.getProperty(FILETYPES_TAG);
		if (fileTypeProperty != null)
		{
			String fts = fileTypeProperty.getValue();
			// returns an empty string if no attribute
			StringTokenizer st = new StringTokenizer(fts);
			int n = st.countTokens();
			String sa[] = new String[n];
			for (int i = 0; i < n; i++) {
				sa[i] = st.nextToken();
			}
			return sa;
		}

		return new String[0];
	}

	/**
	 * Set the value of the "Comment" sub-tag
	 */
	public void setComment(String s) {
		IProperty commentProperty = elm.getProperty(COMMENT_TAG);
		if (null != commentProperty)
		{
			commentProperty.setValue(s);
		}
		else
		{
			elm.addProperty(COMMENT_TAG, s);
		}
		
		setUserChanged(true);
	}

	/**
	 * Set the value of the "Command" sub-tag.
	 */
	public void setCommand(String s) {
		IProperty commandProperty = elm.getProperty(COMMAND_TAG);
		if (null != commandProperty)
		{
			commandProperty.setValue(s);
		}
		else
		{
			elm.addProperty(COMMAND_TAG, s);
		}
		setUserChanged(true);
	}

	/**
	 * Set the value of the "Prompt" attribute
	 */
	public void setPrompt(boolean b) {
		setBooleanAttribute(PROMPT_ATTR, b);
		setUserChanged(true);
	}

	/**
	 * Set the value of the "Refresh" attribute
	 */
	public void setRefresh(boolean b) {
		setBooleanAttribute(REFRESH_ATTR, b);
		setUserChanged(true);
	}

	/**
	 * Set the value of the "Show" attribute
	 */
	public void setShow(boolean b) {
		setBooleanAttribute(SHOW_ATTR, b);
		setUserChanged(true);
	}

	/**
	 * Set the value of the "Collect" attribute
	 */
	public void setCollect(boolean b) {
		setBooleanAttribute(COLLECT_ATTR, b);
		setUserChanged(true);
	}

	/**
	 * Set the value of the "Single Selection Only" attribute
	 */
	public void setSingleSelection(boolean b) {
		setBooleanAttribute(SINGLESEL_ATTR, b);
		setUserChanged(true);
	}

	/**
	 * Set the value of the "FileTypes" sub-tag
	 */
	public void setFileTypes(String sa[]) {
		String s = ""; //$NON-NLS-1$
		for (int i = 0; i < sa.length; i++) {
			s = s + " " + sa[i]; //$NON-NLS-1$
		}
		
		IProperty fileTypeProperty = elm.getProperty(FILETYPES_TAG);
		if (null != fileTypeProperty)
		{
			fileTypeProperty.setValue(s);
		}
		else
		{
			elm.addProperty(FILETYPES_TAG, s);
		}
		
		setUserChanged(true);
	}

	// ***************************************
	//  Determing file type matches
	// ***************************************
	/**
	 * Is this action's file types a generic value
	 */
	public boolean isGeneric() {
		// ?? may not be optimal
		String fts[] = getFileTypes();
		if (0 == fts.length) return true;
		for (int i = 0; i < fts.length; i++) {
			if ("*".equals(fts[i])) //$NON-NLS-1$
				return true;
		}
		return true;
	}

	/**
	 * Set the initial command value
	 */
	public void setInitCommand(String s) {
		this.initCommand = s;
	}

	/**
	 * Get the initial command value
	 */
	public String getInitCommand() {
		return this.initCommand;
	}

	/**
	 * Set data. Useful when used in context like trees.
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Return data as set by setData
	 */
	public Object getData() {
		return data;
	}
}
