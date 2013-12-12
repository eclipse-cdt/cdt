/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.IToken;

/**
 * Signals are connected to slots by referencing them within an expansion of SIGNAL
 * or SLOT.  E.g.,
 *
 * <pre>
 * class A : public QObject
 * {
 *     Q_SIGNAL void signal1( int );
 *     Q_SLOT   void slot1();
 * };
 * A a;
 * QObject::connect( &a, SIGNAL( signal1( int ) ), &a, SLOT( slot1() ) );
 * </pre>
 *
 * The goal is for 'Find References' on the function declarations to find the references
 * in the macro expansions.  The PDOM stores references as a linked list from the binding
 * for the function.
 *
 * This class represents the name within the expansion, i.e., "signal1( int )" within
 * "SIGNAL( signal1( int ) )" and "slot1()" within "SLOT( slot1() )".
 */
public class QtSignalSlotReferenceName implements IASTName {

	private final IASTNode referenceNode;
	private final String argument;
	private final IBinding binding;
	private final IASTImageLocation location;

	private IASTNode parent;
	private ASTNodeProperty propertyInParent;

	public QtSignalSlotReferenceName(IASTNode parent, IASTNode referenceNode, String argument, int offset, int length, IBinding binding) {
		this.parent = parent;
		this.referenceNode = referenceNode;
		this.argument = argument;
		this.binding = binding;

		IASTFileLocation referenceLocation = referenceNode.getFileLocation();
		this.location
			= referenceLocation == null
				? null
				: new QtSignalSlotReferenceLocation(referenceLocation, offset, length);
	}

	@Override
	public char[] toCharArray() {
		return argument.toCharArray();
	}

	@Override
	public char[] getSimpleID() {
		return toCharArray();
	}

	@Override
	public char[] getLookupKey() {
		return toCharArray();
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		return referenceNode.getTranslationUnit();
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return getImageLocation();
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		// The javadoc says that locations that are completely enclosed within a
		// macro expansion return only the location of that expansion.
		return referenceNode.getNodeLocations();
	}

	@Override
	public String getContainingFilename() {
		return referenceNode.getContainingFilename();
	}

	@Override
	public boolean isPartOfTranslationUnitFile() {
		return referenceNode.isPartOfTranslationUnitFile();
	}

	@Override
	public IASTNode[] getChildren() {
		return new IASTNode[0];
	}

	@Override
	public IASTNode getParent() {
		return parent;
	}

	@Override
	public void setParent(IASTNode node) {
		this.parent = node;
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
		// The signal/slot reference has nothing to visit. It will have been
		// reached by the reference node, so we can't visit that, and there is
		// nothing else.
		return false;
	}

	@Override
	public String getRawSignature() {
		// The raw signature of the reference is the text of the argument.
		return argument;
	}

	@Override
	public boolean contains(IASTNode node) {
		// There aren't any nodes contained within the signal/slot reference.
		return false;
	}

	@Override
	public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException, UnsupportedOperationException {
		// The parent is the macro reference name, and this is the entire
		// content of the arguments. Since there is nothing between these, there
		// will not be any leading syntax.
		return null;
	}

	@Override
	public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException, UnsupportedOperationException {
		// The parent is the macro reference name, and this is the entire
		// content of the arguments. Since there is nothing between these, there
		// will not be any leading syntax.
		return null;
	}

	@Override
	public IToken getSyntax() throws ExpansionOverlapsBoundaryException {
		// This reference to the signal/slot function is fully contained within
		// a preprocessor node, which does not support syntax.
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFrozen() {
		return referenceNode.isFrozen();
	}

	@Override
	public boolean isActive() {
		return referenceNode.isActive();
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_reference;
	}

	@Override
	public boolean isDeclaration() {
		return false;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public boolean isDefinition() {
		return false;
	}

	@Override
	public IBinding getBinding() {
		return binding;
	}

	@Override
	public IBinding resolveBinding() {
		return getBinding();
	}

	@Override
	public IASTCompletionContext getCompletionContext() {
		// Signal/slot references are fully contained within a macro expansion,
		// so there is no completion context.
		return null;
	}

	@Override
	public ILinkage getLinkage() {
		return referenceNode instanceof IASTName ? ((IASTName) referenceNode).getLinkage() : null;
	}

	@Override
	public IASTImageLocation getImageLocation() {
		return location;
	}

	@Override
	public IASTName getLastName() {
		// Signal/slot references are not qualified, so return itself.
		return this;
	}

	@Override
	public boolean isQualified() {
		return false;
	}

	@Override
	public IASTName copy() {
		// Signal/slot references are preprocessor nodes, so they don't support
		// copying.
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTName copy(CopyStyle style) {
		// Signal/slot references are preprocessor nodes, so they don't support
		// copying.
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTNode getOriginalNode() {
		return this;
	}

	@Override
	public void setBinding(IBinding binding) {
		// Signal/slot references find their binding on instantiation, they
		// never allow it to be replaced.
		throw new UnsupportedOperationException();
	}

	@Override
	public IBinding getPreBinding() {
		return getBinding();
	}

	@Override
	public IBinding resolvePreBinding() {
		return getBinding();
	}

	@Override
	public String toString() {
		return "QtSignalSlotReference(" + new String(toCharArray()) + ')';
	}
}
