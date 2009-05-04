/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *     Sergey Prigogin (Google)
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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.ui.IRequiredInclude;

import org.eclipse.cdt.internal.ui.editor.CEditorMessages;

/**
 * Add includes to a translation unit.
 * The input is an array of full qualified type names. No elimination of unnecessary
 * includes is not done. Duplicates are eliminated.
 * If the translation unit is open in an editor, be sure to pass over its working copy.
 */
public class AddIncludesOperation implements IWorkspaceRunnable {
	private ITranslationUnit fTranslationUnit;
	private IRequiredInclude[] fIncludes;
	private String[] fUsings;
	private final String fNewLine;
	private IRegion insertedIncludes;

	/**
	 * Generate include statements for the passed java elements
	 */
	public AddIncludesOperation(ITranslationUnit tu, IRequiredInclude[] includes, boolean save) {
		this(tu, includes, null, save);
	}

	/**
	 * Generate include statements for the passed c elements
	 */
	public AddIncludesOperation(ITranslationUnit tu, IRequiredInclude[] includes, String[] usings,
			boolean save) {
		super();
		fIncludes= includes;
		fUsings = usings;
		fTranslationUnit = tu;
		fNewLine= getNewLine(tu);
	}
	
	private String getNewLine(ITranslationUnit tu) {
		try {
			IBuffer buf= tu.getBuffer();
			if (buf instanceof IAdaptable) {
				IDocument doc= (IDocument) ((IAdaptable) buf).getAdapter(IDocument.class);
				if (doc != null) {
					String delim= doc.getLineDelimiter(0);
					if (delim != null) {
						return delim;
					}
				}
			}
		} catch (CModelException e) {
		} catch (BadLocationException e) {
		}
		return System.getProperty("line.separator", "\n");  //$NON-NLS-1$//$NON-NLS-2$
	}

	private void insertIncludes(IProgressMonitor monitor) throws CoreException {
		// Sanity
		if (fIncludes == null || fIncludes.length == 0) {
			return;
		}
		
		if (fTranslationUnit != null) {
			ArrayList<IRequiredInclude> toAdd = new ArrayList<IRequiredInclude>();
			
			monitor.beginTask(CEditorMessages.AddIncludesOperation_description, 2); 
			
			List<ICElement> elements = fTranslationUnit.getChildrenOfType(ICElement.C_INCLUDE);
			for (IRequiredInclude include : fIncludes) {
				String name = include.getIncludeName();
				boolean found = false;
				for (ICElement element : elements) {
					if (name.equals(element.getElementName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					toAdd.add(include);
				}
			}
			
			if (!toAdd.isEmpty()) {
				// So we have our list. Now insert.
				StringBuilder buf = new StringBuilder();
				for (IRequiredInclude include : toAdd) {
					if (include.isStandard()) {
						buf.append("#include <" + include.getIncludeName() + ">").append(fNewLine); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						buf.append("#include \"" + include.getIncludeName() + "\"").append(fNewLine); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				
				int pos = 0;
				if (!elements.isEmpty()) {
					IInclude lastInclude = (IInclude) elements.get(elements.size() - 1);
					ISourceRange range = lastInclude.getSourceRange();
					pos = range.getStartPos() + range.getLength();
				}
				monitor.worked(1);
				replace(pos, buf.toString());
				insertedIncludes = new Region(pos, buf.length());
				monitor.worked(1);
			}
		}
	}

	private void insertUsings(IProgressMonitor monitor) throws CoreException {
		// Sanity
		if (fUsings == null || fUsings.length == 0) {
			return;
		}

		if (fTranslationUnit != null) {
			ArrayList<String> toAdd = new ArrayList<String>(fUsings.length);

			monitor.beginTask(CEditorMessages.AddIncludesOperation_description, 2); 

			List<ICElement> elements = fTranslationUnit.getChildrenOfType(ICElement.C_USING);
			for (String name : fUsings) {
				boolean found = false;
				for (ICElement element : elements) {
					if (name.equals(element.getElementName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					toAdd.add(name);
				}
			}

			if (!toAdd.isEmpty()) {
				// So we have our list. Now insert.
				StringBuilder buf = new StringBuilder();
				for (String using : toAdd) {
					buf.append("using ").append(using).append(';').append(fNewLine); //$NON-NLS-1$
				}

				int pos = 0;
				if (!elements.isEmpty()) {
					IUsing lastUsing = (IUsing) elements.get(elements.size() - 1);
					ISourceRange range = lastUsing.getSourceRange();
					pos = range.getStartPos() + range.getLength();
				} else {
					List<ICElement> includes = fTranslationUnit.getChildrenOfType(ICElement.C_INCLUDE);
					if (!includes.isEmpty()) {
						IInclude lastInclude = (IInclude) includes.get(includes.size() - 1);
						ISourceRange range = lastInclude.getSourceRange();
						pos = range.getStartPos() + range.getLength();
					}
					if (!includes.isEmpty() || insertedIncludes != null) {
						buf.insert(0, fNewLine);
					}
				}
				if (insertedIncludes != null && pos >= insertedIncludes.getOffset()) {
					pos += insertedIncludes.getLength();
				}

				monitor.worked(1);
				replace(pos, buf.toString());
				monitor.worked(1);
			}
		}	
	}

	private void replace(int pos, String s) throws CModelException {
		IBuffer buffer = fTranslationUnit.getBuffer();
		// Now find the next newline and insert after that
		if (pos > 0) {
			while (buffer.getChar(pos) != '\n') {
				pos++;
			}
			if (buffer.getChar(pos) == '\r') {
				pos++;
			}
			pos++;
		}
		buffer.replace(pos, 0, s);
	}

	public void run(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}			
		try {
			insertIncludes(monitor);
			insertUsings(monitor);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @return Returns the scheduling rule for this operation
	 */
	public ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
