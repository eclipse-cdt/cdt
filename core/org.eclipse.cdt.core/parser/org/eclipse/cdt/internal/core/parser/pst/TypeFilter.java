/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.pst;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;

/**
 * @author aniefer
 */
public class TypeFilter {
	
	public TypeFilter(){
	}
	
	public TypeFilter( Set types ){
        acceptedTypes.addAll( types );
	}
	
	public TypeFilter( TypeInfo.eType type ){
		acceptedTypes.add( type );
	}
	
	public TypeFilter( LookupKind kind ){
		acceptedKinds.add( kind );
		populatedAcceptedTypes( kind );
	}
	
    public void addAcceptedType( TypeInfo.eType type ){
    	acceptedTypes.add( type );
    }
    
    public void addAcceptedType( LookupKind kind ) {
    	populatedAcceptedTypes( kind );
        acceptedKinds.add( kind );
    }
    
    public boolean willAccept( TypeInfo.eType type ){
    	return( acceptedTypes.contains( TypeInfo.t_any ) ||
    			acceptedTypes.contains( type ) );
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
			} 
			return false;
		} 
		else if ( typeInfo.isType( TypeInfo.t_type ) && typeInfo.checkBit( TypeInfo.isTypedef ) ){
			if( acceptedKinds.contains( LookupKind.TYPEDEFS ) ||
				acceptedKinds.contains( LookupKind.TYPES ) )
			{
				return true;
			} 
			return false;
		}
		else if ( typeInfo.isType( TypeInfo.t_type ) || typeInfo.isType( TypeInfo.t__Bool, TypeInfo.t_void ) )
		{
			if( ( acceptedKinds.contains( LookupKind.VARIABLES ) 	   && !symbolIsMember && !symbolIsLocal ) ||
				( acceptedKinds.contains( LookupKind.LOCAL_VARIABLES ) && !symbolIsMember && symbolIsLocal )  ||
				( acceptedKinds.contains( LookupKind.FIELDS )          && symbolIsMember ) )
			{
				return true;
			} 
			return false;
		}
		else 
        {
            return acceptedTypes.contains( typeInfo.getType() );
        }
	}
	
	/**
	 * @param lookupKind
	 */
	private void populatedAcceptedTypes(LookupKind kind) {
             if ( kind == LookupKind.ALL )         { acceptedTypes.add( TypeInfo.t_any );         }
        else if ( kind == LookupKind.STRUCTURES )  { acceptedTypes.add( TypeInfo.t_class );
                                                     acceptedTypes.add( TypeInfo.t_struct );
                                                     acceptedTypes.add( TypeInfo.t_union );       }
        else if ( kind == LookupKind.STRUCTS )     { acceptedTypes.add( TypeInfo.t_struct );      }
        else if ( kind == LookupKind.UNIONS )      { acceptedTypes.add( TypeInfo.t_union );       }
        else if ( kind == LookupKind.CLASSES )     { acceptedTypes.add( TypeInfo.t_class );       }
		else if ( kind == LookupKind.CONSTRUCTORS ){ acceptedTypes.add( TypeInfo.t_constructor ); } 
		else if ( kind == LookupKind.NAMESPACES )  { acceptedTypes.add( TypeInfo.t_namespace );   }
		else if ( kind == LookupKind.ENUMERATIONS ){ acceptedTypes.add( TypeInfo.t_enumeration ); } 
		else if ( kind == LookupKind.ENUMERATORS ) { acceptedTypes.add( TypeInfo.t_enumerator );  }
//		else if ( kind == LookupKind.TYPEDEFS )    { acceptedTypes.add( TypeInfo.t_type );  }
		else if ( kind == LookupKind.TYPES )       { acceptedTypes.add( TypeInfo.t_class );
		                                             acceptedTypes.add( TypeInfo.t_struct );
		                                             acceptedTypes.add( TypeInfo.t_union );
		                                             acceptedTypes.add( TypeInfo.t_enumeration ); }
		
	}

	public void setLookingInThis( boolean inThis ){
		lookingInThis = inThis;
	}
	public boolean isLookingInThis(){
		return lookingInThis;
	}

	private Set acceptedTypes = new HashSet();
    private Set acceptedKinds = new HashSet();
    
    private boolean lookingInThis = false;
}
