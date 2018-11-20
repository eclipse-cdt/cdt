/*******************************************************************************
 * Copyright (c) 2007, 2017 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/*
 * Convenience class for setting up projects.
 */
class ProjectBuilder {
	private final String name;
	private final boolean cpp;
	private List<IProject> dependencies = new ArrayList<>();
	private Map<String, String> path2content = new HashMap<>();

	ProjectBuilder(String name, boolean cpp) {
		this.name = name;
		this.cpp = cpp;
	}

	ProjectBuilder addDependency(IProject project) {
		dependencies.add(project);
		return this;
	}

	ProjectBuilder addFile(String relativePath, CharSequence content) {
		path2content.put(relativePath, content.toString());
		return this;
	}

	ICProject create() throws Exception {
		ICProject result = cpp ? CProjectHelper.createCCProject(name, "bin", IPDOMManager.ID_NO_INDEXER)
				: CProjectHelper.createCProject(name, "bin", IPDOMManager.ID_NO_INDEXER);

		IFile lastFile = null;
		for (Map.Entry<String, String> entry : path2content.entrySet()) {
			lastFile = TestSourceReader.createFile(result.getProject(), new Path(entry.getKey()), entry.getValue());
		}

		IProjectDescription desc = result.getProject().getDescription();
		desc.setReferencedProjects(dependencies.toArray(new IProject[dependencies.size()]));
		result.getProject().setDescription(desc, new NullProgressMonitor());

		IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.setIndexerId(result, IPDOMManager.ID_FAST_INDEXER);
		if (lastFile != null) {
			// Call reindex explicitly since setting indexer ID doesn't trigger reindexing.
			indexManager.reindex(result);
			IIndex index = indexManager.getIndex(result);
			BaseTestCase.waitUntilFileIsIndexed(index, lastFile);
		}
		BaseTestCase.waitForIndexer(result);
		return result;
	}
}