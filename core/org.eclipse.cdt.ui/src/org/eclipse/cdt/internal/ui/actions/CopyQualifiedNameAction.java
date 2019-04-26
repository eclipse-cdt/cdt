/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class CopyQualifiedNameAction extends TextEditorAction {

	private ITextEditor fEditor;

	private static class NameVisitor extends ASTVisitor {
		private String qualifiedName;

		public NameVisitor() {
			shouldVisitNames = true;
			qualifiedName = null;
		}

		@Override
		public int visit(IASTName name) {
			IBinding b = name.resolveBinding();
			if (b instanceof ICPPBinding) {
				try {
					qualifiedName = String.join(IQualifiedTypeName.QUALIFIER, ((ICPPBinding) b).getQualifiedName());
				} catch (DOMException e) {
					CUIPlugin.log(e);
				}
			}
			return PROCESS_ABORT;
		}

		public String getQualifiedName() {
			return qualifiedName;
		}
	}

	public CopyQualifiedNameAction(ITextEditor editor) {
		super(CEditorMessages.getBundleForConstructedKeys(), "CopyQualifiedName.", editor); //$NON-NLS-1$
		fEditor = editor;
		setText(ActionMessages.CopyQualifiedNameAction_ActionName);
		setToolTipText(ActionMessages.CopyQualifiedNameAction_ToolTipText);
		setDisabledImageDescriptor(CPluginImages.DESC_DLCL_COPY_QUALIFIED_NAME);
		setImageDescriptor(CPluginImages.DESC_ELCL_COPY_QUALIFIED_NAME);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.COPY_QUALIFIED_NAME_ACTION);
	}

	private boolean isValidSelection(ITextSelection selection) {
		if (selection == null || selection.isEmpty() || selection.getLength() <= 0)
			return false;
		return true;
	}

	@Override
	public void update() {
		super.update();
		if (isEnabled()) {
			setEnabled(isValidSelection(getCurrentSelection()));
		}
	}

	/**
	 * Returns the editor's selection, or <code>null</code> if no selection can be obtained or the
	 * editor is <code>null</code>.
	 *
	 * @return the selection of the action's editor, or <code>null</code>
	 */
	protected ITextSelection getCurrentSelection() {
		if (fEditor != null) {
			ISelectionProvider provider = fEditor.getSelectionProvider();
			if (provider != null) {
				ISelection selection = provider.getSelection();
				if (selection instanceof ITextSelection)
					return (ITextSelection) selection;
			}
		}
		return null;
	}

	@Override
	public void run() {
		ITextSelection textSelection = getCurrentSelection();

		if (textSelection == null || !isValidSelection(textSelection)) {
			return;
		}

		final int offset = textSelection.getOffset();
		final int length = textSelection.getLength();
		ITranslationUnit translationUnit = (ITranslationUnit) CDTUITools
				.getEditorInputCElement(fEditor.getEditorInput());
		String qualName = null;
		try {
			IASTTranslationUnit ast = translationUnit.getAST(null, 0);
			if (ast != null) {
				IASTNode enclosingNode = ast.getNodeSelector(null).findEnclosingNode(offset, length);
				NameVisitor n = new NameVisitor();
				enclosingNode.accept(n);
				qualName = n.getQualifiedName();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}

		final Shell shell = fEditor.getEditorSite().getShell();

		if (qualName == null) {
			MessageDialog.openInformation(shell, ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
					ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
			return;
		}

		Clipboard clipboard = new Clipboard(shell.getDisplay());
		try {
			clipboard.setContents(new String[] { qualName }, new Transfer[] { TextTransfer.getInstance() });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(shell, ActionMessages.CopyQualifiedNameAction_ErrorTitle,
					ActionMessages.CopyQualifiedNameAction_ErrorDescription)) {
				clipboard.setContents(new String[] { qualName }, new Transfer[] { TextTransfer.getInstance() });
			}
		} finally {
			clipboard.dispose();
		}
	}

}
