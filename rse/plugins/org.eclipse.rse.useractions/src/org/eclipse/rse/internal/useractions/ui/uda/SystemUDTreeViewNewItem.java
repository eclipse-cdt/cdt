package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
//import com.ibm.etools.systems.core.*;
/**
 * This represent a tree-node for "New" items
 */
public class SystemUDTreeViewNewItem {
	// state
	private String label;
	private boolean executable; // cascading or not?
	private int domain = -1;
	private boolean wwActionsDialog; // true for ww user actions, false for ww user types
	// constants
	private static SystemUDTreeViewNewItem rootActionInst, rootTypeInst;
	private static SystemUDTreeViewNewItem rootActionOnlyInst, rootTypeOnlyInst;

	/**
	 * Constructor 
	 * @param _executable -> true if this is a leaf node
	 * @param _label -> label to show the user, in the tre
	 * @param _domain -> domain this represents
	 * @param _wwActionsDialog -> true if this is for the ww user actions dialog, false for the ww named types dialog
	 */
	public SystemUDTreeViewNewItem(boolean _executable, String _label, int _domain, boolean _wwActionsDialog) {
		super();
		label = _label;
		executable = _executable;
		domain = _domain;
		wwActionsDialog = _wwActionsDialog;
	}

	/**
	 * Return the label
	 */
	public String toString() {
		return label;
	}

	/**
	 * Is this executable? Ie, should it launch a "New" wizard?
	 */
	public boolean isExecutable() {
		return executable;
	}

	/**
	 * Is this the work with actions dialog (true) or the work with types dialog (false)
	 */
	public boolean isWorkWithActionsDialog() {
		return wwActionsDialog;
	}

	/**
	 * Get the domain this represents
	 */
	public int getDomain() {
		return domain;
	}

	/**
	 * Return singleon instance of new item that does have children.
	 * This is used for the first element when domains are supported.
	 * @param wwActionsDialog true if called from dialog
	 * @param newNodeLabel the translated label for the node. 
	 */
	public static SystemUDTreeViewNewItem getRootNewItem(boolean wwActionsDialog, String newNodeLabel) {
		if (wwActionsDialog) {
			if (rootActionInst == null) rootActionInst = new SystemUDTreeViewNewItem(false, // this item is not executable
					newNodeLabel, 0, wwActionsDialog);
			return rootActionInst;
		} else {
			if (rootTypeInst == null) rootTypeInst = new SystemUDTreeViewNewItem(false, // this item is not executable
					newNodeLabel, 0, wwActionsDialog);
			return rootTypeInst;
		}
	}

	/**
	 * Return singleton instance of root new item that does not have children.
	 * This is used for the first element when domains are not supported.
	 * @param wwActionsDialog true if called from dialog
	 * @param newNodeLabel the translated label for the node. 
	 */
	public static SystemUDTreeViewNewItem getOnlyNewItem(boolean wwActionsDialog, String newNodeLabel) {
		if (wwActionsDialog) {
			if (rootActionOnlyInst == null) rootActionOnlyInst = new SystemUDTreeViewNewItem(true, // this item is executable
					newNodeLabel, -1, wwActionsDialog);
			return rootActionOnlyInst;
		} else {
			if (rootTypeOnlyInst == null) rootTypeOnlyInst = new SystemUDTreeViewNewItem(true, // this item is executable
					newNodeLabel, -1, wwActionsDialog);
			return rootTypeOnlyInst;
		}
	}

	/**
	 * Return non-singleton instance of root new item that does not have children.
	 * This is used for the first element when domains are supported internally, but externally 
	 *  only one is used. 
	 * @param domain - the domain to use
	 * @param wwActionsDialog - true if called from dialog
	 * @param newNodeLabel - the translated label for the node
	 */
	public static SystemUDTreeViewNewItem getOnlyNewItem(int domain, boolean wwActionsDialog, String newNodeLabel) {
		return new SystemUDTreeViewNewItem(true, // this item is executable
				newNodeLabel, domain, wwActionsDialog);
	}
}
