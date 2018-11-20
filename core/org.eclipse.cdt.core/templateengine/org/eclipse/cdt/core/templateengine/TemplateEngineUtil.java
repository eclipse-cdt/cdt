/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class TemplateEngineUtil {
	public static void log(Throwable t) {
		if (t == null) {
			return;
		}
		if (t instanceof InvocationTargetException) {
			t = ((InvocationTargetException) t).getTargetException();
		}
		if (t instanceof CoreException) {
			ResourcesPlugin.getPlugin().getLog().log(((CoreException) t).getStatus());
		}
		if (t instanceof ProcessFailureException) {
			do {
				List<IStatus> statuses = ((ProcessFailureException) t).getStatuses();
				if (statuses != null) {
					for (IStatus status : statuses) {
						ResourcesPlugin.getPlugin().getLog().log(status);
					}
				}
				t = t.getCause();
			} while (t != null && t instanceof ProcessFailureException);
		} else {
			ResourcesPlugin.getPlugin().getLog().log(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.OK,
					t.getMessage() == null ? t.toString() : t.getMessage(), t));
		}
	}
}
