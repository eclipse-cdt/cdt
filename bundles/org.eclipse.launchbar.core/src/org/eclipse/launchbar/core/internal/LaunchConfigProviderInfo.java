package org.eclipse.launchbar.core.internal;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Status;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;

public class LaunchConfigProviderInfo {
	private final String descriptorTypeId;
	private final int priority;
	private IConfigurationElement element;
	private ILaunchConfigurationProvider provider;
	private Expression expression;

	public LaunchConfigProviderInfo(IConfigurationElement element) {
		this.descriptorTypeId = element.getAttribute("descriptorType"); //$NON-NLS-1$

		String priorityStr = element.getAttribute("priority"); //$NON-NLS-1$
		int priorityNum;
		try {
			priorityNum = Integer.parseInt(priorityStr);
		} catch (NumberFormatException e) {
			priorityNum = 0;
		}
		priority = priorityNum;

		this.element = element;

		IConfigurationElement[] enabledExpressions = element.getChildren("enablement");//$NON-NLS-1$
		if (enabledExpressions == null || enabledExpressions.length == 0) {
			Activator.log(new Status(Status.WARNING, Activator.PLUGIN_ID,
					"Enablement expression is missing for config provider for " + descriptorTypeId)); //$NON-NLS-1$
		} else if (enabledExpressions.length > 1) {
			Activator.log(new Status(Status.WARNING, Activator.PLUGIN_ID,
					"Multiple enablement expressions are detected for config provider for "//$NON-NLS-1$
							+ descriptorTypeId));
		} else {
			try {
				expression = ExpressionConverter.getDefault().perform(enabledExpressions[0]);
			} catch (CoreException e) {
				Activator.log(e);
			}
			if (expression == null) {
				Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
						"Cannot parse enablement expression defined in config provider for " + descriptorTypeId)); //$NON-NLS-1$
			}
		}

	}

	public String getDescriptorTypeId() {
		return descriptorTypeId;
	}

	public int getPriority() {
		return priority;
	}

	public ILaunchConfigurationProvider getProvider() throws CoreException {
		if (provider == null) {
			provider = (ILaunchConfigurationProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
			element = null;
		}
		return provider;
	}

	public boolean enabled(Object element) throws CoreException {
		if (expression == null)
			return true;
		EvaluationResult result = expression.evaluate(new EvaluationContext(null, element));
		return (result == EvaluationResult.TRUE);
	}

}