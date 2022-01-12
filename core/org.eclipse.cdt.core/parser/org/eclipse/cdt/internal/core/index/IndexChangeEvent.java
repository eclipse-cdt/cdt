/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.util.Set;

import org.eclipse.cdt.core.index.IIndexChangeEvent;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM.ChangeEvent;

public class IndexChangeEvent implements IIndexChangeEvent {

	private ICProject fAffectedProject;
	private ChangeEvent fChangeEvent;

	public IndexChangeEvent(ICProject projectChanged, ChangeEvent e) {
		fAffectedProject = projectChanged;
		fChangeEvent = e;
	}

	public IndexChangeEvent() {
		fAffectedProject = null;
		fChangeEvent = new ChangeEvent();
	}

	@Override
	public ICProject getAffectedProject() {
		return fAffectedProject;
	}

	public void setAffectedProject(ICProject project, ChangeEvent e) {
		fAffectedProject = project;
		fChangeEvent = e;
	}

	@Override
	public Set<IIndexFileLocation> getFilesCleared() {
		return fChangeEvent.fClearedFiles;
	}

	@Override
	public Set<IIndexFileLocation> getFilesWritten() {
		return fChangeEvent.fFilesWritten;
	}

	@Override
	public boolean isCleared() {
		return fChangeEvent.isCleared();
	}

	@Override
	public boolean isReloaded() {
		return fChangeEvent.isReloaded();
	}

	@Override
	public boolean hasNewFile() {
		return fChangeEvent.hasNewFiles();
	}
}
