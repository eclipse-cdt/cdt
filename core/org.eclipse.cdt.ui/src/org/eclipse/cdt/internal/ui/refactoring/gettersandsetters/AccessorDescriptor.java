/*******************************************************************************
 * Copyright (c) 2011, 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Prigogin (Google) - initial API and implementation
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import com.ibm.icu.text.Collator;

public class AccessorDescriptor implements Comparable<AccessorDescriptor> {
	public enum AccessorKind {
		GETTER, SETTER;
	}

	private static final Collator collator = Collator.getInstance();

	private final AccessorKind kind;
	private final String accessorName;
	private final IASTName fieldName;
	private AccessorFactory accessorFactory;
	private IASTSimpleDeclaration accessorDeclaration;
	private IASTSimpleDeclaration existingAccessorDeclaration;
	private IASTFunctionDefinition existingAccessorDefinition;

	public AccessorDescriptor(AccessorKind kind, String accessorName, FieldDescriptor fieldDescriptor) {
		GetterSetterContext context = fieldDescriptor.getContext();
		this.kind = kind;
		this.accessorName = accessorName;
		this.fieldName = fieldDescriptor.getFieldName();
		if (accessorName != null) {
			this.accessorFactory = AccessorFactory.createFactory(kind, fieldName, fieldDescriptor.getFieldDeclarator(),
					accessorName);
			this.accessorDeclaration = accessorFactory.createDeclaration();

			for (IASTFunctionDefinition currentDefinition : context.existingFunctionDefinitions) {
				if (currentDefinition.getDeclarator().getName().toString().equals(accessorName)) {
					existingAccessorDefinition = currentDefinition;
				}
			}
			for (IASTSimpleDeclaration currentDeclaration : context.existingFunctionDeclarations) {
				if (GetterSetterContext.getDeclarationName(currentDeclaration).toString().equals(accessorName)) {
					existingAccessorDeclaration = currentDeclaration;
				}
			}
		}
	}

	boolean canBeGenerated() {
		return accessorName != null && existingAccessorDeclaration == null && existingAccessorDefinition == null;
	}

	public AccessorKind getKind() {
		return kind;
	}

	public IASTName getFieldName() {
		return fieldName;
	}

	@Override
	public int compareTo(AccessorDescriptor other) {
		int c = collator.compare(fieldName.toString(), other.fieldName.toString());
		if (c != 0)
			return c;
		return kind.ordinal() - other.kind.ordinal();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AccessorDescriptor))
			return false;
		AccessorDescriptor other = (AccessorDescriptor) obj;
		return fieldName == other.fieldName && kind == other.kind;
	}

	@Override
	public String toString() {
		return accessorName;
	}

	public IASTSimpleDeclaration getAccessorDeclaration() {
		return accessorDeclaration;
	}

	public IASTFunctionDefinition getAccessorDefinition(IASTName declaratorName) {
		return accessorFactory.createDefinition(declaratorName);
	}
}
