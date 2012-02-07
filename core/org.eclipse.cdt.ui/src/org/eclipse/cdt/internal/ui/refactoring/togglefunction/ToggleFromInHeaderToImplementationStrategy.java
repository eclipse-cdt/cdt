/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import java.util.List;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCopyLocation;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite.CommentPosition;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;

import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;

public class ToggleFromInHeaderToImplementationStrategy implements IToggleRefactoringStrategy {
	private IASTTranslationUnit implUnit;
	private ToggleRefactoringContext context;
	private TextEditGroup infoText;
	private ASTLiteralNode includeNode;

	public ToggleFromInHeaderToImplementationStrategy(final ToggleRefactoringContext context) {
		this.infoText = new TextEditGroup(Messages.EditGroupName);
		this.context = context;
	}

	@Override
	public void run(ModificationCollector collector) {
		if (!newFileCheck()) {
			return;
		}
//		newFileCheck();
		ICPPASTFunctionDefinition newDefinition = getNewDefinition();
		if (context.getDeclaration() != null) {
			removeDefinitionFromHeader(collector);
		} else {
			replaceDefinitionWithDeclaration(collector);
		}

		ASTRewrite implRewrite = collector.rewriterForTranslationUnit(implUnit);
		if (includeNode != null) {
			implRewrite.insertBefore(implUnit, null, includeNode, infoText);
		}
		
		IASTNode insertionParent = null;
		ICPPASTNamespaceDefinition parent = getParentNamespace();
		
		if (parent != null) {
			adaptQualifiedNameToNamespaceLevel(newDefinition, parent);
			insertionParent = searchNamespaceInImplementation(parent.getName());
			if (insertionParent == null) {
				insertionParent = createNamespace(parent);
				implRewrite = implRewrite.insertBefore(implUnit.getTranslationUnit(), 
						null, insertionParent, infoText);
			}
		} else {
			insertionParent = implUnit.getTranslationUnit();
		}
		
		newDefinition.setParent(insertionParent);
		
		IASTNode insertionPoint = findInsertionPoint(insertionParent, 
				context.getDeclarationUnit());
		ASTRewrite newRewriter = implRewrite.insertBefore(insertionParent, 
				insertionPoint, newDefinition, infoText);
		copyCommentsToNewFile(newDefinition, newRewriter, collector.rewriterForTranslationUnit(context.getDefinitionUnit()));
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
				if (node.getNodeLocations().length > 0 && node.getNodeLocations()[0] instanceof IASTCopyLocation) {
					IASTCopyLocation copyLoc = (IASTCopyLocation) node.getNodeLocations()[0];
					List<IASTComment> comments = oldRewriter.getComments(copyLoc.getOriginalNode(), pos);
					for (IASTComment comment : comments) {
						newRewriter.addComment(node, comment, pos);
					}
				}
			}
		});
	}

	private boolean newFileCheck() {
		implUnit = context.getTUForSiblingFile();
		if (implUnit == null) {
			ToggleFileCreator filecreator = new ToggleFileCreator(context, ".cpp"); //$NON-NLS-1$
			if (filecreator.askUserForFileCreation(context)) {
				filecreator.createNewFile();
				implUnit = filecreator.loadTranslationUnit();
				includeNode = new ASTLiteralNode(filecreator.getIncludeStatement());
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	private ICPPASTNamespaceDefinition getParentNamespace() {
		IASTNode toquery = context.getDeclaration();
		if (toquery == null) {
			toquery = context.getDefinition();
		}
		return ToggleNodeHelper.getAncestorOfType(toquery, ICPPASTNamespaceDefinition.class);
	}

	private IASTNode findInsertionPoint(IASTNode insertionParent, IASTTranslationUnit unit) {
		IASTFunctionDeclarator declarator = context.getDeclaration();
		if (unit == null) {
			unit = context.getDefinitionUnit();
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
		ASTRewrite rw = collector.rewriterForTranslationUnit(context.getDefinitionUnit());
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
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(context.getDefinitionUnit());
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

	private void adaptQualifiedNameToNamespaceLevel(
			IASTFunctionDefinition new_definition, IASTNode parent) {
		if (parent instanceof ICPPASTNamespaceDefinition) {
			ICPPASTNamespaceDefinition ns = (ICPPASTNamespaceDefinition) parent;
			if (new_definition.getDeclarator().getName() instanceof ICPPASTQualifiedName) {
				ICPPASTQualifiedName qname =
						(ICPPASTQualifiedName) new_definition.getDeclarator().getName();
				ICPPASTQualifiedName qname_new = new CPPASTQualifiedName();
				boolean start = false;
				for(IASTName partname: qname.getNames()) {
					if (partname.toString().equals(ns.getName().toString())) {
						start = true;
						continue;
					}
					if (start)
						qname_new.addName(partname);
				}
				if (start)
					new_definition.getDeclarator().setName(qname_new);
			}
		}
	}

	private CPPASTNamespaceDefinition createNamespace(ICPPASTNamespaceDefinition parent_namespace) {
		CPPASTNamespaceDefinition insertionParent = new CPPASTNamespaceDefinition(
				parent_namespace.getName().copy(CopyStyle.withLocations));
		insertionParent.setParent(implUnit);
		return insertionParent;
	}

	private void removeDefinitionFromHeader(ModificationCollector collector) {
		ASTRewrite header_rewrite = collector.rewriterForTranslationUnit(
				context.getDefinitionUnit());
		header_rewrite.remove(ToggleNodeHelper.getParentRemovePoint(context.getDefinition()), infoText);
	}

	private IASTNode searchNamespaceInImplementation(final IASTName name) {
		final Container<IASTNode> result = new Container<IASTNode>();
		this.implUnit.accept(new ASTVisitor() {
			{
				shouldVisitNamespaces = true;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
				if (name.toString().equals(namespaceDefinition.getName().toString())) {
					result.setObject(namespaceDefinition);
					return PROCESS_ABORT;
				}
				return super.visit(namespaceDefinition);
			}
		});
		return result.getObject();
	}
}
