/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.indexer;

import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.PDOM.ChangeEvent;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Used to trigger a change notification when a pdom is loaded.
 * In this situation the pdom itself does not generate a notification.
 */
public class TriggerNotificationTask implements IPDOMIndexerTask {
	private WritablePDOM fPDOM;
	private PDOMManager fManager;

	public TriggerNotificationTask(PDOMManager manager, WritablePDOM pdom) {
		fManager= manager;
		fPDOM= pdom;
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
	public void run(IProgressMonitor monitor) {
		ChangeEvent event= new ChangeEvent();
		event.setReloaded();
		fManager.handleChange(fPDOM, event);
	}

	@Override
	public boolean acceptUrgentTask(IPDOMIndexerTask task) {
		return false;
	}
}
