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
import java.io.FileWriter;
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
 * Copies a File to the Project.
 */
public class Copy extends ProcessRunner {
	
	/**
	 * This method  Copies a File to the Project.
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
					throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("Copy.0") + sourcePath)); //$NON-NLS-1$
				}
			} catch (IOException e1) {
				throw new ProcessFailureException(Messages.getString("Copy.1") + sourcePath); //$NON-NLS-1$
			}
			File targetFile = new File(file[1].getSimpleValue());
			boolean replaceable = file[2].getSimpleValue().equals("true"); //$NON-NLS-1$
			if (replaceable) {
				String fileContents;
				try {
					fileContents = ProcessHelper.readFromFile(sourceURL);
				} catch (IOException e1) {
					throw new ProcessFailureException(Messages.getString("Copy.3") + sourcePath); //$NON-NLS-1$
				}
				fileContents = ProcessHelper.getValueAfterExpandingMacros(fileContents, ProcessHelper.getReplaceKeys(fileContents), template.getValueStore());
				if (!targetFile.getParentFile().exists()) {
					targetFile.getParentFile().mkdirs();
				}
				FileWriter writer = null;
				try {
					writer = new FileWriter(targetFile);
					writer.write(fileContents);
				} catch (IOException e) {
					throw new ProcessFailureException(Messages.getString("Copy.4"), e); //$NON-NLS-1$
				} finally {
					if (writer != null) {
						try {
							writer.close();
						} catch (IOException ioe) {// ignore
						}
					}
				}
			} else {
				try {
					ProcessHelper.copyBinaryFile(sourceURL, targetFile);
				} catch (IOException e) {
					throw new ProcessFailureException(Messages.getString("Copy.5"), e); //$NON-NLS-1$
				}
			}
		}
	}
}
