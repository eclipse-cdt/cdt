/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia and others.
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
 *     Alex Ruiz (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.cxx;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.IPostSaveListener;
import org.eclipse.cdt.ui.ICEditor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Enables Codan's "run as you type", "run on file save" and "run on file open" launch modes.
 */
class CodanPartListener implements IPartListener2 {
	private CodanCReconciler reconciler;
	private IPostSaveListener postSaveListener;

	/**
	 * Installs CodanPartListener on the given workbench window.
	 * Must be called from the UI thread.
	 */
	static void installOnWindow(IWorkbenchWindow window) {
		final IWorkbenchPage page = window.getActivePage();
		CodanPartListener partListener = new CodanPartListener();
		page.addPartListener(partListener);
		// Check current open editors.
		for (IEditorReference ref : page.getEditorReferences()) {
			partListener.partOpened(ref);
		}
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (isCEditor(part)) {
			CEditor editor = (CEditor) part;
			if (reconciler == null) {
				reconciler = new CodanCReconciler();
			}
			reconciler.install(editor);
			if (postSaveListener == null) {
				postSaveListener = new IPostSaveListener() {
					@Override
					public void saved(ITranslationUnit translationUnit, IProgressMonitor monitor) {
						processResource(translationUnit.getResource(), CheckerLaunchMode.RUN_ON_FILE_SAVE);
					}
				};
			}
			editor.addPostSaveListener(postSaveListener);
			IResource resource = editor.getEditorInput().getAdapter(IResource.class);
			processResource(resource, CheckerLaunchMode.RUN_ON_FILE_OPEN);
		}
	}

	private static void processResource(final IResource resource, final CheckerLaunchMode launchMode) {
		if (resource != null) {
			Job job = new Job(NLS.bind(Messages.Startup_AnalyzingFile, resource.getName())) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					CodanRuntime.getInstance().getBuilder().processResource(resource, monitor, launchMode);
					return Status.OK_STATUS;
				}
			};
			job.setRule(resource);
			job.setSystem(true);
			job.schedule();
		}
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
		if (reconciler != null && isCEditor(part)) {
			reconciler.uninstall((CEditor) part);
		}
	}

	private boolean isCEditor(IWorkbenchPart part) {
		// We need to be very careful since this code may be executed in an environment where CDT is
		// installed, but is not actively used.
		// By checking for ICEditor first we avoid loading CEditor class if the part is not a C/C++
		// editor. Loading of CEditor class can be very expensive since it triggers loading of many
		// other classes.
		return part instanceof ICEditor && part instanceof CEditor;
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}
}