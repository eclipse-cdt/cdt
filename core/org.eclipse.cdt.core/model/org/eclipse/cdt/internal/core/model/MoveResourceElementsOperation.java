package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.cdt.core.model.ICElement;

/**
 * This operation moves resources (package fragments and compilation units) from their current
 * container to a specified destination container, optionally renaming the
 * elements.
 * A move resource operation is equivalent to a copy resource operation, where
 * the source resources are deleted after the copy.
 * <p>This operation can be used for reorganizing resources within the same container.
 *
 * @see CopyResourceElementsOperation
 */
public class MoveResourceElementsOperation extends CopyResourceElementsOperation {
	/**
	 * When executed, this operation will move the given elements to the given containers.
	 */
	public MoveResourceElementsOperation(ICElement[] elementsToMove, ICElement[] destContainers, boolean force) {
		super(elementsToMove, destContainers, force);
	}

	/**
	 * @see MultiOperation
	 */
	protected String getMainTaskName() {
		return "operation.moveResourceProgress"; //$NON-NLS-1$
	}

	/**
	 * @see CopyResourceElementsOperation#isMove()
	 */
	protected boolean isMove() {
		return true;
	}
}
