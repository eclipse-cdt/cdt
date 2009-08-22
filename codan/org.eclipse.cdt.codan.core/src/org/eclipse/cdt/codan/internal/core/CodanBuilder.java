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

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.ICAstChecker;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICodanAstReconciler;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.internal.core.model.CodanMarkerProblemReporter;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class CodanBuilder extends IncrementalProjectBuilder implements
		ICodanBuilder, ICodanAstReconciler {
	public static final String BUILDER_ID = "org.eclipse.cdt.codan.core.codanBuilder";

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
					.deleteMarkers(resource);
		}
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
		if (resource instanceof IProject) {
			try {
				resource.accept(getResourceVisitor());
			} catch (CoreException e) {
				CodanCorePlugin.log(e);
			}
		}
	}

	public void reconcileAst(IASTTranslationUnit ast, IProgressMonitor monitor) {
		if (ast == null)
			return;
		String filePath = ast.getFilePath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IFile[] resources;
		URI uri = new File(filePath).toURI();
		resources = root.findFilesForLocationURI(uri);
		if (resources != null && resources.length > 0) {
			IFile resource = resources[0];
			IProblemReporter problemReporter = CodanRuntime.getInstance()
					.getProblemReporter();
			// TODO: this is wrong - should not delete all markers -
			// only those that contributed by the checker that we run now
			if (problemReporter instanceof CodanMarkerProblemReporter) {
				((CodanMarkerProblemReporter) problemReporter)
						.deleteMarkers(resource);
			}
			for (IChecker checker : CheckersRegisry.getInstance()) {
				try {
					boolean run = false;
					if (checker.enabledInContext(resource))
						run = true;
					if (run && checker instanceof ICAstChecker
							&& checker.runInEditor())
						((ICAstChecker) checker).processAst(ast);
				} catch (Throwable e) {
					CodanCorePlugin.log(e);
				}
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
}
