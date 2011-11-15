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
				if (!wrapper.setter.exists() && !wrapper.field.getDeclSpecifier().isConst()) {
					wrapper.childNodes.add(createSetterInserter(wrapper.field));
				}
			}
			children = wrapper.getChildNodes();
		}
		return children.toArray();
	}

	public GetterSetterInsertEditProvider createGetterInserter(IASTSimpleDeclaration simpleDeclaration) {
		IASTName fieldName = getFieldDeclarationName(simpleDeclaration);
		return new GetterSetterInsertEditProvider(fieldName, simpleDeclaration, AccessorKind.GETTER);
	}

	public GetterSetterInsertEditProvider createSetterInserter(IASTSimpleDeclaration simpleDeclaration) {
		IASTName fieldName = getFieldDeclarationName(simpleDeclaration);
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
			for (IASTSimpleDeclaration currentField : existingFields) {
				FieldWrapper wrapper = new FieldWrapper();
				wrapper.field = currentField;
				wrapper.getter = getGetterForField(currentField);
				wrapper.setter = getSetterForField(currentField);
				if (wrapper.missingGetterOrSetter()) {
					wrappedFields.add(wrapper);
				}
			}
		}
		return wrappedFields;
	}

	private FunctionWrapper getGetterForField(IASTSimpleDeclaration currentField) {
		FunctionWrapper wrapper = new FunctionWrapper();
		String name = GetterSetterNameGenerator.generateGetterName(getFieldDeclarationName(currentField));
		setFunctionToWrapper(wrapper, name);
		return wrapper;
	}

	private IASTName getFieldDeclarationName(IASTSimpleDeclaration fieldDeclaration) {
		IASTDeclarator declarator = fieldDeclaration.getDeclarators()[0];
		while (declarator.getNestedDeclarator() != null) {
			declarator = declarator.getNestedDeclarator();
		}
		return declarator.getName();
	}
	
	private FunctionWrapper getSetterForField(IASTSimpleDeclaration currentField) {
		FunctionWrapper wrapper = new FunctionWrapper();
		String name = GetterSetterNameGenerator.generateSetterName(getFieldDeclarationName(currentField));
		setFunctionToWrapper(wrapper, name);
		return wrapper;
	}

	private void setFunctionToWrapper(FunctionWrapper wrapper, String getterName) {
		for (IASTFunctionDefinition currentDefinition : existingFunctionDefinitions) {
			if (currentDefinition.getDeclarator().getName().toString().endsWith(getterName)) {
				wrapper.functionDefinition = currentDefinition;
			}
		}
		
		for (IASTSimpleDeclaration currentDeclaration : existingFunctionDeclarations) {
			if (getFieldDeclarationName(currentDeclaration).toString().endsWith(getterName)) {
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
