/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.Collection;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.runtime.IPath;

public interface ITypeSearchScope {
	
	public boolean isPathScope();
	public boolean isProjectScope();
	public boolean isWorkspaceScope();
	public boolean isEmpty();
	
	public boolean encloses(ITypeSearchScope scope);
	public boolean encloses(ICProject project);
	public boolean encloses(IPath path);
	public boolean encloses(String path);
	public boolean encloses(ICElement element);
	public boolean encloses(IWorkingCopy workingCopy);

	public void add(IWorkingCopy workingCopy);
	public void add(IPath path, boolean addSubfolders, ICProject enclosingProject);
	public void add(ICProject project);
	public void add(ICElement elem);
	public void add(ITypeSearchScope scope);
	public void addWorkspace();
	public void clear();
	public ICProject[] getEnclosingProjects();
	
	public Collection pathSet();
	public Collection containerSet();
	public Collection projectSet();
	public Collection enclosingProjectSet();
}
