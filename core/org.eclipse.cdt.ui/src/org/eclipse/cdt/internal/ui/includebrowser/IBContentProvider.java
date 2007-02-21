/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;

/** 
 * This is the content provider for the include browser.
 */
public class IBContentProvider extends AsyncTreeContentProvider {

	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private boolean fComputeIncludedBy = true;

	/**
	 * Constructs the content provider.
	 */
	public IBContentProvider(Display disp) {
		super(disp);
	}

	public Object getParent(Object element) {
		if (element instanceof IBNode) {
			IBNode node = (IBNode) element;
			return node.getParent();
		}
		return super.getParent(element);
	}

	protected Object[] syncronouslyComputeChildren(Object parentElement) {
		if (parentElement instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit) parentElement;
			return new Object[] { new IBNode(null, new IBFile(tu), null, null, 0, 0, 0) };
		}
		if (parentElement instanceof IBNode) {
			IBNode node = (IBNode) parentElement;
			if (node.isRecursive() || node.getRepresentedTranslationUnit() == null) {
				return NO_CHILDREN;
			}
		}
		// allow for async computation
		return null;
	}

	protected Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
		if (parentElement instanceof IBNode) {
			IBNode node = (IBNode) parentElement;
			ITranslationUnit tu= node.getRepresentedTranslationUnit();
			if (tu == null) {
				return NO_CHILDREN;
			}
			
			IIndex index;
			try {
				index = CCorePlugin.getIndexManager().getIndex(tu.getCProject(), 
						fComputeIncludedBy ? IIndexManager.ADD_DEPENDENT : IIndexManager.ADD_DEPENDENCIES);
				index.acquireReadLock();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
				return NO_CHILDREN;
			} catch (InterruptedException e) {
				return NO_CHILDREN;
			}
			
			try {
				IBFile directiveFile= null;
				IBFile targetFile= null;
				IIndexInclude[] includes;
				if (fComputeIncludedBy) {
					includes= findIncludedBy(index, tu, NPM);
				}
				else {
					includes= findIncludesTo(index, tu, NPM);
					directiveFile= node.getRepresentedFile();
				}
				if (includes.length > 0) {
					ArrayList result= new ArrayList(includes.length);
					for (int i = 0; i < includes.length; i++) {
						IIndexInclude include = includes[i];
						try {
							IIndexFileLocation includesPath= include.getIncludesLocation();
							if (fComputeIncludedBy) {
								directiveFile= targetFile= new IBFile(tu.getCProject(), include.getIncludedByLocation());
							}
							else {
								targetFile= new IBFile(tu.getCProject(), includesPath);
							}
							IPath fullPath= IndexLocationFactory.getPath(includesPath);
							String name= "???";  //$NON-NLS-1$
							if (fullPath != null && fullPath.segmentCount() > 0) {
								name= fullPath.lastSegment();
							}
								
							IBNode newnode= new IBNode(node, targetFile, directiveFile, 
									name, include.getNameOffset(), 
									include.getNameLength(), 
									include.getIncludedBy().getTimestamp());
							newnode.setIsActiveCode(include.isActive());
							newnode.setIsSystemInclude(include.isSystemInclude());
							result.add(newnode);
						}
						catch (CoreException e) {
							CUIPlugin.getDefault().log(e);
						}
					}

					return result.toArray();
				}
			}
			finally {
				index.releaseReadLock();
			}
		}
		return NO_CHILDREN;
	}

	
	
	public void setComputeIncludedBy(boolean value) {
		fComputeIncludedBy = value;
	}

	public boolean getComputeIncludedBy() {
		return fComputeIncludedBy;
	}
	
	
	private IIndexInclude[] findIncludedBy(IIndex index, ITranslationUnit tu, IProgressMonitor pm) {
		try {
			IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
			if (location != null) {
				IIndexFile file= index.getFile(location);
				if (file != null) {
					return index.findIncludedBy(file);
				}
			}
		}
		catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} 
		return new IIndexInclude[0];
	}

	public IIndexInclude[] findIncludesTo(IIndex index, ITranslationUnit tu, IProgressMonitor pm) {
		try {
			IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
			if (location != null) {
				IIndexFile file= index.getFile(location);
				if (file != null) {
					return index.findIncludes(file);
				}
			}
		}
		catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} 
		return new IIndexInclude[0];
	}
}
