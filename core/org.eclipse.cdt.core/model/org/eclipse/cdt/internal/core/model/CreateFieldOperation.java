package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ICModelStatus;
/**
 * <p>This operation creates a field declaration in a type.
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing Type
 *  <li>The source code for the declaration. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateFieldOperation extends CreateMemberOperation {
	/**
	 * When executed, this operation will create a field with the given name
	 * in the given type with the specified source.
	 *
	 * <p>By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	public CreateFieldOperation(IStructure parentElement, String source, boolean force) {
		super(parentElement, source, force);
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName(){
		return "operation.createFieldProgress"; //$NON-NLS-1$
	}

	/**
	 * By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	protected void initializeDefaultPosition() {
		IStructure parentElement = getStructure();
		//try {
			ICElement[] elements = parentElement.getFields();
			if (elements != null && elements.length > 0) {
				createAfter(elements[elements.length - 1]);
			} else {
				elements = parentElement.getChildren();
				if (elements != null && elements.length > 0) {
					createBefore(elements[0]);
				}
			}
		//} catch (CModelException e) {
		//}
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		return getStructure().getField(fSource);
	}

	/**
	 * @see CreateTypeMemberOperation#verifyNameCollision
	 */
	protected ICModelStatus verifyNameCollision() {
		return CModelStatus.VERIFIED_OK;
	}
}
