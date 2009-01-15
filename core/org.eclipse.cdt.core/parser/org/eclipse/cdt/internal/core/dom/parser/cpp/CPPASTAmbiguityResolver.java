/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Visitor to resolve ast ambiguities in the right order
 */
public final class CPPASTAmbiguityResolver extends ASTVisitor {
	private static class ClassContext {
		ArrayList<IASTNode> fDeferredNodes;
		final IASTNode fNode;
		public ClassContext(IASTNode node) {
			fNode= node;
		}
		public void deferNode(IASTNode node) {
			if (fDeferredNodes == null)
				fDeferredNodes= new ArrayList<IASTNode>();
			fDeferredNodes.add(node);
		}
	}
	private LinkedList<ClassContext> fContextStack;
	private ClassContext fCurrentContext;
	private boolean fSkipInitializers;
	
	public CPPASTAmbiguityResolver() {
		super(false);
		shouldVisitAmbiguousNodes= true;
		shouldVisitDeclarations= true;
		shouldVisitDeclSpecifiers= true;
		shouldVisitInitializers= true;
	}

	@Override
	public int visit(ASTAmbiguousNode astAmbiguousNode) {
		astAmbiguousNode.resolveAmbiguity(this);
		return PROCESS_SKIP;
	}
	
	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			if (fCurrentContext != null) {
				// defer visiting nested classes until the outer class body has been visited.
				fCurrentContext.deferNode(declSpec);
				return PROCESS_SKIP;
			}
			pushContext();
			fCurrentContext= new ClassContext(declSpec);
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			assert fCurrentContext != null;
			assert fCurrentContext.fNode == declSpec;
			if (fCurrentContext != null) {
				final List<IASTNode> deferredNodes = fCurrentContext.fDeferredNodes;
				fCurrentContext= null;
				if (deferredNodes != null) {
					for (IASTNode node : deferredNodes) {
						node.accept(this);
					}
				}
				popContext();
			}
		}
		return PROCESS_CONTINUE;
	}

	private void pushContext() {
		if (fCurrentContext==null) {
			if (fContextStack != null && !fContextStack.isEmpty()) {
				fContextStack.addLast(null);
			}
		} else {
			if (fContextStack == null) {
				fContextStack= new LinkedList<ClassContext>();
			}
			fContextStack.addLast(fCurrentContext);
		}
	}

	private void popContext() {
		if (fContextStack == null || fContextStack.isEmpty()) {
			fCurrentContext= null;
		} else {
			fCurrentContext= fContextStack.removeLast();
		}
	}

	@Override
	public int visit(IASTDeclaration decl) {
		if (decl instanceof IASTFunctionDefinition) {
			final IASTFunctionDefinition fdef= (IASTFunctionDefinition) decl;

			// visit the declarator first, it may contain ambiguous template arguments needed 
			// for associating the template declarations.
			fSkipInitializers= true;
			CPPVisitor.findOutermostDeclarator(fdef.getDeclarator()).accept(this);
			fSkipInitializers= false;
			
			if (fCurrentContext != null) {
				// defer visiting the body of the function until the class body has been visited.
				fdef.getDeclSpecifier().accept(this);
				fCurrentContext.deferNode(decl);
				return PROCESS_SKIP;
			}
		} 
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTInitializer initializer) {
		if (fSkipInitializers)
			return PROCESS_SKIP;

		return PROCESS_CONTINUE;
	}
}
