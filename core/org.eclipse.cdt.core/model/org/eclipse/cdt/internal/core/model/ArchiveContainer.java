package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.ICElement;

public class ArchiveContainer extends Parent implements IArchiveContainer {

	CProject cProject;
	private long modificationStamp;

	public ArchiveContainer (CProject cProject) {
		super (cProject, null, "lib", CElement.C_CONTAINER);
		this.cProject = cProject;
		IProject project = cProject.getProject();
		IFolder folder = project.getFolder("Virtual.lib");
		setUnderlyingResource(folder);
	}

	public IArchive[] getArchives() {
		ICElement[] e = getChildren(true);
		IArchive[] a = new IArchive[e.length];
		System.arraycopy(e, 0, a, 0, e.length);
		return a;
	}

	public boolean hasChildren() {
		return (getChildren(true).length > 0);
	}

	public ICElement [] getChildren() {
		return getChildren(false);
	}

	public ICElement [] getChildren(boolean sync) {
		if (!cProject.hasRunElf()) {
			// It is vital to set this to true first, if not we are going to loop
			cProject.setRunElf(true);
			ElfRunner runner = new ElfRunner(cProject);
			Thread thread = new Thread(runner, "Archive Runner");
			// thread.setPriority(Thread.NORM_PRIORITY - 1);
			thread.setDaemon(true);
			thread.start();
			if (sync) {
				try {
					thread.join();
				} catch (InterruptedException e) {
				}
			}
		}
		return super.getChildren();
	}

	void addChildIfLib(IFile file) {
		CModelManager factory = CModelManager.getDefault();
		if (factory.isArchive(file)) {
			ICElement celement = factory.create(file);
			if (celement != null) {
				if (celement instanceof IArchive) {
					addChild (celement);
				}
			}
		}
	}

	public CElementInfo createElementInfo() {
		return new CElementInfo(this);
	}
}
