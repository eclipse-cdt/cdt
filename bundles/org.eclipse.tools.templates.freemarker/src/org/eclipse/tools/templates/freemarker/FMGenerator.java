/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.freemarker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.tools.templates.freemarker.internal.Activator;
import org.osgi.framework.Bundle;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class FMGenerator implements TemplateLoader {

	private final Configuration templateConfig;
	private Bundle bundle;
	private String manifestPath;
	private TemplateManifest manifest;
	private List<IFile> filesToOpen = new ArrayList<>();

	protected FMGenerator() {
		templateConfig = new Configuration(Configuration.VERSION_2_3_22);
		templateConfig.setTemplateLoader(this);
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	public void setTemplateManifestPath(String manifestPath) {
		this.manifestPath = manifestPath;
	}

	public TemplateManifest getManifest() {
		return manifest;
	}

	protected Class<? extends TemplateManifest> getManifestClass() {
		return TemplateManifest.class;
	}

	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		manifest = null;
		try {
			// load manifest file
			StringWriter writer = new StringWriter();
			loadFile(manifestPath, model, writer); // $NON-NLS-1$
			JAXBContext xmlContext = JAXBContext.newInstance(getManifestClass());
			Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
			manifest = (TemplateManifest) unmarshaller.unmarshal(new StringReader(writer.toString()));
		} catch (JAXBException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Loading template manifest", e));
		}

		// generate files
		if (manifest != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile fileToShow = null;
			for (FileTemplate fileTemplate : manifest.getFiles()) {
				IPath destPath = new Path(fileTemplate.getDest());
				IProject project = root.getProject(destPath.segment(0));
				IFile file = project.getFile(destPath.removeFirstSegments(1));
				if (!fileTemplate.isCopy()) {
					generateFile(fileTemplate.getSrc(), model, file, monitor);
				} else {
					try {
						URL url = FileLocator.find(bundle, new Path(fileTemplate.getSrc()), null);
						try (InputStream in = url.openStream()) {
							createParent(file, monitor);
							if (file.exists()) {
								file.setContents(in, true, true, monitor);
							} else {
								file.create(in, true, monitor);
							}
						}
					} catch (IOException e) {
						throw new CoreException(new Status(IStatus.ERROR, Activator.getId(),
								"Reading file " + fileTemplate.getSrc(), e));
					}
				}

				if (fileTemplate.isOpen()) {
					if (fileTemplate.isShow()) {
						if (fileToShow != null) {
							filesToOpen.add(fileToShow);
						}
						fileToShow = file;
					} else {
						filesToOpen.add(file);
					}
				}
			}

			if (fileToShow != null) {
				filesToOpen.add(fileToShow);
			}
		}
	}

	public void loadFile(String templateFile, Object model, Writer out) throws CoreException {
		try {
			Template template = templateConfig.getTemplate(templateFile);
			template.process(model, out);
		} catch (IOException | TemplateException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, Activator.getId(), "Processing template " + templateFile, e));
		}
	}

	public void generateFile(String templateFile, final Object model, final IFile outputFile, IProgressMonitor monitor)
			throws CoreException {
		try (StringWriter writer = new StringWriter()) {
			loadFile(templateFile, model, writer);
			try (ByteArrayInputStream in = new ByteArrayInputStream(
					writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8))) {
				createParent(outputFile, monitor);
				if (outputFile.exists()) {
					outputFile.setContents(in, true, true, monitor);
				} else {
					outputFile.create(in, true, monitor);
				}
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Generating file " + templateFile, e));
		}
	}

	public static void createParent(IResource child, IProgressMonitor monitor) throws CoreException {
		if (child == null)
			return;

		IContainer container = child.getParent();
		if (container.exists()) {
			return;
		}

		IFolder parent = container.getAdapter(IFolder.class);
		createParent(parent, monitor);
		parent.create(true, true, monitor);
	}

	public IFile[] getFilesToOpen() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object findTemplateSource(String name) throws IOException {
		return FileLocator.find(bundle, new Path(name), null);
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
		return new InputStreamReader(url.openStream(), encoding);
	}

	@Override
	public void closeTemplateSource(Object arg0) throws IOException {
		// Nothing
	}

}
