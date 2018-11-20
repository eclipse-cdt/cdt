/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.util.MementoTokenizer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;

public abstract class CElement extends PlatformObject implements ICElement {
	public static final char CEM_ESCAPE = '\\';
	public static final char CEM_CPROJECT = '=';
	public static final char CEM_SOURCEROOT = '/';
	public static final char CEM_SOURCEFOLDER = '<';
	public static final char CEM_TRANSLATIONUNIT = '{';
	public static final char CEM_SOURCEELEMENT = '[';
	public static final char CEM_PARAMETER = '(';
	public static final char CEM_ELEMENTTYPE = '#';

	protected static final CElement[] NO_ELEMENTS = {};

	protected int fType;
	protected ICElement fParent;
	protected String fName;

	protected CElement(ICElement parent, String name, int type) {
		fParent = parent;
		fName = name;
		fType = type;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		// handle all kinds of resources
		if (IResource.class.isAssignableFrom(adapter)) {
			IResource r = getResource();
			if (r != null && adapter.isAssignableFrom(r.getClass())) {
				return r;
			}
		}
		return super.getAdapter(adapter);
	}

	// setters

	public void setElementType(int type) {
		fType = type;
	}

	public void setElementName(String name) {
		fName = name;
	}

	public void setParent(ICElement parent) {
		fParent = parent;
	}

	// getters

	@Override
	public int getElementType() {
		return fType;
	}

	@Override
	public String getElementName() {
		return fName;
	}

	@Override
	public ICElement getParent() {
		return fParent;
	}

	@Override
	public IPath getPath() {
		IResource res = getUnderlyingResource();
		if (res != null)
			return res.getFullPath();
		return new Path(getElementName());
	}

	@Override
	public URI getLocationURI() {
		IResource res = getUnderlyingResource();
		return res == null ? null : res.getLocationURI();
	}

	@Override
	public boolean exists() {
		try {
			return getElementInfo() != null;
		} catch (CModelException e) {
			// Do not log it, otherwise it would fill the .log alarming the user.
			return false;
		}
	}

	/**
	 * Returns the element that is located at the given source offset
	 * in this element.  This is a helper method for <code>ITranslationUnit#getElementAtOffset</code>,
	 * and only works on compilation units and types. The offset given is
	 * known to be within this element's source range already, and if no finer
	 * grained element is found at the offset, this element is returned.
	 */
	protected ICElement getSourceElementAtOffset(int offset) throws CModelException {
		if (this instanceof ISourceReference && this instanceof Parent) {
			ICElement[] children = ((Parent) this).getChildren();
			for (ICElement aChild : children) {
				if (aChild instanceof ISourceReference) {
					ISourceReference child = (ISourceReference) aChild;
					ISourceRange range = child.getSourceRange();
					int startPos = range.getStartPos();
					int endPos = startPos + range.getLength();
					if (offset < endPos && offset >= startPos) {
						if (child instanceof Parent) {
							return ((Parent) child).getSourceElementAtOffset(offset);
						}
						return (ICElement) child;
					}
				}
			}
		} else {
			// should not happen
			//Assert.isTrue(false);
		}
		return this;
	}

	/**
	 * Returns the elements that are located at the given source offset
	 * in this element.  This is a helper method for <code>ITranslationUnit#getElementAtOffset</code>,
	 * and only works on compilation units and types. The offset given is
	 * known to be within this element's source range already, and if no finer
	 * grained element is found at the offset, this element is returned.
	 */
	protected ICElement[] getSourceElementsAtOffset(int offset) throws CModelException {
		if (this instanceof ISourceReference && this instanceof Parent) {
			ArrayList<Object> list = new ArrayList<>();
			ICElement[] children = ((Parent) this).getChildren();
			for (ICElement aChild : children) {
				if (aChild instanceof ISourceReference) {
					ISourceReference child = (ISourceReference) aChild;
					ISourceRange range = child.getSourceRange();
					int startPos = range.getStartPos();
					int endPos = startPos + range.getLength();
					if (offset < endPos && offset >= startPos) {
						if (child instanceof Parent) {
							ICElement[] elements = ((Parent) child).getSourceElementsAtOffset(offset);
							list.addAll(Arrays.asList(elements));
						}
						list.add(child);
					}
				}
			}
			children = new ICElement[list.size()];
			list.toArray(children);
			return children;
		}
		return new ICElement[] { this };
	}

