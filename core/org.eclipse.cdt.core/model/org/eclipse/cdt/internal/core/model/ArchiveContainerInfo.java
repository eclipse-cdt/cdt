package org.eclipse.cdt.internal.core.model;


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 */
public class ArchiveContainerInfo extends OpenableInfo {

	/**
	 * Constructs a new C Model Info 
	 */
	protected ArchiveContainerInfo(CElement element) {
		super(element);
	}

	synchronized void sync() {
		BinaryRunner runner = CModelManager.getDefault().getBinaryRunner(getElement().getCProject());
		if (runner != null) {
			runner.waitIfRunning();
		}
	}

}
