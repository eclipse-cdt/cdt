/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype;

import org.eclipse.core.resources.IProject;

/**
 * Main entry point for dealign with the resolver model.
 */
public interface IResolverModel {
	/**
	 * @return array containing all known languages.
	 */
	public ICLanguage[] getLanguages();  

	/**
	 * @return array containing all known file types.
	 */
	public ICFileType[] getFileTypes();  

	/**
	 * Get the language that has the specified id.
	 * Returns null if no language has that id.
	 * 
	 * @param languageId language id
	 * 
	 * @return language with the specified id, or null
	 */
	public ICLanguage getLanguageById(String languageId);

	/**
	 * Get the file type that has the specified id.
	 * Returns null if no file type has that id.
	 * 
	 * @param typeId file type id
	 * 
	 * @return file type with the specified id, or null
	 */
	public ICFileType getFileTypeById(String typeId);

	/**
	 * Get the resolver for the current workspace.
	 * 
	 * @return workspace resolver
	 */
	public ICFileTypeResolver getResolver();
	
	/**
	 * Create a custom resolver for the specified project.
	 *
	 * The project resolver is set to a custom
	 * resolver, and the resolver data is persisted
	 * in the project (in the .cdtproject file).
	 *
	 * This method fires changed event
	 *  
 	 * @param project - project this resolver applied to
	 * @param copyResolver - retrieve associations for the copy to populate the custom resolver.
	 */
	public ICFileTypeResolver createCustomResolver(IProject project, ICFileTypeResolver copyResolver);

	/**
	 * Remove the custom resolver on the project.
	 * This method fires changed event 
	 * 
	 * @param project
	 */
	public void removeCustomResolver(IProject project);

	/**
	 * Return true if the project has a custom resolver.
	 * 
	 * @param project
	 * @return true if a custom resolver
	 */
	public boolean hasCustomResolver(IProject project);

	/**
	 * Get the resolver for the specified project.
	 * 
	 * If the project resolver is unavailable, or the
	 * project does not use a custom resolver, then the
	 * workspace resolver is returned.
	 * 
	 * @param project to retrieve resolver for
	 * 
	 * @return project resolver
	 */
	public ICFileTypeResolver getResolver(IProject project);

	/**
	 * Create a new file type assocation.  The association
	 * may be added to a type resolver.
	 * 
	 * @param pattern filename pattern for the association.
	 * @param type association file type. 
	 * 
	 * @return newly created file type association
	 */
	public ICFileTypeAssociation createAssocation(String pattern, ICFileType type);

	/**
	 * Adds the given listener for model change events to the model.
	 * Has no effect if an identical listener is already registered.
	 *  
	 * @param listener listener to add
	 */
	public void addResolverChangeListener(IResolverChangeListener listener);

	/**
	 * Removes the given change listener from the model.
	 * Has no effect if an identical listener is not registered.
	 *  
	 * @param listener listener to remove
	 */
	public void removeResolverChangeListener(IResolverChangeListener listener);
}
