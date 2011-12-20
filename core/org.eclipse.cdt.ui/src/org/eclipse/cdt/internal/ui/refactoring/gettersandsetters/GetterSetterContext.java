/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GetterSetterInsertEditProvider.AccessorKind;

public class GetterSetterContext implements ITreeContentProvider {
	public ArrayList<IASTSimpleDeclaration> existingFields = new ArrayList<IASTSimpleDeclaration>();
	public ArrayList<IASTFunctionDefinition> existingFunctionDefinitions = new ArrayList<IASTFunctionDefinition>();
	public ArrayList<IASTSimpleDeclaration> existingFunctionDeclarations = new ArrayList<IASTSimpleDeclaration>();
	public SortedSet<GetterSetterInsertEditProvider> selectedFunctions = new TreeSet<GetterSetterInsertEditProvider>();
	public IASTName selectedName;
	private ArrayList<FieldWrapper> wrappedFields;
	private boolean definitionSeparate;

	@Override
	public Object[] getChildren(Object parentElement) {
		ArrayList<GetterSetterInsertEditProvider> children = new ArrayList<GetterSetterInsertEditProvider>();
		if (parentElement instanceof FieldWrapper) {
			FieldWrapper wrapper = (FieldWrapper) parentElement;
			
			if (wrapper.getChildNodes().isEmpty()) {
				if (!wrapper.getter.exists()) {
					wrapper.childNodes.add(createGetterInserter(wrapper.field));
				}
				if (!wrapper.setter.exists() && isAssignable(wrapper.field)) {
					wrapper.childNodes.add(createSetterInserter(wrapper.field));
				}
			}
			children = wrapper.getChildNodes();
		}
		return children.toArray();
	}

	public GetterSetterInsertEditProvider createGetterInserter(IASTSimpleDeclaration simpleDeclaration) {
		IASTName fieldName = getDeclarationName(simpleDeclaration);
		return new GetterSetterInsertEditProvider(fieldName, simpleDeclaration, AccessorKind.GETTER);
	}

	public GetterSetterInsertEditProvider createSetterInserter(IASTSimpleDeclaration simpleDeclaration) {
		IASTName fieldName = getDeclarationName(simpleDeclaration);
		return new GetterSetterInsertEditProvider(fieldName, simpleDeclaration, AccessorKind.SETTER);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof FieldWrapper) {
			FieldWrapper wrapper = (FieldWrapper) element;
			return wrapper.missingGetterOrSetter();
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getWrappedFields().toArray();
	}
	
	public void refresh() {
		// We only recreate the function declarations instead of recreating
		// GetterSetterInsertEditProviders. That way, selectedFunctions is still valid.
		// Also, the objects inside the TreeViewer are still the same, which is convenient because
		// that way we don't need to save then restore the collapsed/expanded+checked/unchecked
		// state of the TreeViewer.
		for (FieldWrapper wrapper : wrappedFields) {
			for (GetterSetterInsertEditProvider provider : wrapper.childNodes) {
				provider.createFunctionDeclaration();
			}
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	public boolean isDefinitionSeparate() {
		return definitionSeparate;
	}

	public void setDefinitionSeparate(boolean definitionSeparate) {
		this.definitionSeparate = definitionSeparate;
	}

	private ArrayList<FieldWrapper> getWrappedFields() {
		if (wrappedFields == null) {
			wrappedFields = new ArrayList<FieldWrapper>();
			for (IASTSimpleDeclaration field : existingFields) {
				FieldWrapper wrapper = new FieldWrapper();
				wrapper.field = field;
				wrapper.getter = getGetterForField(field);
				wrapper.setter = getSetterForField(field);
				if (wrapper.missingGetterOrSetter()) {
					wrappedFields.add(wrapper);
				}
			}
		}
		return wrappedFields;
	}

	private FunctionWrapper getGetterForField(IASTSimpleDeclaration currentField) {
		FunctionWrapper wrapper = new FunctionWrapper();
		String name = GetterSetterNameGenerator.generateGetterName(getDeclarationName(currentField));
		setFunctionToWrapper(wrapper, name);
		return wrapper;
	}

	private FunctionWrapper getSetterForField(IASTSimpleDeclaration field) {
		FunctionWrapper wrapper = new FunctionWrapper();
		String name = GetterSetterNameGenerator.generateSetterName(getDeclarationName(field));
		setFunctionToWrapper(wrapper, name);
		return wrapper;
	}

	private static IASTName getDeclarationName(IASTSimpleDeclaration declaration) {
		IASTDeclarator declarator = declaration.getDeclarators()[0];
		while (declarator.getNestedDeclarator() != null) {
			declarator = declarator.getNestedDeclarator();
		}
		return declarator.getName();
	}

	private static boolean isAssignable(IASTSimpleDeclaration declaration) {
		IASTName name = getDeclarationName(declaration);
		IBinding binding = name.resolveBinding();
		if (!(binding instanceof ICPPField))
			return false;
		ICPPField field = (ICPPField) binding;
		IType type = field.getType();
		type = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
		if (type instanceof IArrayType || type instanceof ICPPReferenceType)
			return false;
		if (type instanceof IPointerType && ((IPointerType) type).isConst())
			return false;
		if (type instanceof IQualifierType && ((IQualifierType) type).isConst())
			return false;
		return true;
	}

	private void setFunctionToWrapper(FunctionWrapper wrapper, String accessorName) {
		for (IASTFunctionDefinition currentDefinition : existingFunctionDefinitions) {
			if (currentDefinition.getDeclarator().getName().toString().equals(accessorName)) {
				wrapper.functionDefinition = currentDefinition;
			}
		}
		
		for (IASTSimpleDeclaration currentDeclaration : existingFunctionDeclarations) {
			if (getDeclarationName(currentDeclaration).toString().equals(accessorName)) {
				wrapper.functionDeclaration = currentDeclaration;
			}
		}
	}

	protected class FieldWrapper {
		protected IASTSimpleDeclaration field;
		protected FunctionWrapper getter;
		protected FunctionWrapper setter;
		protected ArrayList<GetterSetterInsertEditProvider> childNodes = new ArrayList<GetterSetterInsertEditProvider>(2);
		
		@Override
		public String toString() {
			IASTDeclarator declarator = field.getDeclarators()[0];
			while (declarator.getNestedDeclarator() != null) {
				declarator = declarator.getNestedDeclarator();
			}
			return declarator.getName().toString();
		}

		public ArrayList<GetterSetterInsertEditProvider> getChildNodes() {
			return childNodes;
		}

		public boolean missingGetterOrSetter() {
			return !getter.exists() || !setter.exists();
		}
	}
	
	protected class FunctionWrapper {
		protected IASTSimpleDeclaration functionDeclaration;
		protected IASTFunctionDefinition functionDefinition;
		
		public boolean exists() {
			return functionDeclaration != null || functionDefinition != null;
		}
	}
}
