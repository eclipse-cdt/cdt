/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class QtTemplateGenerator {

	private final Configuration config;

	public QtTemplateGenerator() throws CoreException {
		config = new Configuration(Configuration.VERSION_2_3_22);
		URL templateDirURL = FileLocator.find(Activator.getDefault().getBundle(), new Path("/templates"), null); //$NON-NLS-1$
		try {
			config.setDirectoryForTemplateLoading(new File(FileLocator.toFileURL(templateDirURL).toURI()));
		} catch (IOException | URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Template configuration", e));
		}
	}

	public void generateFile(final Object model, String templateFile, final IFile outputFile, IProgressMonitor monitor)
			throws CoreException {
		try {
			final Template template = config.getTemplate(templateFile);
			try (StringWriter writer = new StringWriter()) {
				template.process(model, writer);
				try (ByteArrayInputStream in = new ByteArrayInputStream(
						writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8))) {
					if (outputFile.exists()) {
						outputFile.setContents(in, true, true, monitor);
					} else {
						outputFile.create(in, true, monitor);
					}
				}
			}
		} catch (IOException | TemplateException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, "Processing template " + templateFile, e));
		}
	}

}
