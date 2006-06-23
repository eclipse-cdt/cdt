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
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * <p>This abstract class implements behavior common to <code>CreateElementInCUOperations</code>.
 * To create a compilation unit, or an element contained in a compilation unit, the
 * source code for the entire compilation unit is updated and saved.
 *
 * <p>The element being created can be positioned relative to an existing
 * element in the compilation unit via the methods <code>#createAfter</code>
 * and <code>#createBefore</code>. By default, the new element is positioned
 * as the last child of its parent element.
 *
 */
public abstract class CreateElementInTUOperation extends CModelOperation {

	/**
	 * A constant meaning to position the new element
	 * as the last child of its parent element.
	 */
	protected static final int INSERT_LAST = 1;

	/**
	 * A constant meaning to position the new element
	 * after the element defined by <code>fAnchorElement</code>.
	 */
	protected static final int INSERT_AFTER = 2;

	/**
	 * A constant meaning to position the new element
	 * before the element defined by <code>fAnchorElement</code>.
	 */
	protected static final int INSERT_BEFORE = 3;

	/**
	 * One of the position constants, describing where
	 * to position the newly created element.
	 */
	protected int fInsertionPolicy = INSERT_LAST;

	/**
	 * The element that is being created.
	 */
	protected String fCreatedElement = null;

	/**
	 * The element that the newly created element is
	 * positioned relative to, as described by
	 * <code>fInsertPosition</code>, or <code>null</code>
	 * if the newly created element will be positioned
	 * last.
	 */
	protected ICElement fAnchorElement = null;

	/**
	 * A flag indicating whether creation of a new element occurred.
	 * A request for creating a duplicate element would request in this
	 * flag being set to <code>false</code>. Ensures that no deltas are generated
	 * when creation does not occur.
	 */
	protected boolean fCreationOccurred = true;

	/**
	 * The position of the element that is being created.
	 */
	protected int fInsertionPosition = -1;

	/**
	 * The number of characters the new element replaces,
	 * or 0 if the new element is inserted,
	 * or -1 if the new element is append to the end of the CU.
	 */
	protected int fReplacementLength = -1;

	/**
	 * Constructs an operation that creates a C Language Element with
	 * the specified parent, contained within a translation unit.
	 */
	public CreateElementInTUOperation(ICElement parentElement) {
		super(null, new ICElement[]{parentElement});
		initializeDefaultPosition();
	}

	/**
	 * Only allow cancelling if this operation is not nested.
	 */
	protected void checkCanceled() {
		if (!fNested) {
			super.checkCanceled();
		}
	}

	/**
	 * Instructs this operation to position the new element after
	 * the given sibling, or to add the new element as the last child
	 * of its parent if <code>null</code>.
	 */
	public void createAfter(ICElement sibling) {
		setRelativePosition(sibling, INSERT_AFTER);
	}

	/**
	 * Instructs this operation to position the new element before
	 * the given sibling, or to add the new element as the last child
	 * of its parent if <code>null</code>.
	 */
	public void createBefore(ICElement sibling) {
		setRelativePosition(sibling, INSERT_BEFORE);
	}

	protected abstract String generateElement(ITranslationUnit unit) throws CModelException;
	
	/**
	 * Execute the operation - generate new source for the compilation unit
	 * and save the results.
	 *
	 * @exception CModelException if the operation is unable to complete
	 */
	protected void executeOperation() throws CModelException {
		beginTask(getMainTaskName(), getMainAmountOfWork());
		CElementDelta delta = newCElementDelta();
		ITranslationUnit unit = getTranslationUnit();
		fCreatedElement = generateElement(unit);
		insertElement();
		if (fCreationOccurred) {
			//a change has really occurred
			IBuffer buffer = unit.getBuffer();
			if (buffer == null) return;
			char[] bufferContents = buffer.getCharacters();
			if (bufferContents == null) return;
			char[] elementContents = Util.normalizeCRs(getCreatedElementCharacters(), bufferContents);
			switch (fReplacementLength) {
				case -1 : 
					// element is append at the end
					buffer.append(elementContents);
					break;

				case 0 :
					// element is inserted
					buffer.replace(fInsertionPosition, 0, elementContents);
					break;

				default :
					// element is replacing the previous one
					buffer.replace(fInsertionPosition, fReplacementLength, elementContents);
			}
			unit.save(null, false);
			boolean isWorkingCopy = unit.isWorkingCopy();
			//if (isWorkingCopy) {
			//	this.setAttributes(...);
			//}
			worked(1);
			fResultElements = generateResultHandles();
			if (!isWorkingCopy) { // if unit is working copy, then save will have already fired the delta
				if (unit.getParent().exists()) {
					for (int i = 0; i < fResultElements.length; i++) {
						delta.added(fResultElements[i]);
					}
					addDelta(delta);
				} // else unit is created outside classpath
				  // non-java resource delta will be notified by delta processor
			}
		}
		done();
	}

