package org.eclipse.cdt.internal.core.parser;
import java.io.Reader;
import java.io.IOException;
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
	IScannerContext initialize(Reader r, String f, int u);
	int read() throws IOException;
	String getFilename();
	int getOffset();
	Reader getReader();
	int getUndo();
	void setUndo(int undo);
}