package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.utils.elf.AR;
import org.eclipse.cdt.utils.elf.ElfHelper;

/** 
 * Info for ICProject.
 */

class ArchiveInfo extends CFileInfo {

	/**
	 */
	public ArchiveInfo(CElement element) {
		super(element);
	}

	public ICElement [] getChildren() {
		init();
		return super.getChildren();
	}

	public void init() {
		if (hasChanged()) {
			removeChildren();
			loadInfo();
		}
	}

	public boolean isArchive() {
		return true;
	}

	protected void loadInfo() {
		IPath location = ((CFile)getElement()).getLocation();
		IFile file = ((CFile)getElement()).getFile();
		try {
			AR ar = new AR(location.toOSString());
			AR.ARHeader[] header = ar.getHeaders();
			for (int i = 0; i < header.length; i++) {
				ElfHelper helper = new ElfHelper(header[i].getElf());
				//IPath path = new Path(header[i].getObjectName());
				// FIXME:  We should create a special IResource for this files
				// but for now, just bypass.
				Binary binary = new Binary(getElement(), file, header[i].getObjectName()) {
					public IResource getCorrespondingResource() {
						return null;
					}
				};
				// Force the loading so we can dispose;
				((BinaryInfo)(binary.getElementInfo())).elfHelper = helper;
				// Force the loading of the chidren right away so we can
				// dispose of the elf Elper.
				binary.getChildren();
				((BinaryInfo)(binary.getElementInfo())).elfHelper = null;
				helper.dispose();
				addChild(binary);
			}
			ar.dispose();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}
