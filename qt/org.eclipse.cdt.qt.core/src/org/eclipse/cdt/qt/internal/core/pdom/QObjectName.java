/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * QObjects are C++ classes that have been annotated with Qt marker macros.  This class is
 * used to introduce the QObject to the Qt linkage.
 */
@SuppressWarnings("restriction")
public class QObjectName extends ASTDelegatedName implements IQtASTName {

	private final ICPPASTCompositeTypeSpecifier spec;
	private final List<QtPropertyName> properties = new ArrayList<QtPropertyName>();
	private final Map<String, String> classInfos = new LinkedHashMap<String, String>();

	private IASTNode parent;
	private ASTNodeProperty propertyInParent;

	public QObjectName(ICPPASTCompositeTypeSpecifier spec) {
		super(spec.getName());
		this.spec = spec;
		this.parent = delegate.getParent();
		this.propertyInParent = delegate.getPropertyInParent();
	}

	public List<QtPropertyName> getProperties() {
		return properties;
	}

	public void addProperty(QtPropertyName property) {
		properties.add(property);
	}

	public Map<String, String> getClassInfos() {
		return classInfos;
	}

	public String addClassInfo(String key, String value) {
		return classInfos.put(key, value);
	}

	@Override
	public QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage) throws CoreException {
		return new QtPDOMQObject(linkage, this, spec.getName());
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
