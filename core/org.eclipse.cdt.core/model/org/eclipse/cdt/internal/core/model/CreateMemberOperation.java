package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;

/**
 * Implements functionality common to
 * operations that create type members.
 */
public abstract class CreateMemberOperation extends CreateElementInTUOperation {
	/**
	 * The source code for the new member.
	 */
	protected String fSource = null;

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
	public CreateMemberOperation(ICElement parentElement, String source, boolean force) {
		super(parentElement);
		fSource= source;
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
		if (fSource == null) {
			return new CModelStatus(ICModelStatusConstants.INVALID_CONTENTS);
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
