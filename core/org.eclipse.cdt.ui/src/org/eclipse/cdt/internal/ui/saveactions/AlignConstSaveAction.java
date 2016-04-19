package org.eclipse.cdt.internal.ui.saveactions;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

public class AlignConstSaveAction implements ISaveAction {
	
	@Override
	public TextEdit perform(TextEdit edit) throws BadLocationException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window != null) {
			IWorkbenchPage activePage = window.getActivePage();
			if(activePage != null) {
				ITextEditor activeEditor = getTextEditor(activePage.getActiveEditor());
				if (activeEditor != null) {
					alignConstInActiveEditor(activeEditor);
				}
			}
		}
		return null;
	}
	
	private void alignConstInActiveEditor(ITextEditor activeEditor) {
		ITranslationUnit translationUnit = (ITranslationUnit) CDTUITools.getEditorInputCElement(activeEditor.getEditorInput());
		
		ICProject cProject = translationUnit.getCProject();
		IProject project = cProject.getProject();
		IPreferencesService preferences = Platform.getPreferencesService();
		final boolean constRight = preferences.getBoolean(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, false,
				CCorePreferenceConstants.getPreferenceScopes(project));
		final java.util.List<IASTDeclSpecifier> specs = new ArrayList<>();
		
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
			index.acquireReadLock();
			IASTTranslationUnit ast = translationUnit.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
			index.releaseReadLock();
			
			ast.accept(new ASTVisitor() {
				{
					shouldVisitDeclSpecifiers = true;
				}
				@Override
				public int visit(IASTDeclSpecifier declSpec) {
					if (!declSpec.isPartOfTranslationUnitFile()) {
						return PROCESS_SKIP;
					} 
					if (declSpec.isConst()) {
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
		} catch (CoreException | InterruptedException e) {
			CUIPlugin.log(e);
		}
	}
	
	private ITextEditor getTextEditor(IEditorPart editor) {
		return editor == null ? null : (ITextEditor) editor.getAdapter(ITextEditor.class);
	}

}
