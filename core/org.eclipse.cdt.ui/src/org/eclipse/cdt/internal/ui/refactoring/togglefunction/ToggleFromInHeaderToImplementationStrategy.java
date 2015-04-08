/*******************************************************************************
 * Copyright (c) 2011, 2015 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Martin Schwab & Thomas Kallenberg - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *     Thomas Corbat (IFS)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite.CommentPosition;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;

import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;

public class ToggleFromInHeaderToImplementationStrategy implements IToggleRefactoringStrategy {
	private class NamespaceFinderVisitor extends ASTVisitor {
		private final List<ICPPASTNamespaceDefinition> namespaces;
		private final Container<IASTNode> result;
		protected int namespaceIndex = -1;
		protected int deepestMatch = -1;

		private NamespaceFinderVisitor(List<ICPPASTNamespaceDefinition> namespaces, Container<IASTNode> result) {
			this.namespaces = namespaces;
			this.result = result;
			shouldVisitNamespaces = true;
		}

		@Override
		public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
			namespaceIndex++;
			String namespaceName = namespaceDefinition.getName().toString();
			if (namespaces.size() > namespaceIndex
					&& namespaces.get(namespaceIndex).getName().toString().equals(namespaceName)) {
				if (namespaceIndex > deepestMatch) {
					result.setObject(namespaceDefinition);
					deepestMatch = namespaceIndex;
				}
				return PROCESS_CONTINUE;
			}
			return PROCESS_SKIP;
		}

		@Override
		public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
			namespaceIndex--;
			return super.leave(namespaceDefinition);
		}
	}
	private IASTTranslationUnit implAst;
	private ToggleRefactoringContext context;
	private TextEditGroup infoText;
	private ASTLiteralNode includeNode;

	public ToggleFromInHeaderToImplementationStrategy(final ToggleRefactoringContext context) {
		this.infoText = new TextEditGroup(Messages.EditGroupName);
		this.context = context;
	}

	@Override
	public void run(ModificationCollector collector) throws CoreException {
		if (!newFileCheck()) {
			return;
		}
		ICPPASTFunctionDefinition newDefinition = getNewDefinition();
		if (context.getDeclaration() != null) {
			removeDefinitionFromHeader(collector);
		} else {
			replaceDefinitionWithDeclaration(collector);
		}

		ASTRewrite implRewrite = collector.rewriterForTranslationUnit(implAst);
		if (includeNode != null) {
			implRewrite.insertBefore(implAst, null, includeNode, infoText);
		}
		
		IASTNode insertionParent = implAst.getTranslationUnit();
		List<ICPPASTNamespaceDefinition> namespaces = getSurroundingNamespaces();
		
		if (!namespaces.isEmpty()) {
			IASTNode namespaceInImplementation = searchNamespaceInImplementation(namespaces);
			if (namespaceInImplementation != null) {
				insertionParent = namespaceInImplementation;
			}
			adaptQualifiedNameToNamespaceLevel(newDefinition, namespaces);

			List<ICPPASTNamespaceDefinition> namespacesToAdd = getNamespacesToAdd(namespaces);
			for (ICPPASTNamespaceDefinition namespace : namespacesToAdd) {
				ICPPASTNamespaceDefinition newNamespace = createNamespace(namespace);
				implRewrite = implRewrite.insertBefore(insertionParent, null, newNamespace, infoText);
				insertionParent = newNamespace;
			}
		}
		
		newDefinition.setParent(insertionParent);
		
		IASTNode insertionPoint = findInsertionPoint(insertionParent, 
				context.getDeclarationAST());
		ASTRewrite newRewriter = implRewrite.insertBefore(insertionParent, 
				insertionPoint, newDefinition, infoText);
		copyCommentsToNewFile(newDefinition, newRewriter, collector.rewriterForTranslationUnit(context.getDefinitionAST()));
		restoreLeadingComments(newDefinition, newRewriter, collector);
	}

	private void copyCommentsToNewFile(ICPPASTFunctionDefinition newDefinition, final ASTRewrite newRewriter,
			final ASTRewrite oldRewriter) {
		newDefinition.accept(new ASTVisitor(true) {
			@Override
			public int visit(IASTName name) {
				copy(name);
				return super.visit(name);
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				copy(declaration);
				return super.visit(declaration);
			}

			@Override
			public int visit(IASTInitializer initializer) {
				copy(initializer);
				return super.visit(initializer);
			}

			@Override
			public int visit(IASTParameterDeclaration parameterDeclaration) {
				copy(parameterDeclaration);
				return super.visit(parameterDeclaration);
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				copy(declarator);
				return super.visit(declarator);
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				copy(declSpec);
				return super.visit(declSpec);
			}

			@Override
			public int visit(IASTArrayModifier arrayModifier) {
				copy(arrayModifier);
				return super.visit(arrayModifier);
			}

			@Override
			public int visit(IASTPointerOperator ptrOperator) {
				copy(ptrOperator);
				return super.visit(ptrOperator);
			}

			@Override
			public int visit(IASTExpression expression) {
				copy(expression);
				return super.visit(expression);
			}

			@Override
			public int visit(IASTStatement statement) {
				copy(statement);
				return super.visit(statement);
			}

			@Override
			public int visit(IASTTypeId typeId) {
				copy(typeId);
				return super.visit(typeId);
			}

			@Override
			public int visit(IASTEnumerator enumerator) {
				copy(enumerator);
				return super.visit(enumerator);
			}

			@Override
			public int visit(IASTProblem problem) {
				copy(problem);
				return super.visit(problem);
			}

			@Override
			public int visit(ICPPASTBaseSpecifier baseSpecifier) {
				copy(baseSpecifier);
				return super.visit(baseSpecifier);
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
				copy(namespaceDefinition);
				return super.visit(namespaceDefinition);
			}

			@Override
			public int visit(ICPPASTTemplateParameter templateParameter) {
				copy(templateParameter);
				return super.visit(templateParameter);
			}

			@Override
			public int visit(ICPPASTCapture capture) {
				copy(capture);
				return super.visit(capture);
			}

			@Override
			public int visit(ICASTDesignator designator) {
				copy(designator);
				return super.visit(designator);
			}

			private void copy(IASTNode node) {
				copyComments(node, newRewriter, oldRewriter, CommentPosition.leading);
				copyComments(node, newRewriter, oldRewriter, CommentPosition.trailing);
				copyComments(node, newRewriter, oldRewriter, CommentPosition.freestanding);
			}

			private void copyComments(IASTNode node, ASTRewrite newRewriter, ASTRewrite oldRewriter,
					CommentPosition pos) {
				IASTNode originalNode = node.getOriginalNode();
				if (originalNode != node) {
					List<IASTComment> comments = oldRewriter.getComments(originalNode, pos);
					for (IASTComment comment : comments) {
						newRewriter.addComment(node, comment, pos);
					}
				}
			}
		});
	}

	private boolean newFileCheck() throws CoreException {
		implAst = context.getASTForPartnerFile();
		if (implAst == null) {
			IProject project = context.getSelectionTU().getCProject().getProject();
			boolean isCC = project.hasNature(CCProjectNature.CC_NATURE_ID);
			ToggleFileCreator fileCreator = new ToggleFileCreator(context, isCC ? ".cpp" : ".c"); //$NON-NLS-1$ //$NON-NLS-2$
			if (fileCreator.askUserForFileCreation(context)) {
				IFile file = fileCreator.createNewFile();
				implAst = context.getAST(file, null);
				includeNode = new ASTLiteralNode(fileCreator.getIncludeStatement());
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	private List<ICPPASTNamespaceDefinition> getSurroundingNamespaces() {
		IASTNode toquery = context.getDeclaration();
		if (toquery == null) {
			toquery = context.getDefinition();
		}
		return ToggleNodeHelper.findSurroundingNamespaces(toquery);
	}

	private IASTNode findInsertionPoint(IASTNode insertionParent, IASTTranslationUnit unit) {
		IASTFunctionDeclarator declarator = context.getDeclaration();
		if (unit == null) {
			unit = context.getDefinitionAST();
		}
		if (declarator == null) {
			declarator = context.getDefinition().getDeclarator();
		}
		IASTNode insertion_point = InsertionPointFinder.findInsertionPoint(
				unit, insertionParent.getTranslationUnit(), declarator);
		return insertion_point;
	}

	private void restoreLeadingComments(ICPPASTFunctionDefinition newDefinition,
			ASTRewrite newRewriter, ModificationCollector collector) {
		ASTRewrite rw = collector.rewriterForTranslationUnit(context.getDefinitionAST());
		List<IASTComment>comments = rw.getComments(context.getDefinition(), CommentPosition.leading);
		if (comments != null) {
			for (IASTComment comment : comments) {
				newRewriter.addComment(newDefinition, comment, CommentPosition.leading);
				if (context.getDeclaration() != null) {
					rw.remove(comment, infoText);
				}
			}
		}
	}

	private void replaceDefinitionWithDeclaration(ModificationCollector collector) {
		IASTSimpleDeclaration newdeclarator =
				ToggleNodeHelper.createDeclarationFromDefinition(context.getDefinition());
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(context.getDefinitionAST());
		rewrite.replace(context.getDefinition(), newdeclarator, infoText);
	}

	private ICPPASTFunctionDefinition getNewDefinition() {
		ICPPASTFunctionDefinition newDefinition =
				ToggleNodeHelper.createFunctionSignatureWithEmptyBody(
						context.getDefinition().getDeclSpecifier().copy(CopyStyle.withLocations),
						context.getDefinition().getDeclarator().copy(CopyStyle.withLocations),
						context.getDefinition().copy(CopyStyle.withLocations));
		newDefinition.getDeclSpecifier().setInline(false);
		newDefinition.setBody(context.getDefinition().getBody().copy(CopyStyle.withLocations));
		if (newDefinition instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTFunctionWithTryBlock newTryFun = (ICPPASTFunctionWithTryBlock) newDefinition;
			ICPPASTFunctionWithTryBlock oldTryFun = (ICPPASTFunctionWithTryBlock) context.getDefinition();
			for (ICPPASTCatchHandler catchHandler : oldTryFun.getCatchHandlers()) {				
				newTryFun.addCatchHandler(catchHandler.copy(CopyStyle.withLocations));
			}
		}
		return newDefinition;
	}

	private void adaptQualifiedNameToNamespaceLevel(IASTFunctionDefinition new_definition,
			List<ICPPASTNamespaceDefinition> namespaces) {
		if (new_definition.getDeclarator().getName() instanceof ICPPASTQualifiedName && !namespaces.isEmpty()) {
			ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
			ICPPASTQualifiedName qname = (ICPPASTQualifiedName) new_definition.getDeclarator().getName();
			ICPPASTName lastNameCopy = nodeFactory.newName(qname.getLastName().toCharArray());
			ICPPASTQualifiedName qname_new = nodeFactory.newQualifiedName(lastNameCopy);
			boolean start = false;
			ICPPASTNameSpecifier[] qualifiers = qname.getQualifier();
			for (int i = 0; i < qualifiers.length; i++) {
				String qualifierName = qualifiers[i].toString();
				if (i < namespaces.size() && qualifierName.equals(namespaces.get(i).getName().toString())) {
					start = true;
				} else if (start) {
					qname_new.addNameSpecifier(qualifiers[i]);
				}
			}
			if (start) {
				new_definition.getDeclarator().setName(qname_new);
			}
		}
	}

	private CPPASTNamespaceDefinition createNamespace(ICPPASTNamespaceDefinition parent_namespace) {
		CPPASTNamespaceDefinition insertionParent = new CPPASTNamespaceDefinition(
				parent_namespace.getName().copy(CopyStyle.withLocations));
		insertionParent.setParent(implAst);
		return insertionParent;
	}

	private void removeDefinitionFromHeader(ModificationCollector collector) {
		ASTRewrite header_rewrite = collector.rewriterForTranslationUnit(
				context.getDefinitionAST());
		header_rewrite.remove(ToggleNodeHelper.getParentRemovePoint(context.getDefinition()), infoText);
	}

	private IASTNode searchNamespaceInImplementation(final List<ICPPASTNamespaceDefinition> namespaces) {
		final Container<IASTNode> result = new Container<IASTNode>();
		ASTVisitor visitor = new NamespaceFinderVisitor(namespaces, result);
		this.implAst.accept(visitor);
		return result.getObject();
	}

	private List<ICPPASTNamespaceDefinition> getNamespacesToAdd(final List<ICPPASTNamespaceDefinition> namespaces) {
		final List<ICPPASTNamespaceDefinition> result = new ArrayList<ICPPASTNamespaceDefinition>();
		this.implAst.accept(new NamespaceFinderVisitor(namespaces, new Container<IASTNode>()) {
			{
				shouldVisitTranslationUnit = true;
			}

			@Override
			public int leave(IASTTranslationUnit tu) {
				int startIndex = deepestMatch + 1;
				int namespacesSize = namespaces.size();
				if (startIndex < namespacesSize) {
					result.addAll(namespaces.subList(startIndex, namespacesSize));
				}
				return PROCESS_CONTINUE;
			}
		});
		return result;
	}
}
