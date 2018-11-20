/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

/**
 * Handles ambiguities for parameter declarations.
 * <pre>
 * void function(const D*); // is D a type?
 * </pre>
 * @since 5.0.1
 */
public class CASTAmbiguousSimpleDeclaration extends ASTAmbiguousNode implements IASTAmbiguousSimpleDeclaration {
	private IASTSimpleDeclaration fSimpleDecl;
	private IASTDeclSpecifier fAltDeclSpec;
	private IASTDeclarator fAltDtor;

	public CASTAmbiguousSimpleDeclaration(IASTSimpleDeclaration decl, IASTDeclSpecifier declSpec, IASTDeclarator dtor) {
		fSimpleDecl = decl;
		fAltDeclSpec = declSpec;
		fAltDtor = dtor;
	}

	@Override
	protected void beforeResolution() {
		// Populate containing scope, so that it will not be affected by the alternative branches.
		IScope scope = CVisitor.getContainingScope(this);
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).populateCache();
		}
	}

	@Override
	public IASTNode[] getNodes() {
		return new IASTNode[] { fSimpleDecl, fAltDeclSpec, fAltDtor };
	}

	@Override
	public IASTSimpleDeclaration copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTSimpleDeclaration copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDeclarator(IASTDeclarator declarator) {
		fSimpleDecl.addDeclarator(declarator);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return fSimpleDecl.getDeclSpecifier();
	}

	@Override
	public IASTDeclarator[] getDeclarators() {
		return fSimpleDecl.getDeclarators();
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
		fSimpleDecl.setDeclSpecifier(declSpec);
	}

	@Override
	public IASTAttribute[] getAttributes() {
		return fSimpleDecl.getAttributes();
	}

	@Deprecated
	@Override
	public void addAttribute(IASTAttribute attribute) {
		fSimpleDecl.addAttribute(attribute);
	}

	@Override
	public IASTAttributeSpecifier[] getAttributeSpecifiers() {
		return fSimpleDecl.getAttributeSpecifiers();
	}

	@Override
	public void addAttributeSpecifier(IASTAttributeSpecifier attributeSpecifier) {
		fSimpleDecl.addAttributeSpecifier(attributeSpecifier);
	}

	@Override
	protected final IASTNode doResolveAmbiguity(ASTVisitor resolver) {
		final IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
		IASTNode nodeToReplace = this;

		// Handle nested ambiguities first.
		owner.replace(nodeToReplace, fSimpleDecl);
		IASTDeclSpecifier declSpec = fSimpleDecl.getDeclSpecifier();
		declSpec.accept(resolver);

		// Find nested names.
		final NameCollector nameCollector = new NameCollector();
		declSpec.accept(nameCollector);
		final IASTName[] names = nameCollector.getNames();

		// Resolve names.
		boolean hasIssue = false;
		for (IASTName name : names) {
			try {
				IBinding b = name.resolveBinding();
				if (b instanceof IProblemBinding) {
					hasIssue = true;
					break;
				}
			} catch (Exception t) {
				hasIssue = true;
				break;
			}
		}
		if (hasIssue) {
			// Use the alternate version.
			final IASTAmbiguityParent parent = (IASTAmbiguityParent) fSimpleDecl;
			parent.replace(declSpec, fAltDeclSpec);
			parent.replace(fSimpleDecl.getDeclarators()[0], fAltDtor);
		}

		// Resolve further nested ambiguities.
		fSimpleDecl.accept(resolver);
		return fSimpleDecl;
	}
}
