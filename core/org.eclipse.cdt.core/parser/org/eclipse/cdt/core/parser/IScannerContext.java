package org.eclipse.cdt.core.parser;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.cdt.core.parser.ast.IASTInclusion;
/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable 
"typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface IScannerContext {
	
	public static int SENTINEL = 0;
	public static int TOP = 1; 
	public static int INCLUSION = 2; 
	public static int MACROEXPANSION = 3; 

	public IScannerContext initialize(Reader r, String f, int k, IASTInclusion i);
	public int read() throws IOException;
	public String getFilename();
	public int getOffset();
	public Reader getReader();
	
	public int undoStackSize();  
	public int popUndo();
	public void pushUndo(int undo);
	
	public int getKind(); 
	public void setKind( int kind ); 

	public IASTInclusion getExtension(); 
	public void setExtension( IASTInclusion ext );	
	
}