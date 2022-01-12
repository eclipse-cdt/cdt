/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.utils.DefinitionFinder;
import org.eclipse.cdt.internal.ui.refactoring.utils.NamespaceHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Finds the information that are needed to tell where a method definition of a certain
 * method declaration should be inserted.
 *
 * @author Mirko Stocker, Lukas Felber
 */
public class MethodDefinitionInsertLocationFinder {
	// We cache DefinitionFinder.getDefinition results because refactorings like Implement Method
	// might want to find multiple insert locations in the same translation unit. This prevents
	// many redundant calls to DefinitionFinder.getDefinition and speeds up the process quite
	// a bit. Unfortunately, this has the minor side-effect or having to instantiate this class.
	Map<IASTSimpleDeclaration, IASTName> cachedDeclarationToDefinition = new HashMap<>();

	public InsertLocation find(ITranslationUnit declarationTu, IASTFileLocation methodDeclarationLocation,
			IASTNode parent, CRefactoringContext refactoringContext, IProgressMonitor pm) throws CoreException {
		return find(declarationTu, methodDeclarationLocation, parent, refactoringContext, pm, -1);
	}

	public InsertLocation find(ITranslationUnit declarationTu, IASTFileLocation methodDeclarationLocation,
			IASTNode parent, CRefactoringContext refactoringContext, IProgressMonitor pm, int functionOffset)
			throws CoreException {
		IASTDeclaration[] declarations = NodeHelper.getDeclarations(parent);
		InsertLocation insertLocation = new InsertLocation();

		Collection<IASTSimpleDeclaration> allPreviousSimpleDeclarationsFromClassInReverseOrder = getAllPreviousSimpleDeclarationsFromClassInReverseOrder(
				declarations, methodDeclarationLocation, pm);
		Collection<IASTSimpleDeclaration> allFollowingSimpleDeclarationsFromClass = getAllFollowingSimpleDeclarationsFromClass(
				declarations, methodDeclarationLocation, pm);

		for (IASTSimpleDeclaration simpleDeclaration : allPreviousSimpleDeclarationsFromClassInReverseOrder) {
			if (pm != null && pm.isCanceled()) {
				throw new OperationCanceledException();
			}

			IASTName definition = null;
			if (cachedDeclarationToDefinition.containsKey(simpleDeclaration)) {
				definition = cachedDeclarationToDefinition.get(simpleDeclaration);
			} else {
				IASTName name = simpleDeclaration.getDeclarators()[0].getName();
				definition = DefinitionFinder.getDefinition(name, refactoringContext, pm);
				if (definition != null) {
					cachedDeclarationToDefinition.put(simpleDeclaration, definition);
				}
			}

			if (definition != null) {
				insertLocation.setNodeToInsertAfter(findFirstSurroundingParentFunctionNode(definition),
						definition.getTranslationUnit().getOriginatingTranslationUnit());
			}
		}

		for (IASTSimpleDeclaration simpleDeclaration : allFollowingSimpleDeclarationsFromClass) {
			if (pm != null && pm.isCanceled()) {
				throw new OperationCanceledException();
			}

			IASTName definition = null;
			if (cachedDeclarationToDefinition.containsKey(simpleDeclaration)) {
				definition = cachedDeclarationToDefinition.get(simpleDeclaration);
			} else {
				IASTName name = simpleDeclaration.getDeclarators()[0].getName();
				definition = DefinitionFinder.getDefinition(name, refactoringContext, pm);
				if (definition != null) {
					cachedDeclarationToDefinition.put(simpleDeclaration, definition);
				}
			}

			if (definition != null) {
				insertLocation.setNodeToInsertBefore(findFirstSurroundingParentFunctionNode(definition),
						definition.getTranslationUnit().getOriginatingTranslationUnit());
			}
		}

		if (insertLocation.getTranslationUnit() == null) {
			if (declarationTu.isHeaderUnit()) {
				ITranslationUnit partner = SourceHeaderPartnerFinder.getPartnerTranslationUnit(declarationTu,
						refactoringContext);
				if (partner != null) {
					if (methodDeclarationLocation == null && functionOffset < 0) {
						insertLocation.setParentNode(refactoringContext.getAST(partner, null), partner);
						return insertLocation;
					}
					final ICPPASTName[] names = NamespaceHelper.getSurroundingNamespace(declarationTu,
							methodDeclarationLocation != null ? methodDeclarationLocation.getNodeOffset()
									: functionOffset,
							refactoringContext);
					IASTTranslationUnit ast = refactoringContext.getAST(partner, null);
					IASTNode[] target = new IASTNode[1];
					if (ast != null) {
						ast.accept(new ASTVisitor() {
							{
								shouldVisitNamespaces = true;
							}

							@Override
							public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
								IASTName name = namespaceDefinition.getName();
								IBinding binding = name.resolveBinding();
								if (!(binding instanceof ICPPNamespace))
									return PROCESS_CONTINUE;
								try {
									char[][] qualNames = ((ICPPNamespace) binding).getQualifiedNameCharArray();
									if (qualNames.length != names.length - 1)
										return PROCESS_CONTINUE;
									for (int i = 0; i < names.length - 1; ++i) {
										if (!CharArrayUtils.equals(qualNames[i], names[i].getSimpleID()))
											return PROCESS_CONTINUE;
									}
								} catch (DOMException e) {
									e.printStackTrace();
									return PROCESS_CONTINUE;
								}
								target[0] = namespaceDefinition;
								return PROCESS_ABORT;
							}
						});
					}
					if (target[0] != null) {
						insertLocation.setParentNode(target[0], partner);
					} else {
						insertLocation.setParentNode(ast, partner);
					}
				}
			} else {
				insertLocation.setParentNode(parent.getTranslationUnit(), declarationTu);
			}
		}

