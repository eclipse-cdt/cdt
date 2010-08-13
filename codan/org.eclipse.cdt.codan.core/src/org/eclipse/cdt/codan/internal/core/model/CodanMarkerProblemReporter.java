/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.AbstractProblemReporter;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemReporterPersistent;
import org.eclipse.cdt.codan.core.model.IProblemReporterSessionPersistent;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Problem reported that created eclipse markers
 */
public class CodanMarkerProblemReporter extends AbstractProblemReporter
		implements IProblemReporterPersistent,
		IProblemReporterSessionPersistent {
	private IResource resource;
	private IChecker checker;
	private ArrayList<ICodanProblemMarker> toAdd = new ArrayList<ICodanProblemMarker>();

	/**
	 * Create instance, which can be use as factory for
	 * IProblemReporterSessionPersistent or
	 * as IProblemReporterPersistent.
	 */
	public CodanMarkerProblemReporter() {
		super();
	}

	/**
	 * @param resource
	 * @param checker
	 */
	public CodanMarkerProblemReporter(IResource resource, IChecker checker) {
		this.resource = resource;
		this.checker = checker;
	}

	public IResource getResource() {
		return resource;
	}

	public IChecker getChecker() {
		return checker;
	}

	@Override
	protected void reportProblem(ICodanProblemMarker codanProblemMarker) {
		if (checker == null) {
			createProblem(codanProblemMarker);
		} else {
			toAdd.add(codanProblemMarker);
		}
	}

	/**
	 * @param codanProblemMarker
	 */
	protected IMarker createProblem(ICodanProblemMarker codanProblemMarker) {
		try {
			return codanProblemMarker.createMarker();
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
			return null;
		}
	}

	public void deleteProblems(IResource file) {
		try {
			file.deleteMarkers(GENERIC_CODE_ANALYSIS_MARKER_TYPE, true,
					IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
	}

	public void deleteAllProblems() {
		try {
			ResourcesPlugin
					.getWorkspace()
					.getRoot()
					.deleteMarkers(GENERIC_CODE_ANALYSIS_MARKER_TYPE, true,
							IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
		}
	}

	public void deleteProblems(final IResource file, final IChecker checker) {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					Collection<IMarker> markers = findResourceMarkers(file,
							checker);
					for (Iterator<IMarker> iterator = markers.iterator(); iterator
							.hasNext();) {
						IMarker iMarker = iterator.next();
						iMarker.delete();
					}
				}
			}, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
		}
	}

	protected Collection<IMarker> findResourceMarkers(IResource resource,
			IChecker checker) throws CoreException {
		Collection<IMarker> res = new ArrayList<IMarker>();
		IMarker[] markers = resource.findMarkers(
				GENERIC_CODE_ANALYSIS_MARKER_TYPE, true,
				IResource.DEPTH_INFINITE);
		ICheckersRegistry reg = CodanRuntime.getInstance()
				.getCheckersRegistry();
		Collection<IProblem> problems = reg.getRefProblems(checker);
		for (int i = 0; i < markers.length; i++) {
			IMarker m = markers[i];
			String id = m.getAttribute(ICodanProblemMarker.ID, ""); //$NON-NLS-1$
			for (Iterator<IProblem> iterator = problems.iterator(); iterator
					.hasNext();) {
				IProblem iProblem = iterator.next();
				if (iProblem.getId().equals(id)) {
					res.add(m);
				}
			}
		}
		return res;
	}

	/**
	 * @param resource
	 * @param checker
	 * @return session aware problem reporter
	 * @since 1.1
	 */
	public IProblemReporterSessionPersistent createReporter(IResource resource,
			IChecker checker) {
		return new CodanMarkerProblemReporter(resource, checker);
	}

	public void start() {
		if (checker == null)
			deleteProblems(false);
	}

	public void done() {
		if (checker != null) {
			if (toAdd.size() == 0)
				deleteProblems(false);
			else
				reconcileMarkers();
			toAdd.clear();
		}
	}

	protected void reconcileMarkers() {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					Collection<IMarker> markers = findResourceMarkers(resource,
							checker);
					for (Iterator<IMarker> iterator = markers.iterator(); iterator
							.hasNext();) {
						IMarker m = iterator.next();
						ICodanProblemMarker cm = similarMarker(m);
						if (cm == null) {
							m.delete();
						} else {
							updateMarker(m, cm);
							toAdd.remove(cm);
						}
					}
					for (Iterator<ICodanProblemMarker> iterator = toAdd
							.iterator(); iterator.hasNext();) {
						ICodanProblemMarker cm = iterator.next();
						cm.createMarker();
					}
				}
			}, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
		}
	}

	/**
	 * @param m
	 * @param cm
	 */
	protected void updateMarker(IMarker m, ICodanProblemMarker cm) {
		IProblemLocation loc = cm.getLocation();
		try {
			if (m.getAttribute(IMarker.LINE_NUMBER, 0) != loc.getLineNumber())
				m.setAttribute(IMarker.LINE_NUMBER, loc.getLineNumber());
			if (m.getAttribute(IMarker.CHAR_START, 0) != loc.getStartingChar())
				m.setAttribute(IMarker.CHAR_START, loc.getStartingChar());
			if (m.getAttribute(IMarker.CHAR_END, 0) != loc.getEndingChar())
				m.setAttribute(IMarker.CHAR_END, loc.getEndingChar());
		} catch (CoreException e) {
			try {
				m.delete();
				cm.createMarker();
			} catch (CoreException e1) {
				CodanCorePlugin.log(e1);
			}
		}
	}

	/**
	 * @param m
	 * @return
	 */
	protected ICodanProblemMarker similarMarker(IMarker m) {
		ICodanProblemMarker mcm = CodanProblemMarker
				.createCodanProblemMarkerFromResourceMarker(m);
		ArrayList<ICodanProblemMarker> cand = new ArrayList<ICodanProblemMarker>();
		for (Iterator<ICodanProblemMarker> iterator = toAdd.iterator(); iterator
				.hasNext();) {
			ICodanProblemMarker cm = iterator.next();
			if (mcm.equals(cm))
				return cm;
			if (similarTo(mcm, cm)) {
				cand.add(cm);
			}
		}
		if (cand.size() == 1)
			return cand.get(0);
		return null;
	}

	/**
	 * @param mcm
	 * @param cm
	 * @return
	 */
	private boolean similarTo(ICodanProblemMarker mcm, ICodanProblemMarker other) {
		if (!mcm.getProblem().getId().equals(other.getProblem().getId()))
			return false;
		if (!Arrays.equals(mcm.getArgs(), other.getArgs()))
			return false;
		IProblemLocation loc1 = mcm.getLocation();
		IProblemLocation loc2 = other.getLocation();
		if (!loc1.getFile().equals(loc2.getFile()))
			return false;
		if (Math.abs(loc1.getLineNumber() - loc2.getLineNumber()) > 2)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemReporterSessionPersistent#
	 * deleteProblems(boolean)
	 */
	public void deleteProblems(boolean all) {
		if (all == false)
			deleteProblems(resource, checker);
		else
			throw new UnsupportedOperationException();
	}
}
