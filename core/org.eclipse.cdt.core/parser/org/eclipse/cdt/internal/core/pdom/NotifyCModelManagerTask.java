/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;

/**
 * Task to notify the CModel manager of changes to the content types.
 */
public class NotifyCModelManagerTask implements IPDOMIndexerTask {
	private final IProject fProject;

	public NotifyCModelManagerTask(IProject project) {
		fProject= project;
	}

	@Override
	public IPDOMIndexer getIndexer() {
		return null;
	}

	@Override
	public IndexerProgress getProgressInformation() {
		return new IndexerProgress();
	}

	@Override
	public void run(IProgressMonitor monitor) throws InterruptedException {
		IContentType ct1= Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_CXXHEADER);
		IContentType ct2= Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE);
		if (ct1 != null && ct2 != null) {
			final ProjectScope scope = new ProjectScope(fProject);
			CModelManager.getDefault().contentTypeChanged(new ContentTypeChangeEvent[] {
					new ContentTypeChangeEvent(ct1, scope),
					new ContentTypeChangeEvent(ct2, scope)
			});
		}
	}

	@Override
	public boolean acceptUrgentTask(IPDOMIndexerTask task) {
		return false;
	}
}
