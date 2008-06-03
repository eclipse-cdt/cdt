/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Javier Montalvo Orus (Symbian) - [174992] default wizard hides special ones
 * David McKnight (IBM) - [216252] MessageFormat.format -> NLS.bind
 * Martin Oberhuber (Wind River) - [235148] get rid of dead code for caching
 * Martin Oberhuber (Wind River) - [235197][api] Unusable wizard after cancelling on first page
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.newconnection;

import java.util.Arrays;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.wizards.registries.IRSEWizardRegistryElement;
import org.eclipse.rse.ui.wizards.registries.RSEAbstractWizardRegistry;

/**
 * RSE New connection wizard registry implementation.
 *
 * Gives access to the new connection wizards contributed by users, by looking
 * up and creating wizard instances based on search criteria like system type or
 * wizard id. Clients should create a new wizard registry instance for each UI
 * "session" using the registry. For instance, an invocation of the new
 * connection wizard (which delegates to sub-wizards) should always create a new
 * registry instance.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RSENewConnectionWizardRegistry extends RSEAbstractWizardRegistry {

	// Initialize-On-Demand Holder Class idiom:
	// Lazy initialization and thread-safe single instance.
	// See http://www-106.ibm.com/developerworks/java/library/j-jtp03304/
	private static class LazyInstanceHolder {
		public static RSENewConnectionWizardRegistry instance = new RSENewConnectionWizardRegistry();
	}

	/**
	 * Return the global new connection wizard registry instance. Note that
	 * using a global registry is problematic because sub-wizard state (and thus
	 * wizard instances) should not be re-used between separate invocations of a
	 * wizard by the user.
	 *
	 * @deprecated Instantiate a wizard registry yourself using
	 *             {@link #RSENewConnectionWizardRegistry()} in order to control
	 *             the lifetime of your wizard registry. Lifetime should be
	 *             limited to the time a wizard is active. Each new wizard
	 *             invocation should create a new wizard registry.
	 */
	public static RSENewConnectionWizardRegistry getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 *
	 * @since org.eclipse.rse.ui 3.0
	 */
	public RSENewConnectionWizardRegistry() {
		super();
	}

	protected IRSEWizardRegistryElement createWizardRegistryElementFor(IConfigurationElement element) {
		IRSEWizardRegistryElement wizardElement = null;

		if ("newConnectionWizard".equals(element.getName())) wizardElement = new RSENewConnectionWizardDescriptor(this, element); //$NON-NLS-1$

		return wizardElement != null ? wizardElement : super.createWizardRegistryElementFor(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.RSEAbstractWizardRegistry#getExtensionPointId()
	 */
	protected String getExtensionPointId() {
		return RSEUIPlugin.getDefault().getBundle().getSymbolicName() + ".newConnectionWizards"; //$NON-NLS-1$
	}

	/**
	 * Returns the new connection wizard descriptor to use for the specified selection.
	 * The selection is expected to contain the selected system type as first element.
	 *
	 * @see #getWizardForSystemType(IRSESystemType) for more information.
	 *
	 * @param selection A structure selection containing the selected system type as first element. Must be not <code>null</code>.
	 * @return A registered new connection wizard descriptor or <code>null</code>.
	 */
	public IRSENewConnectionWizardDescriptor getWizardForSelection(IStructuredSelection selection) {
		assert selection != null && selection.getFirstElement() instanceof IRSESystemType;
		return getWizardForSystemType((IRSESystemType)selection.getFirstElement());
	}

	/**
	 * Returns the new connection wizard to use for the specified system type.
	 * Once queried for a specific system type, the method returns always the same wizard
	 * instance. If there are multiple wizards registered for a specific system type,
	 * the first wizard found will be taken and the other wizards will be dropped. If
	 * this case is happening, a corresponding warning message is send to the error log.
	 *
	 * @param systemType The selected system type to query the wizard for. Must be not <code>null</code>.
	 * @return A registered new connection wizard or the default RSE new connection wizard. Can be only <code>null</code>
	 *         if the default RSE new connection wizard contribution has been removed from plugin.xml!
	 */
	public IRSENewConnectionWizardDescriptor getWizardForSystemType(IRSESystemType systemType) {
		assert systemType != null;

		IRSENewConnectionWizardDescriptor defaultDescriptor = (IRSENewConnectionWizardDescriptor)findElementById("org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizard"); //$NON-NLS-1$
		IRSENewConnectionWizardDescriptor descriptor = null;
		String id = systemType.getId();

		// check if there is any wizard explicitly registered for the given system type
		// Get the list of all wizards and always walk through _all_ of them
		// to find possible duplicates (which will be notified as warnings to
		// the user)
		IRSEWizardRegistryElement[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			IRSEWizardRegistryElement element = elements[i];
			if (element instanceof IRSENewConnectionWizardDescriptor) {
				IRSENewConnectionWizardDescriptor candidate = (IRSENewConnectionWizardDescriptor) element;
				if (candidate != defaultDescriptor) {
					String[] systemTypeIds = candidate.getSystemTypeIds();
					if (Arrays.asList(systemTypeIds).contains(id)) {
						if (descriptor == null) {
							descriptor = candidate;
						} else {
							String message = "Duplicated new connection wizard registration for system type ''{0}'' (wizard id = {1})."; //$NON-NLS-1$
							message = NLS.bind(message, id, candidate.getId());
							RSECorePlugin.getDefault().getLogger().logWarning(message);
						}
					}
				}
			}
		}

		// if the descriptor here is still null, always return the default RSE
		// new connection wizard descriptor
		if (descriptor == null) {
			descriptor = defaultDescriptor;
		}

		return descriptor;
	}
}
