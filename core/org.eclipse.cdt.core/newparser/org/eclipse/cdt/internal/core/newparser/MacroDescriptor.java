/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.newparser;

import java.util.Iterator;
import java.util.List;

public class MacroDescriptor implements IMacroDescriptor {

	public MacroDescriptor()
	{
	}
	
	/**
	 * Method initialize.
	 * @param name 		The name or label that the Macro can be identified by.
	 * @param identifiers	An ordered list of parameters in the macro
	 * definition.
	 * @param tokens		An ordered list of tokens that describe the
	 * RHS expansion in the macro definition.
	 * @param sig			The complete signature of the macro, as a string.  
	 */
	public void initialize( String name, List identifiers, List tokens, String sig )
	{
		this.name = name; 
		identifierParameters = identifiers; 
		tokenizedExpansion = tokens;
		signature = sig;
	}

	private String name; 
	private List identifierParameters; 
	private List tokenizedExpansion; 
	private String signature; 
	/**
	 * Returns the identifiers.
	 * @return List
	 */
	public final List getParameters() {
		return identifierParameters;
	}

	/**
	 * Returns the tokens.
	 * @return List
	 */
	public final List getTokenizedExpansion() {
		return tokenizedExpansion;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public final String getName()
	{
		return name;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer( 128 ); 
		int count = getParameters().size(); 
		
		buffer.append( "MacroDescriptor with name=" + getName() + "\n" );
		buffer.append( "Number of parameters = " + count + "\n" ); 
		Iterator iter = getParameters().iterator(); 
		int current = 0; 
		while( iter.hasNext() )
		{
			buffer.append( "Parameter #" + current++ + " with name=" + (String) iter.next() + "\n" ); 
		}
		
		count = getTokenizedExpansion().size();
		iter = getTokenizedExpansion().iterator(); 
		
		buffer.append( "Number of tokens = " + count + "\n" ); 
		current = 0; 
		while( iter.hasNext() )
		{
			buffer.append( "Token #" + current++ + " is " + ((Token)iter.next()).toString() + "\n" ); 
		}
		
		return buffer.toString(); 
	}

	/**
	 * Returns the signature.
	 * @return String
	 */
	public final String getSignature()
	{
		return signature;
	}

}
