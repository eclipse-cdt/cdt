/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * This operation copies/moves a collection of elements from their current
 * container to a new container, optionally renaming the
 * elements.
 * <p>Notes:<ul>
 *    <li>If there is already an element with the same name in
 *    the new container, the operation either overwrites or aborts,
 *    depending on the collision policy setting. The default setting is
 *	  abort.
 *
 *    <li>When constructors are copied to a type, the constructors
 *    are automatically renamed to the name of the destination
 *    type.
 *
 *	  <li>When main types are renamed (move within the same parent),
 *		the compilation unit and constructors are automatically renamed
 *
 *    <li>The collection of elements being copied must all share the
 *    same type of container (for example, must all be type members).
 *
 *    <li>The elements are inserted in the new container in the order given.
 *
 *    <li>The elements can be positioned in the new container - see #setInsertBefore.
 *    By default, the elements are inserted based on the default positions as specified in
 * 	the creation operation for that element type.
 *
 *    <li>This operation can be used to copy and rename elements within
 *    the same container. 
 *
 *    <li>This operation only copies elements contained within compilation units. 
 * </ul>
 *
 */
public class CopyElementsOperation extends MultiOperation {

	/**
	 * When executed, this operation will copy the given elements to the
	 * given containers.  The elements and destination containers must be in
	 * the correct order. If there is > 1 destination, the number of destinations
	 * must be the same as the number of elements being copied/moved/renamed.
	 */
	public CopyElementsOperation(ICElement[] elementsToCopy, ICElement[] destContainers, boolean force) {
		super(elementsToCopy, destContainers, force);
	}

	/**
	 * When executed, this operation will copy the given elements to the
	 * given container.
	 */
	public CopyElementsOperation(ICElement[] elementsToCopy, ICElement destContainer, boolean force) {
		this(elementsToCopy, new ICElement[]{destContainer}, force);
	}

	/**
	 * Returns the <code>String</code> to use as the main task name
	 * for progress monitoring.
	 */
	protected String getMainTaskName() {
		return "operation.copyElementProgress"; //$NON-NLS-1$
	}

	/**
	 * Returns the nested operation to use for processing this element
	 */
	protected CModelOperation getNestedOperation(ICElement element) {
		ITranslationUnit unit = getDestinationTranslationUnit(element);
		String name = element.getElementName();
		int type = element.getElementType();
		return new CreateSourceReferenceOperation(unit, name, type, getSourceFor(element));
	}

	protected ITranslationUnit getDestinationTranslationUnit(ICElement element) {
		ICElement dest = getDestinationParent(element);
		return (ITranslationUnit)dest.getAncestor(ICElement.C_UNIT);		
	}

	protected ITranslationUnit getSourceTranslationUnit(ICElement element) {
		return (ITranslationUnit)element.getAncestor(ICElement.C_UNIT);		
	}

