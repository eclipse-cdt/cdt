package org.eclipse.cdt.internal.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICOpenable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ICRoot;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

public abstract class CElement extends PlatformObject implements ICElement {
	
	protected int fType;
	
	protected ICElement fParent;

	protected String fName;

	protected int fStartPos;
	protected int fLength;
	protected int fIdStartPos;
	protected int fIdLength;
	protected int fStartLine;
	protected int fEndLine;

	protected CElement[] empty = new CElement[0];
	
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
		try {
			IResource res = getUnderlyingResource();
			if (res != null)
				return res.getFullPath();
		} catch (CModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean exists() {
		try {
			return getResource() != null;
		} catch (CModelException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isReadOnly () {
		try {
			IResource r = getUnderlyingResource();
			if (r != null) {
				return r.isReadOnly();
			}			
		} catch (CModelException e) {
		}
		return false;
	}

	public boolean isStructureKnown() throws CModelException {
		return getElementInfo().isStructureKnown();
	}

	public ICRoot getCRoot () {
		return getParent().getCRoot();
	}

	public ICProject getCProject() {
		return getParent().getCProject();
	}

	protected void addChild(ICElement e) {
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


	public abstract IResource getUnderlyingResource() throws CModelException;

	public abstract IResource getResource() throws CModelException;

	protected abstract CElementInfo createElementInfo();

	/**
	 * Finds a member corresponding to a give element.
	 */     
	//public ICElement findEqualMember(ICElement elem) {
	//	if (this instanceof IParent) {
	//		ICElement[] members = ((IParent)this).getChildren();
	//		if (members != null) {          
	//			for (int i= members.length - 1; i >= 0; i--) {
	//			ICElement curr= members[i];
	//			if (curr.equals(elem)) {
	//				return curr;
	//			} else {
	//				ICElement res= curr.findEqualMember(elem);
	//				if (res != null) {
	//					return res;
	//				}
	//			}
	//		}
	//	}
	//	return null;
	//}

	/**
	 * Tests if an element has the same name, type and an equal parent.
	 */
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o instanceof CElement) {
			CElement other = (CElement) o;
			try {
				IResource tres = getResource();
				IResource ores = other.getResource();
				if (ores != null && tres != null) {
					return tres.equals(ores);
				}
			} catch (CModelException e) {
				//e.printStackTrace();
			}
			if (fType != other.fType)
				return false;
			if (other.fName != null && fName.equals(other.fName)) {
				if (fParent != null && fParent.equals(other.fParent)) {
					return true;
				}
				if (fParent == null && other.fParent == null)
					return true;
			}
		}
		return false;
	}
	
	public CElementInfo getElementInfo () {
		try {
			CModelManager manager;
			synchronized(manager = CModelManager.getDefault()){
				Object info = manager.getInfo(this);
				if (info == null) {
					openHierarchy();
					info= manager.getInfo(this);
					if (info == null) {
						throw newNotPresentException();
					}
				}
				return (CElementInfo)info;
			}
		} catch(CModelException e) {
			return null;
		}
	}

	public String toString() {
		return getElementName();
	}

	public String toDebugString() {
			return getElementName() + " " + getTypeString(getElementType());
	}

	// util
	public static String getTypeString(int type) {
		switch (type) {
			case C_ROOT:
				return "CROOT"; 
			case C_PROJECT:
				return "CPROJECT"; 
			case C_FOLDER:
				return "CFOLDER"; 
			case C_FILE:
				return "CFILE"; 
			case C_FUNCTION:
				return "C_FUNCTION"; 
			case C_FUNCTION_DECLARATION:
				return "C_FUNCTION_DECLARATION"; 
			case C_VARIABLE:
				return "C_VARIABLE"; 
			case C_VARIABLE_DECLARATION:
				return "C_VARIABLE_DECLARATION"; 
			case C_INCLUDE:
				return "C_INCLUDE"; 
			case C_MACRO:
				return "C_MACRO"; 			
			case C_STRUCT:
				return "C_STRUCT";
			case C_CLASS:
				return "C_CLASS";
			case C_UNION:
				return "C_UNION";
			case C_FIELD:
				return "C_FIELD"; 
			case C_METHOD:
				return "C_METHOD"; 						
			default:
				return "UNKNOWN";
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
		Object info = CModelManager.getDefault().peekAtInfo(this);
		if (info != null) {
			if (this instanceof IParent) {
				ICElement[] children = ((CElementInfo) info).getChildren();
				for (int i = 0, size = children.length; i < size; ++i) {
					CElement child = (CElement) children[i];
					child.close();
				}
			}
			closing(info);
			CModelManager.getDefault().removeInfo(this);
		}
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
	public ICOpenable getOpenableParent() {
		
		return (ICOpenable)fParent;
	}


	/**
	 * Opens this element and all parents that are not already open.
	 *
	 * @exception CModelException this element is not present or accessible
	 */
	protected void openHierarchy() throws CModelException {
		if (this instanceof ICOpenable) {
			((CResource) this).openWhenClosed(null);
		} else {
			CResource openableParent = (CResource)getOpenableParent();
			if (openableParent != null) {
				CElementInfo openableParentInfo = (CElementInfo) CModelManager.getDefault().getInfo((ICElement) openableParent);
				if (openableParentInfo == null) {
					openableParent.openWhenClosed(null);
				} else {
					CModelManager.getDefault().putInfo( this, createElementInfo());
				}
			}
		}
	}	
	/**
	 * Returns true if this element is an ancestor of the given element,
	 * otherwise false.
	 */
	protected boolean isAncestorOf(ICElement e) {
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
	 * Removes all cached info from the C Model, including all children,
	 * but does not close this element.
	 */
	protected void removeInfo() {
		Object info = CModelManager.getDefault().peekAtInfo(this);
		if (info != null) {
			if (this instanceof IParent) {
				ICElement[] children = ((CElementInfo)info).getChildren();
				for (int i = 0, size = children.length; i < size; ++i) {
					CElement child = (CElement) children[i];
					child.removeInfo();
				}
			}
			CModelManager.getDefault().removeInfo(this);
		}
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
	
}
