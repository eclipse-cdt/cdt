package org.eclipse.cdt.internal.ui.codemanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Add includes to a translation unit.
 * The input is an array of full qualified type names. No elimination of unnecessary
 * includes is not done. Dublicates are eliminated.
 * If the translation unit is open in an editor, be sure to pass over its working copy.
 */
public class AddIncludeOperation extends WorkspaceModifyOperation {
	
	private ITranslationUnit fTranslationUnit;
	private IRequiredInclude[] fIncludes;
	private String[] fUsings;
	private boolean fDoSave;
	private ITextEditor fEditor;

	private String newLine = System.getProperty("line.separator", "\n");

	/**
	 * Generate import statements for the passed java elements
	 * Elements must be of type IType (-> single import) or IPackageFragment
	 * (on-demand-import). Other JavaElements are ignored
	 */
	public AddIncludeOperation(ITextEditor ed, ITranslationUnit tu, IRequiredInclude[] includes, boolean save) {
		this (ed, tu, includes, null, save);
	}

	/**
	 * Generate import statements for the passed java elements
	 * Elements must be of type IType (-> single import) or IPackageFragment
	 * (on-demand-import). Other JavaElements are ignored
	 */
	public AddIncludeOperation(ITextEditor ed, ITranslationUnit tu, IRequiredInclude[] includes, String[] using, boolean save) {
		super();
		fEditor = ed;
		fIncludes= includes;
		fUsings = using;
		fTranslationUnit = tu;
		fDoSave= save;
	}
	
	public void executeInludes(ITranslationUnit root, IProgressMonitor monitor) throws CoreException {
		// Sanity
		if (fIncludes == null || fIncludes.length == 0) {
			return;
		}
		
		ArrayList toAdd = new ArrayList();
		
		monitor.beginTask(CEditorMessages.getString("AddIncludesOperation.description"), 2); //$NON-NLS-1$
		
		if (root != null) {
			List elements = ((IParent)root).getChildrenOfType(ICElement.C_INCLUDE);
			for (int i = 0; i < fIncludes.length; ++i) {
				String name = fIncludes[i].getIncludeName();
				boolean found = false;
				for (int j = 0; j < elements.size(); ++j) {
					IInclude include = (IInclude)elements.get(j);
					if (name.equals(include.getElementName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					toAdd.add(fIncludes[i]);
				}
			}
			
			if (toAdd.size() > 0) {
				// So we have our list. Now insert.
				StringBuffer insert = new StringBuffer(""); //$NON-NLS-1$
				for(int j = 0; j < toAdd.size(); j++) {
					IRequiredInclude req = (IRequiredInclude)toAdd.get(j);
					if (req.isStandard()) {
						insert.append("#include <" + req.getIncludeName() + ">\n"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						insert.append("#include \"" + req.getIncludeName() + "\"\n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				
				int pos;
				if (elements.size() > 0) {
					IInclude lastInclude = (IInclude)elements.get(elements.size() - 1);
					ISourceRange range = ((IInclude)lastInclude).getSourceRange();
					pos = range.getStartPos() + range.getLength();
				} else {
					pos = 0;
				}
				
				IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
				try {
					// Now find the next newline and insert after that
					if (pos > 0) {
						while (document.getChar(pos) != '\n') {
							pos++;
						}
						if (document.getChar(pos) == '\r') {
							pos++;
						}
						pos++;
					}
					document.replace(pos, 0, insert.toString());
				} catch (BadLocationException e) {}
			}
		}
		
		monitor.worked(1);
		monitor.worked(1);
	}

	public void executeUsings(ITranslationUnit root, IProgressMonitor monitor) throws CoreException {
		// Sanity
		if (fUsings == null || fUsings.length == 0) {
			return;
		}

		ArrayList toAdd = new ArrayList();
		
		monitor.beginTask(CEditorMessages.getString("AddIncludesOperation.description"), 2); //$NON-NLS-1$
		
		if (root != null) {
			List elements = ((IParent)root).getChildrenOfType(ICElement.C_USING);
			for (int i = 0; i < fUsings.length; ++i) {
				String name = fUsings[i];
				boolean found = false;
				for (int j = 0; j < elements.size(); ++j) {
					IUsing using = (IUsing)elements.get(j);
					if (name.equals(using.getElementName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					toAdd.add(fUsings[i]);
				}
			}
			
			if (toAdd.size() > 0) {
				// So we have our list. Now insert.
				StringBuffer insert = new StringBuffer(""); //$NON-NLS-1$
				for(int j = 0; j < toAdd.size(); j++) {
					String using = (String)toAdd.get(j);
					insert.append("using namespace " + using + ";").append(newLine); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				int pos;
				List includes = ((IParent)root).getChildrenOfType(ICElement.C_INCLUDE);
				if (includes.size() > 0) {
					IInclude lastInclude = (IInclude)includes.get(includes.size() - 1);
					ISourceRange range = lastInclude.getSourceRange();
					pos = range.getStartPos() + range.getLength();
				} else if (elements.size() > 0){
					IUsing lastUsing = (IUsing)includes.get(includes.size() - 1);
					ISourceRange range = lastUsing.getSourceRange();
					pos = range.getStartPos() + range.getLength();
				} else {
					pos = 0;
				}
				
				IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
				try {
					// Now find the next newline and insert after that
					if (pos > 0) {
						while (document.getChar(pos) != '\n') {
							pos++;
						}
						if (document.getChar(pos) == '\r') {
							pos++;
						}
						pos++;
					}
					document.replace(pos, 0, insert.toString());
				} catch (BadLocationException e) {}
			}
		}	
		monitor.worked(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {

		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}			

		ITranslationUnit root;
		// Look in content outline
		if (fEditor instanceof CEditor) {
			IWorkingCopyManager mgr = CUIPlugin.getDefault().getWorkingCopyManager();
			root = mgr.getWorkingCopy(fEditor.getEditorInput());
		} else {
			root = fTranslationUnit;
		}

		try {
			executeUsings(root, monitor);
			executeInludes(root, monitor);
		} finally {
			monitor.done();
		}
	}

}


