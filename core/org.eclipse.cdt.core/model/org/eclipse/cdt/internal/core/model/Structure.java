/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

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

public class Structure extends StructureDeclaration implements  IStructure {

	Map<String, ASTAccessVisibility> superClassesNames = new TreeMap<String, ASTAccessVisibility>();

	public Structure(ICElement parent, int kind, String name) {
		super(parent, name, kind);
	}

	@Override
	public IField[] getFields() throws CModelException {
		List<ICElement> fields = new ArrayList<ICElement>();
		fields.addAll(getChildrenOfType(ICElement.C_FIELD));
		return fields.toArray(new IField[fields.size()]);
	}

	@Override
	public IField getField(String name) {
		try {
			IField[] fields = getFields();
			for (IField field : fields) {
				if(field.getElementName().equals(name)){
					return field;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	@Override
	public IMethodDeclaration[] getMethods() throws CModelException {
		List<ICElement> methods = new ArrayList<ICElement>();
		methods.addAll(getChildrenOfType(ICElement.C_METHOD_DECLARATION));
		methods.addAll(getChildrenOfType(ICElement.C_METHOD));
		return methods.toArray(new IMethodDeclaration[methods.size()]);
	}

	@Override
	public IMethodDeclaration getMethod(String name) {
		try {
			IMethodDeclaration[] methods = getMethods();
			for (IMethodDeclaration method : methods) {
				if(method.getElementName().equals(name)){
					return method;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	@Override
	public boolean isAbstract() throws CModelException {
		IMethodDeclaration[] methods = getMethods();
		for (IMethodDeclaration method : methods) {
			if(method.isPureVirtual())
				return true;
		}
		return false;
	}

	@Override
	public String[] getSuperClassesNames(){
		return superClassesNames.keySet().toArray(new String[superClassesNames.keySet().size()]);
	}

	@Override
	public ASTAccessVisibility getSuperClassAccess(String name){
		return superClassesNames.get(name);
	}

	public void addSuperClass(String name) {
		superClassesNames.put(name, ASTAccessVisibility.PUBLIC);
	}

	public void addSuperClass(String name, ASTAccessVisibility access) {
		superClassesNames.put(name, access);
	}

}
