/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 5, 2004
 */
package org.eclipse.cdt.internal.core.parser.pst;

import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.PtrOp;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo.eType;


public class TypeInfoProvider
{
    private static final int BASIC    = 0;
    private static final int TYPE     = 1;
    private static final int TEMPLATE = 2;
    private static final int POOL_SIZE = 16;
    
	private final ITypeInfo [][] pool;
	private final boolean   [][] free;
	private final int       []   firstFreeHint;
	
	protected TypeInfoProvider()
	{
	    
	    pool          = new ITypeInfo[POOL_SIZE][3];
	    free          = new boolean  [POOL_SIZE][3];
	    firstFreeHint = new int [] { 0, 0, 0 };
	    
		for( int i = 0; i < POOL_SIZE; i++ )
		{
			pool[i] = new ITypeInfo[] { newInfo( ITypeInfo.t_void, true ), 
			        					newInfo( ITypeInfo.t_type, true ),
			        					newInfo( ITypeInfo.t_templateParameter, true ) };
			free[i] = new boolean []  { true, true, true };
		}
	}	

	public ITypeInfo getTypeInfo( eType t )
	{
	    int idx = BASIC;
	    if( t == ITypeInfo.t_type || t == ITypeInfo.t_enumerator )					
	        idx = TYPE;
	    else if( t == ITypeInfo.t_templateParameter ) 
	        idx = TEMPLATE;
	    
	    ITypeInfo returnType = null;
		for( int i = firstFreeHint[idx]; i < POOL_SIZE; ++i )
		{
			if( free[i][idx] )
			{
				free[i][idx] = false;
				firstFreeHint[idx] = i + 1;
				returnType = pool[i][idx];
				break;
			}
		}
		if( returnType == null ){
			//if there is nothing free, just give them a new one
			if( t == ITypeInfo.t_type ){
			    returnType = new TypeInfo();
		    } else if( t == ITypeInfo.t_templateParameter ) {
		        returnType = new TemplateParameterTypeInfo();
		    } else {
		        returnType = new BasicTypeInfo();
		    }
		}
		
		returnType.setType( t );
		return returnType;
	}
	
	public void returnTypeInfo( ITypeInfo t )
	{
	    int idx = BASIC;
	    if( t instanceof TemplateParameterTypeInfo )
	        idx = TEMPLATE; 
	    else if ( t instanceof TypeInfo )
	        idx = TYPE;

	    for( int i = 0; i < POOL_SIZE; i++ ){
			if( pool[i][idx] == t ){
				t.clear();
				free[i][idx] = true;
				if( i < firstFreeHint[idx] ){
					firstFreeHint[idx] = i;
				}
				return;
			}
		}
		//else it was one allocated outside the pool
	}
	
	public int numAllocated(){
		int num = 0;
		for( int i = 0; i < POOL_SIZE; i++ ){
		    num += ( free[i][0] ? 0 : 1 ) +
		    	   ( free[i][1] ? 0 : 1 ) +
		    	   ( free[i][2] ? 0 : 1 );
		}
		return num;
	}

    /**
     * @param topInfo
     * @return
     */
    static final public ITypeInfo newTypeInfo( ITypeInfo topInfo ) {
        ITypeInfo newInfo = newInfo( topInfo.getType(), topInfo.getDefault() != null );
                
        newInfo.copy( topInfo );
        return newInfo;
    }

    /**
     * 
     * @param type
     * @param bits
     * @param typeSymbol
     * @param ptrOp
     * @param hasDefault
     * @return
     */
    static final public ITypeInfo newTypeInfo( eType type, int bits, ISymbol typeSymbol, PtrOp ptrOp, boolean hasDefault ) {
        ITypeInfo newInfo = newTypeInfo( type, bits, ptrOp, hasDefault );
        newInfo.setTypeSymbol( typeSymbol );
        return newInfo;
    }
    
    /**
     * 
     * @param type
     * @param bits
     * @param typeSymbol
     * @return
     */
    static final public ITypeInfo newTypeInfo( eType type, int bits, ISymbol typeSymbol ) {
        ITypeInfo newInfo = newTypeInfo( type );
        newInfo.setTypeBits( bits );
        newInfo.setTypeSymbol( typeSymbol );
        return newInfo;
    }

