/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;

import org.eclipse.cdt.internal.ui.util.Messages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchPatternQuery extends PDOMSearchQuery {

	// First bit after the FINDs in PDOMSearchQuery.
	public static final int FIND_CLASS_STRUCT = 0x10;
	public static final int FIND_FUNCTION = 0x20;
	public static final int FIND_VARIABLE = 0x40;
	public static final int FIND_UNION = 0x100;
	public static final int FIND_METHOD = 0x200;
	public static final int FIND_FIELD = 0x400;
	public static final int FIND_ENUM = 0x1000;
	public static final int FIND_ENUMERATOR = 0x2000;
	public static final int FIND_NAMESPACE = 0x4000;
	public static final int FIND_TYPEDEF = 0x10000;
	public static final int FIND_MACRO = 0x20000;
	public static final int FIND_ALL_TYPES
		= FIND_CLASS_STRUCT | FIND_FUNCTION | FIND_VARIABLE
		| FIND_UNION | FIND_METHOD | FIND_FIELD | FIND_ENUM
		| FIND_ENUMERATOR | FIND_NAMESPACE | FIND_TYPEDEF | FIND_MACRO;
	
	private String scopeDesc;
	private String patternStr;
	private Pattern pattern;
	
	public PDOMSearchPatternQuery(
			ICElement[] scope,
			String scopeDesc,
			Pattern pattern,
			String patternStr,
			boolean isCaseSensitive,
			int flags) {
		super(scope, flags);
		this.scopeDesc = scopeDesc;
		this.pattern = pattern;
		this.patternStr = patternStr;
	}
	
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		try {
			for (int i = 0; i < projects.length; ++i)
				searchProject(projects[i], monitor);
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}
	
	private void searchProject(ICProject project, IProgressMonitor monitor) throws CoreException {
		IPDOM pdom = CCorePlugin.getPDOMManager().getPDOM(project);

		try {
			pdom.acquireReadLock();
		} catch (InterruptedException e) {
			return;
		}

		try {
			IBinding[] bindings = pdom.findBindings(pattern);
			for (int i = 0; i < bindings.length; ++i) {
				PDOMBinding pdomBinding = (PDOMBinding)bindings[i];
				createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
			}
		} finally {
			pdom.releaseReadLock();
		}
	}
	
	public String getLabel() {
		return Messages.format(CSearchMessages.getString("PDOMSearchPatternQuery.PatternQuery_labelPatternInScope"), getLabel(), patternStr, scopeDesc); //$NON-NLS-1$
	}
	
}
