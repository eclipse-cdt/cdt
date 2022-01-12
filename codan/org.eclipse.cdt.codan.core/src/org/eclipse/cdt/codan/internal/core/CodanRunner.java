/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * Collection of methods for running checkers.
 *
 * Clients: this should not be called directly, use ICodanBuilder interface instead
 *
 * CodanRuntime.getInstance().getBuilder()
 */
public class CodanRunner {
	/** Do not instantiate. All methods are static */
	private CodanRunner() {
	}

	/**
	 * Runs all checkers that support "run as you type" mode.
	 *
	 * @param model - the model of given resource such as AST
	 * @param resource - the resource to process
	 * @param monitor - the progress monitor
	 */
	public static void runInEditor(Object model, IResource resource, IProgressMonitor monitor) {
		processResource(resource, model, CheckerLaunchMode.RUN_AS_YOU_TYPE, monitor);
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

	private static void processResource(IResource resource, Object model, CheckerLaunchMode checkerLaunchMode,
			IProgressMonitor monitor) {
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
							} catch (OperationCanceledException e) {
								return;
							} catch (Throwable e) {
								CodanCorePlugin.log(e);
							} finally {
								CheckersTimeStats.getInstance().checkerStop(checker.getClass().getName());
								checker.after(resource);
							}
						}
					}
					monitor.worked(1);
				}
			} finally {
				context.dispose();
				CheckersTimeStats.getInstance().checkerStop(CheckersTimeStats.ALL);
				//CheckersTimeStats.getInstance().printStats();
			}

			if (children != null && (checkerLaunchMode == CheckerLaunchMode.RUN_ON_FULL_BUILD
					|| checkerLaunchMode == CheckerLaunchMode.RUN_ON_DEMAND)) {
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
}
