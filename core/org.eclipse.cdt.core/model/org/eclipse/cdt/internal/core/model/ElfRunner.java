package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICRoot;

public class ElfRunner implements Runnable {
	ArchiveContainer clib;
	BinaryContainer cbin;
	CProject cproject;

	public ElfRunner(CProject cprj) {
		cproject = cprj;
		cbin = (BinaryContainer)cprj.getBinaryContainer();
		clib = (ArchiveContainer)cprj.getArchiveContainer();
	}

	public void run() {
		cproject.setRunElf(true);
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
			CModelManager factory = CModelManager.getDefault();
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

	void addChildIfElf(CoreModel factory, IFile file) {
		// Attempt to speed things up by rejecting up front
		// Things we know should not be Elf/Binary files.
		if (!factory.isTranslationUnit(file)) {
			if (factory.isBinary(file)) {
				ICElement celement = factory.create(file);
				if (celement != null) {
					if (celement instanceof IBinary) {
						IBinary bin = (IBinary)celement;
						if (bin.isExecutable() || bin.isSharedLib()) {
							cbin.addChild(bin);
						}
					}
				}
			} else if (factory.isArchive(file)) {
				ICElement celement = factory.create(file);
				if (celement instanceof IArchive) {
					clib.addChild(celement);
				}
			}
		}
	}

	class Visitor implements IResourceVisitor {
		CoreModel factory = CoreModel.getDefault();
		ElfRunner runner;

		public Visitor (ElfRunner r) {
			runner = r;
		}

		public boolean visit(IResource res) throws CoreException {
			if (res instanceof IFile) {
				runner.addChildIfElf(factory, (IFile)res);
				return false;
			}
			return true;
		}
	}
}
