package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.ast.IASTFactory;

/**
 * @author jcamelon
 *
 */
public interface IScanner  {

	public static final int tPOUNDPOUND = -6;
	public static final int tPOUND      = -7;
	
	public void setASTFactory( IASTFactory f );
	public void addDefinition(String key, IMacroDescriptor macroToBeAdded );
	public void addDefinition(String key, String value); 
	public Object getDefinition(String key);

	public String[] getIncludePaths();
	public void addIncludePath(String includePath); 
	public void overwriteIncludePath( String [] newIncludePaths );
	public void setRequestor( ISourceElementRequestor r );
	
	public IToken nextToken() throws ScannerException, EndOfFile;
	public IToken nextToken( boolean next ) throws ScannerException, EndOfFile;
	 
	public void setCppNature( boolean value );
	
	public void setMode(ParserMode mode);
	public void setCallback(IParserCallback c);
	
	public int  getCount();
	public int  getDepth();

	public IToken nextTokenForStringizing() throws ScannerException, EndOfFile;
	public void setTokenizingMacroReplacementList(boolean b);
    
	public void onParseEnd();
}
