package org.eclipse.cdt.core.index;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.cdt.internal.core.index.IndexManager;

public class IndexModel {

	static IndexModel indexModel = null;
	static IndexManager manager = null;

	/**
	 * Search Project for tag symbol.
	 */
	public ITagEntry[] query (IProject project, String tag) {
		return manager.query(project, tag, true, false);
	}

	/**
	 * Search Project for tag symbol.
	 */
	public ITagEntry[] query (IProject project, String tag, boolean ignoreCase, boolean exactMatch) {
		return manager.query(project, tag, ignoreCase, exactMatch);
	}
	
	/**
	 * Add a resource to be index.  Containers(Folder, projects)
	 * resources are recusively search for C Files as define by
	 * CoreModel.isTranslationUnit().
	 */
	public void addResource(IResource resource) {
		manager.addResource(resource);
	}

	/**
	 * Add all the C files recurively going to all projects
	 * identified as C Projects by CoreModel.
	 */
	public void addAll () {
		manager.addAll();
	}

	/**
	 * Initialize default index Model.
	 */
	public static IndexModel getDefault() {
		if (indexModel == null) {
			indexModel = new IndexModel();
			manager = IndexManager.getDefault();
		}
		return indexModel;
	}

	private IndexModel () {
	}
}
