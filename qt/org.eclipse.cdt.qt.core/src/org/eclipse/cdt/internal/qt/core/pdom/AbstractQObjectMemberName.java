/*
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public abstract class AbstractQObjectMemberName extends ASTDelegatedName {

	private final IQtASTName owner;
	private final String name;
	private final IASTImageLocation location;
	private ASTNodeProperty propertyInParent;

	protected AbstractQObjectMemberName(IQtASTName owner, IASTName ast, String name, IASTImageLocation location) {
		super(ast);
		this.owner = owner;
		this.name = name;
		this.location = location;
	}

	protected PDOMBinding getOwner(QtPDOMLinkage linkage) throws CoreException {
		return linkage.getBinding(owner);
	}

	public String getFieldName() {
		return name;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return location;
	}

	@Override
	public IASTNode getParent() {
		return owner;
	}

	@Override
	public IASTNode[] getChildren() {
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	@Override
	public void setParent(IASTNode node) {
		throw new IllegalStateException("attempt to modify parent of QObject field"); //$NON-NLS-1$
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
		return name.toCharArray();
	}

	@Override
	public String getRawSignature() {
		return name;
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
		throw new UnsupportedOperationException("attempt to copy QObject field"); //$NON-NLS-1$
	}
}
