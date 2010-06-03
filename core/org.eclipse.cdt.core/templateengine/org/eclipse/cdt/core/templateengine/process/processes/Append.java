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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


/**
 * Append the contents to the file. 
 */
public class Append extends ProcessRunner {
	
	/**
	 * This method Appends the contents to a file.
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		ProcessArgument[][] files = args[0].getComplexArrayValue();
		for(int i=0; i<files.length; i++) {
			ProcessArgument[] file = files[i];
			String sourcePath = file[0].getSimpleValue();
			URL sourceURL;
			try {
				sourceURL = TemplateEngineHelper.getTemplateResourceURLRelativeToTemplate(template, sourcePath);
				if (sourceURL == null) {
					throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("Append.0") + sourcePath)); //$NON-NLS-1$
				}
			} catch (IOException e1) {
				throw new ProcessFailureException(Messages.getString("Append.1") + sourcePath); //$NON-NLS-1$
			}
			File targetFile = new File(file[1].getSimpleValue());
			boolean replaceable = file[2].getSimpleValue().equals("true"); //$NON-NLS-1$
			String fileContents;
			try {
				fileContents = ProcessHelper.readFromFile(sourceURL);
			} catch (IOException e1) {
				throw new ProcessFailureException(Messages.getString("Append.3") + sourcePath); //$NON-NLS-1$
			}
			if (replaceable) {
				fileContents = ProcessHelper.getValueAfterExpandingMacros(fileContents, ProcessHelper.getReplaceKeys(fileContents), template.getValueStore());
			}
			try {
				ProcessHelper.appendFile(fileContents, targetFile);
			} catch (IOException e) {
				throw new ProcessFailureException(Messages.getString("Append.4"), e); //$NON-NLS-1$
			}
		}
	}
}
