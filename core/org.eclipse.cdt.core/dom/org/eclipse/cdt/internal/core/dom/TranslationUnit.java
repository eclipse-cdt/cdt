package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 */
public class TranslationUnit implements IScope {

	private List declarations = new LinkedList();
	private List macros = new ArrayList(); 
	private List inclusions = new ArrayList(); 
	private List offsetables = new ArrayList(); 
	
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
		offsetables.add(declaration);
	}

	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}
	
	/**
	 * @return
	 */
	public List getInclusions() {
		return Collections.unmodifiableList( inclusions );
	}

	/**
	 * @return
	 */
	public List getMacros() {
		return Collections.unmodifiableList( macros );
	}

	public void addMacro(Macro macro) {
		macros.add(macro);
		offsetables.add(macro);
	}

	public void addInclusion(Inclusion inclusion) {
		inclusions.add(inclusion);
		offsetables.add(inclusion);
	}


	/**
	 * Returns the offsetables.
	 * @return List
	 */
	public List getOffsetables() {
		return Collections.unmodifiableList( offsetables );
	}

}
