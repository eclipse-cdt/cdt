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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.IToken;

/**
 * @author jcamelon
 */
public class ObjectMacroDescriptor implements IMacroDescriptor {

	private static final ArrayList EMPTY_LIST = new ArrayList(0);
	private final String expansionSignature;
	private final String name;
	private final IToken token;
	private Boolean isCircular = null;
	private List tokenizedExpansion = null;

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
	public List getParameters() {
		return EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getTokenizedExpansion()
	 */
	public List getTokenizedExpansion() {
		if( token == null ) return EMPTY_LIST;
		if( tokenizedExpansion == null )
		{
			tokenizedExpansion = new ArrayList(1);
			tokenizedExpansion.add(token);
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