		return insertLocation;
	}

	private static IASTNode findFunctionDefinitionInParents(IASTNode node) {
		if (node == null) {
			return null;
		} else if (node instanceof IASTFunctionDefinition) {
			if (node.getParent() instanceof ICPPASTTemplateDeclaration) {
				node = node.getParent();
			}
			return node;
		}
		return findFunctionDefinitionInParents(node.getParent());
	}

	private static IASTNode findFirstSurroundingParentFunctionNode(IASTNode definition) {
		IASTNode functionDefinitionInParents = findFunctionDefinitionInParents(definition);
		if (functionDefinitionInParents == null) {
			return null;
		}
		if (functionDefinitionInParents.getNodeLocations().length == 0) {
			return null;
		}
		return functionDefinitionInParents;
	}

	/**
	 * Searches the given class for all IASTSimpleDeclarations occurring before 'method'
	 * and returns them in reverse order.
	 *
	 * @param declarations to be searched
	 * @param methodPosition on which the search aborts
	 * @param pm
	 * @return all declarations, sorted in reverse order
	 */
	private static Collection<IASTSimpleDeclaration> getAllPreviousSimpleDeclarationsFromClassInReverseOrder(
			IASTDeclaration[] declarations, IASTFileLocation methodPosition, IProgressMonitor pm) {
		ArrayList<IASTSimpleDeclaration> outputDeclarations = new ArrayList<>();
		if (declarations.length >= 0) {
			for (IASTDeclaration decl : declarations) {
				if (pm != null && pm.isCanceled()) {
					return outputDeclarations;
				}
				if (methodPosition != null
						&& decl.getFileLocation().getStartingLineNumber() >= methodPosition.getStartingLineNumber()) {
					break;
				}
				if (isMemberFunctionDeclaration(decl)) {
					outputDeclarations.add((IASTSimpleDeclaration) decl);
				}
			}
		}
		Collections.reverse(outputDeclarations);
		return outputDeclarations;
	}

	private static Collection<IASTSimpleDeclaration> getAllFollowingSimpleDeclarationsFromClass(
			IASTDeclaration[] declarations, IASTFileLocation methodPosition, IProgressMonitor pm) {
		ArrayList<IASTSimpleDeclaration> outputDeclarations = new ArrayList<>();
		if (methodPosition == null)
			return outputDeclarations;
		if (declarations.length >= 0) {
			for (IASTDeclaration decl : declarations) {
				if (pm != null && pm.isCanceled()) {
					return outputDeclarations;
				}
				if (isMemberFunctionDeclaration(decl)
						&& decl.getFileLocation().getStartingLineNumber() > methodPosition.getStartingLineNumber()) {
					outputDeclarations.add((IASTSimpleDeclaration) decl);
				}
			}
		}
		return outputDeclarations;
	}

	private static boolean isMemberFunctionDeclaration(IASTDeclaration decl) {
		return decl instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) decl).getDeclarators().length > 0
				&& ((IASTSimpleDeclaration) decl).getDeclarators()[0] instanceof IASTFunctionDeclarator;
	}
}
