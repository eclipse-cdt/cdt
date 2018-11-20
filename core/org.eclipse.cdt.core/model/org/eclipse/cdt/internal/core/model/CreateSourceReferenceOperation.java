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
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - Fixed bug 141484
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * CreateSourceReferenceOperation
 */
public class CreateSourceReferenceOperation extends CreateElementInTUOperation {

	/**
	 * Element Name
	 */
	String fName;

	/**
	 * Element Type
	 */
	int fElementType;

	/**
	 * Source Reference element to copy to parent
	 */
	String fSource;

	/**
	 * @param parentElement
	 */
	public CreateSourceReferenceOperation(ICElement parentElement, String name, int elementType, String source) {
		super(parentElement);
		fName = name;
		fElementType = elementType;
		fSource = source;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CreateElementInTUOperation#generateElement(org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	@Override
	protected String generateElement(ITranslationUnit unit) throws CModelException {
		return fSource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CreateElementInTUOperation#generateResultHandle()
	 */
	@Override
	protected ICElement generateResultHandle() {
		IParent parent = (IParent) getParentElement();
		try {
			ICElement[] celements = parent.getChildren();
			for (int i = 0; i < celements.length; ++i) {
				if (celements[i].getElementType() == fElementType) {
					String name = celements[i].getElementName();
					if (name.equals(fName)) {
						return celements[i];
					}
				}
			}
		} catch (CModelException e) {
			//
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CreateElementInTUOperation#getMainTaskName()
	 */
	@Override
	protected String getMainTaskName() {
		return "operation.createsourceReference"; //$NON-NLS-1$
	}

}
