/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import java.util.Map;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.model.IProblemReporterPersistent;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.cdt.codan.internal.core.model.CodanMarkerProblemReporter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class CodanBuilder extends IncrementalProjectBuilder implements
		ICodanBuilder {
	public static final String BUILDER_ID = "org.eclipse.cdt.codan.core.codanBuilder"; //$NON-NLS-1$

	public class CodanDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.cdt.codan.internal.core.ICodanBuilder#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				processResource(resource, new NullProgressMonitor());
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				processResource(resource, new NullProgressMonitor());
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	public class CodanResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			if (!(resource instanceof IProject))
				processResource(resource, new NullProgressMonitor());
			// return true to continue visiting children.
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
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

	public void processResource(IResource resource, IProgressMonitor monitor) {
		// String string = Platform.getPreferencesService().getString(
		// CodanCorePlugin.PLUGIN_ID, "problems", "", null);
		// System.err.println("set = " + string);
		// delete general markers
		IProblemReporter problemReporter = CodanRuntime.getInstance()
				.getProblemReporter();
		if (problemReporter instanceof CodanMarkerProblemReporter) {
			((CodanMarkerProblemReporter) problemReporter)
					.deleteProblems(resource);
		}
		CheckersRegisry chegistry = CheckersRegisry.getInstance();
		for (IChecker checker : chegistry) {
			try {
				if (monitor.isCanceled())
					return;
				if (chegistry.isCheckerEnabled(checker, resource)
						&& checker.enabledInContext(resource)) {
					checker.processResource(resource);
				}
			} catch (Throwable e) {
				CodanCorePlugin.log(e);
			}
		}
		if (resource instanceof IProject) {
			try {
				resource.accept(getResourceVisitor());
			} catch (CoreException e) {
				CodanCorePlugin.log(e);
			}
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new CodanResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new CodanDeltaVisitor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.ICodanBuilder#getResourceVisitor()
	 */
	public CodanResourceVisitor getResourceVisitor() {
		return new CodanResourceVisitor();
	}

	public void runInEditor(Object model, IResource resource,
			IProgressMonitor monitor) {
		if (model == null)
			return;
		IProblemReporter problemReporter = CodanRuntime.getInstance()
				.getProblemReporter();
		// TODO: this is wrong - should not delete all markers -
		// only those that contributed by the checker that we run now
		if (problemReporter instanceof IProblemReporterPersistent) {
			((IProblemReporterPersistent) problemReporter)
					.deleteProblems(resource);
		}
		CheckersRegisry chegistry = CheckersRegisry.getInstance();
		for (IChecker checker : chegistry) {
			try {
				boolean run = false;
				if (checker.enabledInContext(resource)
						&& chegistry.isCheckerEnabled(checker, resource)) {
					run = true;
				}
				if (run && checker.runInEditor()
						&& checker instanceof IRunnableInEditorChecker)
					((IRunnableInEditorChecker) checker).processModel(model);
				if (monitor.isCanceled())
					break;
			} catch (Throwable e) {
				CodanCorePlugin.log(e);
			}
		}
	}
}
