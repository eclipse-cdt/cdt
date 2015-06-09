/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.launcher;

import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.nls.Messages;

/**
 * Abstract launcher delegate implementation.
 */
public abstract class AbstractLauncherDelegate extends PlatformObject implements ILauncherDelegate {
	// The mandatory id of the extension
	private String id = null;
	// The label of the extension
	private String label = null;
	// The converted expression
	private Expression expression;
	// The hidden attribute
	private boolean hidden;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    if (config == null) return;

		// Initialize the id field by reading the <id> extension attribute.
		// Throws an exception if the id is empty or null.
		id = config.getAttribute("id"); //$NON-NLS-1$
		if (id == null || "".equals(id.trim())) { //$NON-NLS-1$
			throw createMissingMandatoryAttributeException("id", config.getContributor().getName()); //$NON-NLS-1$
		}

		// Try the "label" attribute first
		label = config.getAttribute("label"); //$NON-NLS-1$
		// If "label" is not found or empty, try the "name" attribute as fallback
		if (label == null || "".equals(label.trim())) { //$NON-NLS-1$
			label = config.getAttribute("name"); //$NON-NLS-1$
		}

		// Read the sub elements of the extension
		IConfigurationElement[] children = config.getChildren();
		// The "enablement" element is the only expected one
		if (children != null && children.length > 0) {
			expression = ExpressionConverter.getDefault().perform(children[0]);
		}

		// Read "hidden" attribute
		String value = config.getAttribute("hidden"); //$NON-NLS-1$
		if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
			hidden = Boolean.parseBoolean(value);
		}
	}

	/**
	 * Creates a new {@link CoreException} to be thrown if a mandatory extension attribute
	 * is missing.
	 *
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param extensionId The extension id. Must not be <code>null</code>.
	 *
	 * @return The {@link CoreException} instance.
	 */
	protected CoreException createMissingMandatoryAttributeException(String attributeName, String extensionId) {
		Assert.isNotNull(attributeName);
		Assert.isNotNull(extensionId);

		return new CoreException(new Status(IStatus.ERROR,
				UIPlugin.getUniqueIdentifier(),
				0,
				NLS.bind(Messages.Extension_error_missingRequiredAttribute, attributeName, extensionId),
				null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#getLabel()
	 */
	@Override
	public String getLabel() {
		return label != null ? label.trim() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#getEnablement()
	 */
	@Override
    public Expression getEnablement() {
		return expression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#isHidden()
	 */
	@Override
	public boolean isHidden() {
	    return hidden;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractLauncherDelegate) {
			return id.equals(((AbstractLauncherDelegate)obj).id);
		}
	    return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
	    return id.hashCode();
	}

	/**
	 * Get the title from the settings, and use it as the default title.
	 *
	 * @param properties the setting properties map.
	 * @return the value retrieved via the @see {@link ITerminalsConnectorConstants#PROP_TITLE}, or null if the key hasn't been set.
	 */
	protected String getDefaultTerminalTitle(Map<String, Object> properties) {
		String title = (String)properties.get(ITerminalsConnectorConstants.PROP_TITLE);
		return title != null ? title : null;
	}
}
