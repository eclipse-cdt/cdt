/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.codemanipulation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.ui.IRequiredInclude;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


/**
 * Add includes to a translation unit.
 * The input is an array of full qualified type names. No elimination of unnecessary
 * includes is not done. Dublicates are eliminated.
 * If the translation unit is open in an editor, be sure to pass over its working copy.
 */
public class AddIncludesOperation implements IWorkspaceRunnable {
	
	private ITranslationUnit fTranslationUnit;
	private IRequiredInclude[] fIncludes;
	private String[] fUsings;
	private boolean fDoSave;

	private String newLine = System.getProperty("line.separator", "\n");

	/**
	 * Generate include statements for the passed java elements
	 */
	public AddIncludesOperation(ITranslationUnit tu, IRequiredInclude[] includes, boolean save) {
		this (tu, includes, null, save);
	}

	/**
	 * Generate include statements for the passed c elements
	 */
	public AddIncludesOperation(ITranslationUnit tu, IRequiredInclude[] includes, String[] using, boolean save) {
		super();
		fIncludes= includes;
		fUsings = using;
		fTranslationUnit = tu;
		fDoSave= save;
	}
	
	public void executeInludes(IProgressMonitor monitor) throws CoreException {
		// Sanity
		if (fIncludes == null || fIncludes.length == 0) {
			return;
		}
		
		if (fTranslationUnit != null) {
			ArrayList toAdd = new ArrayList();
			
			monitor.beginTask(CEditorMessages.getString("AddIncludesOperation.description"), 2); //$NON-NLS-1$
			
			List elements = fTranslationUnit.getChildrenOfType(ICElement.C_INCLUDE);
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
				monitor.worked(1);
				replace(pos, insert.toString());
				monitor.worked(1);
			}
		}
	}

	public void executeUsings(IProgressMonitor monitor) throws CoreException {
		// Sanity
		if (fUsings == null || fUsings.length == 0) {
			return;
		}

		if (fTranslationUnit != null) {
			ArrayList toAdd = new ArrayList();
			
			monitor.beginTask(CEditorMessages.getString("AddIncludesOperation.description"), 2); //$NON-NLS-1$
			
			List elements = fTranslationUnit.getChildrenOfType(ICElement.C_USING);
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
				List includes = fTranslationUnit.getChildrenOfType(ICElement.C_INCLUDE);
				if (includes.size() > 0) {
					IInclude lastInclude = (IInclude)includes.get(includes.size() - 1);
					ISourceRange range = lastInclude.getSourceRange();
					pos = range.getStartPos() + range.getLength();
				} else if (elements.size() > 0) {
					IUsing lastUsing = (IUsing)includes.get(includes.size() - 1);
					ISourceRange range = lastUsing.getSourceRange();
					pos = range.getStartPos() + range.getLength();
				} else {
					pos = 0;
				}
				
				monitor.worked(1);
				replace(pos, insert.toString());
				monitor.worked(1);
			}
		}	
	}

	void replace(int pos, String s) {
		try {
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
		} catch (Exception e) {
			// ignore; should we log ?
		}
	}

	public void run(IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}			
		try {
			executeUsings(monitor);
			executeInludes(monitor);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @return Returns the scheduling rule for this operation
	 */
	public ISchedulingRule getScheduleRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}
