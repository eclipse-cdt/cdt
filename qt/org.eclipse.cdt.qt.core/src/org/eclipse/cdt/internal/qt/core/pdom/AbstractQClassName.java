/*
 * Copyright (c) 2014 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.CoreException;

/**
 * Qt provides two types of annotations for C++ classes (Q_GADGET and Q_OBJECT).  This
 * class.  This class is used to store the common parts of these Qt classes to the
 * Qt linkage.
 */
@SuppressWarnings("restriction")
public abstract class AbstractQClassName extends ASTDelegatedName implements IQtASTName {

	private final ICPPASTCompositeTypeSpecifier spec;

	private IASTNode parent;
	private ASTNodeProperty propertyInParent;

	public AbstractQClassName(ICPPASTCompositeTypeSpecifier spec) {
		super(spec.getName());
		this.spec = spec;
		this.parent = delegate.getParent();
		this.propertyInParent = delegate.getPropertyInParent();
	}

	protected abstract QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage, IASTName name) throws CoreException;

	protected abstract IASTName copy(CopyStyle style, ICPPASTCompositeTypeSpecifier spec);

	@Override
	public QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage) throws CoreException {
		return createPDOMBinding(linkage, spec.getName());
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		return spec.getTranslationUnit();
	}

	@Override
	public IASTNode[] getChildren() {
		return IASTNode.EMPTY_NODE_ARRAY;
	}

	@Override
	public IASTNode getParent() {
		return parent;
	}

	@Override
	public void setParent(IASTNode node) {
		parent = node;
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
	public boolean accept(ASTVisitor visitor) {
		return false;
	}

	@Override
	public boolean contains(IASTNode node) {
		return false;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.QT_LINKAGE;
	}

	@Override
	public IASTName copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public IASTName copy(CopyStyle style) {
		return copy(style, spec);
	}
}
