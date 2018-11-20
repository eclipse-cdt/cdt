/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index.export;

import java.util.Map;

import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.index.URIRelativeLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

/**
 * An IExportProjectProvider provides a configured ICProject suitable set up for
 * indexing. It is used via the org.eclipse.cdt.core.GeneratePDOM application.
 * <br><br>
 * In general, ISV's may have very specific configuration requirements, and it is
 * expected that they subclass {@link AbstractExportProjectProvider} or {@link ExternalExportProjectProvider}
 * in order to do so.
 * <br><br>
 * If your requirements are very simple, then {@link ExternalExportProjectProvider} may
 * be sufficient for direct usage.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IExportProjectProvider {
	/**
	 * This method will be called by the export framework before any other method
	 * in this class. It passes the application argument received by the export
	 * application
	 * @param arguments the application arguments
	 * @see Platform#getApplicationArgs()
	 */
	public void setApplicationArguments(String[] arguments);

	/**
	 * Creates, configures and returns a project for the indexer to index. This routine should
	 * not itself index the project, as a reindex will be performed by the framework.
	 * May not return null.
	 */
	public ICProject createProject() throws CoreException;

	/**
	 * The location converter to use on export. This converter will be called to convert
	 * IIndexFileLocation's to an external form. The external form is implementation dependent.
	 * @param cproject
	 * @see URIRelativeLocationConverter
	 * @see ResourceContainerRelativeLocationConverter
	 */
	public IIndexLocationConverter getLocationConverter(ICProject cproject);

	/**
	 * Get a String to String map of properties to store with the index
	 * content. The export framework may ignore this if the index format does
	 * not support this. The PDOM format does support properties.
	 * @return a Map of String typed key value pairs representing ISV specific properties. This
	 * may return null.
	 */
	public Map<String, String> getExportProperties();
}
