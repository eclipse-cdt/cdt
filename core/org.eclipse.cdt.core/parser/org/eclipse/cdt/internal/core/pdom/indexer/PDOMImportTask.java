/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
import org.eclipse.core.runtime.IProgressMonitor;

public class PDOMImportTask extends PDOMRebuildTask {

	public PDOMImportTask(IPDOMIndexer indexer) {
		super(indexer);
	}

	public void run(IProgressMonitor monitor) {
		// mstodo try to import pdom first.
		super.run(monitor);
	}
}
