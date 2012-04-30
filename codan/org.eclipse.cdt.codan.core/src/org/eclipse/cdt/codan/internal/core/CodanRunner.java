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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

/**
 * Collection of methods for running checkers.
 */
public class CodanRunner {
	/** Do not instantiate. All methods are static */
	private CodanRunner() {}


	/**
	 * Runs all checkers that support "run as you type" mode.
	 *
	 * @param model - the model of given resource such as AST
	 * @param resource - the resource to process
	 * @param monitor - the progress monitor
	 */
	public static void runInEditor(Object model, IResource resource, IProgressMonitor monitor) {
		CodanRunner.processResource(resource, model, CheckerLaunchMode.RUN_AS_YOU_TYPE, monitor);
	}

	/**
	 * Runs all checkers on a given resource.
	 *
	 * @param resource - the resource to run the checkers on, either IFile or IContainer
	 * @param checkerLaunchMode - the checker launch mode.
	 * @param monitor - the progress monitor
	 */
	public static void processResource(IResource resource, CheckerLaunchMode checkerLaunchMode,
			IProgressMonitor monitor) {
		processResource(resource, null, checkerLaunchMode, monitor);
	}

	private static void processResource(IResource resource, Object model,
			CheckerLaunchMode checkerLaunchMode, IProgressMonitor monitor) {
		CheckersRegistry chegistry = CheckersRegistry.getInstance();
		int checkers = chegistry.getCheckersSize();
		IResource[] children = null;
		if (resource instanceof IContainer) {
			try {
				children = ((IContainer) resource).members();
			} catch (CoreException e) {
				CodanCorePlugin.log(e);
			}
		}
		int numChildren = children == null ? 0 : children.length;
		int childWeight = 10;
		// System.err.println("processing " + resource);
		monitor.beginTask(NLS.bind(Messages.CodanRunner_Code_analysis_on, resource.getFullPath().toString()),
				checkers * (1 + numChildren * childWeight));
		try {
			CheckersTimeStats.getInstance().checkerStart(CheckersTimeStats.ALL);
			ICheckerInvocationContext context = new CheckerInvocationContext(resource);
			try {
				for (IChecker checker : chegistry) {
					try {
						if (monitor.isCanceled())
							return;
						if (chegistry.isCheckerEnabled(checker, resource, checkerLaunchMode)) {
							synchronized (checker) {
								try {
									checker.before(resource);
									CheckersTimeStats.getInstance().checkerStart(checker.getClass().getName());
									if (checkerLaunchMode == CheckerLaunchMode.RUN_AS_YOU_TYPE) {
										((IRunnableInEditorChecker) checker).processModel(model, context);
									} else {
										checker.processResource(resource, context);
									}
								} finally {
									CheckersTimeStats.getInstance().checkerStop(checker.getClass().getName());
									checker.after(resource);
								}
							}
						}
						monitor.worked(1);
					} catch (OperationCanceledException e) {
						return;
					} catch (Throwable e) {
						CodanCorePlugin.log(e);
					}
				}
			} finally {
				context.dispose();
				CheckersTimeStats.getInstance().checkerStop(CheckersTimeStats.ALL);
				//CheckersTimeStats.getInstance().printStats();
			}

			if (children != null &&
					(checkerLaunchMode == CheckerLaunchMode.RUN_ON_FULL_BUILD || checkerLaunchMode == CheckerLaunchMode.RUN_ON_DEMAND)) {
				for (IResource child : children) {
					if (monitor.isCanceled())
						return;
					processResource(child, null, checkerLaunchMode, new SubProgressMonitor(monitor, childWeight));
				}
			}
		} finally {
			monitor.done();
		}
	}

	public static void asynchronouslyRemoveMarkersForDisabledProblems(final IResource resource) {
		Job job = new Job(Messages.CodanRunner_Update_markers) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				removeMarkersForDisabledProblems(resource, monitor);
				return Status.OK_STATUS;
			}
		};
        IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		job.setRule(ruleFactory.markerRule(resource));
		job.setSystem(true);
		job.schedule();
	}

	public static void removeMarkersForDisabledProblems(IResource resource, IProgressMonitor monitor) {
		CheckersRegistry chegistry = CheckersRegistry.getInstance();
		Set<String> markerTypes = new HashSet<String>();
		for (IChecker checker : chegistry) {
			Collection<IProblem> problems = chegistry.getRefProblems(checker);
			for (IProblem problem : problems) {
				markerTypes.add(problem.getMarkerType());
			}
		}
		try {
			removeMarkersForDisabledProblems(chegistry, markerTypes, resource, monitor);
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
		}
	}

	private static void removeMarkersForDisabledProblems(CheckersRegistry chegistry,
			Set<String> markerTypes, IResource resource, IProgressMonitor monitor) throws CoreException {
		IResource[] children = null;
		if (resource instanceof IContainer) {
			children = ((IContainer) resource).members();
		}
		int numChildren = children == null ? 0 : children.length;
		int childWeight = 10;
        SubMonitor progress = SubMonitor.convert(monitor, 1 + numChildren * childWeight);
		IProblemProfile resourceProfile = null;
		for (String markerType : markerTypes) {
			IMarker[] markers = resource.findMarkers(markerType, false, IResource.DEPTH_ZERO);
			for (IMarker marker : markers) {
				String problemId = (String) marker.getAttribute(ICodanProblemMarker.ID);
				if (resourceProfile == null) {
					resourceProfile = chegistry.getResourceProfile(resource);
				}
				IProblem problem = resourceProfile.findProblem(problemId);
				if (problem != null && !problem.isEnabled()) {
					marker.delete();
				}
			}
		}
		progress.worked(1);
		if (children != null) {
			for (IResource child : children) {
				if (monitor.isCanceled())
					return;
				removeMarkersForDisabledProblems(chegistry, markerTypes, child,
						progress.newChild(childWeight));
			}
		}
	}
}
