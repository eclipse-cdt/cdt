package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


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
