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

import java.util.Iterator;
import java.util.List;

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
	public FunctionMacroDescriptor( String name, List identifiers, List tokens, String expansionSignature )
	{
		this.name = name; 
		identifierParameters = identifiers; 
		tokenizedExpansion = tokens;
		this.expansionSignature = expansionSignature;
	}

	private String name; 
	private List identifierParameters; 
	private List tokenizedExpansion; 
	private String expansionSignature;
	private Boolean isCircular = null;
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
		
		buffer.append( "MacroDescriptor with name=" + getName() + "\n" );  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append( "Number of parameters = " + count + "\n" );   //$NON-NLS-1$//$NON-NLS-2$
		Iterator iter = getParameters().iterator(); 
		int current = 0; 
		while( iter.hasNext() )
		{
			buffer.append( "Parameter #" + current++ + " with name=" + (String) iter.next() + "\n" );   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		}
		
		count = getTokenizedExpansion().size();
		iter = getTokenizedExpansion().iterator(); 
		
		buffer.append( "Number of tokens = " + count + "\n" );   //$NON-NLS-1$//$NON-NLS-2$
		current = 0; 
		while( iter.hasNext() )
		{
			buffer.append( "Token #" + current++ + " is " + ((IToken)iter.next()).toString() + "\n" );   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
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
		Iterator iter = getParameters().iterator(); 
		int current = 0; 
		while( iter.hasNext() )
		{
			if (current > 0) fullSignature.append(',');
			fullSignature.append((String)iter.next() );  
			current++;
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
		if( descriptor.getParameters().size() != identifierParameters.size() ) return false; 
		if( descriptor.getTokenizedExpansion().size() != tokenizedExpansion.size() ) return false;
		
		if( ! (descriptor.getParameters().containsAll( identifierParameters ) )) return false;
		if( ! (descriptor.getTokenizedExpansion().containsAll( tokenizedExpansion ))) return false;
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
		if( isCircular == null )
			isCircular = new Boolean( checkIsCircular() );
		return isCircular.booleanValue();
	}

	/**
	 * @return
	 */
	protected boolean checkIsCircular() {
		Iterator i = getTokenizedExpansion().iterator();
		while( i.hasNext() )
		{
			IToken t = (IToken) i.next();
			if( t.getType() == IToken.tIDENTIFIER && t.getImage().equals(getName()))
				return true;
		}
		return false;
	}

}
