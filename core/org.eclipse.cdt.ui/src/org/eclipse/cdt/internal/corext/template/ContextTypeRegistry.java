package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.corext.template.c.CppContextType;


/**
 * A singleton to keep track of all known context types.
 */
public class ContextTypeRegistry {

	/** the singleton */
	private static ContextTypeRegistry fInstance;
	
	/** all known context types */
	private final Map fContextTypes= new HashMap();
	
	/**
	 * Returns the single instance of this class.
	 */
	public static ContextTypeRegistry getInstance() {
		if (fInstance == null)
			fInstance= new ContextTypeRegistry();
			
		return fInstance;	
	}

	/**
	 * Adds a context type to the registry.
	 */	
	public void add(ContextType contextType) {
		fContextTypes.put(contextType.getName(), contextType);
	}
	
	/**
	 * Removes a context type from the registry.
	 */
	public void remove(ContextType contextType) {
		fContextTypes.remove(contextType.getName());
	}

	/**
	 * Returns the context type if the name is valid, <code>null</code> otherwise.
	 */
	public ContextType getContextType(String name) {
		return (ContextType) fContextTypes.get(name);
	}
	
	/**
	 * Returns an iterator over the registered context type names.
	 */
	public Iterator iterator() {
		return fContextTypes.keySet().iterator();	
	}

	// XXX bootstrap with C and C++ types
	private ContextTypeRegistry() {
		add(new CContextType());
		add(new CppContextType());
	}

}
