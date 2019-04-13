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
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.ITextEditorExtension2;
import org.eclipse.ui.texteditor.IUpdate;

public class CopyQualifiedNameAction extends SelectionDispatchAction implements IUpdate {

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
		this(editor.getSite());
		fEditor = editor;
		setEnabled(true);
	}

	public CopyQualifiedNameAction(IWorkbenchSite site) {
		super(site);

		setText(ActionMessages.CopyQualifiedNameAction_ActionName);
		setToolTipText(ActionMessages.CopyQualifiedNameAction_ToolTipText);
		setDisabledImageDescriptor(CPluginImages.DESC_DLCL_COPY_QUALIFIED_NAME);
		setImageDescriptor(CPluginImages.DESC_ELCL_COPY_QUALIFIED_NAME);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.COPY_QUALIFIED_NAME_ACTION);
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(canEnable(selection.toArray()));
	}

	@Override
	public void selectionChanged(ITextSelection selection) {
	}

	private boolean isValidSelection(ITextSelection selection) {
		return selection != null && !selection.isEmpty() && selection.getLength() > 0;
	}

	private boolean canModifyEditor() {
		if (fEditor instanceof ITextEditorExtension2)
			return ((ITextEditorExtension2) fEditor).isEditorInputModifiable();
		else if (fEditor instanceof ITextEditorExtension)
			return !((ITextEditorExtension) fEditor).isEditorInputReadOnly();
		else if (fEditor != null)
			return fEditor.isEditable();
		else
			return false;
	}

	@Override
	public void update() {
		if (fEditor == null)
			return;
		if ((!canModifyEditor() || !isValidSelection(getCurrentSelection())))
			setEnabled(false);
		else
			setEnabled(true);
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

	private boolean canEnable(Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			Object element = objects[i];
			if (isValidElement(element))
				return true;
		}

		return false;
	}

	private boolean isValidElement(Object element) {
		if (element instanceof IDeclaration)
			return true;
		return false;
	}

	@Override
	public void run(IStructuredSelection selection) {
		ICElement element = getCElement(selection);
		if (element == null) {
			MessageDialog.openInformation(getShell(), ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
					ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
			return;
		}
		String qualName = TypeUtil.getFullyQualifiedName(element).getFullyQualifiedName();

		Clipboard clipboard = new Clipboard(getShell().getDisplay());
		try {
			clipboard.setContents(new String[] { qualName }, new Transfer[] { TextTransfer.getInstance() });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(getShell(), ActionMessages.CopyQualifiedNameAction_ErrorTitle,
					ActionMessages.CopyQualifiedNameAction_ErrorDescription)) {
				clipboard.setContents(new String[] { qualName }, new Transfer[] { TextTransfer.getInstance() });
			}
		} finally {
			clipboard.dispose();
		}
	}

	@Override
	public void run(ITextSelection textSelection) {
		if (textSelection.isEmpty() || textSelection.getLength() <= 0) {
			MessageDialog.openInformation(getShell(), ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
					ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
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

		if (qualName == null) {
			MessageDialog.openInformation(getShell(), ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
					ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
			return;
		}

		Clipboard clipboard = new Clipboard(getShell().getDisplay());
		try {
			clipboard.setContents(new String[] { qualName }, new Transfer[] { TextTransfer.getInstance() });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(getShell(), ActionMessages.CopyQualifiedNameAction_ErrorTitle,
					ActionMessages.CopyQualifiedNameAction_ErrorDescription)) {
				clipboard.setContents(new String[] { qualName }, new Transfer[] { TextTransfer.getInstance() });
			}
		} finally {
			clipboard.dispose();
		}
	}

	private ICElement getCElement(IStructuredSelection selection) {
		IStructuredSelection ss = selection;
		if (ss.size() == 1) {
			Object o = ss.getFirstElement();
			if (o instanceof ICElement && o instanceof ISourceReference) {
				return (ICElement) o;
			}
		}
		return null;
	}
}
