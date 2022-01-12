/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Markus Schorn (Wind River Systems)
 *     Mike Kucera (IBM) - implicit names
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTClassVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDecltypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;

/**
 * Abstract base class for all visitors to traverse AST nodes. <br>
 * visit() methods implement a top-down traversal, and <br>
 * leave() methods implement a bottom-up traversal. <br>
 *
 * <p>Clients may subclass.</p>
 */
public abstract class ASTVisitor {
	/**
	 * Skip the traversal of children of this node, don't call leave on this node.
	 */
	public final static int PROCESS_SKIP = 1;
	/**
	 * Abort the entire traversal.
	 */
	public final static int PROCESS_ABORT = 2;
	/**
	 * Continue with traversing the children of this node.
	 */
	public final static int PROCESS_CONTINUE = 3;

	/**
	 * Set this flag to visit names.
	 */
	public boolean shouldVisitNames = false;
	/**
	 * Set this flag to visit declarations.
	 */
	public boolean shouldVisitDeclarations = false;
	/**
	 * Set this flag to visit initializers.
	 */
	public boolean shouldVisitInitializers = false;
	/**
	 * Set this flag to visit parameter declarations.
	 */
	public boolean shouldVisitParameterDeclarations = false;
	/**
	 * Set this flag to visit declarators.
	 */
	public boolean shouldVisitDeclarators = false;
	/**
	 * Set this flag to visit declaration specifiers.
	 */
	public boolean shouldVisitDeclSpecifiers = false;
	/**
	 * Set this flag to visit array modifiers.
	 * @since 5.1
	 */
	public boolean shouldVisitArrayModifiers = false;
	/**
	 * Set this flag to visit pointer operators of declarators.
	 * @since 5.1
	 */
	public boolean shouldVisitPointerOperators = false;
	/**
	 * Set this flag to visit attributes.
	 * @since 5.4
	 */
	public boolean shouldVisitAttributes = false;
	/**
	 * Set this flag to visit token nodes.
	 * @since 5.4
	 */
	public boolean shouldVisitTokens = false;
	/**
	 * Set this flag to visit expressions.
	 */
	public boolean shouldVisitExpressions = false;
	/**
	 * Set this flag to visit statements.
	 */
	public boolean shouldVisitStatements = false;
	/**
	 * Set this flag to visit typeids.
	 */
	public boolean shouldVisitTypeIds = false;
	/**
	 * Set this flag to visit enumerators.
	 */
	public boolean shouldVisitEnumerators = false;
	/**
	 * Set this flag to visit translation units.
	 */
	public boolean shouldVisitTranslationUnit = false;
	/**
	 * Set this flag to visit problem nodes.
	 */
	public boolean shouldVisitProblems = false;

	/**
	 * Set this flag to visit designators of initializers.
	 */
	public boolean shouldVisitDesignators = false;

	/**
	 * Set this flag to visit base specifiers off composite types.
	 */
	public boolean shouldVisitBaseSpecifiers = false;

	/**
	 * Set this flag to visit namespace definitions.
	 */
	public boolean shouldVisitNamespaces = false;

	/**
	 * Set this flag to visit template parameters.
	 */
	public boolean shouldVisitTemplateParameters = false;

	/**
	 * Set this flag to visit captures
	 * @since 5.3
	 */
	public boolean shouldVisitCaptures = false;

	/**
	 * Set this flag to visit virt-specifiers.
	 * @since 5.7
	 */
	public boolean shouldVisitVirtSpecifiers = false;

	/**
	 * Set this flag to visit decltype-specifiers.
	 * @since 5.8
	 */
	public boolean shouldVisitDecltypeSpecifiers = false;

	/**
	 * Per default inactive nodes are not visited. You can change that by setting
	 * this flag to <code>true</code>.
	 * @since 5.1
	 */
	public boolean includeInactiveNodes = false;

	/**
	 * Normally neither ambiguous nodes nor their children are visited. By setting
	 * this flag to <code>true</code> ambiguous nodes are visited, their children
	 * are not.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public boolean shouldVisitAmbiguousNodes = false;

	/**
	 * Implicit names are created to allow implicit bindings to be resolved,
	 * normally they are not visited, set this flag to true to visit them.
	 * @since 5.1
	 * @see #visit(IASTName)
	 * @see IASTImplicitName
	 */
	public boolean shouldVisitImplicitNames = false;

	/**
	 * Sometimes more than one implicit name is created for a binding,
	 * set this flag to true to visit more than one name for an implicit binding.
	 * @since 5.1
	 * @see #visit(IASTName)
	 * @see IASTImplicitName
	 */
	public boolean shouldVisitImplicitNameAlternates = false;

	/**
	 * Implicit destructor names are created to mark code locations where destructors of temporaries and
	 * variables going out of scope are called, normally they are not visited, set this flag to true to visit
	 * them.
	 * @since 5.10
	 * @see #visit(IASTName)
	 * @see IASTImplicitDestructorName
	 */
	public boolean shouldVisitImplicitDestructorNames = false;

