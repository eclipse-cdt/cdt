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
package org.eclipse.cdt.core.parser;

import java.util.HashMap;
import java.util.Map;

public class ScannerException extends Exception {

	private final String info;
    private final String fileName;
    private final int offset;
    private final static int OFFSET_NOT_PROVIDED = -1;
    private final ErrorCode code;

    public static class ErrorCode extends Enum  
	{        
        public static final ErrorCode POUND_ERROR = new ErrorCode( 0 );
        public static final ErrorCode INCLUSION_NOT_FOUND = new ErrorCode( 1 );
		public static final ErrorCode DEFINITION_NOT_FOUND = new ErrorCode( 2 );
		public static final ErrorCode UNBALANCED_CONDITIONALS = new ErrorCode( 3 ); 
		public static final ErrorCode MALFORMED_MACRO_DEFN = new ErrorCode( 4 );
		public static final ErrorCode UNBOUNDED_STRING = new ErrorCode( 5 );
		public static final ErrorCode BAD_FLOATING_POINT = new ErrorCode( 6 );
		public static final ErrorCode BAD_HEXIDECIMAL_FORMAT = new ErrorCode( 7 );
		public static final ErrorCode INVALID_PREPROCESSOR_DIRECTIVE = new ErrorCode( 8 );
		public static final ErrorCode ATTEMPTED_REDEFINITION = new ErrorCode( 9 );
		public static final ErrorCode INVALID_ESCAPE_CHARACTER_SEQUENCE = new ErrorCode( 11 );
		public static final ErrorCode EXPRESSION_EVALUATION_ERROR = new ErrorCode( 12 );
		public static final ErrorCode UNEXPECTED_EOF = new ErrorCode(13);
		public static final ErrorCode MACRO_USAGE_ERROR = new ErrorCode( 14 );
		public static final ErrorCode MACRO_PASTING_ERROR = new ErrorCode( 15 );
		public static final ErrorCode CIRCULAR_INCLUSION = new ErrorCode( 16 );
		public static final ErrorCode BAD_CHARACTER = new ErrorCode( 17 );
        /**
         * @param enumValue
         */
        protected ErrorCode(int enumValue)
        {
            super(enumValue);
        }
        
        public boolean hasInfo()
        {
        	if( this == ErrorCode.UNBALANCED_CONDITIONALS ||  
				this == ErrorCode.UNBOUNDED_STRING  ||
				this == ErrorCode.BAD_FLOATING_POINT ||
				this == ErrorCode.BAD_HEXIDECIMAL_FORMAT ||
				this == ErrorCode.INVALID_PREPROCESSOR_DIRECTIVE ||
				this == ErrorCode.UNEXPECTED_EOF || 
				this == ErrorCode.MACRO_PASTING_ERROR )
					return false; 
			return true;
        }
        
        public boolean hasOffsetInfo()
        {
        	if( this == INCLUSION_NOT_FOUND || this == POUND_ERROR  )
        		return false;
        	return true;
        }

        /**
         * @param mode
         * @return
         */
        public boolean isSeriousError(ParserMode mode)
        {
        	if(this == ErrorCode.INVALID_PREPROCESSOR_DIRECTIVE)
        		return true;
        	if( mode == ParserMode.COMPLETE_PARSE )
				if( this == ErrorCode.POUND_ERROR ||
					this == ErrorCode.UNBALANCED_CONDITIONALS ||		  
					this == ErrorCode.MALFORMED_MACRO_DEFN ||
					this == ErrorCode.UNEXPECTED_EOF  ||
					this == ErrorCode.MACRO_USAGE_ERROR  ||
					this == ErrorCode.MACRO_PASTING_ERROR ||
					this == ErrorCode.EXPRESSION_EVALUATION_ERROR )
						return true;
			return false;
        }
	}

	public ScannerException( ErrorCode code )
	{
		this( code, "", "UNKNOWN", OFFSET_NOT_PROVIDED );
	}

	public ScannerException( ErrorCode code, String info )
	{
		this( code, info, "UNKNOWN", OFFSET_NOT_PROVIDED );
	}

	public ScannerException( ErrorCode code, String fileName, int offset )
	{
		this( code, "", fileName, offset );
	}
	
	static Map errorMessages = new HashMap();
	
	static {
		errorMessages.put( ErrorCode.POUND_ERROR, "#error " ); 
		errorMessages.put( ErrorCode.INCLUSION_NOT_FOUND, "Inclusion not found: " ); 
		errorMessages.put( ErrorCode.DEFINITION_NOT_FOUND, "Definition not found: " ); 
		errorMessages.put( ErrorCode.MALFORMED_MACRO_DEFN, "Macro definition malformed: " ); 
		errorMessages.put( ErrorCode.ATTEMPTED_REDEFINITION, "" );
		errorMessages.put( ErrorCode.INVALID_ESCAPE_CHARACTER_SEQUENCE, "" );
		errorMessages.put( ErrorCode.EXPRESSION_EVALUATION_ERROR, "" );
		errorMessages.put( ErrorCode.MACRO_USAGE_ERROR, "" );
		errorMessages.put( ErrorCode.CIRCULAR_INCLUSION, "" );
		
		errorMessages.put( ErrorCode.UNBALANCED_CONDITIONALS , "Conditionals unbalanced " ); 
		errorMessages.put( ErrorCode.UNBOUNDED_STRING, "Unbounded string " ); 
		errorMessages.put( ErrorCode.BAD_FLOATING_POINT, "Invalid floating point format " );
		errorMessages.put( ErrorCode.BAD_HEXIDECIMAL_FORMAT, "Invalid hexidecimal format " );
		errorMessages.put( ErrorCode.INVALID_PREPROCESSOR_DIRECTIVE, "Invalid preprocessor directive format " );
		errorMessages.put( ErrorCode.UNEXPECTED_EOF, "Unexpected End Of File " );		
		errorMessages.put( ErrorCode.MACRO_PASTING_ERROR, "Invalid use of macro pasting " );
		errorMessages.put( ErrorCode.BAD_CHARACTER, "Bad character sequence encountered:");		
	}
	
	
	public ScannerException( ErrorCode code, String info, String fileName, int offset )
	{
		this.code = code;
		this.info = info; 
		this.fileName = fileName; 
		this.offset = offset;
	}	

    /**
     * @return
     */
    public ErrorCode getErrorCode()
    {
        return code;
    }
    
    public String getMessage()
    {
 		StringBuffer buff = new StringBuffer(); 
 		String errorMessage = (String)errorMessages.get( getErrorCode() );
 		
 		if( errorMessage == null ) return "";
 		buff.append( errorMessage );
 		if( getErrorCode().hasInfo() )
 			buff.append( info ); 
 		if( getErrorCode().hasOffsetInfo() )
 		{
 			buff.append( "from file: ");
 			buff.append( fileName );
 			buff.append( " offset @ ");
 			buff.append( offset );
 		}
 		return buff.toString();	
    }
    
    public boolean isSeriousError( ParserMode mode )
    {
    	return getErrorCode().isSeriousError( mode );
    }
    /**
     * @return
     */
    public String getInfoString()
    {
        return info;
    }

}
