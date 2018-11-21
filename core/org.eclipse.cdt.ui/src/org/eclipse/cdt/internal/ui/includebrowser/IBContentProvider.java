/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

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

	@Override
	public Object getParent(Object element) {
		if (element instanceof IBNode) {
			IBNode node = (IBNode) element;
			return node.getParent();
		}
		return super.getParent(element);
	}

	@Override
	protected Object[] syncronouslyComputeChildren(Object parentElement) {
		if (parentElement instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit) parentElement;
			return new Object[] { new IBNode(null, new IBFile(tu), null, 0, 0, 0) };
		}
		if (parentElement instanceof IBNode) {
			IBNode node = (IBNode) parentElement;
			if (node.isRecursive() || node.getRepresentedIFL() == null) {
				return NO_CHILDREN;
			}
		}
		// Allow for asynchronous computation
		return null;
	}

	@Override
	protected Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
		if (parentElement instanceof IBNode) {
			IBNode node = (IBNode) parentElement;
			IIndexFileLocation ifl = node.getRepresentedIFL();
			ICProject project = node.getCProject();
			if (ifl == null) {
				return NO_CHILDREN;
			}

			IIndex index;
			try {
				ICProject[] scope = CoreModel.getDefault().getCModel().getCProjects();
				index = CCorePlugin.getIndexManager().getIndex(scope,
						IIndexManager.ADD_EXTENSION_FRAGMENTS_INCLUDE_BROWSER);
				index.acquireReadLock();
			} catch (CoreException e) {
				CUIPlugin.log(e);
				return NO_CHILDREN;
			} catch (InterruptedException e) {
				return NO_CHILDREN;
			}

			try {
				IBFile directiveFile = null;
				IBFile targetFile = null;
				IIndexInclude[] includes;
				if (fComputeIncludedBy) {
					includes = findIncludedBy(index, ifl, NPM);
				} else {
					includes = findIncludesTo(index, ifl, NPM);
					directiveFile = node.getRepresentedFile();
				}
				if (includes.length > 0) {
					Set<IBNode> result = new LinkedHashSet<>(includes.length);
					for (IIndexInclude include : includes) {
						try {
							if (fComputeIncludedBy) {
								directiveFile = targetFile = new IBFile(project, include.getIncludedByLocation());
							} else {
								IIndexFileLocation includesPath = include.getIncludesLocation();
								if (includesPath == null) {
									targetFile = new IBFile(include.getFullName());
								} else {
									targetFile = new IBFile(project, includesPath);
								}
							}
							IBNode newnode = new IBNode(node, targetFile, directiveFile, include.getNameOffset(),
									include.getNameLength(), include.getIncludedBy().getTimestamp());
							newnode.setIsActiveCode(include.isActive());
							newnode.setIsSystemInclude(include.isSystemInclude());
							if (!result.contains(newnode) || newnode.isActiveCode())
								result.add(newnode);
						} catch (CoreException e) {
							CUIPlugin.log(e);
						}
					}

					return result.toArray();
				}
			} finally {
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

	private IIndexInclude[] findIncludedBy(IIndex index, IIndexFileLocation ifl, IProgressMonitor pm) {
		try {
			if (ifl != null) {
				IIndexFile[] files = index.getFiles(ifl);
				if (files.length == 1) {
					return index.findIncludedBy(files[0]);
				}
				if (files.length > 0) {
					ArrayList<IIndexInclude> list = new ArrayList<>();
					HashSet<IIndexFileLocation> handled = new HashSet<>();
					for (IIndexFile file : files) {
						final IIndexInclude[] includes = index.findIncludedBy(file);
						for (IIndexInclude indexInclude : includes) {
							if (handled.add(indexInclude.getIncludedByLocation())) {
								list.add(indexInclude);
							}
						}
					}
					return list.toArray(new IIndexInclude[list.size()]);
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	public IIndexInclude[] findIncludesTo(IIndex index, IIndexFileLocation ifl, IProgressMonitor pm) {
		try {
			if (ifl != null) {
				IIndexFile[] files = index.getFiles(ifl);
				if (files.length == 1) {
					return index.findIncludes(files[0]);
				}
				if (files.length > 0) {
					ArrayList<IIndexInclude> list = new ArrayList<>();
					HashSet<IIndexFileLocation> handled = new HashSet<>();
					for (IIndexFile file : files) {
						final IIndexInclude[] includes = index.findIncludes(file);
						for (IIndexInclude indexInclude : includes) {
							if (handled.add(indexInclude.getIncludesLocation())) {
								list.add(indexInclude);
							}
						}
					}
					return list.toArray(new IIndexInclude[list.size()]);
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}
}
