package org.eclipse.cdt.internal.core.parser;

import java.io.Reader;
import java.util.List;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable 
"typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface IScanner {
	
	public IScanner initialize( Reader sourceToBeRead, String fileName );
	
	public void addDefinition(String key, IMacroDescriptor macroToBeAdded );
	public void addDefinition(String key, String value); 
	public Object getDefinition(String key);
	
	public Object[] getIncludePaths();
	public void addIncludePath(String includePath); 
	public void overwriteIncludePath( List newIncludePaths );
	
	public Token nextToken() throws ScannerException;
	 
	public void setQuickScan(boolean qs);
	public void setCallback(IParserCallback c);
}
