/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;

public final class ContributedProcessorDescriptor {
	private final IConfigurationElement fConfigurationElement;
	private Object fProcessorInstance;
	private Boolean fStatus;
	private boolean fLastResult;
	private final Set<String> fHandledMarkerTypes;

	private static final String ID = "id"; //$NON-NLS-1$
	private static final String CLASS = "class"; //$NON-NLS-1$

	private static final String HANDLED_MARKER_TYPES = "handledMarkerTypes"; //$NON-NLS-1$
	private static final String MARKER_TYPE = "markerType"; //$NON-NLS-1$

	public ContributedProcessorDescriptor(IConfigurationElement element, boolean testMarkerTypes) {
		fConfigurationElement = element;
		fProcessorInstance = null;
		fStatus = null; // undefined
		if (fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT).length == 0) {
			fStatus = Boolean.TRUE;
		}
		fHandledMarkerTypes = testMarkerTypes ? getHandledMarkerTypes(element) : null;
	}

	private Set<String> getHandledMarkerTypes(IConfigurationElement element) {
		HashSet<String> map = new HashSet<>(7);
		IConfigurationElement[] children = element.getChildren(HANDLED_MARKER_TYPES);
		for (IConfigurationElement element2 : children) {
			IConfigurationElement[] types = element2.getChildren(MARKER_TYPE);
			for (IConfigurationElement type : types) {
				String attribute = type.getAttribute(ID);
				if (attribute != null) {
					map.add(attribute);
				}
			}
		}
		if (map.isEmpty()) {
			map.add(ICModelMarker.TASK_MARKER);
		}
		return map;
	}

	public IStatus checkSyntax() {
		IConfigurationElement[] children = fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		if (children.length > 1) {
			String id = fConfigurationElement.getAttribute(ID);
			return new StatusInfo(IStatus.ERROR, "Only one < enablement > element allowed. Disabling " + id); //$NON-NLS-1$
		}
		return new StatusInfo(IStatus.OK, "Syntactically correct quick assist/fix processor"); //$NON-NLS-1$
	}

	private boolean matches(ITranslationUnit cunit) {
		if (fStatus != null) {
			return fStatus.booleanValue();
		}

		IConfigurationElement[] children = fConfigurationElement.getChildren(ExpressionTagNames.ENABLEMENT);
		if (children.length == 1) {
			try {
				ExpressionConverter parser = ExpressionConverter.getDefault();
				Expression expression = parser.perform(children[0]);
				EvaluationContext evalContext = new EvaluationContext(null, cunit);
				evalContext.addVariable("compilationUnit", cunit); //$NON-NLS-1$
				ICProject cProject = cunit.getCProject();
				String[] natures = cProject.getProject().getDescription().getNatureIds();
				evalContext.addVariable("projectNatures", Arrays.asList(natures)); //$NON-NLS-1$
				fLastResult = !(expression.evaluate(evalContext) != EvaluationResult.TRUE);
				return fLastResult;
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
		fStatus = Boolean.FALSE;
		return false;
	}

	public Object getProcessor(ITranslationUnit cunit) throws CoreException {
		if (matches(cunit)) {
			if (fProcessorInstance == null) {
				fProcessorInstance = fConfigurationElement.createExecutableExtension(CLASS);
			}
			return fProcessorInstance;
		}
		return null;
	}

	public boolean canHandleMarkerType(String markerType) {
		return fHandledMarkerTypes == null || fHandledMarkerTypes.contains(markerType);
	}
}
