package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ICModelStatus;

/**
 * <p>This operation creates an instance method. 
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing type
 *  <li>The source code for the method. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateMethodOperation extends CreateMemberOperation {
	protected String[] fParameterTypes;

	/**
	 * When executed, this operation will create a method
	 * in the given type with the specified source.
	 */
	public CreateMethodOperation(IStructure parentElement, String source, boolean force) {
		super(parentElement, source, force);
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		return getStructure().getMethod(fSource);
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName(){
		return "operation.createMethodProgress"; //$NON-NLS-1$
	}

	/**
	 * @see CreateTypeMemberOperation#verifyNameCollision
	 */
	protected ICModelStatus verifyNameCollision() {
		return CModelStatus.VERIFIED_OK;
	}
}
