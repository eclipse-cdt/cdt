package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IField;

public class Structure extends SourceManipulation implements IStructure {
	
	String [] baseTypes;

	public Structure(ICElement parent, int kind, String name) {
		super(parent, name, kind);
		baseTypes = new String[0];
	}

	public IField[] getFields() {
		return new IField[0];
	}

	public IField getField(String name) {
		return null;
	}

	public IMethod[] getMethods() {
		return new IMethod[0];
	}

	public IMethod getMethod(String name) {
		return null;
	}

	public boolean isUnion() {
		return getElementType() == ICElement.C_UNION;
	}

	public boolean isClass() {
		return getElementType() == ICElement.C_CLASS;
	}

	public boolean isStruct() {
		return getElementType() == ICElement.C_STRUCT;
	}

	public boolean isAbstract() {
		return false;
	}

	public int getAccessControl() throws CModelException {
		return 0;
	}

	/**
	 * Return the inherited structures.
	 * @IInheritance
	 */
	public IStructure [] getBaseTypes() throws CModelException {
		return new IStructure[0];
	}

	/**
	 * @see IVariable
	 */
	public String getType() {
		if (isClass())
			return "class";
		if (isUnion())
			return "union";
		return "struct";
	}

	/**
	 * @see IVariable
	 */
	public String getInitializer() {
		return "";
	}

	public void addSuperClass(String name) {
		String[] newBase = new String[baseTypes.length + 1];
		System.arraycopy(baseTypes, 0, newBase, 0, baseTypes.length);
		newBase[baseTypes.length] = name;
		baseTypes = newBase;
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}

	/**
	 * Return the access control for each inherited structure.
	 * @IInheritance
	 */
	public int getAccessControl(int pos) throws CModelException {
		return 0;
	}


	/**
	 * @see org.eclipse.cdt.core.model.IVariableDeclaration#getAccesControl()
	 */
	public int getAccesControl() {
		return 0;
	}

}
