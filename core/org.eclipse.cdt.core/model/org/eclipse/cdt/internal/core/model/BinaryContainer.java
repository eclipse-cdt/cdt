package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

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
		if (!cProject.hasStartBinaryRunner()) {
			cProject.setStartBinaryRunner(true);
			BinaryRunner runner = new BinaryRunner(cProject);
			Thread thread = new Thread(runner, "Binary Runner");
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

	public CElementInfo createElementInfo() {
		return new CElementInfo(this);
	}
}
