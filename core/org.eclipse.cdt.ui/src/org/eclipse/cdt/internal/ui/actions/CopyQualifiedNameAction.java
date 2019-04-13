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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IDeclaration;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.SelectionDispatchAction;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class CopyQualifiedNameAction extends SelectionDispatchAction {

	private ITextEditor fEditor;
	private final Map<ITranslationUnit, IASTTranslationUnit> fASTCache;
	private IIndex fIndex;
	private IASTTranslationUnit fSharedAST;
	private static final int PARSE_MODE = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT | ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	private static class NameVisitor extends ASTVisitor {
		private Set<String> qualifiedNames;
		private String lookupKey;

		public NameVisitor() {
			shouldVisitNames = true;
			qualifiedNames = new LinkedHashSet<>();
		}

		public NameVisitor(String key) {
			shouldVisitNames = true;
			qualifiedNames = new LinkedHashSet<>();
			lookupKey = key;
		}

		@Override
		public int visit(IASTName name) {
			if (lookupKey != null && !lookupKey.equals(new String(name.getSimpleID()))) {
				return PROCESS_CONTINUE;
			}
			IBinding b = name.resolveBinding();
			if (b instanceof ICPPBinding) {
				try {
					qualifiedNames.add(String.join("::", ((ICPPBinding) b).getQualifiedName())); //$NON-NLS-1$
				} catch (DOMException e) {
					CUIPlugin.log(e);
				}
			}
			return PROCESS_CONTINUE;
		}

		public Set<String> getQualifiedNames() {
			return qualifiedNames;
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
		fASTCache = new ConcurrentHashMap<>();
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(canEnable(selection.toArray()));
	}

	@Override
	public void selectionChanged(ITextSelection selection) {
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
		try {
			ICElement element = getCElement(selection);
			if (element == null) {
				MessageDialog.openInformation(getShell(), ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
						ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
				return;
			}
			Set<String> qualNames = Collections.emptySet();
			ISourceReference sourceRef = (ISourceReference) element;
			ITranslationUnit tu = CModelUtil.toWorkingCopy(sourceRef.getTranslationUnit());
			try {
				IASTTranslationUnit ast = getAST(tu);
				NameVisitor n = new NameVisitor(element.getElementName());
				ast.accept(n);
				qualNames = n.getQualifiedNames();
			} catch (CoreException e1) {
				CUIPlugin.log(e1);
			}

			if (qualNames.isEmpty()) {
				MessageDialog.openInformation(getShell(), ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
						ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
				return;
			}

			Clipboard clipboard = new Clipboard(getShell().getDisplay());
			String contents = String.join(" ", qualNames); //$NON-NLS-1$
			try {
				clipboard.setContents(new String[] { contents }, new Transfer[] { TextTransfer.getInstance() });
			} catch (SWTError e) {
				if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
					throw e;
				}
				if (MessageDialog.openQuestion(getShell(), ActionMessages.CopyQualifiedNameAction_ErrorTitle,
						ActionMessages.CopyQualifiedNameAction_ErrorDescription)) {
					clipboard.setContents(new String[] { contents }, new Transfer[] { TextTransfer.getInstance() });
				}
			} finally {
				clipboard.dispose();
			}
		} finally {
			dispose();
		}
	}

	@Override
	public void run(ITextSelection textSelection) {
		if (textSelection.isEmpty()) {
			MessageDialog.openInformation(getShell(), ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
					ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
			return;
		}
		final int offset = textSelection.getOffset();
		final int length = textSelection.getLength();
		ITranslationUnit translationUnit = (ITranslationUnit) CDTUITools
				.getEditorInputCElement(fEditor.getEditorInput());
		Set<String> qualNames = Collections.emptySet();
		try {
			IASTTranslationUnit ast = translationUnit.getAST(null, 0);
			if (ast != null) {
				IASTNode enclosingNode = ast.getNodeSelector(null).findEnclosingNode(offset, length);
				NameVisitor n = new NameVisitor();
				enclosingNode.accept(n);
				qualNames = n.getQualifiedNames();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}

		if (qualNames.isEmpty()) {
			MessageDialog.openInformation(getShell(), ActionMessages.CopyQualifiedNameAction_InfoDialogTitel,
					ActionMessages.CopyQualifiedNameAction_NoElementToQualify);
			return;
		}

		Clipboard clipboard = new Clipboard(getShell().getDisplay());
		String contents = String.join(" ", qualNames); //$NON-NLS-1$
		try {
			clipboard.setContents(new String[] { contents }, new Transfer[] { TextTransfer.getInstance() });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(getShell(), ActionMessages.CopyQualifiedNameAction_ErrorTitle,
					ActionMessages.CopyQualifiedNameAction_ErrorDescription)) {
				clipboard.setContents(new String[] { contents }, new Transfer[] { TextTransfer.getInstance() });
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

	private IASTTranslationUnit getAST(ITranslationUnit tu) throws CoreException, OperationCanceledException {
		getIndex(); // Make sure the index is locked.
		tu = CModelUtil.toWorkingCopy(tu);
		// Try to get a shared AST before creating our own.
		IASTTranslationUnit ast = fASTCache.get(tu);
		if (ast == null) {
			if (fSharedAST != null && tu.equals(fSharedAST.getOriginatingTranslationUnit())) {
				ast = fSharedAST;
			} else {
				ast = ASTProvider.getASTProvider().acquireSharedAST(tu, fIndex, ASTProvider.WAIT_ACTIVE_ONLY, null);
				if (ast != null && ast.hasNodesOmitted()) {
					// Don't use an incomplete AST.
					ASTProvider.getASTProvider().releaseSharedAST(ast);
					ast = null;
				}
				if (ast == null) {
					ast = tu.getAST(fIndex, PARSE_MODE);
					fASTCache.put(tu, ast);
				} else {
					if (fSharedAST != null) {
						ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
					}
					fSharedAST = ast;
				}
			}
		}
		return ast;
	}

	/**
	 * Returns the index that can be safely used for reading until dispose is called
	 *
	 * @return The index.
	 */
	private IIndex getIndex() throws CoreException, OperationCanceledException {
		if (fIndex == null) {
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			IIndex index = CCorePlugin.getIndexManager().getIndex(projects,
					IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR);
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
			fIndex = index;
		}
		return fIndex;
	}

	private void dispose() {
		if (fSharedAST != null) {
			ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
		}
		if (fIndex != null) {
			fIndex.releaseReadLock();
		}
	}
}
