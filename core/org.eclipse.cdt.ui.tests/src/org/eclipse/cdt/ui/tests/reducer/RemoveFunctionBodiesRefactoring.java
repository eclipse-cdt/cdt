/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.reducer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class RemoveFunctionBodiesRefactoring extends CRefactoring {
	private static final String PROTECTION_TOKEN = "PRESERVE";
	private INodeFactory nodeFactory;
	private final DefaultCodeFormatterOptions formattingOptions;

	private IIndex index;
	private IASTTranslationUnit ast;
	private IRegion region;

	public RemoveFunctionBodiesRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		name = Messages.RemoveFunctionBodiesRefactoring_RemoveFunctionBodies;
		formattingOptions = new DefaultCodeFormatterOptions(project.getOptions(true));
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(pm, 10);

		RefactoringStatus status = super.checkInitialConditions(progress.newChild(8));
		if (status.hasError()) {
			return status;
		}

		ast = getAST(tu, progress.newChild(1));
		index = getIndex();
		nodeFactory = ast.getASTNodeFactory();
		region = selectedRegion.getLength() == 0 ? new Region(0, ast.getFileLocation().getNodeLength())
				: selectedRegion;

		if (isProgressMonitorCanceled(progress, initStatus))
			return initStatus;

		return initStatus;
	}

	private ICPPASTFunctionDeclarator getDeclaration(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node = node.getParent();
		}
		if (node != null) {
			IASTFunctionDeclarator declarator = ((IASTFunctionDefinition) node).getDeclarator();
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				return (ICPPASTFunctionDeclarator) declarator;
			}
		}
		return null;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext) {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		// This method bypasses the standard refactoring framework involving ModificationCollector and ASTRewrite since
		// it is too slow for the gigantic changes this refactoring has to deal with.
		FunctionDefinitionCollector finder = new FunctionDefinitionCollector();
		ast.accept(finder);
		String code = ast.getRawSignature();
		CTextFileChange fileChange = new CTextFileChange(tu.getElementName(), tu);
		fileChange.setEdit(new MultiTextEdit());
		for (IASTFunctionDefinition definition : finder.functionDefinitions) {
			if (!SelectionHelper.isNodeInsideRegion(definition, region) || containsProtectionToken(definition, code))
				continue;
			IASTStatement body = definition.getBody();
			IASTName name = definition.getDeclarator().getName();
			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPInternalBinding) {
				IASTNode[] declarations = ((ICPPInternalBinding) binding).getDeclarations();
				if (declarations != null && declarations.length != 0
						&& ((ASTNode) declarations[0]).getOffset() < ((ASTNode) definition).getOffset()) {
					IASTNode node = definition;
					IASTNode parent;
					while ((parent = node.getParent()) instanceof ICPPASTTemplateDeclaration) {
						node = parent;
					}
					int offset = ASTNodes.offset(node);
					int endOffset = ASTNodes.endOffset(node);
					offset = skipWhitespaceBefore(offset, code);
					// Remove the whole definition since the function is declared already.
					fileChange.addEdit(new DeleteEdit(offset, endOffset - offset));
					continue;
				}
			}

			int offset = ASTNodes.offset(body);
			int endOffset = ASTNodes.endOffset(body);
			if (definition instanceof ICPPASTFunctionDefinition) {
				ICPPASTConstructorChainInitializer[] initializers = ((ICPPASTFunctionDefinition) definition)
						.getMemberInitializers();
				if (initializers.length != 0) {
					offset = ASTNodes.offset(initializers[0]);
					offset = skipWhitespaceBefore(offset, code);
					if (offset > 0 && code.charAt(offset - 1) == ':')
						offset--;
				}
			}
			offset = skipWhitespaceBefore(offset, code);
			fileChange.addEdit(new ReplaceEdit(offset, endOffset - offset, ";"));
		}

		CCompositeChange change = new CCompositeChange(""); //$NON-NLS-1$
		change.markAsSynthetic();
		change.add(fileChange);
		change.setDescription(new RefactoringChangeDescriptor(getRefactoringDescriptor()));
		return change;
	}

	/**
	 * Checks if the node or the rest of its last line contain text matching {@link #PROTECTION_TOKEN}.
	 */
	private boolean containsProtectionToken(IASTNode node, String code) {
		int offset = ASTNodes.offset(node);
		int endOffset = ASTNodes.skipToNextLineAfterNode(code, node);
		for (int i = offset; i < endOffset - PROTECTION_TOKEN.length(); i++) {
			if (code.regionMatches(i, PROTECTION_TOKEN, 0, PROTECTION_TOKEN.length()))
				return true;
		}
		return false;
	}

	private static int skipWhitespaceBefore(int offset, String text) {
		while (--offset >= 0) {
			char c = text.charAt(offset);
			if (!Character.isWhitespace(c))
				break;
		}
		return offset + 1;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		// This method is no-op for this refactoring. The change is created in the createChange method.
	}

	/**
	 * Finds function definitions that have bodies, are not constexpr, and don't contain problem bindings.
	 */
	private class FunctionDefinitionCollector extends ASTVisitor {
		final List<IASTFunctionDefinition> functionDefinitions = new ArrayList<>();
		final ProblemFinder problemFinder = new ProblemFinder();

		FunctionDefinitionCollector() {
			shouldVisitDeclarations = true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (!declaration.isPartOfTranslationUnitFile())
				return PROCESS_SKIP;
			if (!(declaration instanceof IASTFunctionDefinition))
				return PROCESS_CONTINUE;
			IASTFunctionDefinition definition = (IASTFunctionDefinition) declaration;
			if (definition.getBody() == null)
				return PROCESS_SKIP;
			IASTDeclSpecifier declSpec = definition.getDeclSpecifier();
			if (declSpec instanceof ICPPASTDeclSpecifier && ((ICPPASTDeclSpecifier) declSpec).isConstexpr())
				return PROCESS_SKIP;
			if (problemFinder.containsProblemBinding(declaration))
				return PROCESS_SKIP;
			functionDefinitions.add(definition);
			return PROCESS_SKIP;
		}
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		return null;
	}
}
