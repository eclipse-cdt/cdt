package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICRoot;

public class BinaryContainer extends Parent implements IBinaryContainer {

	CProject cProject;
	private long modificationStamp;

	public BinaryContainer (CProject cProject) {
		this (cProject, "bin");
	}

	public BinaryContainer (CProject cProject, String name) {
		super (cProject, null, name, CElement.C_CONTAINER);
		this.cProject = cProject;
		IProject project = cProject.getProject();
		IFolder folder = project.getFolder("Virtual.bin");
		setUnderlyingResource(folder);
	}

	public IBinary[] getBinaries() {
		ICElement[] e = getChildren(true);
		IBinary[] b = new IBinary[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	public boolean hasChildren() {
		return (getChildren(true).length > 0);
	}

	public ICElement [] getChildren() {
		return getChildren(false);
	}

	public ICElement [] getChildren(boolean sync) {
		// The first time probe the entire project to discover binaries.
		if (!cProject.hasRunElf()) {
			cProject.setRunElf(true);
			ElfRunner runner = new ElfRunner(cProject);
			Thread thread = new Thread(runner, "Elf Runner");
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

	public IResource getCorrespondingResource() {
		return null;
	}

	//public IResource getUnderlyingResource() {
	//	return null;
	//}

	void addChildIfExec(CoreModel factory, IFile file) {
		// Attempt to speed things up by rejecting up front
		// Things we know should not be Elf/Binary files.
		if (!factory.isTranslationUnit(file)) {
			ICElement celement = factory.create(file);
			if (celement != null) {
				if (celement instanceof IBinary) {
					IBinary bin = (IBinary)celement;
					if (bin.isExecutable() || bin.isSharedLib()) {
						addChild (bin);
					}
				}
			}
		}
	}

	public CElementInfo createElementInfo() {
		return new CElementInfo(this);
	}

	class Visitor implements IResourceVisitor {
		CoreModel factory = CoreModel.getDefault();
		BinaryContainer cbin;

		public Visitor (BinaryContainer element) {
			cbin = element;
		}

		public boolean visit(IResource res) throws CoreException {
			if (res instanceof IFile) {
				cbin.addChildIfExec(factory, (IFile)res);
				return false;
			}
			return true;
		}
	}
}
