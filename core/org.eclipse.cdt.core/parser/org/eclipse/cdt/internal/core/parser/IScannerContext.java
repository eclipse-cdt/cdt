package org.eclipse.cdt.internal.core.parser;
import java.io.IOException;
import java.io.Reader;
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

	
	IScannerContext initialize(Reader r, String f, int k);
	int read() throws IOException;
	String getFilename();
	int getOffset();
	Reader getReader();
	
	int undoStackSize();  
	int popUndo();
	void pushUndo(int undo);
	
	
	int getKind(); 
	void setKind( int kind ); 
}