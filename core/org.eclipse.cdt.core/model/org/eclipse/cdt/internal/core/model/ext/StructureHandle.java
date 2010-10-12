/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;

public class StructureHandle extends CElementHandle implements IStructure {
	private static final IField[] EMPTY_FIELDS = new IField[0];
	private static final IMethodDeclaration[] EMPTY_METHODS = new IMethodDeclaration[0];

	public StructureHandle(ICElement parent, ICompositeType type) {
		super(parent, convertKey(type.getKey()), type.getName());
	}

	private static int convertKey(int astKey) {
		switch(astKey) {
		case ICompositeType.k_struct:
			return ICElement.C_STRUCT;
		case ICompositeType.k_union:
			return ICElement.C_UNION;
		}
		return ICElement.C_CLASS;
	}

	public IField getField(String name) {
		return null;
	}

	public IField[] getFields() throws CModelException {
		return EMPTY_FIELDS;
	}

	public IMethodDeclaration getMethod(String name) {
		return null;
	}

	public IMethodDeclaration[] getMethods() throws CModelException {
		return EMPTY_METHODS;
	}
	
	public boolean isClass() throws CModelException {
		return getElementType() == ICElement.C_CLASS;
	}

	public boolean isStruct() throws CModelException {
		return getElementType() == ICElement.C_STRUCT;
	}

	public boolean isUnion() throws CModelException {
		return getElementType() == ICElement.C_UNION;
	}

	public boolean isStatic() throws CModelException {
		return false;
	}
}
