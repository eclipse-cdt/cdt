/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;


public class TypeInfo extends BasicTypeInfo implements ITypeInfo{
    public TypeInfo(){
		super();	
	}

    public ISymbol getTypeSymbol() {	
    	return _typeDeclaration; 
    }

    public void setTypeSymbol( ISymbol type ) {
    	_typeDeclaration = type;
    }
    
    public boolean getHasDefault() {
    	return _hasDefaultValue;
    }

    public void setHasDefault( boolean def ) {
    	_hasDefaultValue = def;
    }

    public void clear() {
        super.clear();
    	_typeDeclaration = null;
    	_hasDefaultValue = false;
    }

    public void copy( ITypeInfo t ) {
        super.copy( t );
    	_typeDeclaration = t.getTypeSymbol();
    	_hasDefaultValue = t.getHasDefault();
    }

    public boolean equals( Object t ) {
    	if( !super.equals( t ) ){
    		return false;
    	}
    
    	ITypeInfo type = (ITypeInfo)t;
    
    	boolean result = true;
    	ISymbol symbol = type.getTypeSymbol();
    	if( _typeDeclaration != null && symbol != null ){
    		if( _typeDeclaration.isType( t__Bool, t_void ) &&
    			symbol.isType( t__Bool, t_void ) )
    		{
    			//if typeDeclaration is a basic type, then only need the types the same
    			result &= ( _typeDeclaration.getType() == symbol.getType() );	
    		} else if( _typeDeclaration.isType( t_function ) &&
    				   symbol.isType( t_function ) )
    		{
    			//function pointers... functions must have same parameter lists and return types
    			IParameterizedSymbol f1 = (IParameterizedSymbol) _typeDeclaration;
    			IParameterizedSymbol f2 = (IParameterizedSymbol) symbol;
    			
    			result &= f1.hasSameParameters( f2 );
    			if( f1.getReturnType() != null && f2.getReturnType() != null )
    				result &= f1.getReturnType().getTypeInfo().equals( f2.getReturnType().getTypeInfo() );
    			else
    				result &= (f1.getReturnType() == f2.getReturnType());
    		} else if( _typeDeclaration.isType( t_templateParameter ) &&
    				   symbol.isType( t_templateParameter ) )
    		{
    			//template parameters
    			result &= TemplateEngine.templateParametersAreEquivalent( _typeDeclaration, symbol );
    		} else if ( _typeDeclaration instanceof IDeferredTemplateInstance &&
    				    symbol instanceof IDeferredTemplateInstance )
    		{
    			result &= TemplateEngine.deferedInstancesAreEquivalent( (IDeferredTemplateInstance) _typeDeclaration, (IDeferredTemplateInstance)symbol );
    		}else {
    			//otherwise, its a user defined type, need the decls the same
    			result &= ( _typeDeclaration == symbol );
    		}	
    	} else {
    		result &= ( _typeDeclaration == symbol );
    	}
    	return result;
    }

	private ISymbol _typeDeclaration = null;
	private boolean _hasDefaultValue = false;
}
