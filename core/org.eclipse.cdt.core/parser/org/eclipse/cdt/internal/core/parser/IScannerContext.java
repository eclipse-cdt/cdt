package org.eclipse.cdt.internal.core.parser;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.cdt.core.parser.ast.IASTInclusion;
/**
 * @author jcamelon
 *
 */
public interface IScannerContext {
	
	public static int SENTINEL = 0;
	public static int TOP = 1; 
	public static int INCLUSION = 2; 
	public static int MACROEXPANSION = 3; 

    /**
     * This initializer is used for scanner contexts which are macro expansions.
     * 
     * @param macroOffset   Offset of the expanding macro
     * @param macroLength   Length of the macro identifier
     * @return
     */
    public IScannerContext initialize(Reader r, String f, int k, IASTInclusion i, int macroOffset, int macroLength, int line );
    
	public IScannerContext initialize(Reader r, String f, int k, IASTInclusion i);
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
	
	public int getKind(); 
	public void setKind( int kind ); 

	public IASTInclusion getExtension(); 
	public void setExtension( IASTInclusion ext );

	/**
	 * @return
	 */
	public int getLine();	
	
}