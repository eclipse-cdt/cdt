/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency;

import java.io.IOException;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class AddFileToDependencyTree extends DependencyRequest {
	public static final String[] FILE_TYPES= new String[] {"cpp"}; //$NON-NLS-1$

	IFile resource;
	char[] contents;
	IScannerInfo buildInfo;

	/**
	 * @param path
	 * @param manager
	 */
	public AddFileToDependencyTree(
		IFile resource,
		IPath path,
		DependencyManager manager,
		IScannerInfo info) {
		super(path, manager);
		this.resource = resource;
		this.buildInfo = info;
	}
 
	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
		/* ensure no concurrent write access to tree */
		IDependencyTree tree = manager.getDependencyTree(this.dependencyTreePath, true, /*reuse tree file*/ true /*create if none*/);
		if (tree == null) return true;
		ReadWriteMonitor monitor = manager.getMonitorFor(tree);
		if (monitor == null) return true; // tree got deleted since acquired
		try {
			monitor.enterWrite(); // ask permission to write
			if (!addDocumentToTree(tree)) return false;
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to calculate dependency for " + this.resource + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitWrite(); // free write lock
		}
		return true;
	}
	
	protected boolean addDocumentToTree(IDependencyTree dTree) throws IOException {
		if (!initializeContents()) return false;
		//Need to create document to get string content...
		IDocument document = new IFileDocument(resource, this.contents);
		if (!shouldAddToTree(document)) return false;
		
		String docPath = resource.getLocation().toOSString();
		IScannerInfo newInfo = new ScannerInfo((this.buildInfo != null) ? this.buildInfo.getDefinedSymbols() : null,(this.buildInfo != null) ? this.buildInfo.getIncludePaths() : null);
		
		dTree.add(document,docPath,newInfo);
		return true;
	}
	
	public boolean initializeContents() {
		if (this.contents == null) {
			try {
				IPath location = resource.getLocation();
				if (location != null)
					this.contents = org.eclipse.cdt.internal.core.Util.getFileCharContent(location.toFile(), null);
			} catch (IOException e) {
			}
		}
		return this.contents != null;
	}

	public String toString() {
		return "calculating dependency for:  " + this.resource.getFullPath(); //$NON-NLS-1$
	}
	
	public String[] getFileTypes(){
		return FILE_TYPES;
	}
	
	public boolean shouldAddToTree(IDocument document) {
			String type = document.getType();
			String[] supportedTypes = this.getFileTypes();
			for (int i = 0; i < supportedTypes.length; ++i) {
				if (supportedTypes[i].equals(type))
					return true;
			}
			return false;
	}

}
