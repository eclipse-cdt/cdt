package org.eclipse.cdt.core.pdom;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class PDOM {

	// The DOM hash map
	private static Map pdomMap = new HashMap();

	private Connection dbConn;
	
	private PDOM(Connection conn) {
		dbConn = conn;
	}
	
	/**
	 * Look up the name from the DOM in the PDOM and return the PDOM
	 * version of the Binding.
	 * 
	 * @param name
	 * @return binding
	 */
	public IBinding resolveBinding(IASTName name) {
		// look up the name in the PDOM
		return null;
	}

	/**
	 * Return all bindings that have this name as a prefix.
	 * This is used for content assist.
	 * 
	 * @param prefix
	 * @return bindings
	 */
	public IBinding[] resolvePrefix(IASTName name) {
		return new IBinding[0];
	}
	
	/**
	 * Add the name to the PDOM. This will also add the binding to the
	 * PDOM if it is not already there.
	 * 
	 * @param name
	 */
	public void addName(IASTName name) {
		
	}

	/**
	 * Get all names stored in the PDOM for a given binding.
	 * 
	 * @param binding
	 * @return declarations
	 */
	public Iterator getDeclarations(IBinding binding) {
		return null;
	}

	/**
	 * Get all names in the PDOM that refer to a given binding.
	 * @param binding
	 * @return references
	 */
	public Iterator getReferences(IBinding binding) {
		return null;
	}
	
	/**
	 * Clear all names from the PDOM that appear in the given file.
	 * This is used when reparsing a file.
	 * 
	 * @param filename
	 */
	public void clearNamesInFile(String filename) {
		
	}

	/**
	 * Return the PDOM for the given project
	 * 
	 * @param project
	 * @return the PDOM for the project
	 * @throws CoreException
	 */
	public static PDOM getPDOM(IProject project) throws CoreException {
		// Get the name for the db
		boolean create = false;
		String dbName = project.getPersistentProperty(IPDOMDatabaseProvider.dbNameKey);
		if (dbName == null) {
			// New database
			create = true;
			// TODO - the name shouldn't be the project name in case the
			// user changes it and creates a new one with the old name
			dbName = project.getName();
			project.setPersistentProperty(IPDOMDatabaseProvider.dbNameKey, dbName);
		} else {
			// See if we got one already
			PDOM pdom = (PDOM)pdomMap.get(dbName);
			if (pdom != null)
				return pdom;
		}
		
		// Find our database provider
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "PDOMDatabaseProvider"); //$NON-NLS-1$
		IExtension[] extensions = extensionPoint.getExtensions();
		
		if (extensions.length == 0)
			// No DB providers
			return null;
		
		// For now pick the first one. In the future we may want to support more
		IConfigurationElement[] elements = extensions[0].getConfigurationElements();
		IPDOMDatabaseProvider dbProvider
			= (IPDOMDatabaseProvider)elements[0].createExecutableExtension("class"); //$NON-NLS-1$
		
		// create the PDOM
		PDOM pdom = new PDOM(dbProvider.getDatabase(dbName, create));
		pdomMap.put(dbName, pdom);
		return pdom;
	}
}
