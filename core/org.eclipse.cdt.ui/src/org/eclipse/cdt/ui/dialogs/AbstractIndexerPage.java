/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author Bogdan Gheorghe
 */
public abstract class AbstractIndexerPage extends AbstractCOptionPage {
	protected static final String INDEX_ALL_FILES = DialogsMessages.AbstractIndexerPage_indexAllFiles;
	protected static final String TRUE = String.valueOf(true);

	protected AbstractIndexerPage() {
		super();
	}

	final public IProject getCurrentProject() {
		ICOptionContainer container = getContainer();
		if (container != null) {
			return container.getProject();
		}
		return null;
	}

	/**
	 * Use the properties to initialize the controls of the page. Fill in defaults 
	 * for properties that are missing.
	 * @since 4.0
	 */
	abstract public void setProperties(Properties properties);

	/**
	 * Return the properties according to the selections on the page.
	 * @since 4.0
	 */
	abstract public Properties getProperties();

	/**
	 * {@link #getProperties()} will be called instead.
	 */
	final public void performApply(IProgressMonitor monitor) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@link #setProperties(Properties)} will be called instead.
	 */
	final public void performDefaults() {
		throw new UnsupportedOperationException();
	}

	/**
	 * The framework disables and enables controls created by this page.
	 * After controls are enabled {@link #updateEnablement()} is called to
	 * allow for disabeling controls.
	 * @since 4.0
	 */
	public void updateEnablement() {
	}
}
