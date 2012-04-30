/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Alex Ruiz (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.cxx;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.internal.core.CodanRunner;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Alena Laskavaia
 */
public class Startup implements IStartup {
	@Override
	public void earlyStartup() {
		registerListeners();
	}

	/**
	 * Register part listener for editor to install c ast reconcile listener
	 */
	private void registerListeners() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow active = workbench.getActiveWorkbenchWindow();
				final IWorkbenchPage page = active.getActivePage();
				IPartListener2 partListener = new IPartListener2() {
					CodanCReconciler reconciler;

					@Override
					public void partActivated(IWorkbenchPartReference partRef) {
					}

					@Override
					public void partDeactivated(IWorkbenchPartReference partRef) {
					}

					@Override
					public void partOpened(IWorkbenchPartReference partRef) {
						IWorkbenchPart part = partRef.getPart(false);
						// We need to be very careful since this code may be executed in
						// an invironement where CDT is installed, but is not actively used.
						// By checking for ICEditor first we avoid loading CEditor class if
						// the part is not a C/C++ editor. Loading of CEditor class can be very
						// expensive since it triggers loading of many other classes.
						if (part instanceof ICEditor && part instanceof CEditor) {
							CEditor editor = (CEditor) part;
							if (reconciler == null) {
								reconciler = new CodanCReconciler();
							}
							reconciler.install(editor);
							IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
							if (resource != null) {
								processResource(resource);
							}
						}
					}

					private void processResource(final IResource resource) {
						Job job = new Job(NLS.bind(Messages.Startup_AnalyzingFile, resource.getName())) {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								CodanRunner.processResource(resource, monitor, CheckerLaunchMode.RUN_ON_FILE_OPEN);
								return Status.OK_STATUS;
							}
						};
						job.setRule(resource);
						job.setSystem(true);
						job.schedule();
					}

					@Override
					public void partHidden(IWorkbenchPartReference partRef) {
					}

					@Override
					public void partVisible(IWorkbenchPartReference partRef) {
					}

					@Override
					public void partClosed(IWorkbenchPartReference partRef) {
						IWorkbenchPart part = partRef.getPart(false);
						if (reconciler != null && part instanceof ICEditor && part instanceof CEditor) {
							reconciler.uninstall((CEditor) part);
						}
					}

					@Override
					public void partBroughtToTop(IWorkbenchPartReference partRef) {
					}

					@Override
					public void partInputChanged(IWorkbenchPartReference partRef) {
					}
				};

				page.addPartListener(partListener);
				// Check current open editors.
				IEditorReference[] editorReferences = page.getEditorReferences();
				for (IEditorReference ref : editorReferences) {
					partListener.partOpened(ref);
				}
			}
		});
	}
}
