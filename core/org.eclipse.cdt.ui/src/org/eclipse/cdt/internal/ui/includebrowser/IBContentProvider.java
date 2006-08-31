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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.missingapi.CIndexQueries;
import org.eclipse.cdt.internal.ui.missingapi.CIndexIncludeRelation;
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
			return new Object[] { new IBNode(null, new IBFile(tu), null, null, 0, 0) };
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
			if (tu != null) {
				CIndexIncludeRelation[] includes;
				IBFile directiveFile= null;
				IBFile targetFile= null;
				if (fComputeIncludedBy) {
					ICProject[] projects;
					try {
						projects = CoreModel.getDefault().getCModel().getCProjects();
					} catch (CModelException e) {
						CUIPlugin.getDefault().log(e);
						return NO_CHILDREN;
					}
					includes= CIndexQueries.getInstance().findIncludedBy(projects, tu, NPM);
				}
				else {
					includes= CIndexQueries.getInstance().findIncludesTo(tu, NPM);
					directiveFile= node.getRepresentedFile();
				}
				if (includes.length > 0) {
					ArrayList result= new ArrayList(includes.length);
					for (int i = 0; i < includes.length; i++) {
						CIndexIncludeRelation include = includes[i];
						try {
							if (fComputeIncludedBy) {
								directiveFile= targetFile= new IBFile(tu.getCProject(), include.getIncludedBy());
							}
							else {
								targetFile= new IBFile(tu.getCProject(), include.getIncludes());
							}
						} catch (CModelException e) {
							CUIPlugin.getDefault().log(e);
							targetFile= null;
						}

						if (targetFile != null) {
							IBNode newnode= new IBNode(node, targetFile, directiveFile, 
									include.getName(), include.getOffset(), include.getTimestamp());
							newnode.setIsActiveCode(include.isActiveCode());
							newnode.setIsSystemInclude(include.isSystemInclude());
							result.add(newnode);
						}
					}
					return result.toArray();
				}
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
}