	private char[] getCreatedElementCharacters() {
		return fCreatedElement.toCharArray();
	}

	/**
	 * Creates and returns the handle for the element this operation created.
	 */
	protected abstract ICElement generateResultHandle();

	/**
	 * Creates and returns the handles for the elements this operation created.
	 */
	protected ICElement[] generateResultHandles() throws CModelException {
		return new ICElement[]{generateResultHandle()};
	}

	/**
	 * Returns the compilation unit in which the new element is being created.
	 */
	protected ITranslationUnit getTranslationUnit() {
		return ((ISourceReference)getParentElement()).getTranslationUnit();
	}

	/**
	 * Returns the amount of work for the main task of this operation for
	 * progress reporting.
	 * @see executeOperation()
	 */
	protected int getMainAmountOfWork(){
		return 2;
	}

	/**
	 * Returns the name of the main task of this operation for
	 * progress reporting.
	 * @see executeOperation()
	 */
	protected abstract String getMainTaskName();

	/**
	 * Returns the elements created by this operation.
	 */
	public ICElement[] getResultElements() {
		return fResultElements;
	}

	/**
	 * Sets the default position in which to create the new type
	 * member. By default, the new element is positioned as the
	 * last child of the parent element in which it is created.
	 * Operations that require a different default position must
	 * override this method.
	 */
	protected void initializeDefaultPosition() {
	}

	/**
	 * Inserts the given child into the given JDOM, 
	 * based on the position settings of this operation.
	 *
	 * @see createAfter(IJavaElement)
	 * @see createBefore(IJavaElement);
	 */
	protected void insertElement() throws CModelException {
		if (fInsertionPolicy != INSERT_LAST) {
			ISourceRange range = ((ISourceReference)fAnchorElement).getSourceRange();
			switch (fInsertionPolicy) {
				case INSERT_AFTER:
					fReplacementLength = 0;
					fInsertionPosition = range.getStartPos() + range.getLength();
				break;

				case INSERT_BEFORE:
					fReplacementLength = 0;
					fInsertionPosition = range.getStartPos();
				break;

				default:
					fReplacementLength = range.getStartPos() + range.getLength();
					fInsertionPosition = range.getStartPos();
			}
			return;
		}
		//add as the last element of the parent
		fReplacementLength = -1;
	}

	/**
	 * Sets the name of the <code>DOMNode</code> that will be used to
	 * create this new element.
	 * Used by the <code>CopyElementsOperation</code> for renaming.
	 * Only used for <code>CreateTypeMemberOperation</code>
	 */
	protected void setAlteredName(String newName) {
	}

	/**
	 * Instructs this operation to position the new element relative
	 * to the given sibling, or to add the new element as the last child
	 * of its parent if <code>null</code>. The <code>position</code>
	 * must be one of the position constants.
	 */
	protected void setRelativePosition(ICElement sibling, int policy) throws IllegalArgumentException {
		if (sibling == null) {
			fAnchorElement = null;
			fInsertionPolicy = INSERT_LAST;
		} else {
			fAnchorElement = sibling;
			fInsertionPolicy = policy;
		}
	}

	/**
	 * Possible failures: <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the operation is
	 * 		<code>null</code>.
	 *  <li>INVALID_NAME - no name, a name was null or not a valid
	 * 		import declaration name.
	 *  <li>INVALID_SIBLING - the sibling provided for positioning is not valid.
	 * </ul>
	 * @see ICModelStatus
	 * @see CNamingConventions
	 */
	public ICModelStatus verify() {
		if (getParentElement() == null) {
			return new CModelStatus(ICModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		if (fAnchorElement != null) {
			ICElement domPresentParent = fAnchorElement.getParent();
			if (!domPresentParent.equals(getParentElement())) {
				return new CModelStatus(ICModelStatusConstants.INVALID_SIBLING, fAnchorElement);
			}
		}
		return CModelStatus.VERIFIED_OK;
	}

}
