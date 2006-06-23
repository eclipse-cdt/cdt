/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ITranslationUnit;
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
	 * Initializer for Element
	 */
	String fInitializer;

	/**
	 * When executed, this operation will create a field with the given name
	 * in the given type with the specified source.
	 *
	 * <p>By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	public CreateFieldOperation(IStructure parentElement, String name, String returnType, String initializer, boolean force) {
		super(parentElement, name, returnType, force);
		fInitializer = initializer;
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
		try {
			ICElement[] elements = parentElement.getFields();
			if (elements != null && elements.length > 0) {
				createAfter(elements[elements.length - 1]);
			} else {
				elements = parentElement.getChildren();
				if (elements != null && elements.length > 0) {
					createBefore(elements[0]);
				}
			}
		} catch (CModelException e) {
		}
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		return getStructure().getField(fName);
	}

	/**
	 * @see CreateTypeMemberOperation#verifyNameCollision
	 */
	protected ICModelStatus verifyNameCollision() {
		return super.verifyNameCollision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CreateElementInTUOperation#generateElement(org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	protected String generateElement(ITranslationUnit unit) throws CModelException {
		StringBuffer sb = new StringBuffer();
		sb.append(fReturnType).append(' ');
		sb.append(fName);
		if (fInitializer != null && fInitializer.length() > 0) {
			sb.append(' ').append('=').append(' ');
			sb.append(fInitializer);
		}
		sb.append(';');
		return sb.toString();
	}
}
