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
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Enables Codan's "run as you type", "run on file save" and "run on file open" launch modes.
 */
class CodanPartListener implements IPartListener2 {
	private CodanCReconciler reconciler;
	private IPostSaveListener postSaveListener;

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
			IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
			processResource(resource, CheckerLaunchMode.RUN_ON_FILE_OPEN);
		}
	}

	private void processResource(final IResource resource, final CheckerLaunchMode launchMode) {
		if (resource != null) {
			Job job = new Job(NLS.bind(Messages.Startup_AnalyzingFile, resource.getName())) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					CodanRunner.processResource(resource, launchMode, monitor);
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