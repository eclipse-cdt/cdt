/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.core.parser.pst;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;

/**
 * @author aniefer
 */
public class TypeFilter {
	
	public TypeFilter(){
        acceptedTypes.add( TypeInfo.t_any );
	}
	
	public TypeFilter( Set types ){
        acceptedTypes.addAll( types );
	}
	
	public TypeFilter( TypeInfo.eType type ){
		acceptedTypes.add( type );
	}
	
	public TypeFilter( LookupKind kind ){
		acceptedKinds.add( kind );
		populatedFilteredTypes( kind );
	}
	
    public void addFilteredType( TypeInfo.eType type ){
    	acceptedTypes.add( type );
    }
    
    public void addFilteredType( LookupKind kind ) {
    	populatedFilteredTypes( kind );
        acceptedKinds.add( kind );
    }
    
	public boolean shouldAccept( ISymbol symbol ){
		return shouldAccept( symbol, symbol.getTypeInfo() );
	}
	public boolean shouldAccept( ISymbol symbol, TypeInfo typeInfo ){
		if( acceptedTypes.contains( TypeInfo.t_any ) ){
			return true;         
        }
        
		if( acceptedKinds.isEmpty() ){
			return acceptedTypes.contains( typeInfo.getType() ); 
		} 
		
		IContainerSymbol container = symbol.getContainingSymbol();
		
		boolean symbolIsMember = container.isType( TypeInfo.t_class, TypeInfo.t_union );
		boolean symbolIsLocal = container.isType( TypeInfo.t_constructor, TypeInfo.t_function ) ||
								container.isType( TypeInfo.t_block );
		
		if( typeInfo.isType( TypeInfo.t_function ) )
		{
			if( ( acceptedKinds.contains( LookupKind.FUNCTIONS ) && !symbolIsMember ) ||
				( acceptedKinds.contains( LookupKind.METHODS )   &&  symbolIsMember ) )
			{
				return true;
			} else {
				return false;
			}
		} 
		else if ( typeInfo.isType( TypeInfo.t_type ) || typeInfo.isType( TypeInfo.t_bool, TypeInfo.t_enumerator ) )
		{
			if( ( acceptedKinds.contains( LookupKind.VARIABLES ) 	   && !symbolIsMember && !symbolIsLocal ) ||
				( acceptedKinds.contains( LookupKind.LOCAL_VARIABLES ) && !symbolIsMember && symbolIsLocal )  ||
				( acceptedKinds.contains( LookupKind.FIELDS )          && symbolIsMember ) )
			{
				return true;
			} else {
				return false;
			}
		}
		else 
        {
            return acceptedTypes.contains( typeInfo.getType() );
        }
	}
	
	/**
	 * @param lookupKind
	 */
	private void populatedFilteredTypes(LookupKind kind) {
             if ( kind == LookupKind.STRUCTURES )  { acceptedTypes.add( TypeInfo.t_class );
                                                     acceptedTypes.add( TypeInfo.t_struct );
                                                     acceptedTypes.add( TypeInfo.t_union );       }
        else if ( kind == LookupKind.STRUCS )      { acceptedTypes.add( TypeInfo.t_struct );      }
        else if ( kind == LookupKind.UNIONS )      { acceptedTypes.add( TypeInfo.t_union );       }
        else if ( kind == LookupKind.CLASSES )     { acceptedTypes.add( TypeInfo.t_class );       }
		else if ( kind == LookupKind.CONSTRUCTORS ){ acceptedTypes.add( TypeInfo.t_constructor ); } 
		else if ( kind == LookupKind.NAMESPACES )  { acceptedTypes.add( TypeInfo.t_namespace );   }
		else if ( kind == LookupKind.ENUMERATIONS ){ acceptedTypes.add( TypeInfo.t_enumeration ); } 
		else if ( kind == LookupKind.ENUMERATORS ) { acceptedTypes.add( TypeInfo.t_enumerator );  }
	}


	private Set acceptedTypes = new HashSet();
    private Set acceptedKinds = new HashSet();
}