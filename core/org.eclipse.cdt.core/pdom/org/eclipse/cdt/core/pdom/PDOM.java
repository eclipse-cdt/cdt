package org.eclipse.cdt.core.pdom;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

public class PDOM {

	// To reduce storage requirements we have a single table that contains all
	// of the strings in the PDOM. To check for equality, all you need to do
	// is compare string handles.
	private Set stringTable = new HashSet();
	
	private IScope globalScope;
	
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

}
