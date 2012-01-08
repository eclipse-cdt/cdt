/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.codemanipulation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.IRequiredInclude;

import org.eclipse.cdt.internal.ui.editor.CEditorMessages;

/**
 * Adds includes and 'using' declarations to a translation unit.
 * If the translation unit is open in an editor, be sure to pass over its working copy.
 */
public class AddIncludesOperation implements IWorkspaceRunnable {
	private final ITranslationUnit fTranslationUnit;
	private final int fBeforeOffset;
	private final IRequiredInclude[] fIncludes;
	private final String[] fUsings;
	private String fNewLine;
	private IBuffer fBuffer;
	private List<ICElement> fExistingIncludes;
	private List<ICElement> fExistingUsings;
	private InsertEdit fIncludesInsert;
	private InsertEdit fUsingsInsert;
	private int fIncludesPos= -1;

	/**
	 * @param tu a translation unit.
	 * @param beforeOffset includes and 'using' declarations have to be inserted before this offset.
	 * @param includes '#include' statements to insert.
	 * @param usings 'using' statements to insert.
	 */
	public AddIncludesOperation(ITranslationUnit tu, int beforeOffset, IRequiredInclude[] includes,
			String[] usings) {
		fTranslationUnit = tu;
		fBeforeOffset = beforeOffset;
		fIncludes= includes;
		fUsings = usings;
	}

	/**
	 * @return Returns the scheduling rule for this operation
	 */
	public ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		try {
			monitor.beginTask(CEditorMessages.AddIncludesOperation_description, 3);

			fBuffer = fTranslationUnit.getBuffer();
			fNewLine= getLineSeparator();
			fExistingIncludes = fTranslationUnit.getChildrenOfType(ICElement.C_INCLUDE);
			fIncludesInsert = getIncludesInsert();
			monitor.worked(1);
			if (fUsings != null && fUsings.length > 0) {
				fExistingUsings = fTranslationUnit.getChildrenOfType(ICElement.C_USING);
			}
			fUsingsInsert = getUsingsInsert();
			monitor.worked(1);

			if (fIncludesInsert != null) {
				fBuffer.replace(fIncludesInsert.getOffset(), 0, fIncludesInsert.getText());
			}
			if (fUsingsInsert != null) {
				int offset = fUsingsInsert.getOffset();
				if (fIncludesInsert != null && offset >= fIncludesInsert.getOffset()) {
					offset += fIncludesInsert.getText().length();
				}
				fBuffer.replace(offset, 0, fUsingsInsert.getText());
			}
			monitor.worked(1);
		} finally {
			monitor.done();
		}
	}

	private InsertEdit getIncludesInsert() throws CoreException {
		if (fIncludes == null || fIncludes.length == 0) {
			return null;
		}

		ArrayList<IRequiredInclude> toAdd = new ArrayList<IRequiredInclude>();
		for (IRequiredInclude include : fIncludes) {
			String name = include.getIncludeName();
			boolean found = false;
			for (ICElement element : fExistingIncludes) {
				ISourceRange range = ((ISourceReference) element).getSourceRange();
				if (range.getStartPos() + range.getLength() > fBeforeOffset) {
					break;
				}
				if (name.equals(element.getElementName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				toAdd.add(include);
			}
		}
		if (toAdd.isEmpty()) {
			return null;
		}

		// So we have our list. Now insert.
		StringBuilder buf = new StringBuilder();
		for (IRequiredInclude include : toAdd) {
			if (include.isStandard()) {
				buf.append("#include <" + include.getIncludeName() + ">").append(fNewLine); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				buf.append("#include \"" + include.getIncludeName() + "\"").append(fNewLine); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		int pos= getIncludeInsertionPosition();
		return new InsertEdit(pos, buf.toString()); 
	}

	private int getIncludeInsertionPosition() throws CModelException {
		if (fIncludesPos < 0) {
			if (fExistingIncludes.isEmpty()) {
				fIncludesPos= getOffsetAfterLeadingMacroDefinitions();
			} else { 
				fIncludesPos = getOffsetAfterLast(fExistingIncludes);
			}
		}
		return fIncludesPos;
	}

	private InsertEdit getUsingsInsert() throws CoreException {
		if (fUsings == null || fUsings.length == 0) {
			return null;
		}

		ArrayList<String> toAdd = new ArrayList<String>(fUsings.length);
		for (String name : fUsings) {
			boolean found = false;
			for (ICElement element : fExistingUsings) {
				ISourceRange range = ((ISourceReference) element).getSourceRange();
				if (range.getStartPos() + range.getLength() > fBeforeOffset) {
					break;
				}
				if (name.equals(element.getElementName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				toAdd.add(name);
			}
		}
		if (toAdd.isEmpty()) {
			return null;
		}

		// So we have our list. Now insert.
		StringBuilder buf = new StringBuilder();
		for (String using : toAdd) {
			buf.append("using ").append(using).append(';').append(fNewLine); //$NON-NLS-1$
		}

		int pos = getOffsetAfterLast(fExistingUsings);
		int pos2 = getIncludeInsertionPosition();
		if (pos <= pos2) {
			pos = pos2;
			buf.insert(0, fNewLine); // Add a blank line between #include and using statements.
		}

		return new InsertEdit(pos, buf.toString());
	}

	/**
	 * Find the last of elements located before fBeforeOffset and returns offset of the following line.
	 * @param elements source elements to consider.
	 * @return offset of the line after the last of elements located before fBeforeOffset, or
	 * zero, if there is no such element.
	 * @throws CModelException
	 */
	private int getOffsetAfterLast(List<ICElement> elements) throws CModelException {
		for (int i = elements.size(); --i >= 0;) {
			ISourceRange range = ((ISourceReference) elements.get(i)).getSourceRange();
			int end = range.getStartPos() + range.getLength(); 
			if (end <= fBeforeOffset) {
				return findNewLine(range.getStartPos() + range.getLength());
			}
		}
		return 0;
	}

	/**
	 * Find the last leading macro definition before <code>fBeforeOffset</code>.
	 * And returns the offset of the line after.
	 */
	private int getOffsetAfterLeadingMacroDefinitions() throws CModelException {
		ISourceRange found= null;
		for (ICElement child: fTranslationUnit.getChildren()) {
			if (!(child instanceof IMacro) || !(child instanceof ISourceReference))
				break;
			
			final ISourceReference sourceRef = (ISourceReference) child;
			if (!sourceRef.isActive())
				break;
			
			ISourceRange range= sourceRef.getSourceRange();
			if (range.getStartPos() + range.getLength() > fBeforeOffset)
				break;
			
			found= range;
		}
		if (found != null) {
			return findNewLine(found.getStartPos() + found.getLength());
		}
		return 0;
	}

	private int findNewLine(int pos) {
		while (fBuffer.getChar(pos) != '\n') {
			pos++;
		}
		if (fBuffer.getChar(pos) == '\r') {
			pos++;
		}
		return pos + 1;
	}

	private String getLineSeparator() {
		try {
			if (fBuffer instanceof IAdaptable) {
				IDocument doc= (IDocument) ((IAdaptable) fBuffer).getAdapter(IDocument.class);
				if (doc != null) {
					String delim= doc.getLineDelimiter(0);
					if (delim != null) {
						return delim;
					}
				}
			}
		} catch (BadLocationException e) {
		}
		return System.getProperty("line.separator", "\n");  //$NON-NLS-1$//$NON-NLS-2$
	}
}
