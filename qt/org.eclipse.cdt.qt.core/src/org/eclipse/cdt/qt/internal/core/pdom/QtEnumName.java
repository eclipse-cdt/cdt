/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.Linkage;

@SuppressWarnings("restriction")
public class QtEnumName extends ASTDelegatedName {

	private final QObjectName qobjName;
	private final String qtEnumName;
	private final IASTName cppEnumName;
	private final QtASTImageLocation location;
	private final boolean isFlag;

	private ASTNodeProperty propertyInParent;

	public QtEnumName(QObjectName qobjName, IASTName ast, String qtEnumName, IASTName cppEnumName, QtASTImageLocation location, boolean isFlag) {
		super(ast);
		this.qobjName = qobjName;
		this.qtEnumName = qtEnumName;
		this.cppEnumName = cppEnumName;
		this.location = location;
		this.isFlag = isFlag;
	}

	public boolean isFlag() {
		return isFlag;
	}

	@Override
	protected IBinding createBinding() {
		IBinding owner = qobjName.getBinding();
		QtBinding qobj = owner == null ? null : (QtBinding) owner.getAdapter(QtBinding.class);
		return new QtBinding(QtPDOMNodeType.QEnum, qobj, this, cppEnumName);
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return location;
	}

	@Override
	public IASTNode getParent() {
		return qobjName;
	}

	@Override
	public IASTNode[] getChildren() {
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	@Override
	public void setParent(IASTNode node) {
		throw new IllegalStateException("attempt to modify parent of Qt Enum"); //$NON-NLS-1$
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		return propertyInParent;
	}

	@Override
	public void setPropertyInParent(ASTNodeProperty property) {
		propertyInParent = property;
	}

	@Override
	public char[] getSimpleID() {
		return qtEnumName.toCharArray();
	}

	@Override
	public String getRawSignature() {
		return qtEnumName;
	}

	@Override
	public IASTNode getOriginalNode() {
		return this;
	}

	@Override
	public boolean isDeclaration() {
		return false;
	}

	@Override
	public boolean isReference() {
		return false;
	}

	@Override
	public boolean isDefinition() {
		return true;
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_definition;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.QT_LINKAGE;
	}

	@Override
	public IASTImageLocation getImageLocation() {
		return location;
	}

	@Override
	public IASTName copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public IASTName copy(CopyStyle style) {
		return new QtEnumName(qobjName, delegate, qtEnumName, cppEnumName, location, isFlag);
	}
}
