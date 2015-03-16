/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import java.util.Map;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Implementation of {@link ICodanBuilder}
 */
public class CodanBuilder extends IncrementalProjectBuilder implements ICodanBuilder {
	/**
	 * Codan builder id
	 */
	public static final String BUILDER_ID = "org.eclipse.cdt.codan.core.codanBuilder"; //$NON-NLS-1$
	private boolean enabled = true;

	{
		if (Boolean.valueOf(System.getProperty("codan.disabled"))) { //$NON-NLS-1$
			enabled = false;
		}
	}

	private class CodanDeltaVisitor implements IResourceDeltaVisitor {
		private IProgressMonitor monitor;

		public CodanDeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					// Handle added resource
					processResourceDelta(resource, monitor);
					break;
				case IResourceDelta.REMOVED:
					// Handle removed resource
					break;
				case IResourceDelta.CHANGED:
					// Handle changed resource
					processResourceDelta(resource, monitor);
					break;
			}
			// Return true to continue visiting children.
			return true;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (!enabled)
			return null;
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	@Override
	public void processResource(IResource resource, IProgressMonitor monitor) {
		processResource(resource, monitor, CheckerLaunchMode.RUN_ON_FULL_BUILD);
	}

	/**
	 * Run code analysis on given resource in a given mode
	 *
	 * @param resource - resource to process
	 * @param monitor - progress monitor
	 * @param mode - launch mode, @see {@link CheckerLaunchMode}
	 * @since 2.0
	 */
	@Override
	public void processResource(IResource resource, IProgressMonitor monitor, CheckerLaunchMode mode) {
		if (!enabled)
			return;
		CodanRunner.processResource(resource, mode, monitor);
	}

	private void processResourceDelta(IResource resource, IProgressMonitor monitor) {
		processResource(resource, monitor, CheckerLaunchMode.RUN_ON_INC_BUILD);
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		processResource(getProject(), monitor);
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// The visitor does the work.
		delta.accept(new CodanDeltaVisitor(monitor));
	}

	@Override
	public void processResource(IResource resource, IProgressMonitor monitor, CheckerLaunchMode mode, Object model) {
		if (!enabled) return;
		if (model != null) {
			if (mode == CheckerLaunchMode.RUN_AS_YOU_TYPE)
				CodanRunner.runInEditor(model, resource, monitor);
			else
				throw new IllegalArgumentException("This mode is not supported"); //$NON-NLS-1$
		} else {
			processResource(resource, monitor, mode);
		}
	}

	/**
	 * @return true if build is enable, i.e. running codan
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Globally disable/enable of running codan
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
