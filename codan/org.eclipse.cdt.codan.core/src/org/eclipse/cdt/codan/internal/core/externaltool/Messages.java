/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.externaltool;

import org.eclipse.osgi.util.NLS;

/**
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
	public static String ConfigurationSettings_args_format;
	public static String ConfigurationSettings_path_format;
	public static String ConfigurationSettings_should_display_output;

	static {
		Class<Messages> clazz = Messages.class;
		NLS.initializeMessages(clazz.getName(), clazz);
	}

	private Messages() {}
}
