/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
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

	public Collection<IPath> pathSet();

	public Collection<IPath> containerSet();

	public Collection<ICProject> projectSet();

	public Collection<ICProject> enclosingProjectSet();
}
