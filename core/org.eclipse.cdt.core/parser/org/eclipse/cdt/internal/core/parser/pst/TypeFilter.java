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

import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;
import org.eclipse.cdt.core.parser.util.ObjectSet;

/**
 * @author aniefer
 */
public class TypeFilter {
	
	public TypeFilter(){
	    //empty
	}
	
	public TypeFilter( ITypeInfo.eType type ){
		acceptedTypes.put( type );
	}
	
	public TypeFilter( LookupKind kind ){
		acceptedKinds.put( kind );
		populatedAcceptedTypes( kind );
	}
	
    public void addAcceptedType( ITypeInfo.eType type ){
    	acceptedTypes.put( type );
    }
    
    public void addAcceptedType( LookupKind kind ) {
    	populatedAcceptedTypes( kind );
        acceptedKinds.put( kind );
    }
    
    public boolean willAccept( ITypeInfo.eType type ){
    	return( acceptedTypes.containsKey( ITypeInfo.t_any ) ||
    			acceptedTypes.containsKey( type ) );
    }
    
	public boolean shouldAccept( ISymbol symbol ){
		return shouldAccept( symbol, symbol.getTypeInfo() );
	}
	public boolean shouldAccept( ISymbol symbol, ITypeInfo typeInfo ){
		if( acceptedTypes.containsKey( ITypeInfo.t_any ) ){
			return true;         
        }
        
		if( acceptedKinds.isEmpty() ){
			return acceptedTypes.containsKey( typeInfo.getType() ); 
		} 
		
		IContainerSymbol container = symbol.getContainingSymbol();
		
		boolean symbolIsMember = container.isType( ITypeInfo.t_class, ITypeInfo.t_union );
		boolean symbolIsLocal = container.isType( ITypeInfo.t_constructor, ITypeInfo.t_function ) ||
								container.isType( ITypeInfo.t_block );
		
		if( typeInfo.isType( ITypeInfo.t_function ) )
		{
			if( ( acceptedKinds.containsKey( LookupKind.FUNCTIONS ) && !symbolIsMember ) ||
				( acceptedKinds.containsKey( LookupKind.METHODS )   &&  symbolIsMember ) ||
				( acceptedKinds.containsKey( LookupKind.MEMBERS )   &&  symbolIsMember ) )
			{
				return true;
			} 
			return false;
		} 
		else if ( typeInfo.isType( ITypeInfo.t_type ) && typeInfo.checkBit( ITypeInfo.isTypedef ) ){
			if( acceptedKinds.containsKey( LookupKind.TYPEDEFS ) ||
				acceptedKinds.containsKey( LookupKind.TYPES ) )
			{
				return true;
			} 
			return false;
		}
		else if ( typeInfo.isType( ITypeInfo.t_type ) || typeInfo.isType( ITypeInfo.t__Bool, ITypeInfo.t_void ) )
		{
			if( ( acceptedKinds.containsKey( LookupKind.VARIABLES ) 	   && !symbolIsMember && !symbolIsLocal ) ||
				( acceptedKinds.containsKey( LookupKind.LOCAL_VARIABLES ) && !symbolIsMember && symbolIsLocal )  ||
				( acceptedKinds.containsKey( LookupKind.FIELDS )          && symbolIsMember ) ||
				( acceptedKinds.containsKey( LookupKind.MEMBERS )         && symbolIsMember ) )
			{
				return true;
			} 
			return false;
		}
		else 
        {
            return acceptedTypes.containsKey( typeInfo.getType() );
        }
	}
	
	/**
	 * @param lookupKind
	 */
	private void populatedAcceptedTypes(LookupKind kind) {
             if ( kind == LookupKind.ALL )         { acceptedTypes.put( ITypeInfo.t_any );         }
        else if ( kind == LookupKind.STRUCTURES )  { acceptedTypes.put( ITypeInfo.t_class );
                                                     acceptedTypes.put( ITypeInfo.t_struct );
                                                     acceptedTypes.put( ITypeInfo.t_union );       }
        else if ( kind == LookupKind.STRUCTS )     { acceptedTypes.put( ITypeInfo.t_struct );      }
        else if ( kind == LookupKind.UNIONS )      { acceptedTypes.put( ITypeInfo.t_union );       }
        else if ( kind == LookupKind.CLASSES )     { acceptedTypes.put( ITypeInfo.t_class );       }
		else if ( kind == LookupKind.CONSTRUCTORS ){ acceptedTypes.put( ITypeInfo.t_constructor ); } 
		else if ( kind == LookupKind.NAMESPACES )  { acceptedTypes.put( ITypeInfo.t_namespace );   }
		else if ( kind == LookupKind.ENUMERATIONS ){ acceptedTypes.put( ITypeInfo.t_enumeration ); } 
		else if ( kind == LookupKind.ENUMERATORS ) { acceptedTypes.put( ITypeInfo.t_enumerator );  }
//		else if ( kind == LookupKind.TYPEDEFS )    { acceptedTypes.put( TypeInfo.t_type );  }
		else if ( kind == LookupKind.TYPES )       { acceptedTypes.put( ITypeInfo.t_class );
		                                             acceptedTypes.put( ITypeInfo.t_struct );
		                                             acceptedTypes.put( ITypeInfo.t_union );
		                                             acceptedTypes.put( ITypeInfo.t_enumeration ); }
		
	}

	public void setLookingInThis( boolean inThis ){
		lookingInThis = inThis;
	}
	public boolean isLookingInThis(){
		return lookingInThis;
	}

	private ObjectSet acceptedTypes = new ObjectSet(2);
    private ObjectSet acceptedKinds = new ObjectSet(2);
    
    private boolean lookingInThis = false;
}
