package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.CModelException;

/**
 * This operation renames resources (Package fragments and compilation units).
 *
 * <p>Notes:<ul>
 * <li>When a compilation unit is renamed, its main type and the constructors of the 
 * 		main type are renamed.
 * </ul>
 */
public class RenameResourceElementsOperation extends MoveResourceElementsOperation {
	/**
	 * When executed, this operation will rename the specified elements with the given names in the
	 * corresponding destinations.
	 */
	public RenameResourceElementsOperation(ICElement[] elements, ICElement[] destinations, String[] newNames, boolean force) {
		//a rename is a move to the same parent with a new name specified
		//these elements are from different parents
		super(elements, destinations, force);
		setRenamings(newNames);
	}

	/**
	 * @see MultiOperation
	 */
	protected String getMainTaskName() {
		return "operation.renameResourceProgress"; //$NON-NLS-1$
	}

	/**
	 * @see CopyResourceElementsOperation#isRename()
	 */
	protected boolean isRename() {
		return true;
	}

	/**
	 * @see MultiOperation
	 */
	protected void verify(ICElement element) throws CModelException {
		super.verify(element);
		verifyRenaming(element);
	}
}
