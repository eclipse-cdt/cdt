/*******************************************************************************
 * Copyright (c) 2011 - 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.interfaces.ILauncherDelegate;
import org.eclipse.tcf.te.ui.terminals.nls.Messages;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Terminal launcher delegate manager implementation.
 */
public class LauncherDelegateManager {
	// Flag to mark the extension point manager initialized (extensions loaded).
	private boolean initialized = false;

	// The map containing all loaded contributions
	private final Map<String, Proxy> extensionsMap = new HashMap<String, Proxy>();

	// The extension point comparator
	private ExtensionPointComparator comparator = null;

	/**
	 * Executable extension proxy implementation.
	 */
	/* default */ static class Proxy {
		// The extension instance. Created on first access
		private ILauncherDelegate instance;
		// The configuration element
		private final IConfigurationElement element;
		// The unique id of the extension.
		private String id;

		/**
		 * Constructor.
		 *
		 * @param element The configuration element. Must not be <code>null</code>.
		 * @throws CoreException In case the configuration element attribute <i>id</i> is <code>null</code> or empty.
		 */
		public Proxy(IConfigurationElement element) throws CoreException {
			Assert.isNotNull(element);
			this.element = element;

			// Extract the extension attributes
			id = element.getAttribute("id"); //$NON-NLS-1$
			if (id == null || id.trim().length() == 0) {
				throw new CoreException(new Status(IStatus.ERROR,
						UIPlugin.getUniqueIdentifier(),
						0,
						NLS.bind(Messages.Extension_error_missingRequiredAttribute, "id", element.getContributor().getName()), //$NON-NLS-1$
						null));
			}

			instance = null;
		}

		/**
		 * Returns the extensions unique id.
		 *
		 * @return The unique id.
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns the configuration element for this extension.
		 *
		 * @return The configuration element.
		 */
		public IConfigurationElement getConfigurationElement() {
			return element;
		}

		/**
		 * Returns the extension class instance. The contributing
		 * plug-in will be activated if not yet activated anyway.
		 *
		 * @return The extension class instance or <code>null</code> if the instantiation fails.
		 */
		public ILauncherDelegate getInstance() {
			if (instance == null) instance = newInstance();
			return instance;
		}

		/**
		 * Returns always a new extension class instance which is different
		 * to what {@link #getInstance()} would return.
		 *
		 * @return A new extension class instance or <code>null</code> if the instantiation fails.
		 */
	    public ILauncherDelegate newInstance() {
			IConfigurationElement element = getConfigurationElement();
			Assert.isNotNull(element);

			// The "class" to load can be specified either as attribute or as child element
			if (element.getAttribute("class") != null || element.getChildren("class").length > 0) { //$NON-NLS-1$ //$NON-NLS-2$
				try {
					return (ILauncherDelegate)element.createExecutableExtension("class"); //$NON-NLS-1$
				} catch (Exception e) {
					// Possible exceptions: CoreException, ClassCastException.
					Platform.getLog(UIPlugin.getDefault().getBundle()).log(new Status(IStatus.ERROR,
									UIPlugin.getUniqueIdentifier(),
									NLS.bind(Messages.Extension_error_invalidExtensionPoint, element.getDeclaringExtension().getUniqueIdentifier()), e));
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// Proxies are equal if they have encapsulate an element
			// with the same unique id
			if (obj instanceof Proxy) {
				return getId().equals(((Proxy)obj).getId());
			}
			return super.equals(obj);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			// The hash code of a proxy is the one from the id
			return getId().hashCode();
		}
	}

	/**
	 * Extension point comparator implementation.
	 * <p>
	 * The comparator assure that extension are read in a predictable order.
	 * <p>
	 * The order of the extensions is defined as following:<br>
	 * <ul><li>Extensions contributed by our own plug-ins (<code>org.eclipse.tcf.te.*</code>)
	 *         in ascending alphabetic order and</li>
	 *     <li>Extensions contributed by any other plug-in in ascending alphabetic order.</li>
	 *     <li>Extensions contributed by the same plug-in in ascending alphabetic order by the
	 *         extensions unique id</li>
	 */
	/* default */ static class ExtensionPointComparator implements Comparator<IExtension> {
		private final static String OWN_PLUGINS_PATTERN = "org.eclipse.tcf.te."; //$NON-NLS-1$

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
	    public int compare(IExtension o1, IExtension o2) {
			// We ignore any comparisation with null and
			if (o1 == null || o2 == null) return 0;
			// Check if it is the exact same element
			if (o1 == o2) return 0;

			// The extensions are compared by the unique id of the contributing plug-in first
			String contributor1 = o1.getContributor().getName();
			String contributor2 = o2.getContributor().getName();

			// Contributions from our own plug-ins comes before 3rdParty plug-ins
			if (contributor1.startsWith(OWN_PLUGINS_PATTERN) && !contributor2.startsWith(OWN_PLUGINS_PATTERN))
				return -1;
			if (!contributor1.startsWith(OWN_PLUGINS_PATTERN) && contributor2.startsWith(OWN_PLUGINS_PATTERN))
				return 1;
			if (contributor1.startsWith(OWN_PLUGINS_PATTERN) && contributor2.startsWith(OWN_PLUGINS_PATTERN)) {
				int value = contributor1.compareTo(contributor2);
				// Within the same plug-in, the extension are sorted by their unique id (if available)
				if (value == 0 && o1.getUniqueIdentifier() != null && o2.getUniqueIdentifier() != null)
					return o1.getUniqueIdentifier().compareTo(o2.getUniqueIdentifier());
				// Otherwise, just return the comparisation result from the contributors
				return value;
			}

			// Contributions from all other plug-ins are sorted alphabetical
			int value = contributor1.compareTo(contributor2);
			// Within the same plug-in, the extension are sorted by their unique id (if available)
			if (value == 0 && o1.getUniqueIdentifier() != null && o2.getUniqueIdentifier() != null)
				return o1.getUniqueIdentifier().compareTo(o2.getUniqueIdentifier());
			// Otherwise, just return the comparisation result from the contributors
			return value;
		}

	}

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static LauncherDelegateManager instance = new LauncherDelegateManager();
	}

	/**
	 * Returns the singleton instance.
	 */
	public static LauncherDelegateManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	LauncherDelegateManager() {
		super();
	}

	/**
	 * Returns the list of all contributed terminal launcher delegates.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed terminal launcher delegate.
	 *
	 * @return The list of contributed terminal launcher delegates, or an empty array.
	 */
	public ILauncherDelegate[] getLauncherDelegates(boolean unique) {
		List<ILauncherDelegate> contributions = new ArrayList<ILauncherDelegate>();
		for (Proxy launcherDelegate : getExtensions().values()) {
			ILauncherDelegate instance = unique ? launcherDelegate.newInstance() : launcherDelegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new ILauncherDelegate[contributions.size()]);
	}

	/**
	 * Returns the terminal launcher delegate identified by its unique id. If no terminal
	 * launcher delegate with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the terminal launcher delegate or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the terminal launcher delegate contribution.
	 *
	 * @return The terminal launcher delegate instance or <code>null</code>.
	 */
	public ILauncherDelegate getLauncherDelegate(String id, boolean unique) {
		ILauncherDelegate contribution = null;
		Map<String, Proxy> extensions = getExtensions();
		if (extensions.containsKey(id)) {
			Proxy proxy = extensions.get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}

	/**
	 * Returns the applicable terminal launcher delegates for the given selection.
	 *
	 * @param selection The selection or <code>null</code>.
	 * @return The list of applicable terminal launcher delegates or an empty array.
	 */
	public ILauncherDelegate[] getApplicableLauncherDelegates(ISelection selection) {
		List<ILauncherDelegate> applicable = new ArrayList<ILauncherDelegate>();

		for (ILauncherDelegate delegate : getLauncherDelegates(false)) {
			Expression enablement = delegate.getEnablement();

			// The launcher delegate is applicable by default if
			// no expression is specified.
			boolean isApplicable = enablement == null;

			if (enablement != null) {
				if (selection != null) {
					// Set the default variable to selection.
					IEvaluationContext currentState = ((IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class)).getCurrentState();
					EvaluationContext context = new EvaluationContext(currentState, selection);
					// Set the "selection" variable to the selection.
					context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
					// Allow plug-in activation
					context.setAllowPluginActivation(true);
					// Evaluate the expression
					try {
						isApplicable = enablement.evaluate(context).equals(EvaluationResult.TRUE);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
						UIPlugin.getDefault().getLog().log(status);
					}
				} else {
					// The enablement is false by definition if
					// there is no selection.
					isApplicable = false;
				}
			}

			// Add the page if applicable
			if (isApplicable) applicable.add(delegate);
		}

		return applicable.toArray(new ILauncherDelegate[applicable.size()]);
	}

	/**
	 * Returns the map of managed extensions. If not loaded before,
	 * this methods trigger the loading of the extensions to the managed
	 * extension point.
	 *
	 * @return The map of extensions.
	 */
	protected Map<String, Proxy> getExtensions() {
		// Load and store the extensions thread-safe!
		synchronized (extensionsMap) {
			if (!initialized) { loadExtensions(); initialized = true; }
		}
		return extensionsMap;
	}

	/**
	 * Returns the extension point comparator instance. If not available,
	 * {@link #doCreateExtensionPointComparator()} is called to create a new instance.
	 *
	 * @return The extension point comparator or <code>null</code> if the instance creation fails.
	 */
	protected final ExtensionPointComparator getExtensionPointComparator() {
		if (comparator == null) {
			comparator = new ExtensionPointComparator();
		}
		return comparator;
	}

	/**
	 * Returns the extensions of the specified extension point sorted.
	 * <p>
	 * For the order of the extensions, see {@link ExtensionPointComparator}.
	 *
	 * @param point The extension point. Must not be <code>null</code>.
	 * @return The extensions in sorted order or an empty array if the extension point has no extensions.
	 */
	protected IExtension[] getExtensionsSorted(IExtensionPoint point) {
		Assert.isNotNull(point);

		List<IExtension> extensions = new ArrayList<IExtension>(Arrays.asList(point.getExtensions()));
		if (extensions.size() > 0) {
			Collections.sort(extensions, getExtensionPointComparator());
		}

		return extensions.toArray(new IExtension[extensions.size()]);
	}

	/**
	 * Loads the extensions for the managed extension point.
	 */
	protected void loadExtensions() {
		// If already initialized, this method will do nothing.
		if (initialized)  return;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint("org.eclipse.tcf.te.ui.terminals.launcherDelegates"); //$NON-NLS-1$
		if (point != null) {
			IExtension[] extensions = getExtensionsSorted(point);
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if ("delegate".equals(element.getName())) { //$NON-NLS-1$
						try {
							Proxy candidate = new Proxy(element);
							if (candidate.getId() != null) {
								// If no extension with this id had been registered before, register now.
								if (!extensionsMap.containsKey(candidate.getId())) {
									extensionsMap.put(candidate.getId(), candidate);
								}
								else {
									throw new CoreException(new Status(IStatus.ERROR,
											UIPlugin.getUniqueIdentifier(),
											0,
											NLS.bind(Messages.Extension_error_duplicateExtension, candidate.getId(), element.getContributor().getName()),
											null));
								}
							} else {
								throw new CoreException(new Status(IStatus.ERROR,
										UIPlugin.getUniqueIdentifier(),
										0,
										NLS.bind(Messages.Extension_error_missingRequiredAttribute, "id", element.getAttribute("label")), //$NON-NLS-1$ //$NON-NLS-2$
										null));
							}
						} catch (CoreException e) {
							Platform.getLog(UIPlugin.getDefault().getBundle()).log(new Status(IStatus.ERROR,
											UIPlugin.getUniqueIdentifier(),
											NLS.bind(Messages.Extension_error_invalidExtensionPoint, element.getDeclaringExtension().getUniqueIdentifier()), e));
						}
					}
				}
			}
		}
	}

}