	/**
	 * Returns the cached source for this element or compute it if not already cached.
	 */
	private String getSourceFor(ICElement element)  {
		if (element instanceof ISourceReference) {
			// TODO: remove this hack when we have ASTRewrite and doit properly
			try {
				ISourceReference source = (ISourceReference)element;
				ISourceRange range = source.getSourceRange();
				String contents = source.getSource();
				StringBuffer sb = new StringBuffer(contents);
				// Copy the extra spaces and newLines like it is part of
				// the element.  Note: the DeleteElementAction is doing the same.
				IBuffer buffer = getSourceTranslationUnit(element).getBuffer();
				boolean newLineFound = false;
				for (int offset = range.getStartPos() + range.getLength();;++offset) {
					try {
						char c = buffer.getChar(offset);
						// TODO:Bug in the Parser, it does not give the semicolon
						if (c == ';') {
							sb.append(c) ;
						} else if (c == '\r' || c == '\n') {
							newLineFound = true;
							sb.append(c) ;
						} else if (!newLineFound && c == ' ') { // Do not include the spaces after the newline
							sb.append(c) ;
						} else {
							break;
						}
					} catch (Exception e) {
						break;
					}
				}
				contents = sb.toString();
				if (! contents.endsWith(Util.LINE_SEPARATOR)) {
					contents += Util.LINE_SEPARATOR;
				}
				return contents;
			} catch (CModelException e) {
				//
			}
		}
		return ""; //$NON-NLS-1$
	}
	/**
	 * Copy/move the element from the source to destination, renaming
	 * the elements as specified, honoring the collision policy.
	 *
	 * @exception CModelException if the operation is unable to
	 * be completed
	 */
	protected void processElement(ICElement element) throws CModelException {
		CModelOperation op = getNestedOperation(element);
		if (op == null) {
			return;
		}

		boolean isInTUOperation = op instanceof CreateElementInTUOperation;

		if (isInTUOperation && isMove()) {
			DeleteElementsOperation deleteOp = new DeleteElementsOperation(new ICElement[] { element }, fForce);
			executeNestedOperation(deleteOp, 1);
		}

		if (isInTUOperation) {
			CreateElementInTUOperation inTUop = (CreateElementInTUOperation)op;
			ICElement sibling = (ICElement) fInsertBeforeElements.get(element);
			if (sibling != null) {
				(inTUop).setRelativePosition(sibling, CreateElementInTUOperation.INSERT_BEFORE);
			} else if (isRename()) {
				ICElement anchor = resolveRenameAnchor(element);
				if (anchor != null) {
					inTUop.setRelativePosition(anchor, CreateElementInTUOperation.INSERT_AFTER); // insert after so that the anchor is found before when deleted below
				}
			}
			String newName = getNewNameFor(element);
			if (newName != null) {
				inTUop.setAlteredName(newName);
			}
		}
		executeNestedOperation(op, 1);

		ITranslationUnit destUnit = getDestinationTranslationUnit(element);
		if (!destUnit.isWorkingCopy()) {
			destUnit.close();
		}
	}

	/**
	 * Returns the anchor used for positioning in the destination for 
	 * the element being renamed. For renaming, if no anchor has
	 * explicitly been provided, the element is anchored in the same position.
	 */
	private ICElement resolveRenameAnchor(ICElement element) throws CModelException {
		IParent parent = (IParent) element.getParent();
		ICElement[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			ICElement child = children[i];
			if (child.equals(element)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Possible failures:
	 * <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - no elements supplied to the operation
	 *	<li>INDEX_OUT_OF_BOUNDS - the number of renamings supplied to the operation
	 *		does not match the number of elements that were supplied.
	 * </ul>
	 */
	protected ICModelStatus verify() {
		ICModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		if (fRenamingsList != null && fRenamingsList.length != fElementsToProcess.length) {
			return new CModelStatus(ICModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		}
		return CModelStatus.VERIFIED_OK;
	}

	/**
	 * @see MultiOperation
	 *
	 * Possible failure codes:
	 * <ul>
	 *
	 *	<li>ELEMENT_DOES_NOT_EXIST - <code>element</code> or its specified destination is
	 *		is <code>null</code> or does not exist. If a <code>null</code> element is
	 *		supplied, no element is provided in the status, otherwise, the non-existant element
	 *		is supplied in the status.
	 *	<li>INVALID_ELEMENT_TYPES - <code>element</code> is not contained within a compilation unit.
	 *		This operation only operates on elements contained within compilation units.
	 *  <li>READ_ONLY - <code>element</code> is read only.
	 *	<li>INVALID_DESTINATION - The destination parent specified for <code>element</code>
	 *		is of an incompatible type. The destination for a package declaration or import declaration must
	 *		be a compilation unit; the destination for a type must be a type or compilation
	 *		unit; the destinaion for any type member (other than a type) must be a type. When
	 *		this error occurs, the element provided in the operation status is the <code>element</code>.
	 *	<li>INVALID_NAME - the new name for <code>element</code> does not have valid syntax.
	 *      In this case the element and name are provided in the status.

	 * </ul>
	 */
	protected void verify(ICElement element) throws CModelException {
		if (element == null || !element.exists())
			error(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, element);

		if (element.getElementType() < ICElement.C_UNIT)
			error(ICModelStatusConstants.INVALID_ELEMENT_TYPES, element);

		if (element.isReadOnly())
			error(ICModelStatusConstants.READ_ONLY, element);

		ICElement dest = getDestinationParent(element);
		verifyDestination(element, dest);
		verifySibling(element, dest);
		if (fRenamingsList != null) {
			verifyRenaming(element);
		}
	}
}
