package org.eclipse.cdt.internal.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;

public abstract class CElement extends PlatformObject implements ICElement {
	
	protected static final CElement[] NO_ELEMENTS = new CElement[0];
	protected int fType;
	
	protected ICElement fParent;

	protected String fName;

	protected int fStartPos;
	protected int fLength;
	protected int fIdStartPos;
	protected int fIdLength;
	protected int fStartLine;
	protected int fEndLine;

	protected CElement(ICElement parent, String name, int type) {
		fParent= parent;
		fName= name;
		fType= type;
	}
	
	// setters

	public void setElementType (int type) {
		fType= type;
	}

	public void setElementName(String name) {
		fName = name;
	}
	
	public void setParent (ICElement parent) {
		fParent = parent;
	}
	
	// getters
	
	public int getElementType() {
		return fType;
	}	

	public String getElementName() {
		return fName;
	}
	
	public ICElement getParent() {
		return fParent;
	}

	public IPath getPath() {
		IResource res = getUnderlyingResource();
		if (res != null)
			return res.getFullPath();
		return new Path(getElementName());
	}

	public boolean exists() {
		try {
			return getElementInfo() != null;
		} catch (CModelException e) {
			// Do not log it, it will fil the .log alarming the user.
			//CCorePlugin.log(e);
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
			ICElement[] children = ((Parent)this).getChildren();
			for (int i = 0; i < children.length; i++) {
				ICElement aChild = children[i];
				if (aChild instanceof ISourceReference) {
					ISourceReference child = (ISourceReference) children[i];
					ISourceRange range = child.getSourceRange();
					int startPos = range.getStartPos();
					int endPos = startPos + range.getLength();
					if (offset < endPos && offset >= startPos) {
						if (child instanceof Parent) {
							return ((Parent)child).getSourceElementAtOffset(offset);
						}
						return (ICElement)child;
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
			ArrayList list = new ArrayList();
			ICElement[] children = ((Parent)this).getChildren();
			for (int i = 0; i < children.length; i++) {
				ICElement aChild = children[i];
				if (aChild instanceof ISourceReference) {
					ISourceReference child = (ISourceReference) children[i];
					ISourceRange range = child.getSourceRange();
					int startPos = range.getStartPos();
					int endPos = startPos + range.getLength();
					if (offset < endPos && offset >= startPos) {
						if (child instanceof Parent) {
							ICElement[] elements = ((Parent)child).getSourceElementsAtOffset(offset);
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
		return new ICElement[]{this};
	}
	
	public boolean isReadOnly () {
		IResource r = getUnderlyingResource();
		if (r != null) {
			return r.isReadOnly();
		}			
		return false;
	}

	public boolean isStructureKnown() throws CModelException {
		return getElementInfo().isStructureKnown();
	}

	public ICModel getCModel () {
		ICElement current = this;
		do {
			if (current instanceof ICModel) return (ICModel) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	public ICProject getCProject() {
		ICElement current = this;
		do {
			if (current instanceof ICProject) return (ICProject) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	protected void addChild(ICElement e) throws CModelException {
	}

	public void setPos(int startPos, int length) {
		fStartPos = startPos;
		fLength = length;
	}
                
	public int getStartPos() {
		return fStartPos;
	}

	public int getLength() {
		return fLength;
	}
        
	public void setIdPos(int startPos, int length) {
		fIdStartPos= startPos;
		fIdLength= length;
	}

	public int getIdStartPos() {
		return fIdStartPos;
	}
        
	public int getIdLength() {
		return fIdLength;
	}

	public int getStartLine() {
		return fStartLine;
	}

	public int getEndLine() {
		return fEndLine;
	}

	public void setLines(int startLine, int endLine) {
		fStartLine = startLine;
		fEndLine = endLine;
	}

	public IResource getUnderlyingResource() {
		IResource res = getResource();
		if (res == null) {
			ICElement p = getParent();
			if (p != null) {
				res = p.getUnderlyingResource();
			}
		}
		return res;
	}

	public abstract IResource getResource() ;

	protected abstract CElementInfo createElementInfo();

	/**
	 * Tests if an element has the same name, type and an equal parent.
	 */
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o instanceof CElement) {
			CElement other = (CElement) o;
			if( fName == null  || other.fName == null ) 
				return false;
			if( fName.length() == 0  || other.fName.length() == 0 ) 
				return false;
			if (fType != other.fType)
				return false;
			if (fName.equals(other.fName)) {
				if (fParent != null && fParent.equals(other.fParent)) {
					return true;
				}
				if (fParent == null && other.fParent == null)
					return true;
			}
		}
		return false;
	}

	public CElementInfo getElementInfo() throws CModelException {
		return getElementInfo(null);
	}

	public CElementInfo getElementInfo (IProgressMonitor monitor) throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		CElementInfo info = (CElementInfo)manager.getInfo(this);
		if (info != null) {
			return info;
		}
		info = createElementInfo();
		openWhenClosed(info, monitor);
		return info;
	}

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
				return "CMODEL";  //$NON-NLS-1$
			case C_PROJECT:
				return "CPROJECT";  //$NON-NLS-1$
			case C_CCONTAINER:
				return "CCONTAINER"; //$NON-NLS-1$
			case C_UNIT:
				if (this instanceof IWorkingCopy) {
					return "WORKING_UNIT";  //$NON-NLS-1$
				}
				return "TRANSLATION_UNIT";  //$NON-NLS-1$
			case C_FUNCTION:
				return "C_FUNCTION";  //$NON-NLS-1$
			case C_FUNCTION_DECLARATION:
				return "C_FUNCTION_DECLARATION";  //$NON-NLS-1$
			case C_VARIABLE:
				return "C_VARIABLE";  //$NON-NLS-1$
			case C_VARIABLE_DECLARATION:
				return "C_VARIABLE_DECLARATION";  //$NON-NLS-1$
			case C_INCLUDE:
				return "C_INCLUDE";  //$NON-NLS-1$
			case C_MACRO:
				return "C_MACRO"; 			 //$NON-NLS-1$
			case C_STRUCT:
				return "C_STRUCT"; //$NON-NLS-1$
			case C_CLASS:
				return "C_CLASS"; //$NON-NLS-1$
			case C_UNION:
				return "C_UNION"; //$NON-NLS-1$
			case C_FIELD:
				return "C_FIELD";  //$NON-NLS-1$
			case C_METHOD:
				return "C_METHOD"; 						 //$NON-NLS-1$
			case C_NAMESPACE:
				return "C_NAMESPACE"; 						 //$NON-NLS-1$
			case C_USING:
				return "C_USING"; 						 //$NON-NLS-1$
			default:
				return "UNKNOWN"; //$NON-NLS-1$
		}
	}

	/**
	 * Runs a C Model Operation
	 */
	protected void runOperation(CModelOperation operation, IProgressMonitor monitor) throws CModelException {
		CModelManager.getDefault().runOperation(operation, monitor);
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
			return (IOpenable)fParent;
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
	protected abstract void generateInfos(Object info, Map newElements, IProgressMonitor monitor) throws CModelException;

	/**
	 * Open a <code>IOpenable</code> that is known to be closed (no check for
	 * <code>isOpen()</code>).
	 */
	protected void openWhenClosed(CElementInfo info, IProgressMonitor pm) throws CModelException {
		CModelManager manager = CModelManager.getDefault();
		boolean hadTemporaryCache = manager.hasTemporaryCache();
		try {
			HashMap newElements = manager.getTemporaryCache();
			generateInfos(info, newElements, pm);
			if (info == null) {
				info = (CElementInfo)newElements.get(this);
			}
			if (info == null) { // a source ref element could not be opened
				// close any buffer that was opened for the openable parent
				Iterator iterator = newElements.keySet().iterator();
				while (iterator.hasNext()) {
					ICElement element = (ICElement)iterator.next();
					if (element instanceof Openable) {
						((Openable)element).closeBuffer();
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
	public ICElement getAncestor(int ancestorType) {
		ICElement element = this;
		while (element != null) {
			if (element.getElementType() == ancestorType) {
				 return element;
			}
			element= element.getParent();
		}
		return null;
	}

	/**
	 * Returns true if this element is an ancestor of the given element,
	 * otherwise false.
	 */
	public boolean isAncestorOf(ICElement e) {
		ICElement parent= e.getParent();
		while (parent != null && !parent.equals(this)) {
			parent= parent.getParent();
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
	public int hashCode() {
		if (fParent == null) return super.hashCode();
		return Util.combineHashCodes(fName.hashCode(), fParent.hashCode());
	}
	
	/*
	 * Test to see if two objects are identical
	 * Subclasses should override accordingly
	 */
	public boolean isIdentical( CElement otherElement){
		return this.equals(otherElement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#accept(org.eclipse.cdt.core.model.ICElementVisitor)
	 */
	public void accept(ICElementVisitor visitor) throws CoreException {
		// Visit me, return right away if the visitor doesn't want to visit my children
		if (!visitor.visit(this))
			return;

		// If I am a Parent, visit my children
		if (this instanceof IParent) {
			ICElement [] children = ((IParent)this).getChildren();
			for (int i = 0; i < children.length; ++i)
				children[i].accept(visitor);
		}
	}

}
