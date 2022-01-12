/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.internal.qt.core.index.IQMakeEnvProvider.IController;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.ExpressionTagNames;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Represents a IQMakeEnvProvider that is registered via qmakeEnvProvider extension point.
 */
public final class QMakeEnvProviderDescriptor implements Comparable<QMakeEnvProviderDescriptor> {

	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_PRIORITY = "priority"; //$NON-NLS-1$
	private static final String VAR_PROJECTNATURES = "projectNatures"; //$NON-NLS-1$

	private final IConfigurationElement element;
	private final String id;
	private final int priority;
	private final AtomicReference<Boolean> evaluation = new AtomicReference<>();
	private final Expression enablementExpression;

	QMakeEnvProviderDescriptor(IConfigurationElement element) {
		this.element = element;

		id = element.getContributor().getName();

		// parse priority
		int prio = 0;
		String priorityString = element.getAttribute(ATTR_PRIORITY);
		if (priorityString != null) {
			try {
				prio = Integer.parseInt(priorityString);
			} catch (NumberFormatException e) {
				Activator.log("Invalid priority value of " + id, e); //$NON-NLS-1$
			}
		}
		this.priority = prio;

		// parse enablement expression
		Expression expr = null;
		IConfigurationElement[] children = element.getChildren(ExpressionTagNames.ENABLEMENT);
		switch (children.length) {
		case 0:
			evaluation.set(Boolean.TRUE);
			break;
		case 1:
			try {
				ExpressionConverter parser = ExpressionConverter.getDefault();
				expr = parser.perform(children[0]);
			} catch (CoreException e) {
				Activator.log("Error in enablement expression of " + id, e); //$NON-NLS-1$
			}
			break;
		default:
			Activator.log("Too many enablement expressions for " + id); //$NON-NLS-1$
			evaluation.set(Boolean.FALSE);
			break;
		}
		enablementExpression = expr;
	}

	@Override
	public int compareTo(QMakeEnvProviderDescriptor that) {
		if (that == null) {
			return -1;
		}
		return this.priority - that.priority;
	}

	/**
	 * Used by QMakeEnvProviderManager to ask for creating a IQMakeEnv for a specific IQMakeEnvProvider.IController using the related IQMakeEnvProvider
	 *
	 * @param controller the controller
	 * @return the IQMakeEnv instance; or null if no instance is provided
	 */
	public IQMakeEnv createEnv(IController controller) {
		if (!matches(controller)) {
			return null;
		}
		IQMakeEnvProvider provider;
		try {
			provider = (IQMakeEnvProvider) element.createExecutableExtension(ATTR_CLASS);
		} catch (CoreException e) {
			Activator.log("Error in class attribute of " + id, e); //$NON-NLS-1$
			return null;
		}
		return provider.createEnv(controller);
	}

	/**
	 * Checks whether an enablement expression evaluation is true.
	 *
	 * @param controller the controller
	 * @return true if the provider can be used; false otherwise
	 */
	private boolean matches(IController controller) {
		Boolean eval = evaluation.get();
		if (eval != null) {
			return eval.booleanValue();
		}
		if (enablementExpression != null) {
			ICConfigurationDescription configuration = controller != null ? controller.getConfiguration() : null;
			IProject project = configuration != null ? configuration.getProjectDescription().getProject() : null;
			EvaluationContext evalContext = new EvaluationContext(null, project);
			try {
				if (project != null) {
					String[] natures = project.getDescription().getNatureIds();
					evalContext.addVariable(VAR_PROJECTNATURES, Arrays.asList(natures));
				}
				return enablementExpression.evaluate(evalContext) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				Activator.log("Error while evaluating enablement expression for " + id, e); //$NON-NLS-1$
			}
		}
		evaluation.set(Boolean.FALSE);
		return false;
	}

}
