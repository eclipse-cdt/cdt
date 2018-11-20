/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.tools.templates.freemarker.SourceRoot;
import org.eclipse.tools.templates.freemarker.TemplateManifest;
import org.osgi.framework.Bundle;

/**
 * Generator for Makefile projects.
 *
 * @since 7.4
 */
public class MakefileProjectGenerator extends FMProjectGenerator {

	public MakefileProjectGenerator(String manifestPath) {
		super(manifestPath);
	}

	@Override
	protected Bundle getSourceBundle() {
		return MakeCorePlugin.getDefault().getBundle();
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) throws CoreException {
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, MakeProjectNature.NATURE_ID });
		ICommand command = description.newCommand();
		CBuilder.setupBuilder(command);
		description.setBuildSpec(new ICommand[] { command });
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		super.generate(model, monitor);

		List<IPathEntry> entries = new ArrayList<>();
		IProject project = getProject();

		// Create the source and output folders
		IFolder buildFolder = getProject().getFolder("build"); //$NON-NLS-1$

		TemplateManifest manifest = getManifest();
		if (manifest != null) {
			List<SourceRoot> srcRoots = getManifest().getSrcRoots();
			if (srcRoots != null && !srcRoots.isEmpty()) {
				for (SourceRoot srcRoot : srcRoots) {
					IFolder sourceFolder = project.getFolder(srcRoot.getDir());
					if (!sourceFolder.exists()) {
						sourceFolder.create(true, true, monitor);
					}

					entries.add(CoreModel.newSourceEntry(sourceFolder.getFullPath(),
							new IPath[] { buildFolder.getFullPath() }));
				}
			} else {
				entries.add(CoreModel.newSourceEntry(getProject().getFullPath()));
			}
		}

		entries.add(CoreModel.newOutputEntry(buildFolder.getFullPath()));
		CoreModel.getDefault().create(project).setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]),
				monitor);
	}

}
