package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/** 
 * Info for ICProject.
 */

class ArchiveInfo extends CFileInfo {

	IBinaryArchive archive;

	/**
	 */
	public ArchiveInfo(CElement element) {
		super(element);
	}

	public ICElement[] getChildren() {
		if (hasChanged()) {
			removeChildren();
			IResource res = null;
			try {
				res = getElement().getResource();
			} catch (CModelException e) {
			}
			IBinaryArchive ar = getBinaryArchive();
			IBinaryObject[] objects = ar.getObjects();
			for (int i = 0; i < objects.length; i++) {
				final IBinaryObject obj = objects[i];
				Binary binary = new Binary(getElement(), res.getLocation().append(obj.getName())) {
					public CElementInfo createElementInfo() {
						return new BinaryInfo(this) {
							/**
							 * @see org.eclipse.cdt.internal.core.model.BinaryInfo#getBinaryObject()
							 */
							IBinaryObject getBinaryObject() {
								return obj;
							}
						};
					}
				};
				BinaryInfo info = (BinaryInfo)binary.getElementInfo();
				info.loadChildren();
				addChild(binary);
			}
		}
		return super.getChildren();
	}

	public boolean isArchive() {
		return true;
	}

	IBinaryArchive getBinaryArchive() {
		if (archive == null) {
			IProject project = getElement().getCProject().getProject();
			IBinaryParser parser = CModelManager.getDefault().getBinaryParser(project);
			if (parser != null) {
				try {
					IFile file = (IFile) getElement().getUnderlyingResource();
					IBinaryFile bfile = parser.getBinary(file);
					if (bfile instanceof IBinaryArchive) {
						archive = (IBinaryArchive) bfile;
					}
				} catch (CModelException e) {
				} catch (IOException e) {
				}
			}
		}
		return archive;
	}
}
