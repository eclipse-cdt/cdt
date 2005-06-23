/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ICModelStatus;

/**
 * Implements functionality common to
 * operations that create type members.
 */
public abstract class CreateMemberOperation extends CreateElementInTUOperation {
	/**
	 * The element Name
	 */
	protected String fName;

	/**
	 * Element Type;
	 */
	protected String fReturnType;


	/**
	 * The name of the <code>DOMNode</code> that may be used to
	 * create this new element.
	 * Used by the <code>CopyElementsOperation</code> for renaming
	 */
	protected String fAlteredName;

	/**
	 * When executed, this operation will create a type member
	 * in the given parent element with the specified source.
	 */
	public CreateMemberOperation(IStructure parentElement, String name, String returnType, boolean force) {
		super(parentElement);
		fName = name;
		fReturnType = returnType;
		fForce= force;
	}

	/**
	 * Returns the IType the member is to be created in.
	 */
	protected IStructure getStructure() {
		return (IStructure)getParentElement();
	}

	/**
	 * Sets the name of the <code>DOMNode</code> that will be used to
	 * create this new element.
	 * Used by the <code>CopyElementsOperation</code> for renaming
	 */
	protected void setAlteredName(String newName) {
		fAlteredName = newName;
	}

	/**
	 * Possible failures: <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - the parent element supplied to the operation is
	 * 		<code>null</code>.
	 *	<li>INVALID_CONTENTS - The source is <code>null</code> or has serious syntax errors.
	  *	<li>NAME_COLLISION - A name collision occurred in the destination
	 * </ul>
	 */
	public ICModelStatus verify() {
		ICModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		if (!fForce) {
			//check for name collisions
			//if (node == null) {
			//	return new CModelStatus(ICModelStatusConstants.INVALID_CONTENTS);
			//	}
			//} catch (CModelException jme) {
			//}
			return verifyNameCollision();
		}

		return CModelStatus.VERIFIED_OK;
	}

	/**
	 * Verify for a name collision in the destination container.
	 */
	protected ICModelStatus verifyNameCollision() {
		return CModelStatus.VERIFIED_OK;
	}
}
