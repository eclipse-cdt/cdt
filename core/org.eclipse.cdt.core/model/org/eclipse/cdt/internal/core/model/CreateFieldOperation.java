/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.IStructure;
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
	public CreateFieldOperation(IStructure parentElement, String name, String returnType, String initializer,
			boolean force) {
		super(parentElement, name, returnType, force);
		fInitializer = initializer;
	}

	/**
	 * @see CreateElementInTUOperation#getMainTaskName
	 */
	@Override
	public String getMainTaskName() {
		return CoreModelMessages.getString("operation.createFieldProgress"); //$NON-NLS-1$
	}

	/**
	 * By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	@Override
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
	 * @see CreateElementInTUOperation#generateResultHandle
	 */
	@Override
	protected ICElement generateResultHandle() {
		return getStructure().getField(fName);
	}

	/**
	 * @see CreateMemberOperation#verifyNameCollision
	 */
	@Override
	protected ICModelStatus verifyNameCollision() {
		return super.verifyNameCollision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CreateElementInTUOperation#generateElement(org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	@Override
	protected String generateElement(ITranslationUnit unit) throws CModelException {
		StringBuilder sb = new StringBuilder();
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
