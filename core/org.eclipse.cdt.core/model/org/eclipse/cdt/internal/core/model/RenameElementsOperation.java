/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ISourceReference;

/**
 * RenameElementsOperation
 */
public class RenameElementsOperation extends MoveElementsOperation {
	/**
	 * When executed, this operation will rename the specified elements with the given names in the
	 * corresponding destinations.
	 */
	public RenameElementsOperation(ICElement[] elements, ICElement[] destinations, String[] newNames, boolean force) {
		//a rename is a move to the same parent with a new name specified
		//these elements are from different parents
		super(elements, destinations, force);
		setRenamings(newNames);
	}

	/**
	 * @see MultiOperation
	 */
	@Override
	protected String getMainTaskName() {
		return CoreModelMessages.getString("operation.renameElementProgress"); //$NON-NLS-1$
	}

	/**
	 * @see CopyElementsOperation#isRename()
	 */
	@Override
	protected boolean isRename() {
		return true;
	}

	/**
	 * @see MultiOperation
	 */
	@Override
	protected ICModelStatus verify() {
		ICModelStatus status = super.verify();
		if (!status.isOK())
			return status;
		if (this.fRenamingsList == null || this.fRenamingsList.length == 0)
			return new CModelStatus(ICModelStatusConstants.NULL_NAME);
		return CModelStatus.VERIFIED_OK;
	}

	/**
	 * @see MultiOperation
	 */
	@Override
	protected void verify(ICElement element) throws CModelException {

		if (element == null || !element.exists())
			error(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, element);
		else {
			int elementType = element.getElementType();
			if (element.isReadOnly())
				error(ICModelStatusConstants.READ_ONLY, element);

			if (!(element instanceof ISourceReference))
				error(ICModelStatusConstants.INVALID_ELEMENT_TYPES, element);

			if (elementType < ICElement.C_UNIT /*|| elementType == ICElement.INITIALIZER*/)
				error(ICModelStatusConstants.INVALID_ELEMENT_TYPES, element);

			//		Member localContext;
			//		if (element instanceof Member && (localContext = ((Member)element).getOuterMostLocalContext()) != null && localContext != element) {
			//			// JDOM doesn't support source manipulation in local/anonymous types
			//			error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
			//		}

			verifyRenaming(element);
		}
	}

}
