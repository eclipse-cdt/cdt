/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.core.terminals.activator.CoreBundleActivator;
import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalContextPropertiesProvider;
import org.eclipse.tcf.te.core.terminals.nls.Messages;

/**
 * Terminal context properties provider factory.
 */
public final class TerminalContextPropertiesProviderFactory {
	// Flag to remember if the contributions got loaded
	private static boolean contributionsLoaded = false;

	// The list of all loaded contributions
	private static final List<Proxy> contributions = new ArrayList<Proxy>();

	// The proxy used to achieve lazy class loading and plug-in activation
	private static class Proxy implements IExecutableExtension {
		// Reference to the configuration element
		private IConfigurationElement configElement = null;
		// The class implementing the provider
		public String clazz;
		// The context properties provider instance
		private ITerminalContextPropertiesProvider provider = null;
		// The converted expression
		private Expression expression;

		/**
		 * Constructor.
		 */
		protected Proxy() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
		 */
		@Override
		public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
			Assert.isNotNull(config);
			this.configElement = config;

			// Read the class attribute.
			// Throws an exception if the attribute value is empty or null.
			clazz = config.getAttribute("class"); //$NON-NLS-1$
			if (clazz == null || "".equals(clazz.trim())) { //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR,
										CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.Extension_error_missingRequiredAttribute, "class", config.getContributor().getName()))); //$NON-NLS-1$
			}

			// Read the "enablement" sub element of the extension
			IConfigurationElement[] children = configElement.getChildren("enablement"); //$NON-NLS-1$
			if (children == null || children.length == 0) {
				throw new CoreException(new Status(IStatus.ERROR,
								CoreBundleActivator.getUniqueIdentifier(),
								NLS.bind(Messages.Extension_error_missingRequiredAttribute, "enablement", config.getContributor().getName()))); //$NON-NLS-1$
			}
			// Only one "enablement" element is expected
			expression = ExpressionConverter.getDefault().perform(children[0]);
		}

		/**
		 * Return the real terminal context properties provider instance for this proxy.
		 */
		protected ITerminalContextPropertiesProvider getProvider() {
			if (provider == null && configElement != null) {
				try {
					// Create the service class instance via the configuration element
					Object provider = configElement.createExecutableExtension("class"); //$NON-NLS-1$
					if (provider instanceof ITerminalContextPropertiesProvider) {
						this.provider = (ITerminalContextPropertiesProvider)provider;
					}
					else {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "Terminal context properties provider '" + provider.getClass().getName() + "' not of type ITerminalContextPropertiesProvider."); //$NON-NLS-1$ //$NON-NLS-2$
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
				}
				catch (CoreException e) {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "Cannot create terminal context properties provider '" + clazz + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
			}
			return provider;
		}

		/**
		 * Returns if or if not the context properties provider contribution is enabled for
		 * the given terminal context.
		 *
		 * @param context The terminal context or <code>null</code>.
		 * @return <code>True</code> if the context properties provider contribution is enabled
		 *         for the given terminal context, <code>false</code> otherwise.
		 */
		protected boolean isEnabled(Object context) {
			if (context == null) {
				return getEnablement() == null;
			}

			Expression enablement = getEnablement();

			// The service contribution is enabled by default if no expression is specified.
			boolean enabled = enablement == null;

			if (enablement != null) {
				// Set the default variable to the service context.
				EvaluationContext evalContext = new EvaluationContext(null, context);
				// Allow plug-in activation
				evalContext.setAllowPluginActivation(true);
				// Evaluate the expression
				try {
					enabled = enablement.evaluate(evalContext).equals(EvaluationResult.TRUE);
				} catch (CoreException e) {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e);
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
			}

			return enabled;
		}

		/**
		 * Returns the enablement expression.
		 *
		 * @return The enablement expression or <code>null</code>.
		 */
		protected Expression getEnablement() {
			return expression;
		}
	}


	/**
	 * Creates a new terminal context properties provider proxy instance and initialize it.
	 *
	 * @param config The configuration element. Must not be <code>null</code>.
	 * @return The new terminal context properties provider proxy instance.
	 */
	private static Proxy getProxy(IConfigurationElement config) {
		Assert.isNotNull(config);
		Proxy proxy = new Proxy();
		try {
			proxy.setInitializationData(config, null, null);
		} catch (CoreException e) {
			if (Platform.inDebugMode()) {
				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(e.getStatus());
			}
		}
		return proxy;
	}

	/**
	 * Load the terminal context properties provider contributions.
	 */
	private static void loadContributions() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.tcf.te.core.terminals.contextPropertiesProviders"); //$NON-NLS-1$
		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();
			if (extensions != null) {
				for (IExtension extension : extensions) {
					IConfigurationElement[] configElements = extension.getConfigurationElements();
					if (configElements != null) {
						for (IConfigurationElement configElement : configElements) {
							if ("contextPropertiesProvider".equals(configElement.getName())) { //$NON-NLS-1$
								Proxy proxy = getProxy(configElement);
								contributions.add(proxy);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Get the terminal context properties provider for the given context. The first terminal
	 * context properties provider which is enabled is returned.
	 *
	 * @param context The terminal context. Must not be <code>null</code>.
	 *
	 * @return The service or <code>null</code>.
	 */
	public static ITerminalContextPropertiesProvider getProvider(Object context) {
		Assert.isNotNull(context);

		// Load the contributions if not yet loaded
		synchronized (contributions) {
			if (!contributionsLoaded) {
				loadContributions();
				contributionsLoaded = true;
			}
        }

		for (Proxy proxy : contributions) {
			if (proxy.isEnabled(context)) {
				return proxy.getProvider();
			}
		}

		return null;
	}

}
