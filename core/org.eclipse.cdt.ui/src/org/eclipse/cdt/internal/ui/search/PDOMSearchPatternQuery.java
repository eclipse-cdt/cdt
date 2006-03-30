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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
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
	
	private IResource[] scope;
	private String scopeDesc;
	private String pattern;
	
	public PDOMSearchPatternQuery(
			IResource[] scope,
			String scopeDesc,
			String pattern,
			boolean isCaseSensitive,
			int flags) {
		super(flags);
		this.scope = scope;
		this.scopeDesc = scopeDesc;
		this.pattern = pattern;
	}
	
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		// get the list of projects we need to search
		List projects = new ArrayList();
		for (int i = 0; i < scope.length; ++i) {
			if (scope[i] instanceof IWorkspaceRoot) {
				IProject[] p = ((IWorkspaceRoot)scope[i]).getProjects();
				for (int j = 0; j < p.length; ++j)
					projects.add(p[j]);
			} else
				projects.add(scope[i].getProject());
		}
		
		try {
			for (Iterator iproject = projects.iterator(); iproject.hasNext();)
				searchProject((IProject)iproject.next());
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}
	
	private void searchProject(IProject project) throws CoreException {
		if (!CoreModel.hasCNature(project))
			// Not a CDT project
			return;

		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
		PDOMBinding[] bindings = pdom.findBindings(pattern);
		
		for (int i = 0; i < bindings.length; ++i) {
			createMatches(bindings[i]);
		}
	}
	
	public String getLabel() {
		return super.getLabel() + " " + pattern + " in " + scopeDesc;
	}
	
}
