package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;

/** 
 * Info for ICProject.
 */

class CProjectInfo extends CResourceInfo {

	private BinaryContainer vBin;
	private ArchiveContainer vLib;

	synchronized public IBinaryContainer getBinaryContainer() {
		if (vBin == null) {
			vBin = new BinaryContainer((CProject)getElement());
			addChild(vBin);
		}
		return vBin;
	}

	synchronized public IArchiveContainer getArchiveContainer() {
		if (vLib == null) {
			vLib = new ArchiveContainer((CProject)getElement());
			addChild(vLib);
		}
		return vLib;
	}

	public ICElement[] getChildren() {
        // ensure that BinaryContqainer and ArchiveContainer
        // have been added as children. Side affect of get methods!
		getBinaryContainer();
		getArchiveContainer();
		return super.getChildren();
	}

	/**
	 */
	public CProjectInfo(CElement element) {
		super(element);
		vBin = null;
		vLib = null;
	}
}
