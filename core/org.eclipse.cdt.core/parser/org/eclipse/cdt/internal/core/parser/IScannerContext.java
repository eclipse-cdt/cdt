package org.eclipse.cdt.internal.core.parser;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.cdt.core.parser.Enum;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
/**
 * @author jcamelon
 *
 */
public interface IScannerContext {
	
	
	public static class ContextKind extends Enum  
	{
		public static ContextKind SENTINEL = new ContextKind( 0 );
		public static ContextKind TOP = new ContextKind( 1 ); 
		public static ContextKind INCLUSION = new ContextKind( 2 ); 
		public static ContextKind MACROEXPANSION = new ContextKind( 3 ); 
		
		/**
		 * @param enumValue
		 */
		protected ContextKind(int enumValue) {
			super(enumValue);
			// 
		}
	}

    /**
     * This initializer is used for scanner contexts which are macro expansions.
     * 
     * @param macroOffset   Offset of the expanding macro
     * @param macroLength   Length of the macro identifier
     * @return
     */
	public int read() throws IOException;
	public String getFilename();
    
    /**
     * Returns macro offset (the offset of the top expanded macro).
     * @return int
     */
    public int getMacroOffset();
    
    /**
     * Returns macro length (the length of the top expanded macro identifier).
     * @return int
     */
    public int getMacroLength();
    
    /**
     * Returns the offset.
     * @return int
     */
	public int getOffset();
    
    /**
     * Returns relative offset (relative to the beginning of the ScannerContext).
     * @return int
     */
    public int getRelativeOffset();
    
	public Reader getReader();
	
	public int undoStackSize();  
	public int popUndo();
	public void pushUndo(int undo);
	
	public ContextKind getKind(); 
	public void setKind( ContextKind kind ); 

	public IASTInclusion getExtension(); 
	public void setExtension( IASTInclusion ext );

	/**
	 * @return
	 */
	public int getLine();	
	
}