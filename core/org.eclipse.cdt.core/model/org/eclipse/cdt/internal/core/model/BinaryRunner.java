package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class BinaryRunner implements Runnable {
	ArchiveContainer clib;
	BinaryContainer cbin;
	CProject cproject;
	CModelManager factory;
	
	public BinaryRunner(CProject cprj) {
		cproject = cprj;
		cbin = (BinaryContainer)cprj.getBinaryContainer();
		clib = (ArchiveContainer)cprj.getArchiveContainer();
		factory = CModelManager.getDefault();
	}

	public void run() {
		cproject.setStartBinaryRunner(true);
		clib.removeChildren();
		cbin.removeChildren();
		try {
			cproject.getProject().accept(new Visitor(this));
		} catch (CoreException e) {
			//e.printStackTrace();
		}
		fireEvents(cbin);
		fireEvents(clib);
	}

	public void fireEvents(Parent container) {
		// Fired the event.
		ICElement[] children = container.getChildren();
		if (children.length > 0) {
			ICElement root = (ICRoot)factory.getCRoot();
			CElementDelta cdelta = new CElementDelta(root);
			cdelta.added(cproject);
			cdelta.added(container);
			for (int i = 0; i < children.length; i++) {
				cdelta.added(children[i]);
			}
			factory.registerCModelDelta(cdelta);
			factory.fire();
		}
	}

	void addChildIfBinary(IFile file) {
		// Attempt to speed things up by rejecting up front
		// Things we know should not be Binary files.
		if (!factory.isTranslationUnit(file)) {
			IBinaryFile bin = factory.getBinaryFile(file);
			if (bin != null) {
				IResource res = file.getParent();
				ICElement parent = factory.create(res);
				// By creating the element, it will be added to the correct (bin/archive)container.
				factory.create(parent, file, bin);
			}
		}
	}

	class Visitor implements IResourceVisitor {
		BinaryRunner runner;

		public Visitor (BinaryRunner r) {
			runner = r;
		}

		public boolean visit(IResource res) throws CoreException {
			if (res instanceof IFile) {
				runner.addChildIfBinary((IFile)res);
				return false;
			}
			return true;
		}
	}
}
