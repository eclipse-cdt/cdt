package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinaryParser;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.internal.core.model.parser.BinaryFileAdapter;
import org.eclipse.core.resources.IContainer;
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
			if (res != null && res instanceof IContainer) {
				IContainer container = (IContainer)res;
				IBinaryArchive ar = getBinaryArchive();
				IBinaryObject[] objects = ar.getObjects();
				for (int i = 0; i < objects.length; i++) {
					final IBinaryObject obj = objects[i];
					IFile file = new BinaryFileAdapter(container, obj);
					Binary binary = new Binary(getElement(), file) {
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
					addChild(binary);
				}
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
			IBinaryParser parser = CModelManager.getBinaryParser(project);
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
