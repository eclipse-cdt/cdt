package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

public class AlignConstAction extends TextEditorAction {
	
	/**
	 * Creates a new instance.
	 *
	 * @param bundle the resource bundle
	 * @param prefix the prefix to use for keys in <code>bundle</code>
	 * @param editor the text editor
	 * @param isTabAction whether the action should insert tabs if over the indentation
	 */
	public AlignConstAction(ResourceBundle bundle, String prefix, ITextEditor editor, boolean isTabAction) {
		super(bundle, prefix, editor);
	}
	
	@Override
	public void run() {
		ITextSelection textSelection = getSelection();
		if(textSelection.isEmpty()) {
			return;
		}
		ITranslationUnit translationUnit = (ITranslationUnit) CDTUITools.getEditorInputCElement(getTextEditor().getEditorInput());
		ICProject cProject = translationUnit.getCProject();
		IProject project = cProject.getProject();
		IPreferencesService preferences = Platform.getPreferencesService();
		final boolean constRight = preferences.getBoolean(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, false,
				CCorePreferenceConstants.getPreferenceScopes(project));
		final java.util.List<IASTDeclSpecifier> specs = new ArrayList<>();
		
		final int offset = textSelection.getOffset();
		final int length = textSelection.getLength();
		final int startline = textSelection.getStartLine();
		final int endline = textSelection.getEndLine();
		
		try {
			//IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
			//IASTTranslationUnit ast = translationUnit.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTTranslationUnit ast = translationUnit.getAST();
			
			IASTNode enclosingNode = ast.getNodeSelector(null).findEnclosingNode(offset, length);
			
			enclosingNode.accept(new ASTVisitor() {
				{
					shouldVisitDeclSpecifiers = true;
				}
				@Override
				public int visit(IASTDeclSpecifier declSpec) {
					if (declSpec.isConst() && declSpec.getFileLocation().getStartingLineNumber()-1 >= startline && declSpec.getFileLocation().getEndingLineNumber()-1 <= endline) {
						if (!constRight && !declSpec.getRawSignature().substring(0, 4).equals("const")) { //$NON-NLS-1$
							specs.add(declSpec);
						} else if (constRight && !declSpec.getRawSignature().substring(declSpec.getRawSignature().length() - 5).equals("const")) { //$NON-NLS-1$
							specs.add(declSpec);
						}
					}
					return PROCESS_CONTINUE;
				}
			});
			if (!specs.isEmpty()) {
				ASTRewrite rewrite = ASTRewrite.create(ast);
				for (IASTDeclSpecifier spec : specs) {
					rewrite.replace(spec, spec, null);
				}
				rewrite.rewriteAST().perform(new NullProgressMonitor());
				specs.clear();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Returns the selection on the editor or an invalid selection if none can be obtained. Returns
	 * never <code>null</code>.
	 *
	 * @return the current selection, never <code>null</code>
	 */
	private ITextSelection getSelection() {
		ISelectionProvider provider= getSelectionProvider();
		if (provider != null) {
			ISelection selection= provider.getSelection();
			if (selection instanceof ITextSelection)
				return (ITextSelection) selection;
		}

		// null object
		return TextSelection.emptySelection();
	}
	
	/**
	 * Returns the editor's selection provider.
	 *
	 * @return the editor's selection provider or <code>null</code>
	 */
	private ISelectionProvider getSelectionProvider() {
		ITextEditor editor= getTextEditor();
		if (editor != null) {
			return editor.getSelectionProvider();
		}
		return null;
	}

}
