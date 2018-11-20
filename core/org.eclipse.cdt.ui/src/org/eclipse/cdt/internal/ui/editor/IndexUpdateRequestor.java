/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Objects;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

/**
 * A controller for on-demand indexing of files opened in C/C++ editors.
 */
public class IndexUpdateRequestor implements IPreferenceChangeListener {
	private static class IndexUpdateRequestorJob extends Job {
		private final ITranslationUnit tuToAdd;
		private final ITranslationUnit tuToReset;

		/**
		 * @param tu The translation unit to add or to remove from the index.
		 * @param add {@code true} to add, {@code false} to reset index inclusion.
		 */
		IndexUpdateRequestorJob(ITranslationUnit tuToAdd, ITranslationUnit tuToReset) {
			super(CEditorMessages.IndexUpdateRequestor_job_name);
			this.tuToAdd = tuToAdd;
			this.tuToReset = tuToReset;
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				IIndexManager indexManager = CCorePlugin.getIndexManager();
				if (tuToReset != null) {
					indexManager.update(new ICElement[] { CModelUtil.toOriginal(tuToReset) },
							IIndexManager.RESET_INDEX_INCLUSION | IIndexManager.UPDATE_CHECK_TIMESTAMPS);
				}
				if (tuToAdd != null) {
					indexManager.update(new ICElement[] { CModelUtil.toOriginal(tuToAdd) },
							IIndexManager.FORCE_INDEX_INCLUSION | IIndexManager.UPDATE_CHECK_TIMESTAMPS);
				}
			} catch (CoreException e) {
			}
			return Status.OK_STATUS;
		}
	}

	private ITranslationUnit fTu;
	private ITranslationUnit fTuAddedToIndex;

	public void updateIndexInclusion(ITranslationUnit tu) {
		IProject oldProject;
		IProject newProject;
		synchronized (this) {
			oldProject = fTu == null ? null : fTu.getCProject().getProject();
			newProject = tu == null ? null : tu.getCProject().getProject();
			fTu = tu;
		}

		if (Objects.equals(newProject, oldProject)) {
			if (oldProject != null) {
				IndexerPreferences.removeChangeListener(oldProject, this);
			}
			if (newProject != null) {
				IndexerPreferences.addChangeListener(newProject, this);
			}
		}

		if (tu != null) {
			IProject project = tu.getCProject().getProject();
			if (!String.valueOf(true)
					.equals(IndexerPreferences.get(project, IndexerPreferences.KEY_INDEX_ON_OPEN, null))) {
				tu = null;
			}
		}
		requestIndexUpdate(tu);
	}

	private synchronized void requestIndexUpdate(ITranslationUnit tu) {
		if (!Objects.equals(tu, fTuAddedToIndex)) {
			IndexUpdateRequestorJob job = new IndexUpdateRequestorJob(tu, fTuAddedToIndex);
			fTuAddedToIndex = tu;
			job.schedule();
		}
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (IndexerPreferences.KEY_INDEX_ON_OPEN.equals(event.getKey())) {
			requestIndexUpdate(null);
		}
	}
}
