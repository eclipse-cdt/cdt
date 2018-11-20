/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * Locates sources for a C/C++ debug session.
 */
public class CSourceManager implements ICSourceLocator, IPersistableSourceLocator, IAdaptable {
	private ISourceLocator fSourceLocator = null;
	private ILaunch fLaunch = null;

	/**
	 * Constructor for CSourceManager.
	 */
	public CSourceManager(ISourceLocator sourceLocator) {
		setSourceLocator(sourceLocator);
	}

	@Override
	public int getLineNumber(IStackFrame frame) {
		if (getCSourceLocator() != null) {
			return getCSourceLocator().getLineNumber(frame);
		}
		if (frame instanceof ICStackFrame) {
			return ((ICStackFrame) frame).getFrameLineNumber();
		}
		return 0;
	}

	@Override
	public ICSourceLocation[] getSourceLocations() {
		return (getCSourceLocator() != null) ? getCSourceLocator().getSourceLocations() : new ICSourceLocation[0];
	}

	@Override
	public void setSourceLocations(ICSourceLocation[] locations) {
		if (getCSourceLocator() != null) {
			getCSourceLocator().setSourceLocations(locations);
		}
	}

	@Override
	public boolean contains(IResource resource) {
		return (getCSourceLocator() != null) ? getCSourceLocator().contains(resource) : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(CSourceManager.class))
			return (T) this;
		if (adapter.equals(ICSourceLocator.class))
			return (T) this;
		if (adapter.equals(IPersistableSourceLocator.class))
			return (T) this;
		if (adapter.equals(IResourceChangeListener.class) && fSourceLocator instanceof IResourceChangeListener)
			return (T) fSourceLocator;
		return null;
	}

	@Override
	public Object getSourceElement(IStackFrame stackFrame) {
		Object result = null;
		if (getSourceLocator() != null)
			result = getSourceLocator().getSourceElement(stackFrame);
		return result;
	}

	protected ICSourceLocator getCSourceLocator() {
		if (getSourceLocator() instanceof ICSourceLocator)
			return (ICSourceLocator) getSourceLocator();
		return null;
	}

	protected ISourceLocator getSourceLocator() {
		if (fSourceLocator != null)
			return fSourceLocator;
		else if (fLaunch != null)
			return fLaunch.getSourceLocator();
		return null;
	}

	private void setSourceLocator(ISourceLocator sl) {
		fSourceLocator = sl;
	}

	@Override
	public Object findSourceElement(String fileName) {
		if (getCSourceLocator() != null) {
			return getCSourceLocator().findSourceElement(fileName);
		}
		return null;
	}

	@Override
	public String getMemento() throws CoreException {
		if (getPersistableSourceLocator() != null)
			return getPersistableSourceLocator().getMemento();
		return null;
	}

	@Override
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
		if (getPersistableSourceLocator() != null)
			getPersistableSourceLocator().initializeDefaults(configuration);
	}

	@Override
	public void initializeFromMemento(String memento) throws CoreException {
		if (getPersistableSourceLocator() != null)
			getPersistableSourceLocator().initializeFromMemento(memento);
	}

	private IPersistableSourceLocator getPersistableSourceLocator() {
		if (fSourceLocator instanceof IPersistableSourceLocator)
			return (IPersistableSourceLocator) fSourceLocator;
		return null;
	}

	@Override
	public IProject getProject() {
		return (getCSourceLocator() != null) ? getCSourceLocator().getProject() : null;
	}

	@Override
	public void setSearchForDuplicateFiles(boolean search) {
		if (getCSourceLocator() != null)
			getCSourceLocator().setSearchForDuplicateFiles(search);
	}

	@Override
	public boolean searchForDuplicateFiles() {
		return getCSourceLocator() != null && getCSourceLocator().searchForDuplicateFiles();
	}
}
