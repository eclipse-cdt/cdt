/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process.processes;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Creates a template macro value that can be used as a pseudo-unique resource identifier.
 * It is based on the name of the application and is in the form of four capital letters.
 * e.g. Helloworld => HELL
 */
public class CreateResourceIdentifier extends ProcessRunner {

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String valueName = args[0].getSimpleValue();
		String appName = args[1].getSimpleValue();
		
		String value = ""; //$NON-NLS-1$
		if (appName.length() >= 4) {
			value = appName.substring(0, 4).toUpperCase();
		} else {
			value = appName.toUpperCase();
			for (int i=0; i<4-appName.length(); i++) {
				value = value + "X"; //$NON-NLS-1$
			}
		}
		template.getValueStore().put(valueName, value);
	}
}
