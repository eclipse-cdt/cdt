package org.eclipse.cdt.internal.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICRoot;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.CModelException;

public abstract class CElement extends PlatformObject implements ICElement {
	
	protected int fType;
	
	protected ICElement fParent;

	protected CElementInfo fCElementInfo;
	
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
		fCElementInfo = null;
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
		return getElementInfo().isReadOnly();
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
		if (fCElementInfo == null) {
			fCElementInfo = createElementInfo();
		}
		return fCElementInfo;
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
}