    /**
     * 
     * @param type
     * @param bits
     * @param ptrOp
     * @param hasDefault
     * @return
     */
    static final public ITypeInfo newTypeInfo( eType type, int bits, PtrOp ptrOp, boolean hasDefault ) {
        ITypeInfo newInfo = newTypeInfo( type );
        newInfo.setTypeBits( bits );
        newInfo.addPtrOperator( ptrOp );
        newInfo.setHasDefault( hasDefault );
        return newInfo;
    }

    /**
     * @param typeInfo
     * @return
     */
    static final public ITypeInfo newTypeInfo( eType type ) {
        ITypeInfo newInfo = newInfo( type, false );   
        newInfo.setType( type );
        return newInfo;
    }

    /**
     * 
     * @param type
     * @param bits
     * @param symbol
     * @param op
     * @param def
     * @return
     */
    public final static ITypeInfo newTypeInfo( eType type, int bits, ISymbol symbol, PtrOp op, Object def ) {
        ITypeInfo newInfo = newInfo( type, def != null );
        
        newInfo.setType( type );
        newInfo.setTypeBits( bits );
        newInfo.setDefault( def );
        newInfo.setTypeSymbol( symbol );
        newInfo.addPtrOperator( op );
        return newInfo;
    } 
    /**
     * @return
     */
    public final static ITypeInfo newTypeInfo() {
        return new BasicTypeInfo();
    }

    /**
     * Functions for constructing a Type info a piece at a time.
     */
    private eType type;
    private ISymbol typeSymbol;
    private int bits;
    private Object defaultObj;
    private boolean hasDef;
    private eType templateParamType;

    public void setType( eType t ) 			{ type = t; 		}
    public void setTypeSymbol( ISymbol s ) 	{ typeSymbol = s; 	}
    public void setTypeBits( int b )		{ bits = b;			}
    public void setHasDef( boolean b )      { hasDef = b;       }
    public void setDefaultObj( Object obj ) { defaultObj = obj; }
    public void setTemplateParameterType( eType t ) { templateParamType = t; }

    public void setBit( boolean b, int mask ) {
        if( b )	bits = bits | mask; 
        else 	bits = bits & ~mask; 
	} 
    
    public void beginTypeConstruction(){
        type = ITypeInfo.t_undef;
        typeSymbol = null;
        bits = 0;
        defaultObj = null;
        templateParamType = null;
        hasDef = false;
    }
    
    public ITypeInfo completeConstruction(){
        ITypeInfo newInfo = newTypeInfo( type, bits, typeSymbol, null, defaultObj );
        newInfo.setHasDefault( hasDef );
        if( templateParamType != null )
            newInfo.setTemplateParameterType( templateParamType );
        
        //clear the fields
        beginTypeConstruction();
        return newInfo;
    }   
    
    private final static ITypeInfo newInfo( eType type, boolean def ){
        ITypeInfo newInfo = null;
        if( type == ITypeInfo.t_type || type == ITypeInfo.t_enumerator ){
            if( def )
                newInfo = new TypeInfo(){
                	public void copy( ITypeInfo t )    { super.copy( t ); _defObj = t.getDefault(); }
	                public void setDefault( Object t ) { _defObj = t;   }
	                public Object getDefault() 		   { return _defObj;}
                	private Object _defObj;
            	};
            else 
                newInfo = new TypeInfo();
        } else if( type == ITypeInfo.t_templateParameter ){
            if( def )
                newInfo = new TemplateParameterTypeInfo(){
                	public void copy( ITypeInfo t )    { super.copy( t ); _defObj = t.getDefault(); }
	                public void setDefault( Object t ) { _defObj = t;   }
	                public Object getDefault() 		   { return _defObj;}
	            	private Object _defObj;
        		};
	        else 
	            newInfo = new TemplateParameterTypeInfo();
        } else {
            if( def )
                newInfo = new BasicTypeInfo(){
                	public void copy( ITypeInfo t )    { super.copy( t ); _defObj = t.getDefault(); }
	                public void setDefault( Object t ) { _defObj = t;   }
	                public Object getDefault() 		   { return _defObj;}
	            	private Object _defObj;
        		};
	        else 
	            newInfo = new BasicTypeInfo();  
        }
        return newInfo;
    }
}