/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.Status;

public class DescriptionStatus extends Status {

	public DescriptionStatus(String message) {
		this(message, null);
	}
	public DescriptionStatus(String message, Throwable exception) {
		this(OK, message, exception);
	}

	public DescriptionStatus(Throwable exception) {
		this(exception.getLocalizedMessage(), exception);
	}

	public DescriptionStatus(int code, String message, Throwable exception) {
		super(ERROR, CCorePlugin.PLUGIN_ID, code, message, exception);
	}

}
