package org.eclipse.cdt.core.parser;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.Parser;

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

	public Object[] getIncludePaths();
	public void addIncludePath(String includePath); 
	public void overwriteIncludePath( List newIncludePaths );
	public void setRequestor( ISourceElementRequestor r );
	
	public IToken nextToken() throws ScannerException, Parser.EndOfFile;
	public IToken nextToken( boolean next ) throws ScannerException, Parser.EndOfFile;
	 
	public void setCppNature( boolean value );
	
	public void setMode(ParserMode mode);
	public void setCallback(IParserCallback c);
	
	public int  getCount();
	public int  getDepth();
	/**
	 * @return
	 */
	public IToken nextTokenForStringizing() throws ScannerException, Parser.EndOfFile;
	/**
	 * @param b
	 */
	public void setTokenizingMacroReplacementList(boolean b);
}
