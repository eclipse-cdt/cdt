package org.eclipse.cdt.core.parser;

import java.util.Map;

import org.eclipse.cdt.core.parser.ast.IASTFactory;

/**
 * @author jcamelon
 *
 */
public interface IScanner  {

	public static final int tPOUNDPOUND = -6;
	public static final int tPOUND      = -7;
	
	public void setOffsetBoundary( int offset );
	
	public void setASTFactory( IASTFactory f );
	public void addDefinition(String key, IMacroDescriptor macroToBeAdded );
	public void addDefinition(String key, String value); 
	public IMacroDescriptor getDefinition(String key);
	public Map 				getDefinitions();

	public String[] getIncludePaths();
	public void addIncludePath(String includePath); 
	public void overwriteIncludePath( String [] newIncludePaths );
	
	public IToken nextToken() throws ScannerException, EndOfFileException;
	public IToken nextToken( boolean next ) throws ScannerException, EndOfFileException;
			
	public int  getCount();
	public int  getDepth();

	public IToken nextTokenForStringizing() throws ScannerException, EndOfFileException;
	public void setTokenizingMacroReplacementList(boolean b);
	public void setThrowExceptionOnBadCharacterRead( boolean throwOnBad );
    public int getLineNumberForOffset(int i);
}
