package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

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
	protected ISourceReference fCreatedElement = null;

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
		// generateNewTranslationUnitDOM(unit);
		insertElement();
		if (fCreationOccurred) {
			//a change has really occurred
			IBuffer buffer = unit.getBuffer();
			if (buffer == null) return;
			char[] bufferContents = buffer.getCharacters();
			if (bufferContents == null) return;
			//char[] elementContents = normalizeCRS(..);
			char[] elementContents = fCreatedElement.getSource().toCharArray();
			//IFile file = (IFile)((ICResource)unit).getResource();
			//StringBuffer buffer = getContent(file);
			switch (fReplacementLength) {
				case -1 : 
					// element is append at the end
					//buffer.append(fCreatedElement.getSource());
					buffer.append(elementContents);
					break;

				case 0 :
					// element is inserted
					//buffer.insert(fInsertionPosition, fCreatedElement.getSource());
					buffer.replace(fInsertionPosition, 0, elementContents);
					break;

				default :
					// element is replacing the previous one
					buffer.replace(fInsertionPosition, fReplacementLength, fCreatedElement.getSource());
			}
			unit.save(null, false);
			//save(buffer, file);
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

				default:
					fReplacementLength = range.getStartPos() + range.getLength();
					fInsertionPosition = range.getStartPos();
				break;
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
			//if (domPresentParent.getElementType() == ICElement.IMPORT_CONTAINER) {
			//	domPresentParent = domPresentParent.getParent();
			//}
			if (!domPresentParent.equals(getParentElement())) {
				return new CModelStatus(ICModelStatusConstants.INVALID_SIBLING, fAnchorElement);
			}
		}
		return CModelStatus.VERIFIED_OK;
	}


	StringBuffer getContent(IFile file) throws CModelException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(file.getContents(true));
		} catch (CoreException e) {
			throw new CModelException(e);
		}
		try {
			char [] b = getInputStreamAsCharArray(stream, -1, null);
			return new StringBuffer(b.length).append(b);
		} catch (IOException e) {
			throw new CModelException(e, ICModelStatusConstants.IO_EXCEPTION);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Returns the given input stream's contents as a character array.
	 * If a length is specified (ie. if length != -1), only length chars
	 * are returned. Otherwise all chars in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream, int length, String encoding)
		throws IOException {
		InputStreamReader reader = null;
		reader = encoding == null
			? new InputStreamReader(stream)
			: new InputStreamReader(stream, encoding);
		char[] contents;
		if (length == -1) {
			contents = new char[0];
			int contentsLength = 0;
			int charsRead = -1;
			do {
				int available = stream.available();

				// resize contents if needed
				if (contentsLength + available > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new char[contentsLength + available],
						0,
						contentsLength);
				}

				// read as many chars as possible
				charsRead = reader.read(contents, contentsLength, available);

				if (charsRead > 0) {
					// remember length of contents
					contentsLength += charsRead;
				}
			} while (charsRead > 0);

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new char[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new char[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = reader.read(contents, len, length - len);
			}
			// See PR 1FMS89U
		// Now we need to resize in case the default encoding used more than one byte for each
		// character
			if (len != length)
				System.arraycopy(contents, 0, (contents = new char[len]), 0, len);
		}

		return contents;
	}

	void save (StringBuffer buffer, IFile file) throws CModelException {
		byte[] bytes = buffer.toString().getBytes();
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		// use a platform operation to update the resource contents
		try {
			boolean force = true;
			file.setContents(stream, force, true, null); // record history
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}
}
