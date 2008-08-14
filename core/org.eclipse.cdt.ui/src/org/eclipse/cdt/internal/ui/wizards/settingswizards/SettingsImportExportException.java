/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

/**
 * An exception that represents a problem with importing or exporting
 * settings.
 * 
 * @since 5.1
 */
public class SettingsImportExportException extends Exception {

	public SettingsImportExportException() {
	}

	public SettingsImportExportException(String message) {
		super(message);
	}

	public SettingsImportExportException(Throwable t) {
		super(t);
	}

	public SettingsImportExportException(String message, Throwable t) {
		super(message, t);
	}

}
