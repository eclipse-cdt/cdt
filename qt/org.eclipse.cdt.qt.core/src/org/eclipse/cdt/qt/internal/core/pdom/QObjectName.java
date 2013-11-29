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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.Linkage;

/**
 * QObjects are C++ classes that have been annotated with Qt marker macros.  This class is
 * used to introduce the QObject to the Qt linkage.
 */
@SuppressWarnings("restriction")
public class QObjectName extends ASTDelegatedName {

	private final ICPPASTCompositeTypeSpecifier spec;

	private IASTNode parent;
	private ASTNodeProperty propertyInParent;

	public QObjectName(ICPPASTCompositeTypeSpecifier spec) {
		super(spec.getName());
		this.spec = spec;
		this.parent = delegate.getParent();
		this.propertyInParent = delegate.getPropertyInParent();
	}

	@Override
	protected IBinding createBinding() {
		return new QtBinding(QtPDOMNodeType.QObject, this, spec.getName());
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
		return new QObjectName(spec);
	}
}
