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
package org.eclipse.cdt.codan.core.builder;

import java.util.Map;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.model.CheckersRegisry;
import org.eclipse.cdt.codan.core.model.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CodanBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "org.eclipse.cdt.codan.core.codanBuilder";

	public class CodanDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				processResource(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				processResource(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	public class CodanResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			processResource(resource);
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

	public void processResource(IResource resource) {
		// String string = Platform.getPreferencesService().getString(
		// CodanCorePlugin.PLUGIN_ID, "problems", "", null);
		// System.err.println("set = " + string);
		// delete general markers
		CodanRuntime.getInstance().getProblemReporter().deleteMarkers(resource);
		for (IChecker checker : CheckersRegisry.getInstance()) {
			try {
				boolean run = false;
				if (checker.enabledInContext(resource))
					run = true;
				if (run)
					checker.processResource(resource);
			} catch (Throwable e) {
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
}
