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
/*
 * Created on Dec 5, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.rse.internal.useractions.ui.uda;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author coulthar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface ISystemUDTreeView {
	/**
	 * Expand the non-new domain (parent) nodes
	 */
	public abstract void expandDomainNodes();

	/**
	 * Expand the given domain (parent) node, named by its
	 *  translatable name.
	 */
	public abstract void expandDomainNode(String displayName);

	/**
	 * Convenience method for returning the shell of this viewer.
	 */
	public abstract Shell getShell();

	/**
	 * Return the action or type manager
	 */
	public abstract SystemUDBaseManager getDocumentManager();

	/**
	 * Get the selected action or type name.
	 * Returns "" if nothing selected
	 */
	public abstract String getSelectedElementName();

	/**
	 * Get the selected action or type domain.
	 * Returns -1 if nothing selected or domains not supported
	 */
	public abstract int getSelectedElementDomain();

	/**
	 * Return true if currently selected element is "ALL"
	 */
	public boolean isElementAllSelected();

	// ------------------------------------
	public abstract SystemXMLElementWrapper getSelectedElement();

	/**
	 * Select the given type
	 */
	public abstract void selectElement(SystemXMLElementWrapper element);

	/**
	 * Find the parent tree item of the given type.
	 * If it is not currently shown in the tree, or there is no parent, returns null.
	 */
	public abstract TreeItem findParentItem(SystemXMLElementWrapper element);

	/**
	 * Refresh the parent of the given action.
	 * That is, find the parent and refresh the children.
	 * If the parent is not found, assume it is because it is new too,
	 *  so refresh the whole tree.
	 */
	public abstract void refreshElementParent(SystemXMLElementWrapper element);

	/**
	 * Returns the tree item of the first selected object.
	 */
	public abstract TreeItem getSelectedTreeItem();

	/**
	 * Returns the tree item of the sibling before the first selected object.
	 */
	public abstract TreeItem getSelectedPreviousTreeItem();

	/**
	 * Returns the tree item of the sibling after the first selected object.
	 */
	public abstract TreeItem getSelectedNextTreeItem();

	/**
	 * Returns the tree item of the sibling two after the first selected object.
	 */
	public abstract TreeItem getSelectedNextNextTreeItem();

	/**
	 * Return true if currently selected element is vendor supplied
	 */
	public boolean isSelectionVendorSupplied();

	/**
	 * Return the vendor that is responsible for pre-supplying this existing type,
	 *  or null if not applicable.
	 */
	public String getVendorOfSelection();

	/**
	 * Set the selection
	 */
	public void setSelection(ISelection selection);

	/**
	 * Refresh given element
	 */
	public void refresh(Object element);
}
