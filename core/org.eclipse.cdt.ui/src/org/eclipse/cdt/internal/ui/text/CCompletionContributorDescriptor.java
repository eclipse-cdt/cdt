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
package org.eclipse.cdt.internal.ui.text;

import java.util.Arrays;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICCompletionContributor;

/**
 * CCompletionContributorDescriptor
 */
public class CCompletionContributorDescriptor {
	private IConfigurationElement fConfigurationElement;
	private ICCompletionContributor fContributorInstance;
	private ITranslationUnit fLastUnit;
	private Boolean fStatus;
	private boolean fLastResult;

	private static final String ID= "id"; //$NON-NLS-1$
	private static final String CLASS= "class"; //$NON-NLS-1$
	
	public CCompletionContributorDescriptor(IConfigurationElement element) {
		fConfigurationElement= element;
		fContributorInstance= null;
		fLastUnit= null;
		fStatus= null; // undefined
		if (fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT).length == 0) {
			fStatus= Boolean.TRUE;
		}
	}
			
	public IStatus checkSyntax() {
		IConfigurationElement[] children= fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		if (children.length > 1) {
			String id= fConfigurationElement.getAttribute(ID);
			return new StatusInfo(IStatus.ERROR, "Only one <enablement> element allowed. Disabling " + id); //$NON-NLS-1$
		}
		return new StatusInfo(IStatus.OK, "Syntactically correct completion contributor"); //$NON-NLS-1$
	}
	
	private boolean matches(ITranslationUnit unit) {
		if (fStatus != null) {
			return fStatus.booleanValue();
		}
		
		IConfigurationElement[] children= fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		if (children.length == 1) {
			if (unit.equals(fLastUnit)) {
				return fLastResult;
			}
			try {
				ExpressionConverter parser= ExpressionConverter.getDefault();
				Expression expression= parser.perform(children[0]);
				EvaluationContext evalContext= new EvaluationContext(null, unit);
				evalContext.addVariable("translationUnit", unit); //$NON-NLS-1$
				String[] natures= unit.getCProject().getProject().getDescription().getNatureIds();
				evalContext.addVariable("projectNatures", Arrays.asList(natures)); //$NON-NLS-1$
	
				fLastResult= !(expression.evaluate(evalContext) != EvaluationResult.TRUE);
				fLastUnit= unit;
				return fLastResult;
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
		fStatus= Boolean.FALSE;
		return false;
	}

	public ICCompletionContributor getCCompletionContributor(ITranslationUnit cunit) throws CoreException {
		if (matches(cunit)) {
			if (fContributorInstance == null) {
				try {
					fContributorInstance= (ICCompletionContributor)fConfigurationElement.createExecutableExtension(CLASS);
					final ICCompletionContributor c = fContributorInstance;
					// Run the initialiser the class
					ISafeRunnable runnable = new ISafeRunnable() {
						public void run() throws Exception {
							// Initialize
							c.initialize();
						}
						public void handleException(Throwable exception) {
						}
					};
					Platform.run(runnable);
				} catch (ClassCastException e) {
					throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "", e)); //$NON-NLS-1$
				}
			}
			return fContributorInstance;
		}
		return null;
	}


	private boolean matches(IProject project) {
		if (fStatus != null) {
			return fStatus.booleanValue();
		}
		
		IConfigurationElement[] children= fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		if (children.length == 1 && project != null) {
			try {
				ExpressionConverter parser= ExpressionConverter.getDefault();
				Expression expression= parser.perform(children[0]);
				EvaluationContext evalContext= new EvaluationContext(null, project);
				String[] natures= project.getDescription().getNatureIds();
				evalContext.addVariable("projectNatures", Arrays.asList(natures)); //$NON-NLS-1$
	
				fLastResult= !(expression.evaluate(evalContext) != EvaluationResult.TRUE);
				return fLastResult;
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
		fStatus= Boolean.FALSE;
		return false;
	}

	public ICCompletionContributor getCCompletionContributor(IProject project) throws CoreException {
		if (matches(project)) {
			if (fContributorInstance == null) {
				try {
					fContributorInstance= (ICCompletionContributor)fConfigurationElement.createExecutableExtension(CLASS);
					final ICCompletionContributor c = fContributorInstance;
					// Run the initialiser the class
					ISafeRunnable runnable = new ISafeRunnable() {
						public void run() throws Exception {
							// Initialize
							c.initialize();
						}
						public void handleException(Throwable exception) {
						}
					};
					Platform.run(runnable);
				} catch (ClassCastException e) {
					throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "", e)); //$NON-NLS-1$
				}
			}
			return fContributorInstance;
		}
		return null;
	}

}
