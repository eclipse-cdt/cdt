/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.registries;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.RSECorePlugin;

/**
 * Abstract core implementation of a wizard registry.
 */
public abstract class RSEAbstractWizardRegistry {
	private final Map elements = new LinkedHashMap();
	private boolean isInitialized = false;
	
	/**
	 * Constructor.
	 */
	public RSEAbstractWizardRegistry() {
	}

	/**
	 * Initialize the wizard registry by reading the associated wizard
	 * extension point.
	 */
	protected void initialize() {
		elements.clear();
		
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(getExtensionPointId());
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element == null) continue;
			internalCreateRegistryElementFor(element);
		}
		
		isInitialized = true;
	}
	
	// Internal handle the creation of wizard registry elements.
	private void internalCreateRegistryElementFor(IConfigurationElement element) {
		assert element != null;
		IRSEWizardRegistryElement wizardElement = createWizardRegistryElementFor(element);
		if (wizardElement != null && wizardElement.isValid() && !elements.containsKey(wizardElement.getId())) {
			elements.put(wizardElement.getId(), wizardElement);
		} else if (wizardElement != null && wizardElement.isValid()){
			String message = "Wizard element contribution skipped. Non-unique element id (plugin: {0}, extension point: {1}, id: {2}, element name: {3})."; //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] { element.getContributor().getName(), getExtensionPointId(), wizardElement.getId(), element.getName()});
			RSECorePlugin.getDefault().getLogger().logWarning(message);
		} else if (wizardElement != null) {
			String message = "Wizard element contribution skipped. Invalid or incomplete (plugin: {0}, extension point: {1}, id: {2}, element name: {3})."; //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] { element.getContributor().getName(), getExtensionPointId(), wizardElement.getId(), element.getName()});
			RSECorePlugin.getDefault().getLogger().logWarning(message);
		} else {
			String message = "Wizard contribution skipped. Failed to create wizard descriptor (plugin: {0}, extension point: {1}, extension: {2})."; //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] { element.getContributor().getName(), getExtensionPointId(), element.getDeclaringExtension().getLabel()});
			RSECorePlugin.getDefault().getLogger().logWarning(message);
		}
	}
	
	/**
	 * Returns if or if not the wizard registry has been initialized already.
	 * The initialized state of a wizard registry can be set only by calling
	 * the method <code>RSEAbstractWizardRegistry.initialize()</code>.
	 * 
	 * @return <code>true</code> if the wizard registry is initialized, <code>false</code> otherwise.
	 */
	protected final boolean isInitialized() {
		return isInitialized;
	}
	
	/**
	 * Returns the fully qualified id of the wizard extension point.
	 * 
	 * @return The fully qualified wizard extension point id. Must be never <code>null</code>.
	 */
	protected abstract String getExtensionPointId();

	/**
	 * Creates a wizard registry element instance for the specified configuration element.
	 * The method may return null if the creation of the wizard registry element instance fails.
	 * 
	 * @param element The configuration element. Must be not <code>null</code>.
	 * @return The wizard registry element instance or <code>null</code>.
	 */
	protected IRSEWizardRegistryElement createWizardRegistryElementFor(IConfigurationElement element) {
		IRSEWizardRegistryElement wizardElement = null;
		
		if ("category".equals(element.getName())) wizardElement = new RSEWizardCategory(this, element); //$NON-NLS-1$
		if (wizardElement == null) new RSEWizardRegistryElement(this, element);
		
		return wizardElement;
	}
	
	/**
	 * Returns the list of registered wizard registry elements.
	 * 
	 * @return The list of registered wizard registry elements. May be empty but never <code>null</code>.
	 */
	public IRSEWizardRegistryElement[] getElements() {
		if (!isInitialized()) initialize();
		return (IRSEWizardRegistryElement[])elements.values().toArray(new IRSEWizardRegistryElement[elements.values().size()]);
	}
	

	/**
	 * Look up a registered wizard registry element by the specified id. If no wizard
	 * registry element has been registered under this id, the method will return
	 * <code>null</code>.
	 * 
	 * @param id The fully qualified wizard registry element id. Must be not <code>null</code>.
	 * @return The wizard or <code>null</code>.
	 */
	public IRSEWizardRegistryElement findElementById(String id) {
		assert id != null;
		IRSEWizardRegistryElement[] elements = getElements();
		for (int i = 0; i < elements.length; i++) {
			if (id.equals(elements[i].getId())) return elements[i]; 
		}
		return null;
	}
	
}
