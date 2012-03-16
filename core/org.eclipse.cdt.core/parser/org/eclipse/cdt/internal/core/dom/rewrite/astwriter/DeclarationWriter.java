/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of declaration nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 *
 * @see Scribe
 * @see IASTDeclaration
 * @author Emanuel Graf IFS
 */
public class DeclarationWriter extends NodeWriter {
	private static final String ASM_END = ")"; //$NON-NLS-1$
	private static final String ASM_START = "asm("; //$NON-NLS-1$
	private static final String TEMPLATE_DECLARATION = "template<"; //$NON-NLS-1$
	private static final String EXPORT = "export "; //$NON-NLS-1$
	private static final String TEMPLATE_SPECIALIZATION = "template <> "; //$NON-NLS-1$
	private static final String NAMESPACE = "namespace "; //$NON-NLS-1$
	private static final String USING = "using "; //$NON-NLS-1$
	private boolean printSemicolon;

	public DeclarationWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}

	protected void writeDeclaration(IASTDeclaration declaration) throws ProblemRuntimeException{
		writeDeclaration(declaration, true);
	}

	protected void writeDeclaration(IASTDeclaration declaration, boolean writeSemicolon) {
		boolean addNewLine = true;
		printSemicolon = writeSemicolon;
		if (declaration instanceof IASTASMDeclaration) {
			writeASMDeclatation((IASTASMDeclaration) declaration);
		} else if (declaration instanceof IASTFunctionDefinition) {
			writeFunctionDefinition((IASTFunctionDefinition) declaration);
			addNewLine = false;
		} else if (declaration instanceof IASTProblemDeclaration) {
			throw new ProblemRuntimeException((IASTProblemDeclaration) declaration);
		} else if (declaration instanceof IASTSimpleDeclaration) {
			writeSimpleDeclaration((IASTSimpleDeclaration) declaration);
		} else if (declaration instanceof ICPPASTExplicitTemplateInstantiation) {
			writeExplicitTemplateInstantiation((ICPPASTExplicitTemplateInstantiation) declaration);
			addNewLine = false;
		} else if (declaration instanceof ICPPASTLinkageSpecification) {
			writeLinkageSpecification((ICPPASTLinkageSpecification) declaration);
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
			writeNamespaceAlias((ICPPASTNamespaceAlias) declaration);
		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
			writeTemplateDeclaration((ICPPASTTemplateDeclaration) declaration);
			addNewLine = false;
		} else if (declaration instanceof ICPPASTTemplateSpecialization) {
			writeTemplateSpecialization((ICPPASTTemplateSpecialization) declaration);
			addNewLine = false;
		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			writeUsingDeclaration((ICPPASTUsingDeclaration) declaration);
		} else if (declaration instanceof ICPPASTUsingDirective) {
			writeUsingDirective((ICPPASTUsingDirective) declaration);
		} else if (declaration instanceof ICPPASTVisibilityLabel) {
			writeVisibilityLabel((ICPPASTVisibilityLabel) declaration);
		}

		writeTrailingComments(declaration, addNewLine);
		if (hasFreestandingComments(declaration)) {
			if (declaration instanceof IASTFunctionDefinition) {
				scribe.newLine();
			}
			writeFreestandingComments(declaration);
		}
	}

	private void writeVisibilityLabel(ICPPASTVisibilityLabel visiblityLabel) {
		scribe.decrementIndentationLevel();
		switch (visiblityLabel.getVisibility()) {
		case ICPPASTVisibilityLabel.v_private:
			scribe.print(PRIVATE);
			scribe.print(':');
			break;
		case ICPPASTVisibilityLabel.v_protected:
			scribe.print(PROTECTED);
			scribe.print(':');
			break;
		case ICPPASTVisibilityLabel.v_public:
			scribe.print(PUBLIC);
			scribe.print(':');
			break;
		default:
			return;
		}
		scribe.incrementIndentationLevel();
	}

	private void writeUsingDirective(ICPPASTUsingDirective usingDirective) {
		scribe.print(USING + NAMESPACE);
		usingDirective.getQualifiedName().accept(visitor);
		scribe.printSemicolon();
	}

	private void writeUsingDeclaration(ICPPASTUsingDeclaration usingDeclaration) {
		scribe.print(USING);
		if (usingDeclaration.isTypename()) {
			scribe.print(TYPENAME);
		}
		usingDeclaration.getName().accept(visitor);
		scribe.printSemicolon();
	}

	private void writeTemplateSpecialization(ICPPASTTemplateSpecialization templateSpecialization) {
		scribe.print(TEMPLATE_SPECIALIZATION);
		templateSpecialization.getDeclaration().accept(visitor);
	}

	protected void writeTemplateDeclaration(ICPPASTTemplateDeclaration templateDeclaration) {
		if (templateDeclaration.isExported()) {
			scribe.print(EXPORT);
		}
		scribe.print(TEMPLATE_DECLARATION);
		ICPPASTTemplateParameter[] paraDecls = templateDeclaration.getTemplateParameters();
		for (int i = 0; i < paraDecls.length; ++i) {
			paraDecls[i].accept(visitor);
			if (i + 1 < paraDecls.length) {
				scribe.print(',');
				scribe.printSpaces(1);
			}
		}
		scribe.print('>');
		scribe.printSpace();
		templateDeclaration.getDeclaration().accept(visitor);
	}

	protected void writeDeclaration(ICPPASTNamespaceDefinition declaration) {
		printSemicolon = true;
		writeNamespaceDefinition(declaration);
	}

	private void writeNamespaceDefinition(ICPPASTNamespaceDefinition namespaceDefinition) {
		scribe.print(NAMESPACE);
		namespaceDefinition.getName().accept(visitor);
		if (!hasTrailingComments(namespaceDefinition.getName())) {
			scribe.newLine();
		}
		scribe.print('{');
		scribe.newLine(2);
		writeDeclarationsInNamespace(namespaceDefinition, namespaceDefinition.getDeclarations());
		if (hasFreestandingComments(namespaceDefinition)) {
			writeFreestandingComments(namespaceDefinition);
		}
		scribe.newLine();
		scribe.print('}');

		if (hasTrailingComments(namespaceDefinition)) {
			writeTrailingComments(namespaceDefinition);
		} else {
			scribe.newLine();
		}
	}

	protected void writeDeclarationsInNamespace(ICPPASTNamespaceDefinition namespaceDefinition, IASTDeclaration[] declarations) {
		for (IASTDeclaration declaration : declarations) {
			declaration.accept(visitor);
		}
	}

	private void writeNamespaceAlias(ICPPASTNamespaceAlias namespaceAliasDefinition) {
		scribe.print(NAMESPACE);
		namespaceAliasDefinition.getAlias().accept(visitor);
		scribe.print(EQUALS);
		namespaceAliasDefinition.getMappingName().accept(visitor);
		printSemicolon();
	}

	private void writeLinkageSpecification(ICPPASTLinkageSpecification linkageSpecification) {
		scribe.print(EXTERN);
		scribe.print(linkageSpecification.getLiteral());
		scribe.printSpaces(1);

		IASTDeclaration[] declarations = linkageSpecification.getDeclarations();
		if (declarations.length > 1) {
			scribe.printLBrace();
			scribe.decrementIndentationLevel();
			scribe.newLine();
			for (IASTDeclaration declaration : declarations) {
				declaration.accept(visitor);
			}
			scribe.printRBrace();
			scribe.incrementIndentationLevel();
		} else if (declarations.length > 0) {
			visitNodeIfNotNull(declarations[0]);
		}
	}

	private void writeExplicitTemplateInstantiation(ICPPASTExplicitTemplateInstantiation explicitTemplateInstantiation) {
		switch(explicitTemplateInstantiation.getModifier()) {
		case ICPPASTExplicitTemplateInstantiation.EXTERN:
			scribe.print(EXTERN);
			break;
		case ICPPASTExplicitTemplateInstantiation.INLINE:
			scribe.print(INLINE);
			break;
		case ICPPASTExplicitTemplateInstantiation.STATIC:
			scribe.print(STATIC);
			break;
		}

		scribe.print(TEMPLATE);
		explicitTemplateInstantiation.getDeclaration().accept(visitor);
	}

	private void writeASMDeclatation(IASTASMDeclaration asmDeclaration) {
		scribe.print(ASM_START);
		scribe.print(asmDeclaration.getAssembly());
		scribe.print(ASM_END);
		printSemicolon();
	}

	private void printSemicolon() {
		if (printSemicolon) {
			scribe.printSemicolon();
		}
	}

	private void writeFunctionDefinition(IASTFunctionDefinition funcDef) {
		IASTDeclSpecifier declSpecifier = funcDef.getDeclSpecifier();
		if (declSpecifier != null)
			declSpecifier.accept(visitor);
		if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simDeclSpec = (IASTSimpleDeclSpecifier) declSpecifier;
			if (simDeclSpec.getType() != IASTSimpleDeclSpecifier.t_unspecified) {
				visitor.setSpaceNeededBeforeName(true);
			}
		} else {
			visitor.setSpaceNeededBeforeName(true);
		}
		IASTDeclarator declarator = ASTQueries.findOutermostDeclarator(funcDef.getDeclarator());
		declarator.accept(visitor);

		if (funcDef instanceof ICPPASTFunctionWithTryBlock) {
			scribe.newLine();
			scribe.print(Keywords.TRY);
		}

		if (funcDef instanceof ICPPASTFunctionDefinition) {
			ICPPASTFunctionDefinition cppFuncDef= (ICPPASTFunctionDefinition) funcDef;
			writeCtorChainInitializer(cppFuncDef, cppFuncDef.getMemberInitializers());
		}
		scribe.newLine();

		funcDef.getBody().accept(visitor);

		if (funcDef instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTFunctionWithTryBlock tryblock = (ICPPASTFunctionWithTryBlock) funcDef;
			ICPPASTCatchHandler[] catches = tryblock.getCatchHandlers();
			for (ICPPASTCatchHandler handler : catches) {
				handler.accept(visitor);
			}
		}
	}

	protected void writeCtorChainInitializer(ICPPASTFunctionDefinition funcDec,
			ICPPASTConstructorChainInitializer[] ctorInitChain) {
		if (ctorInitChain.length != 0) {
			scribe.newLine();
			scribe.print(':');
		}
		for (int i = 0; i < ctorInitChain.length; ++i) {
			ICPPASTConstructorChainInitializer initializer = ctorInitChain[i];
			initializer.accept(visitor);
			if (i + 1 < ctorInitChain.length) {
				scribe.print(COMMA_SPACE);
			}
		}
	}

	private void writeSimpleDeclaration(IASTSimpleDeclaration simpDec) {
		IASTDeclSpecifier declSpecifier = simpDec.getDeclSpecifier();
		IASTDeclarator[] decls = simpDec.getDeclarators();

		declSpecifier.accept(visitor);
		boolean noSpace = false;
		if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier) declSpecifier;
			if (simpleDeclSpecifier.getType() == IASTSimpleDeclSpecifier.t_unspecified) {
				noSpace = true;
			}
		}

		if (decls.length > 0) {
			if (decls.length == 1) {
				if (!noSpace)
					visitor.setSpaceNeededBeforeName(true);
				decls[0].accept(visitor);
			} else {
				if (!noSpace) {
					scribe.printSpace();
				}
				writeNodeList(decls);
			}
		}

		printSemicolon();
	}
}
