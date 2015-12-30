/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ArduinoTemplateGenerator implements TemplateLoader {

	private final Configuration config;
	private Path templateRoot = new Path("/templates"); //$NON-NLS-1$

	public ArduinoTemplateGenerator() throws CoreException {
		config = new Configuration(Configuration.VERSION_2_3_22);
		config.setTemplateLoader(this);
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
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.getId(), "Processing template " + templateFile, e)); //$NON-NLS-1$
		}
	}

	@Override
	public Object findTemplateSource(String name) throws IOException {
		return FileLocator.find(Activator.getContext().getBundle(), templateRoot.append(name), null);
	}

	@Override
	public long getLastModified(Object source) {
		try {
			URL url = (URL) source;
			if (url.getProtocol().equals("file")) { //$NON-NLS-1$
				File file = new File(url.toURI());
				return file.lastModified();
			} else {
				return 0;
			}
		} catch (URISyntaxException e) {
			return 0;
		}
	}

	@Override
	public Reader getReader(Object source, String encoding) throws IOException {
		URL url = (URL) source;
		return new InputStreamReader(url.openStream());
	}

	@Override
	public void closeTemplateSource(Object arg0) throws IOException {
		// Nothing to do
	}

}
