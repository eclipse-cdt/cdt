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
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.Arrays;

import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IToken;

public class FunctionMacroDescriptor implements IMacroDescriptor {
	
	/**
	 * Method initialize.
	 * @param name 		The name or label that the Macro can be identified by.
	 * @param identifiers	An ordered list of parameters in the macro
	 * definition.
	 * @param tokens		An ordered list of tokens that describe the
	 * RHS expansion in the macro definition.
	 * @param sig			The complete signature of the macro, as a string.
	 */
	public FunctionMacroDescriptor( String name, String[] identifiers, IToken[] tokens, String expansionSignature )
	{
		this.name = name; 
		identifierParameters = identifiers; 
		tokenizedExpansion = tokens;
		this.expansionSignature = expansionSignature;
	}

	private String name; 
	private String [] identifierParameters; 
	private IToken [] tokenizedExpansion; 
	private String expansionSignature;
	
	/**
	 * Returns the identifiers.
	 * @return List
	 */
	public final String[] getParameters() {
		return identifierParameters;
	}

	/**
	 * Returns the tokens.
	 * @return List
	 */
	public final IToken[] getTokenizedExpansion() {
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
		
		int count = identifierParameters.length; 
		
		buffer.append( "MacroDescriptor with name=" + getName() + "\n" );  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append( "Number of parameters = " + count + "\n" );   //$NON-NLS-1$//$NON-NLS-2$
		 
		for( int current = 0;  current < count; ++current)
			buffer.append( "Parameter #" + current + " with name=" + identifierParameters[current] + "\n" );   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		
		
		count = tokenizedExpansion.length;
		 
		
		buffer.append( "Number of tokens = " + count + "\n" );   //$NON-NLS-1$//$NON-NLS-2$
		for( int current = 0; current < count; ++current )
		{
			buffer.append( "Token #" + current++ + " is " + tokenizedExpansion[current].toString() + "\n" );   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		}
		
		return buffer.toString(); 
	}

	/**
	 * Returns the signature.
	 * @return String
	 */
	public final String getCompleteSignature()
	{
		StringBuffer fullSignature = new StringBuffer( "#define " ); //$NON-NLS-1$
		fullSignature.append( name );
		fullSignature.append( '(');
		
		for( int current = 0; current < identifierParameters.length; ++current )
		{
			if (current > 0) fullSignature.append(',');
			fullSignature.append(identifierParameters[current] );  
		}
		fullSignature.append( ") "); //$NON-NLS-1$
		fullSignature.append( expansionSignature );
		return fullSignature.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#compatible(org.eclipse.cdt.core.parser.IMacroDescriptor)
	 */
	public boolean compatible(IMacroDescriptor descriptor) {
		if( descriptor.getName() == null ) return false;
		if( descriptor.getTokenizedExpansion() == null ) return false;
		if( descriptor.getParameters() == null ) return false;
		if( descriptor.getMacroType() != getMacroType() ) return false;
		if( ! name.equals( descriptor.getName() )) return false;
		if( descriptor.getParameters().length != identifierParameters.length ) return false; 
		if( descriptor.getTokenizedExpansion().length != tokenizedExpansion.length ) return false;
		
		if( ! equivalentArrayContents( descriptor.getParameters(), getParameters() ) ) return false;
		if( ! equivalentArrayContents( descriptor.getTokenizedExpansion(), getTokenizedExpansion() ) ) return false;
		return true;
	}

	/**
	 * @param list1
	 * @param list2 
	 * @return
	 */
	private boolean equivalentArrayContents(Object[] list1, Object[] list2 ) {
		if( Arrays.equals( list1, list2  )) return true;
		// otherwise
		topLoop: for( int i = 0; i < list1.length; ++i )
		{
			Object key = list1[i];
			for( int j = 0; j < list2 .length; ++j )
			{
				if( key.equals( list2 [j]) )
					continue topLoop;
					
			}
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getMacroType()
	 */
	public MacroType getMacroType() {
		return MacroType.FUNCTION_LIKE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getExpansionSignature()
	 */
	public String getExpansionSignature() {
		return expansionSignature;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#isCircular()
	 */
	public boolean isCircular() {
		for( int i = 0; i < tokenizedExpansion.length; ++i )
		{
			IToken t = tokenizedExpansion[i];
			if( t.getType() == IToken.tIDENTIFIER && t.getImage().equals(getName()))
				return true;
		}
		return false;
	}
}
