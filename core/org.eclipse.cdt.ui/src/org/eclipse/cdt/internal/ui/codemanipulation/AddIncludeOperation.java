package org.eclipse.cdt.internal.ui.codemanipulation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.internal.ui.CFileElementWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.ui.IRequiredInclude;
import java.util.ArrayList;
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
	
	private CFileElementWorkingCopy fTranslationUnit;
	private IRequiredInclude[] fIncludes;
	private boolean fDoSave;
	private ITextEditor fEditor;
	
	private IInclude[] fAddedIncludes;
	
	/**
	 * Generate import statements for the passed java elements
	 * Elements must be of type IType (-> single import) or IPackageFragment
	 * (on-demand-import). Other JavaElements are ignored
	 */
	public AddIncludeOperation(ITextEditor ed, CFileElementWorkingCopy tu, IRequiredInclude[] includes, boolean save) {
		super();
		fEditor = ed;
		fIncludes= includes;
		fTranslationUnit= tu;
		fAddedIncludes= null;
		fDoSave= save;
	}
	
	public void execute(IProgressMonitor monitor) throws CoreException {
		try {
			ArrayList toAdd = new ArrayList();
			
			if (monitor == null) {
				monitor= new NullProgressMonitor();
			}			
			
			monitor.beginTask(CEditorMessages.getString("AddIncludesOperation.description"), 2); //$NON-NLS-1$
			
			ICElement root;
			// Look in content outline
			if(fEditor instanceof CEditor) {
				CContentOutlinePage outline = ((CEditor)fEditor).getOutlinePage();
				root = outline.getRoot();
			} else {
				root = fTranslationUnit;
			}
			if (root != null && root instanceof IParent && ((IParent)root).hasChildren()) {
			//// Get children of tu
			// Build list of include statement
			//fTranslationUnit.update();
			//if(fTranslationUnit.hasChildren()) {
				ICElement lastInclude = null;
				ICElement[] elements = ((IParent)root).getChildren();

				for(int j = 0; j < fIncludes.length; j++) {
					//System.out.println("Comparing to " + fIncludes[j].getIncludeName());
					toAdd.add(fIncludes[j]);
				}
			
				for(int i = 0; i < elements.length; i++) {
					if(elements[i].getElementType() == ICElement.C_INCLUDE) {
						lastInclude = elements[i];
						//System.out.println("Element " + elements[i].getElementName() + "sys " + ((IInclude)elements[i]).isStandard());
						for(int j = 0; j < toAdd.size(); j++) {
							//System.out.println("Comparing to " + ((IRequiredInclude)toAdd.get(j)).getIncludeName());
							if(elements[i].getElementName().equals(((IRequiredInclude)toAdd.get(j)).getIncludeName())) {
								toAdd.remove(j);
							}
						}	
					}
				}
				
				if(toAdd.size() > 0) {
					// So we have our list. Now insert.
					StringBuffer insert = new StringBuffer("");
					for(int j = 0; j < toAdd.size(); j++) {
						insert.append("#include <" + ((IRequiredInclude)toAdd.get(j)).getIncludeName() + ">\n");
					}
					int pos;
					
					if(lastInclude != null) {
						ISourceRange range = ((IInclude)lastInclude).getSourceRange();
						pos = range.getStartPos() + range.getLength();
					} else {
						pos = 0;
					}
					
					IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
					try {
						// Now find the next newline and insert after that
						if(pos > 0) {
							while(document.getChar(pos) != '\n') pos++;
							if(document.getChar(pos) == '\r') pos++;
							pos++;
						}
						document.replace(pos, 0, insert.toString());
					} catch (BadLocationException e) {}
				}
				
			}
			
			
			/*for (int i= 0; i < nImports; i++) {
				IJavaElement imp= fIncludes[i];
				if (imp instanceof IType) {
					IType type= (IType)imp;
					String packageName= type.getPackageFragment().getElementName();
					impStructure.addImport(packageName, type.getElementName());
				} else if (imp instanceof IPackageFragment) {
					String packageName= ((IPackageFragment)imp).getElementName();
					impStructure.addImport(packageName, "*"); //$NON-NLS-1$
				}
			} */
			monitor.worked(1);
			//fAddedImports= impStructure.create(fDoSave, null);
			monitor.worked(1);
		} finally {
			monitor.done();
		}
	}
}


