package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Represents a container of all the IArchive's found in the project
 * while inspecting the project.
 */
public interface IArchiveContainer extends ICElement, IParent, IOpenable {
	public IArchive[] getArchives();
}
