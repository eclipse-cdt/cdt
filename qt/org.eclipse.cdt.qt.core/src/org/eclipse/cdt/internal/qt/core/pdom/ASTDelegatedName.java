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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.IToken;

/**
 * The Qt linkage introduces several names that are based on names from the C++ linkage.  This
 * utility class is used to delegate operations to that base C++ name.  Methods can be overridden
 * by implementing in a subclass.
 *
 * @see QObjectName
 */
public abstract class ASTDelegatedName implements IASTName {

	protected final IASTName delegate;

	protected IBinding binding;

	protected ASTDelegatedName(IASTName delegate) {
		this.delegate = delegate;
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		return delegate.getTranslationUnit();
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		return delegate.getNodeLocations();
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return delegate.getFileLocation();
	}

	@Override
	public String getContainingFilename() {
		return delegate.getContainingFilename();
	}

	@Override
	public boolean isPartOfTranslationUnitFile() {
		return delegate.isPartOfTranslationUnitFile();
	}

	@Override
	public IASTNode getParent() {
		return delegate.getParent();
	}

	@Override
	public IASTNode[] getChildren() {
		return delegate.getChildren();
	}

	@Override
	public void setParent(IASTNode node) {
		delegate.setParent(node);
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		return delegate.getPropertyInParent();
	}

	@Override
	public void setPropertyInParent(ASTNodeProperty property) {
		delegate.setPropertyInParent(property);
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
		return delegate.accept(visitor);
	}

	@Override
	public String getRawSignature() {
		return delegate.getRawSignature();
	}

	@Override
	public boolean contains(IASTNode node) {
		return delegate.contains(node);
	}

	@Override
	public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException, UnsupportedOperationException {
		return delegate.getLeadingSyntax();
	}

	@Override
	public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException, UnsupportedOperationException {
		return delegate.getTrailingSyntax();
	}

	@Override
	public IToken getSyntax() throws ExpansionOverlapsBoundaryException {
		return delegate.getSyntax();
	}

	@Override
	public boolean isFrozen() {
		return delegate.isFrozen();
	}

	@Override
	public boolean isActive() {
		return delegate.isActive();
	}

	@Override
	public IASTNode getOriginalNode() {
		return delegate.getOriginalNode();
	}

	@Override
	public boolean isDeclaration() {
		return delegate.isDeclaration();
	}

	@Override
	public boolean isReference() {
		return delegate.isReference();
	}

	@Override
	public boolean isDefinition() {
		return delegate.isDefinition();
	}

	@Override
	public char[] getSimpleID() {
		return delegate.getSimpleID();
	}

	@Override
	public char[] toCharArray() {
		return getSimpleID();
	}

	@Override
	public char[] getLookupKey() {
		return getSimpleID();
	}

	@Override
	public IBinding getBinding() {
		return binding;
	}

	@Override
	public IBinding resolveBinding() {
		return null;
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		return delegate.getRoleOfName(allowResolution);
	}

	@Override
	public IASTCompletionContext getCompletionContext() {
		return delegate.getCompletionContext();
	}

	@Override
	public ILinkage getLinkage() {
		return delegate.getLinkage();
	}

	@Override
	public IASTImageLocation getImageLocation() {
		return delegate.getImageLocation();
	}

	@Override
	public IASTName getLastName() {
		return delegate.getLastName();
	}

	@Override
	public IASTName copy() {
		return delegate.copy();
	}

	@Override
	public IASTName copy(CopyStyle style) {
		return delegate.copy(style);
	}

	@Override
	public void setBinding(IBinding binding) {
		this.binding = binding;
	}

	@Override
	public IBinding getPreBinding() {
		return binding;
	}

	@Override
	public IBinding resolvePreBinding() {
		return resolveBinding();
	}

	@Override
	public boolean isQualified() {
		return delegate.isQualified();
	}
}