	/**
	 * Creates a visitor that does not visit any kind of node per default.
	 */
	public ASTVisitor() {
		this(false);
	}

	/**
	 * Creates a visitor.
	 * @param visitNodes whether visitor is setup to visit all nodes per default, except
	 * ambiguous nodes ({@link #shouldVisitAmbiguousNodes}),
	 * inactive nodes ({@link #includeInactiveNodes}),
	 * implicit names ({@link #shouldVisitImplicitNames}),
	 * and tokens ({@link #shouldVisitTokens}).
	 * @since 5.1
	 */
	public ASTVisitor(boolean visitNodes) {
		shouldVisitArrayModifiers = visitNodes;
		shouldVisitBaseSpecifiers = visitNodes;
		shouldVisitCaptures = visitNodes;
		shouldVisitDeclarations = visitNodes;
		shouldVisitDeclarators = visitNodes;
		shouldVisitDeclSpecifiers = visitNodes;
		shouldVisitDesignators = visitNodes;
		shouldVisitEnumerators = visitNodes;
		shouldVisitExpressions = visitNodes;
		shouldVisitInitializers = visitNodes;
		shouldVisitNames = visitNodes;
		shouldVisitNamespaces = visitNodes;
		shouldVisitParameterDeclarations = visitNodes;
		shouldVisitPointerOperators = visitNodes;
		shouldVisitAttributes = visitNodes;
		shouldVisitProblems = visitNodes;
		shouldVisitStatements = visitNodes;
		shouldVisitTemplateParameters = visitNodes;
		shouldVisitTranslationUnit = visitNodes;
		shouldVisitTypeIds = visitNodes;
		shouldVisitVirtSpecifiers = visitNodes;
		shouldVisitDecltypeSpecifiers = visitNodes;
	}

	// visit methods
	public int visit(IASTTranslationUnit tu) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTName name) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclaration declaration) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTInitializer initializer) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclarator declarator) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTDeclSpecifier declSpec) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.1 */
	public int visit(IASTArrayModifier arrayModifier) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.1 */
	public int visit(IASTPointerOperator ptrOperator) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.4 */
	public int visit(IASTAttribute attribute) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.7 */
	public int visit(IASTAttributeSpecifier specifier) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.4 */
	public int visit(IASTToken token) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTExpression expression) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTStatement statement) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTTypeId typeId) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTEnumerator enumerator) {
		return PROCESS_CONTINUE;
	}

	public int visit(IASTProblem problem) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int visit(ICPPASTTemplateParameter templateParameter) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int visit(ICPPASTCapture capture) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int visit(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 6.0
	 */
	public int visit(ICPPASTDesignator designator) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.7
	 */
	public int visit(ICPPASTVirtSpecifier virtSpecifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.7
	 */
	public int visit(ICPPASTClassVirtSpecifier classVirtSpecifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.8
	 */
	public int visit(ICPPASTDecltypeSpecifier decltypeSpecifier) {
		return PROCESS_CONTINUE;
	}

	// leave methods
	public int leave(IASTTranslationUnit tu) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTName name) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTDeclaration declaration) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTInitializer initializer) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTParameterDeclaration parameterDeclaration) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTDeclarator declarator) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTDeclSpecifier declSpec) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.1 */
	public int leave(IASTArrayModifier arrayModifier) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.1 */
	public int leave(IASTPointerOperator ptrOperator) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.4 */
	public int leave(IASTAttribute attribute) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.7 */
	public int leave(IASTAttributeSpecifier specifier) {
		return PROCESS_CONTINUE;
	}

	/** @since 5.4 */
	public int leave(IASTToken token) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTExpression expression) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTStatement statement) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTTypeId typeId) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTEnumerator enumerator) {
		return PROCESS_CONTINUE;
	}

	public int leave(IASTProblem problem) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int leave(ICPPASTBaseSpecifier baseSpecifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int leave(ICPPASTTemplateParameter templateParameter) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int leave(ICPPASTCapture capture) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.3
	 */
	public int leave(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 6.0
	 */
	public int leave(ICPPASTDesignator designator) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.7
	 */
	public int leave(ICPPASTVirtSpecifier virtSpecifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.7
	 */
	public int leave(ICPPASTClassVirtSpecifier virtSpecifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * @since 5.8
	 */
	public int leave(ICPPASTDecltypeSpecifier decltypeSpecifier) {
		return PROCESS_CONTINUE;
	}

	/**
	 * For internal use, only. When {@link ASTVisitor#shouldVisitAmbiguousNodes} is set to true, the
	 * visitor will be called for ambiguous nodes. However, the children of an ambiguous will not be
	 * traversed.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public int visit(ASTAmbiguousNode astAmbiguousNode) {
		return PROCESS_CONTINUE;
	}
}
