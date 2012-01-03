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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.AccessorDescriptor.AccessorKind;

public class GetterSetterContext implements ITreeContentProvider {
	final List<IASTSimpleDeclaration> existingFields = new ArrayList<IASTSimpleDeclaration>();
	final List<IASTFunctionDefinition> existingFunctionDefinitions = new ArrayList<IASTFunctionDefinition>();
	final List<IASTSimpleDeclaration> existingFunctionDeclarations = new ArrayList<IASTSimpleDeclaration>();
	final SortedSet<AccessorDescriptor> selectedAccessors = new TreeSet<AccessorDescriptor>();
	IASTName selectedName;
	private List<FieldDescriptor> fieldDescriptors;
	private boolean definitionSeparate;
	private static final Object[] NO_CHILDREN = {};

	@Override
	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof FieldDescriptor))
			return NO_CHILDREN;
		return ((FieldDescriptor) parentElement).getChildNodes();
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof FieldDescriptor) {
			FieldDescriptor descriptor = (FieldDescriptor) element;
			return descriptor.missingGetterOrSetter();
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getFieldDescriptors().toArray();
	}
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void recreateFieldDescriptors() {
		// Delete field descriptors so that they are recreated by the next getFieldDescriptors call.
		fieldDescriptors = null;
		SortedSet<AccessorDescriptor> oldSelected = new TreeSet<AccessorDescriptor>(selectedAccessors);
		selectedAccessors.clear();
		for (FieldDescriptor descriptor : getFieldDescriptors()) {
			for (AccessorDescriptor accessor : descriptor.getChildNodes()) {
				if (oldSelected.contains(accessor)) {
					selectedAccessors.add(accessor);
				}
			}
		}
	}

	public void selectAccessorForField(String name, AccessorKind kind) {
		for (FieldDescriptor descriptor : getFieldDescriptors()) {
			if (name.equals(String.valueOf(descriptor.getFieldName().getSimpleID()))) {
				for (Object child : descriptor.getChildNodes()) {
					AccessorDescriptor accessor = (AccessorDescriptor) child;
					if (accessor.getKind() == kind) {
						selectedAccessors.add(accessor);
						break;
					}
				}
			}
		}
	}

	public boolean isDefinitionSeparate() {
		return definitionSeparate;
	}

	public void setDefinitionSeparate(boolean definitionSeparate) {
		this.definitionSeparate = definitionSeparate;
	}

	private List<FieldDescriptor> getFieldDescriptors() {
		if (fieldDescriptors == null) {
			fieldDescriptors = new ArrayList<FieldDescriptor>();
			for (IASTSimpleDeclaration field : existingFields) {
				FieldDescriptor descriptor = new FieldDescriptor(field, this);
				if (descriptor.missingGetterOrSetter()) {
					fieldDescriptors.add(descriptor);
				}
			}
		}
		return fieldDescriptors;
	}

	static IASTName getDeclarationName(IASTSimpleDeclaration declaration) {
		IASTDeclarator declarator = declaration.getDeclarators()[0];
		while (declarator.getNestedDeclarator() != null) {
			declarator = declarator.getNestedDeclarator();
		}
		return declarator.getName();
	}
}
