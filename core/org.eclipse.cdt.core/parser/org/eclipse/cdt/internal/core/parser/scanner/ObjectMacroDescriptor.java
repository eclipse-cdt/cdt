/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.IMacroDescriptor;

/**
 * @author jcamelon
 */
public class ObjectMacroDescriptor implements IMacroDescriptor {

	private static final List EMPTY_LIST = new ArrayList();
	private final String fullSignature, expansionSignature;
	private final String name;
	private final List tokenizedExpansion;

	public ObjectMacroDescriptor( String name, String expansionSignature )
	{
		this.name = name;
		this.expansionSignature = expansionSignature;
		fullSignature = "#define " + name + " " + expansionSignature;
		tokenizedExpansion = EMPTY_LIST;
	}
	
	public ObjectMacroDescriptor( String name, String signature, List tokenizedExpansion, String expansionSignature )
	{
		this.name = name;
		this.tokenizedExpansion = tokenizedExpansion;
		this.fullSignature = signature;
		this.expansionSignature = expansionSignature;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getMacroType()
	 */
	public MacroType getMacroType() {
		return MacroType.OBJECT_LIKE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getParameters()
	 */
	public List getParameters() {
		return EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getTokenizedExpansion()
	 */
	public List getTokenizedExpansion() {
		return tokenizedExpansion;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getSignature()
	 */
	public String getCompleteSignature() {
		return fullSignature;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#compatible(org.eclipse.cdt.core.parser.IMacroDescriptor)
	 */
	public boolean compatible(IMacroDescriptor descriptor) {
		if( descriptor.getName() == null ) return false;
		if( descriptor.getMacroType() != getMacroType() ) return false;
		if( descriptor.getTokenizedExpansion() == null ) return false;
		if( ! name.equals( descriptor.getName() )) return false; 
		if( descriptor.getTokenizedExpansion().size() != tokenizedExpansion.size() ) return false;
		
		if( ! (descriptor.getTokenizedExpansion().containsAll( tokenizedExpansion ))) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getExpansionSignature()
	 */
	public String getExpansionSignature() {
		return expansionSignature;
	}

}
