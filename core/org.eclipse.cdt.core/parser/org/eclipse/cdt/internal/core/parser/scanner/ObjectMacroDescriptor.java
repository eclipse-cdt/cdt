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

import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IToken;

/**
 * @author jcamelon
 */
public class ObjectMacroDescriptor implements IMacroDescriptor {

	private final String expansionSignature;
	private final String name;
	private final IToken token;
	private IToken[] tokenizedExpansion = null;
	private static final String[] EMPTY_STRING_ARRAY  = new String[0];
	private static final IToken[] EMPTY_TOKEN_ARRAY = new IToken[0];

	public ObjectMacroDescriptor( String name, String expansionSignature )
	{
		this.name = name;
		this.expansionSignature = expansionSignature;
		token = null;
	}
	
	public ObjectMacroDescriptor( String name, IToken t, String expansionSignature )
	{
		this.name = name;
		this.token = t;
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
	public String[] getParameters() {
		return EMPTY_STRING_ARRAY ;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getTokenizedExpansion()
	 */
	public IToken[] getTokenizedExpansion() {
		if( token == null ) return EMPTY_TOKEN_ARRAY;
		if( tokenizedExpansion == null )
		{
			tokenizedExpansion = new IToken[1];
			tokenizedExpansion[0]= token;
		}
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
		StringBuffer signatureBuffer  = new StringBuffer();
		signatureBuffer.append( "#define " ); //$NON-NLS-1$
		signatureBuffer.append( name );
		signatureBuffer.append( ' ' );
		signatureBuffer.append( expansionSignature );
		return signatureBuffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#compatible(org.eclipse.cdt.core.parser.IMacroDescriptor)
	 */
	public boolean compatible(IMacroDescriptor descriptor) {
		if( descriptor.getName() == null ) return false;
		if( descriptor.getMacroType() != getMacroType() ) return false;
		
		// Both macros are ObjectMacroDescriptors!

		if( ! name.equals( descriptor.getName() )) return false; 
		String result = descriptor.getExpansionSignature();	
		if( result == null ) return expansionSignature == null;
	
		return result.equals(expansionSignature);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getExpansionSignature()
	 */
	public String getExpansionSignature() {
		return expansionSignature;
	}

	/**
	 * @return
	 */
	public boolean isCircular() {
		for( int i = 0; i < getTokenizedExpansion().length; ++i)
		{
			IToken t = tokenizedExpansion[i];
			if( t.getType() == IToken.tIDENTIFIER && t.getImage().equals(getName()))
				return true;
		}
		return false;
	}
}
