/*******************************************************************************
 * Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPAliasTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

public class CPPAliasTemplateInstance extends PlatformObject
		implements ICPPAliasTemplateInstance, ITypeContainer, ISerializableType {
	private final char[] name;
	private final ICPPAliasTemplate aliasTemplate;
	private IType aliasedType;

	public CPPAliasTemplateInstance(char[] name, ICPPAliasTemplate aliasTemplate, IType aliasedType) {
		this.name = name;
		this.aliasTemplate = aliasTemplate;
		this.aliasedType = aliasedType;
	}

	@Override
	public ICPPAliasTemplate getTemplateDefinition() {
		return aliasTemplate;
	}
	
	@Override
	public boolean isSameType(IType other) {
		if (other == aliasedType)
			return true;
		if (aliasedType != null) {
			return aliasedType.isSameType(other);
		}
		return false;
	}

	@Override
	public IType getType() {
		return aliasedType;
	}

	@Override
	public void setType(IType type) {
		aliasedType = type;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		if (name != null) {
			return name;
		}
		return new char[0];
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		if (aliasTemplate != null) {
			return aliasTemplate.getOwner();
		}
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		if (aliasTemplate != null) {
			return aliasTemplate.getScope();
		}
		return null;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.ALIAS_TEMPLATE;
		buffer.putShort(firstBytes);
		buffer.putCharArray(name);
		buffer.marshalType(aliasedType);
		buffer.marshalBinding(aliasTemplate);
	}

	public static IType unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		char[] name = buffer.getCharArray();
		IType unmarshalledAliasedTypeInstance = buffer.unmarshalType();
		ICPPAliasTemplate unmarshalledAlias = (ICPPAliasTemplate)buffer.unmarshalBinding();
		return new CPPAliasTemplateInstance(name, unmarshalledAlias, unmarshalledAliasedTypeInstance);
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getQualifiedName(this) + " -> " + ASTTypeUtil.getType(aliasedType, true); //$NON-NLS-1$
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPBinding) aliasTemplate).isGloballyQualified();
	}
}
