/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.make.core.makefile.IMakefileValidator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;

public class GNUMakefileChecker extends ACBuilder {

	public class MyResourceDeltaVisitor implements IResourceDeltaVisitor {
		IProgressMonitor monitor;

		public MyResourceDeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource != null && resource.getProject() == getProject()) {
				if (resource instanceof IFile) {
					// see if this a makefile candidate
					IFile candidate = (IFile) resource;
					if (isMakefileCandidate(candidate)) {
						//  ok verify.
						if (delta.getKind() != IResourceDelta.REMOVED) {
							checkMakefile(candidate, monitor);
						}
					}
				}
			}
			return true;
		}
	}

	protected Map validatorMap = new HashMap();

	public GNUMakefileChecker() {
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IResourceDelta delta = null;

		// For non-full-build fetch the deltas
		if (kind != FULL_BUILD) {
			delta = getDelta(getProject());
		}
                                                                                                                             
		if (delta == null || kind == FULL_BUILD) {
			// Full build
			checkProject(getProject(), monitor);
		} else {
			MyResourceDeltaVisitor vis = new MyResourceDeltaVisitor(monitor);
			if (delta != null) {
				delta.accept(vis);
			}
		}
		checkCancel(monitor);
		return new IProject[0];
	}

	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	protected void checkProject(IProject project, IProgressMonitor monitor) {
		IFile[] files = getCandidateMakefiles(project);
		for (int i = 0; i < files.length; i++) {
			checkMakefile(files[i], monitor);
		}
	}

	protected boolean isMakefileCandidate(IFile file) {
		IFile[] files = getCandidateMakefiles(file.getProject());
		for (int i = 0; i < files.length; i++) {
			if (files[i].getFullPath().equals(file.getFullPath())) {
				return true;
			}
		}
		return false;
	}

	protected void checkMakefile(IFile file, IProgressMonitor monitor) {
		IMakefileValidator validator = getMakefileValidator(file);
		try {
			removeAllMarkers(file);
		} catch (CoreException e) {
			//e.printStackTrace();
		}
		validator.checkFile(file, monitor);
	}

	protected IFile[] getCandidateMakefiles(IProject proj) {
		// FIXME: Find the candidate in the store somewhere.
		IFile defaultMakefile = proj.getFile(new Path("Makefile")); //$NON-NLS-1$
		if (defaultMakefile.exists()) {
			return new IFile[] {defaultMakefile};
		}
		return new IFile[0];
	}

	protected IMakefileValidator getMakefileValidator(IFile file) {
		IMakefileValidator validator = (IMakefileValidator) validatorMap.get(file.getProject());
		if (validator == null) {
			// FIXME: look int the preference store for a value.
			validator = new GNUMakefileValidator();
			validator.setMarkerGenerator(this);
			validatorMap.put(file.getProject(), validator);
		}
		return validator;
	}

	private void removeAllMarkers(IFile file) throws CoreException {
		IWorkspace workspace = file.getWorkspace();

		// remove all markers
		IMarker[] markers = file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			workspace.deleteMarkers(markers);
		}
	}
}
