package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;

/** 
 * Info for ICProject.
 */

class CProjectInfo extends CContainerInfo {

	BinaryContainer vBin;
	ArchiveContainer vLib;

	/**
	 */
	public CProjectInfo(CElement element) {
		super(element);
		vBin = null;
		vLib = null;
	}

	synchronized public IBinaryContainer getBinaryContainer() {
		if (vBin == null) {
			vBin = new BinaryContainer((CProject)getElement());
		}
		return vBin;
	}

	synchronized public IArchiveContainer getArchiveContainer() {
		if (vLib == null) {
			vLib = new ArchiveContainer((CProject)getElement());
		}
		return vLib;
	}

}