	@Override
	public boolean isReadOnly() {
		IResource r = getUnderlyingResource();
		if (r != null) {
			ResourceAttributes attributes = r.getResourceAttributes();
			if (attributes != null) {
				return attributes.isReadOnly();
			}
		}
		return false;
	}

	@Override
	public boolean isStructureKnown() throws CModelException {
		return getElementInfo().isStructureKnown();
	}

	@Override
	public ICModel getCModel() {
		ICElement current = this;
		do {
			if (current instanceof ICModel)
				return (ICModel) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	@Override
	public ICProject getCProject() {
		ICElement current = this;
		do {
			if (current instanceof ICProject)
				return (ICProject) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	protected void addChild(ICElement e) throws CModelException {
	}

	@Override
	public IResource getUnderlyingResource() {
		ICElement current = this;
		do {
			IResource res = getResource();
			if (res != null)
				return res;
		} while ((current = current.getParent()) != null);
		return null;
	}

	@Override
	public abstract IResource getResource();

	protected abstract CElementInfo createElementInfo();

	/**
	 * Tests if an element has the same name, type and an equal parent.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof ICElement) {
			return equals(this, (ICElement) o);
		}
		return false;
	}

	public static boolean equals(ICElement lhs, ICElement rhs) {
		if (lhs == rhs) {
			return true;
		}
		if (lhs.getElementType() != rhs.getElementType()) {
			return false;
		}
		String lhsName = lhs.getElementName();
		String rhsName = rhs.getElementName();
		if (lhsName == null || rhsName == null || lhsName.length() != rhsName.length() || !lhsName.equals(rhsName)) {
			return false;
		}

		if (lhs instanceof ISourceReference && rhs instanceof ISourceReference) {
			if (((ISourceReference) lhs).getIndex() != ((ISourceReference) rhs).getIndex()) {
				return false;
			}
		}

		ICElement lhsParent = lhs.getParent();
		ICElement rhsParent = rhs.getParent();
		if (lhsParent == rhsParent) {
			return true;
		}

		return lhsParent != null && lhsParent.equals(rhsParent);
	}

	public CElementInfo getElementInfo() throws CModelException {
		return getElementInfo(null);
	}

	public CElementInfo getElementInfo(IProgressMonitor monitor) throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		CElementInfo info = (CElementInfo) manager.getInfo(this);
		if (info != null) {
			return info;
		}
		info = createElementInfo();
		openWhenClosed(info, monitor);
		return info;
	}

	@Override
	public String toString() {
		return getElementName();
	}

	public String toDebugString() {
		return getElementName() + " " + getTypeString(); //$NON-NLS-1$
	}

	// util
	public String getTypeString() {
		switch (getElementType()) {
		case C_MODEL:
			return "CMODEL"; //$NON-NLS-1$
		case C_PROJECT:
			return "CPROJECT"; //$NON-NLS-1$
		case C_CCONTAINER:
			if (this instanceof ISourceRoot) {
				return "SOURCE_ROOT"; //$NON-NLS-1$
			}
			return "CCONTAINER"; //$NON-NLS-1$
		case C_UNIT:
			if (this instanceof IWorkingCopy) {
				return "WORKING_UNIT"; //$NON-NLS-1$
			}
			return "TRANSLATION_UNIT"; //$NON-NLS-1$
		case C_FUNCTION:
			return "C_FUNCTION"; //$NON-NLS-1$
		case C_FUNCTION_DECLARATION:
			return "C_FUNCTION_DECLARATION"; //$NON-NLS-1$
		case C_VARIABLE:
			return "C_VARIABLE"; //$NON-NLS-1$
		case C_VARIABLE_DECLARATION:
			return "C_VARIABLE_DECLARATION"; //$NON-NLS-1$
		case C_INCLUDE:
			return "C_INCLUDE"; //$NON-NLS-1$
		case C_MACRO:
			return "C_MACRO"; //$NON-NLS-1$
		case C_STRUCT:
			return "C_STRUCT"; //$NON-NLS-1$
		case C_CLASS:
			return "C_CLASS"; //$NON-NLS-1$
		case C_UNION:
			return "C_UNION"; //$NON-NLS-1$
		case C_FIELD:
			return "C_FIELD"; //$NON-NLS-1$
		case C_METHOD:
			return "C_METHOD"; //$NON-NLS-1$
		case C_NAMESPACE:
			return "C_NAMESPACE"; //$NON-NLS-1$
		case C_USING:
			return "C_USING"; //$NON-NLS-1$
		case C_VCONTAINER:
			return "C_CONTAINER"; //$NON-NLS-1$
		case C_BINARY:
			return "C_BINARY"; //$NON-NLS-1$
		case C_ARCHIVE:
			return "C_ARCHIVE"; //$NON-NLS-1$
		default:
			return "UNKNOWN"; //$NON-NLS-1$
		}
	}

	/**
	 * Close the C Element
	 * @throws CModelException
	 */
	public void close() throws CModelException {
		CModelManager.getDefault().releaseCElement(this);
	}

	/**
	 * This element is being closed.  Do any necessary cleanup.
	 */
	protected void closing(Object info) throws CModelException {
	}

	/**
	 * This element has just been opened.  Do any necessary setup.
	 */
	protected void opening(Object info) {
	}

	/**
	 * Return the first instance of IOpenable in the parent
	 * hierarchy of this element.
	 *
	 * <p>Subclasses that are not IOpenable's must override this method.
	 */
	public IOpenable getOpenableParent() {
		if (fParent instanceof IOpenable) {
			return (IOpenable) fParent;
		}
		return null;
	}

	/**
	 * Builds this element's structure and properties in the given
	 * info object, based on this element's current contents (i.e. buffer
	 * contents if this element has an open buffer, or resource contents
	 * if this element does not have an open buffer). Children
	 * are placed in the given newElements table (note, this element
	 * has already been placed in the newElements table). Returns true
	 * if successful, or false if an error is encountered while determining
	 * the structure of this element.
	 */
	protected abstract void generateInfos(CElementInfo info, Map<ICElement, CElementInfo> newElements,
			IProgressMonitor monitor) throws CModelException;

	/**
	 * Open a <code>IOpenable</code> that is known to be closed (no check for
	 * <code>isOpen()</code>).
	 */
	protected void openWhenClosed(CElementInfo info, IProgressMonitor pm) throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		boolean hadTemporaryCache = manager.hasTemporaryCache();
		try {
			Map<ICElement, CElementInfo> newElements = manager.getTemporaryCache();
			generateInfos(info, newElements, pm);
			if (info == null) {
				info = newElements.get(this);
			}
			if (info == null) {
				// A source ref element could not be opened.
				// Close any buffer that was opened for the openable parent.
				Iterator<ICElement> iterator = newElements.keySet().iterator();
				while (iterator.hasNext()) {
					ICElement element = iterator.next();
					if (element instanceof Openable) {
						((Openable) element).closeBuffer();
					}
				}
				throw newNotPresentException();
			}
			if (!hadTemporaryCache) {
				manager.putInfos(this, newElements);
			}
		} finally {
			if (!hadTemporaryCache) {
				manager.resetTemporaryCache();
			}
		}
	}

	/**
	 * @see ICElement
	 */
	@Override
	public ICElement getAncestor(int ancestorType) {
		ICElement element = this;
		while (element != null) {
			if (element.getElementType() == ancestorType) {
				return element;
			}
			element = element.getParent();
		}
		return null;
	}

	/**
	 * Returns true if this element is an ancestor of the given element,
	 * otherwise false.
	 */
	public boolean isAncestorOf(ICElement e) {
		ICElement parent = e.getParent();
		while (parent != null && !parent.equals(this)) {
			parent = parent.getParent();
		}
		return parent != null;
	}

	/**
	 * Creates and returns and not present exception for this element.
	 */
	protected CModelException newNotPresentException() {
		return new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
	}

	/**
	 * Returns the hash code for this Java element. By default,
	 * the hash code for an element is a combination of its name
	 * and parent's hash code. Elements with other requirements must
	 * override this method.
	 */
	// CHECKPOINT: making not equal objects seem equal
	// What elements should override this?
	@Override
	public int hashCode() {
		return hashCode(this);
	}

	public static int hashCode(ICElement elem) {
		ICElement parent = elem.getParent();
		if (parent == null) {
			return System.identityHashCode(elem);
		}
		return Util.combineHashCodes(elem.getElementName().hashCode(), parent.hashCode());
	}

	/**
	 * Checks if two objects are identical
	 * Subclasses should override accordingly
	 */
	public boolean isIdentical(CElement otherElement) {
		return this.equals(otherElement);
	}

	@Override
	public void accept(ICElementVisitor visitor) throws CoreException {
		// Visit me, return right away if the visitor doesn't want to visit my children
		if (!visitor.visit(this))
			return;

		// If I am a Parent, visit my children
		if (this instanceof IParent) {
			ICElement[] children = ((IParent) this).getChildren();
			for (int i = 0; i < children.length; ++i) {
				children[i].accept(visitor);
			}
		}
	}

	@Override
	public String getHandleIdentifier() {
		return getHandleMemento();
	}

	/**
	 * Builds a string representation of this element.
	 *
	 * @return  the string representation
	 */
	public String getHandleMemento() {
		StringBuilder buff = new StringBuilder();
		getHandleMemento(buff);
		return buff.toString();
	}

	/**
	 * Append this elements memento string to the given buffer.
	 *
	 * @param buff  the buffer building the memento string
	 */
	public void getHandleMemento(StringBuilder buff) {
		((CElement) getParent()).getHandleMemento(buff);
		buff.append(getHandleMementoDelimiter());
		escapeMementoName(buff, getElementName());
	}

	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */
	protected abstract char getHandleMementoDelimiter();

	/**
	 * Creates a C element handle from the given memento.
	 *
	 * @param memento  the memento tokenizer
	 */
	public ICElement getHandleFromMemento(MementoTokenizer memento) {
		if (!memento.hasMoreTokens())
			return this;
		String token = memento.nextToken();
		return getHandleFromMemento(token, memento);
	}

	/**
	 * Creates a C element handle from the given memento.
	 * The given token is the current delimiter indicating the type of the next token(s).
	 *
	 * @param token  the curren memento token
	 * @param memento  the memento tokenizer
	 */
	public abstract ICElement getHandleFromMemento(String token, MementoTokenizer memento);

	/**
	 * Escape special characters in the given name and append the result to buffer.
	 *
	 * @param buffer  the buffer to build the memento string
	 * @param mementoName  the name to escape
	 */
	public static void escapeMementoName(StringBuilder buffer, String mementoName) {
		for (int i = 0, length = mementoName.length(); i < length; i++) {
			char character = mementoName.charAt(i);
			switch (character) {
			case CEM_ESCAPE:
			case CEM_CPROJECT:
			case CEM_TRANSLATIONUNIT:
			case CEM_SOURCEROOT:
			case CEM_SOURCEFOLDER:
			case CEM_SOURCEELEMENT:
			case CEM_ELEMENTTYPE:
			case CEM_PARAMETER:
				buffer.append(CEM_ESCAPE);
			}
			buffer.append(character);
		}
	}
}
