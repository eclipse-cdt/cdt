package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class Structure extends SourceManipulation implements  IStructure {
	
	Map superClassesNames = new TreeMap();

	public Structure(ICElement parent, int kind, String name) {
		super(parent, name, kind);
	}

	public IField[] getFields() throws CModelException {
		List fields = new ArrayList();
		fields.addAll(getChildrenOfType(ICElement.C_FIELD));
		return (IField[]) fields.toArray(new IField[fields.size()]);
	}

	public IField getField(String name) {
		try {
			IField[] fields = getFields();
			for (int i = 0; i<fields.length; i++){
				IField field = fields[i];
				if(field.getElementName().equals(name)){
					return field;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	public IMethodDeclaration[] getMethods() throws CModelException {
		List methods = new ArrayList();
		methods.addAll(getChildrenOfType(ICElement.C_METHOD_DECLARATION));
		methods.addAll(getChildrenOfType(ICElement.C_METHOD));
		return (IMethodDeclaration[])methods.toArray(new IMethodDeclaration[methods.size()]);
	}

	public IMethodDeclaration getMethod(String name) {
		try {
			IMethodDeclaration[] methods = getMethods();
			for (int i = 0; i<methods.length; i++){
				IMethodDeclaration method = methods[i];
				if(method.getElementName().equals(name)){
					return method;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	public boolean isUnion() throws CModelException {
		return getStructureInfo().isUnion();
	}

	public boolean isClass() throws CModelException {
		return getStructureInfo().isClass();
	}

	public boolean isStruct() throws CModelException {
		return getStructureInfo().isStruct();
	}

	public boolean isAbstract() throws CModelException {
		IMethodDeclaration[] methods = getMethods();
		for(int i=0; i<methods.length; i++){
			IMethodDeclaration method = methods[i];
			if(method.isPureVirtual())
				return true;
		}
		return false;
	}

	public String[] getSuperClassesNames(){
		return (String[])superClassesNames.keySet().toArray(new String[superClassesNames.keySet().size()]);
	}

	public ASTAccessVisibility getSuperClassAccess(String name){
		return (ASTAccessVisibility)superClassesNames.get(name);
	}

	public String getTypeName() throws CModelException {
		return getStructureInfo().getTypeName();
	}
	
	public void setTypeName(String type) throws CModelException{
		getStructureInfo().setTypeString(type);
	}
	
	public boolean isConst() throws CModelException {
		return getStructureInfo().isConst();
	}

	public void setConst(boolean isConst) throws CModelException {
		getStructureInfo().setConst(isConst);
	}

	public boolean isVolatile() throws CModelException {
		return getStructureInfo().isVolatile();
	}

	public void setVolatile(boolean isVolatile) throws CModelException {
		getStructureInfo().setVolatile(isVolatile);
	}

	public boolean isStatic() throws CModelException {
		return getStructureInfo().isStatic();
	}
	
	public void setStatic(boolean isStatic) throws CModelException {
		getStructureInfo().setStatic(isStatic);
	}
	
	public StructureInfo getStructureInfo() throws CModelException{
		return (StructureInfo) getElementInfo();
	}

	public void addSuperClass(String name) {
		superClassesNames.put(name, ASTAccessVisibility.PUBLIC);
	}

	public void addSuperClass(String name, ASTAccessVisibility access) {
		superClassesNames.put(name, access);
	}

	protected CElementInfo createElementInfo () {
		return new StructureInfo(this);
	}

}
