/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;

/**
 * This operation deletes a collection of resources and all of their children.
 * It does not delete resources which do not belong to the C Model
 * (eg GIF files).
 */
public class DeleteResourceElementsOperation extends MultiOperation {
	/**
	 * When executed, this operation will delete the given elements. The elements
	 * to delete cannot be <code>null</code> or empty, and must have a corresponding
	 * resource.
	 */
	protected DeleteResourceElementsOperation(ICElement[] elementsToProcess, boolean force) {
		super(elementsToProcess, force);
	}

	/**
	 * @see MultiOperation
	 */
	@Override
	protected String getMainTaskName() {
		return CoreModelMessages.getString("operation.deleteResourceProgress"); //$NON-NLS-1$
	}

	/**
	 * @see MultiOperation This method delegate to <code>deleteResource</code> or
	 * <code>deletePackageFragment</code> depending on the type of <code>element</code>.
	 */
	@Override
	protected void processElement(ICElement element) throws CModelException {
		deleteResource(element.getResource(), fForce);
	}

	/**
	 * @see MultiOperation
	 */
	@Override
	protected void verify(ICElement element) throws CModelException {
		if (element == null || !element.exists())
			error(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, element);
	}
}
