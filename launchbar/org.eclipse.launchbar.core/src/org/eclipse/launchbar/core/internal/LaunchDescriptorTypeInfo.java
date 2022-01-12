/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Status;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

public class LaunchDescriptorTypeInfo {
	private final String id;
	private int priority;
	private IConfigurationElement element;
	private ILaunchDescriptorType type;
	private Expression expression;

	public LaunchDescriptorTypeInfo(IConfigurationElement element) {
		this.id = element.getAttribute("id"); //$NON-NLS-1$
		String priorityStr = element.getAttribute("priority"); //$NON-NLS-1$
		this.priority = 1;
		if (priorityStr != null) {
			try {
				priority = Integer.parseInt(priorityStr);
			} catch (NumberFormatException e) {
				// Log it but keep going with the default
				Activator.log(e);
			}
		}

		this.element = element;

		IConfigurationElement[] enabledExpressions = element.getChildren("enablement");//$NON-NLS-1$
		if (enabledExpressions == null || enabledExpressions.length == 0) {
			Activator.log(new Status(Status.WARNING, Activator.PLUGIN_ID,
					"Enablement expression is missing for descriptor type " + id));//$NON-NLS-1$
		} else if (enabledExpressions.length > 1) {
			Activator.log(new Status(Status.WARNING, Activator.PLUGIN_ID,
					"Multiple enablement expressions are detected for descriptor type "//$NON-NLS-1$
							+ id));
		} else {
			try {
				expression = ExpressionConverter.getDefault().perform(enabledExpressions[0]);
			} catch (CoreException e) {
				Activator.log(e);
			}
			if (expression == null) {
				Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
						"Cannot parse enablement expression defined in descriptor type " + id)); //$NON-NLS-1$
			}
		}
	}

	// Used for testing
	LaunchDescriptorTypeInfo(String id, int priority, ILaunchDescriptorType type) {
		this.id = id;
		this.priority = priority;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public int getPriority() {
		return priority;
	}

	public ILaunchDescriptorType getType() throws CoreException {
		if (type == null) {
			type = (ILaunchDescriptorType) element.createExecutableExtension("class"); //$NON-NLS-1$
			element = null;
		}
		return type;
	}

	public boolean enabled(Object launchObject) throws CoreException {
		if (expression == null)
			return true;
		EvaluationContext context = new EvaluationContext(null, launchObject);
		context.setAllowPluginActivation(true);
		EvaluationResult result = expression.evaluate(context);
		return (result == EvaluationResult.TRUE);
	}
}