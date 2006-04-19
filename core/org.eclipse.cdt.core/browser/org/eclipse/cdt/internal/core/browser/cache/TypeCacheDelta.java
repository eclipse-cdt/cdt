/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.core.resources.IProject;


public class TypeCacheDelta {
	private IProject fProject = null;
	private ICElementDelta fCElementDelta = null;
	private ITypeSearchScope fScope = null;
	private TypeCacherJob fJob = null;
	
	public TypeCacheDelta(IProject project, ICElementDelta delta) {
		fProject = project;
		fCElementDelta = delta;
	}
	
	public TypeCacheDelta(IProject project, ITypeSearchScope scope) {
		fProject = project;
		fScope = scope;
	}
	
	public TypeCacheDelta(IProject project) {
		fProject = project;
		fScope = new TypeSearchScope();
		fScope.add(project);
	}

	public IProject getProject() {
		return fProject;
	}

	public ITypeSearchScope getScope() {
		return fScope;
	}

	public ICElementDelta getCElementDelta() {
		return fCElementDelta;
	}
	
	public void assignToJob(TypeCacherJob job) {
		fJob = job;
	}
	
	public TypeCacherJob getJob() {
		return fJob;
	}
}
