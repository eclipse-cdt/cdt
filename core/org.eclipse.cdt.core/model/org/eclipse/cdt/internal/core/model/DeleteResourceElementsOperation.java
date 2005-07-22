/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
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
	protected String getMainTaskName() {
		return "operation.deleteResourceProgress"; //$NON-NLS-1$
	}

	/**
	 * @see MultiOperation. This method delegate to <code>deleteResource</code> or
	 * <code>deletePackageFragment</code> depending on the type of <code>element</code>.
	 */
	protected void processElement(ICElement element) throws CModelException {
		deleteResource(element.getResource(), fForce);
	}

	/**
	 * @see MultiOperation
	 */
	protected void verify(ICElement element) throws CModelException {
		if (element == null || !element.exists())
			error(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, element);
	}
}
