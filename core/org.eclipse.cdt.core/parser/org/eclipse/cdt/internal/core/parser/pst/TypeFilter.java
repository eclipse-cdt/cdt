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
	
	public TypeFilter( ITypeInfo.eType type ){
		acceptedTypes.add( type );
	}
	
	public TypeFilter( LookupKind kind ){
		acceptedKinds.add( kind );
		populatedAcceptedTypes( kind );
	}
	
    public void addAcceptedType( ITypeInfo.eType type ){
    	acceptedTypes.add( type );
    }
    
    public void addAcceptedType( LookupKind kind ) {
    	populatedAcceptedTypes( kind );
        acceptedKinds.add( kind );
    }
    
    public boolean willAccept( ITypeInfo.eType type ){
    	return( acceptedTypes.contains( ITypeInfo.t_any ) ||
    			acceptedTypes.contains( type ) );
    }
    
	public boolean shouldAccept( ISymbol symbol ){
		return shouldAccept( symbol, symbol.getTypeInfo() );
	}
	public boolean shouldAccept( ISymbol symbol, ITypeInfo typeInfo ){
		if( acceptedTypes.contains( ITypeInfo.t_any ) ){
			return true;         
        }
        
		if( acceptedKinds.isEmpty() ){
			return acceptedTypes.contains( typeInfo.getType() ); 
		} 
		
		IContainerSymbol container = symbol.getContainingSymbol();
		
		boolean symbolIsMember = container.isType( ITypeInfo.t_class, ITypeInfo.t_union );
		boolean symbolIsLocal = container.isType( ITypeInfo.t_constructor, ITypeInfo.t_function ) ||
								container.isType( ITypeInfo.t_block );
		
		if( typeInfo.isType( ITypeInfo.t_function ) )
		{
			if( ( acceptedKinds.contains( LookupKind.FUNCTIONS ) && !symbolIsMember ) ||
				( acceptedKinds.contains( LookupKind.METHODS )   &&  symbolIsMember ) )
			{
				return true;
			} 
			return false;
		} 
		else if ( typeInfo.isType( ITypeInfo.t_type ) && typeInfo.checkBit( ITypeInfo.isTypedef ) ){
			if( acceptedKinds.contains( LookupKind.TYPEDEFS ) ||
				acceptedKinds.contains( LookupKind.TYPES ) )
			{
				return true;
			} 
			return false;
		}
		else if ( typeInfo.isType( ITypeInfo.t_type ) || typeInfo.isType( ITypeInfo.t__Bool, ITypeInfo.t_void ) )
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
             if ( kind == LookupKind.ALL )         { acceptedTypes.add( ITypeInfo.t_any );         }
        else if ( kind == LookupKind.STRUCTURES )  { acceptedTypes.add( ITypeInfo.t_class );
                                                     acceptedTypes.add( ITypeInfo.t_struct );
                                                     acceptedTypes.add( ITypeInfo.t_union );       }
        else if ( kind == LookupKind.STRUCTS )     { acceptedTypes.add( ITypeInfo.t_struct );      }
        else if ( kind == LookupKind.UNIONS )      { acceptedTypes.add( ITypeInfo.t_union );       }
        else if ( kind == LookupKind.CLASSES )     { acceptedTypes.add( ITypeInfo.t_class );       }
		else if ( kind == LookupKind.CONSTRUCTORS ){ acceptedTypes.add( ITypeInfo.t_constructor ); } 
		else if ( kind == LookupKind.NAMESPACES )  { acceptedTypes.add( ITypeInfo.t_namespace );   }
		else if ( kind == LookupKind.ENUMERATIONS ){ acceptedTypes.add( ITypeInfo.t_enumeration ); } 
		else if ( kind == LookupKind.ENUMERATORS ) { acceptedTypes.add( ITypeInfo.t_enumerator );  }
//		else if ( kind == LookupKind.TYPEDEFS )    { acceptedTypes.add( TypeInfo.t_type );  }
		else if ( kind == LookupKind.TYPES )       { acceptedTypes.add( ITypeInfo.t_class );
		                                             acceptedTypes.add( ITypeInfo.t_struct );
		                                             acceptedTypes.add( ITypeInfo.t_union );
		                                             acceptedTypes.add( ITypeInfo.t_enumeration ); }
		
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
