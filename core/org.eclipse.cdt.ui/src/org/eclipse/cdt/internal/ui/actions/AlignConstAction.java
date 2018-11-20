/*******************************************************************************
 * Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class AlignConstAction extends TextEditorAction {

	private static class MisalignedConstFinderVisitor extends ASTVisitor {
		private final boolean expectConstRight;
		private final List<IASTDeclSpecifier> declSpecifiersWithMisalignedConst = new ArrayList<>();
		{
			shouldVisitDeclSpecifiers = true;
		}

		public MisalignedConstFinderVisitor(boolean expectConstRight) {
			this.expectConstRight = expectConstRight;
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			if (declSpec.isConst()) {
				String rawSignature = declSpec.getRawSignature();
				rawSignature = rawSignature.replace(Keywords.VOLATILE, "").trim(); //$NON-NLS-1$
				if (!expectConstRight && !startsWithConst(rawSignature)) {
					declSpecifiersWithMisalignedConst.add(declSpec);
				} else if (expectConstRight && !endsWithConst(rawSignature)) {
					declSpecifiersWithMisalignedConst.add(declSpec);
				}
			}
			return PROCESS_CONTINUE;
		}

		private boolean startsWithConst(String signature) {
			if (!signature.startsWith(Keywords.CONST)) {
				return false;
			}
			String candidate = signature.substring(0, Keywords.CONST.length() + 1);
			return !IdentifierHelper.checkIdentifierName(candidate).isCorrect();
		}

		private boolean endsWithConst(String signature) {
			if (!signature.endsWith(Keywords.CONST)) {
				return false;
			}
			String candidate = signature.substring(signature.length() - Keywords.CONST.length() - 1);
			return !IdentifierHelper.checkIdentifierName(candidate).isCorrect();
		}

		public List<IASTDeclSpecifier> getDeclSpecifiersWithMisaligedConst() {
			return declSpecifiersWithMisalignedConst;
		}
	}

	private static List<IASTDeclSpecifier> findMisalignedConsts(IASTNode rootNode, ICProject cProject) {
		boolean expectConstRight = CCorePreferenceConstants.getPreference(
				CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, cProject,
				CCorePreferenceConstants.DEFAULT_PLACE_CONST_RIGHT_OF_TYPE);
		MisalignedConstFinderVisitor misalignedConstVisitor = new MisalignedConstFinderVisitor(expectConstRight);
		rootNode.accept(misalignedConstVisitor);
		return misalignedConstVisitor.getDeclSpecifiersWithMisaligedConst();
	}

	public static void rewriteMisalignedConstSpecifiers(IASTNode node, IProgressMonitor monitor) throws CoreException {
		ICProject cProject = node.getTranslationUnit().getOriginatingTranslationUnit().getCProject();
		List<IASTDeclSpecifier> misalignedSpecifiers = findMisalignedConsts(node, cProject);
		if (!misalignedSpecifiers.isEmpty()) {
			IASTTranslationUnit ast = node.getTranslationUnit();
			ASTRewrite rewrite = ASTRewrite.create(ast);
			for (IASTDeclSpecifier spec : misalignedSpecifiers) {
				rewrite.replace(spec, spec, null);
			}
			rewrite.rewriteAST().perform(monitor);
		}
	}

	/**
	 * Creates a new AlignConstAction instance.
	 *
	 * @param bundle
	 *            the resource bundle
	 * @param prefix
	 *            the prefix to use for keys in <code>bundle</code>
	 * @param editor
	 *            the text editor
	 */
	public AlignConstAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	public void run() {
		ITextSelection textSelection = getSelection();
		if (textSelection.isEmpty()) {
			return;
		}
		final int offset = textSelection.getOffset();
		final int length = textSelection.getLength();
		ITextEditor activeEditor = getTextEditor();

		alignConstQualifiers(offset, length, activeEditor);
	}

	private void alignConstQualifiers(final int offset, final int length, ITextEditor activeEditor) {
		ITranslationUnit translationUnit = (ITranslationUnit) CDTUITools
				.getEditorInputCElement(activeEditor.getEditorInput());

		try {
			IASTTranslationUnit ast = translationUnit.getAST(null, ITranslationUnit.AST_SKIP_ALL_HEADERS);
			if (ast != null) {
				IASTNode enclosingNode = ast.getNodeSelector(null).findEnclosingNode(offset, length);
				rewriteMisalignedConstSpecifiers(enclosingNode, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Returns the selection in the editor or an invalid selection if none can
	 * be obtained. Never returns <code>null</code>.
	 *
	 * @return the current selection, never <code>null</code>
	 */
	private ITextSelection getSelection() {
		ISelectionProvider provider = getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection)
				return (ITextSelection) selection;
		}

		return TextSelection.emptySelection();
	}

	/**
	 * Returns the editor's selection provider.
	 *
	 * @return the editor's selection provider or <code>null</code>
	 */
	private ISelectionProvider getSelectionProvider() {
		ITextEditor editor = getTextEditor();
		if (editor != null) {
			return editor.getSelectionProvider();
		}
		return null;
	}
}