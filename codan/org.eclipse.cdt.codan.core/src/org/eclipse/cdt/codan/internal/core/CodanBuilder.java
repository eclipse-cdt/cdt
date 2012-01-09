/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
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

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.Messages;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.Checkers;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Implementation of {@link ICodanBuilder}
 */
public class CodanBuilder extends IncrementalProjectBuilder implements ICodanBuilder {
	/**
	 * Codan builder id
	 */
	public static final String BUILDER_ID = "org.eclipse.cdt.codan.core.codanBuilder"; //$NON-NLS-1$

	private class CodanDeltaVisitor implements IResourceDeltaVisitor {
		private IProgressMonitor monitor;

		/**
		 * @param monitor
		 */
		public CodanDeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					// handle added resource
					processResourceDelta(resource, monitor);
					break;
				case IResourceDelta.REMOVED:
					// handle removed resource
					break;
				case IResourceDelta.CHANGED:
					// handle changed resource
					processResourceDelta(resource, monitor);
					break;
			}
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
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
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
		processResource(resource, monitor, null, CheckerLaunchMode.RUN_ON_FULL_BUILD);
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
		processResource(resource, monitor, null, mode);
	}

	private void processResourceDelta(IResource resource, IProgressMonitor monitor) {
		processResource(resource, monitor, CheckerLaunchMode.RUN_ON_INC_BUILD);
	}

	protected void processResource(IResource resource, IProgressMonitor monitor, Object model, CheckerLaunchMode checkerLaunchMode) {
		CheckersRegistry chegistry = CheckersRegistry.getInstance();
		int checkers = chegistry.getCheckersSize();
		int memsize = 0;
		if (resource instanceof IContainer) {
			try {
				IResource[] members = ((IContainer) resource).members();
				memsize = members.length;
			} catch (CoreException e) {
				CodanCorePlugin.log(e);
			}
		}
		int tick = 1000;
		// System.err.println("processing " + resource);
		monitor.beginTask(Messages.CodanBuilder_Code_Analysis_On + resource, checkers + memsize * tick);
		try {
			CheckersTimeStats.getInstance().checkerStart(CheckersTimeStats.ALL);
			ICheckerInvocationContext context = new CheckerInvocationContext(resource);
			try {
				for (IChecker checker : chegistry) {
					try {
						if (monitor.isCanceled())
							return;
						if (doesCheckerSupportLaunchMode(checker, checkerLaunchMode)
								&& checker.enabledInContext(resource)
								&& chegistry.isCheckerEnabledForLaunchMode(checker, resource, checkerLaunchMode)) {
							synchronized (checker) {
								try {
									checker.before(resource);
									if (chegistry.isCheckerEnabled(checker, resource)) {
										try {
											CheckersTimeStats.getInstance().checkerStart(checker.getClass().getName());
											if (checkerLaunchMode == CheckerLaunchMode.RUN_AS_YOU_TYPE) {
												((IRunnableInEditorChecker) checker).processModel(model, context);
											} else {
												checker.processResource(resource, context);
											}
										} finally {
											CheckersTimeStats.getInstance().checkerStop(checker.getClass().getName());
										}
									}
								} finally {
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
			if (resource instanceof IContainer
					&& (checkerLaunchMode == CheckerLaunchMode.RUN_ON_FULL_BUILD || checkerLaunchMode == CheckerLaunchMode.RUN_ON_DEMAND)) {
				try {
					IResource[] members = ((IContainer) resource).members();
					for (int i = 0; i < members.length; i++) {
						if (monitor.isCanceled())
							return;
						IResource member = members[i];
						processResource(member, new SubProgressMonitor(monitor, tick));
					}
				} catch (CoreException e) {
					CodanCorePlugin.log(e);
				}
			}
		} finally {
			monitor.done();
		}
	}

	private boolean doesCheckerSupportLaunchMode(IChecker checker, CheckerLaunchMode mode) {
		if (mode == CheckerLaunchMode.RUN_AS_YOU_TYPE)
			return Checkers.canCheckerRunAsYouType(checker);
		return true;
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		processResource(getProject(), monitor);
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new CodanDeltaVisitor(monitor));
	}

	/**
	 * Run all checkers that support "check as you type" mode
	 *
	 * @param model - model of given resource such as ast
	 * @param resource - resource to process
	 * @param monitor - progress monitor
	 */
	public void runInEditor(Object model, IResource resource, IProgressMonitor monitor) {
		if (model == null)
			return;
		processResource(resource, monitor, model, CheckerLaunchMode.RUN_AS_YOU_TYPE);
	}
}
