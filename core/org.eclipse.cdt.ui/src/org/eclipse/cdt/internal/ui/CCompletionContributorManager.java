/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;


import java.util.ArrayList;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.text.CCompletionContributorDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICCompletionContributor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICCompletionInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

/**
 * Manages external plugins that contribute completion and function
 * info through the CCompletionContributor extension point
 */

public class CCompletionContributorManager {

	public static final String CONTRIBUTION_EXTENSION = "CCompletionContributor"; //$NON-NLS-1$
	private static CCompletionContributorDescriptor[] fCCompletionContributorDescriptors = null;
	

	static private CCompletionContributorManager fInstance;

	private CCompletionContributorManager() {
		// Initialize and scan the extension points
	}

	public static CCompletionContributorManager getDefault() {
		if (fInstance == null) {
			fInstance = new CCompletionContributorManager();
		}
		return fInstance;
	}

	public IFunctionSummary getFunctionInfo(ICCompletionInvocationContext context, String name) {
		CCompletionContributorDescriptor[] desc = getCCompletionContributorDescriptors();
		for (int i = 0; i < desc.length; i++) {
			try {
				ICCompletionContributor c = null;
				ITranslationUnit unit = context.getTranslationUnit();
				if (unit != null) {
					c = desc[i].getCCompletionContributor(unit);
				} else {
					IProject project = context.getProject();
					c = desc[i].getCCompletionContributor(project);
				}
				IFunctionSummary f = c.getFunctionInfo(context, name);
				if (f != null) {
					return f;
				}
			} catch (CoreException e) {
				//
			}
		}
		
		return null;
	}

	public IFunctionSummary[] getMatchingFunctions(ICCompletionInvocationContext context, String frag) {

		IFunctionSummary[] fs = null;

		CCompletionContributorDescriptor[] desc = getCCompletionContributorDescriptors();
		for (int i = 0; i < desc.length; i++) {
			try {
				ICCompletionContributor c = null;
				ITranslationUnit unit = context.getTranslationUnit();
				if (unit != null) {
					c = desc[i].getCCompletionContributor(unit);
				} else {
					IProject project = context.getProject();
					c = desc[i].getCCompletionContributor(project);
				}
				if (c == null) {
					continue;
				}
				IFunctionSummary[] f = c.getMatchingFunctions(context, frag);
				if (f != null) {
					if (fs == null) {
						fs = f;
					} else {
						IFunctionSummary[] dest = new IFunctionSummary[fs.length + f.length];
						System.arraycopy(fs, 0, dest, 0, fs.length);
						System.arraycopy(f, 0, dest, fs.length, f.length);
						fs = dest;
					}
				}
			} catch (CoreException e) {
				//
			}
		}

		return fs;
	}

	private static CCompletionContributorDescriptor[] getCCompletionContributorDescriptors() {
		if (fCCompletionContributorDescriptors == null) {
			fCCompletionContributorDescriptors= getCCompletionContributorDescriptors(CONTRIBUTION_EXTENSION);
		}
		return fCCompletionContributorDescriptors;
	}


	private static CCompletionContributorDescriptor[] getCCompletionContributorDescriptors(String contributionId) {
		IConfigurationElement[] elements= Platform.getExtensionRegistry().getConfigurationElementsFor(CUIPlugin.PLUGIN_ID, contributionId);
		ArrayList res= new ArrayList(elements.length);
		for (int i= 0; i < elements.length; i++) {
			CCompletionContributorDescriptor desc= new CCompletionContributorDescriptor(elements[i]);
			IStatus status= desc.checkSyntax();
			if (status.isOK()) {
				res.add(desc);
			} else {
				CUIPlugin.getDefault().log(status);
			}
		}
		return (CCompletionContributorDescriptor[]) res.toArray(new CCompletionContributorDescriptor[res.size()]);		
	}	

}
