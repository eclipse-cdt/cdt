/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ContextStack {

	private static class SentinelContext implements IScannerContext {
		public int getChar() { return '\n'; }
		public String getContextName() { return ""; } //$NON-NLS-1$
		public int getOffset() { return 0; }
		public void ungetChar(int undo) { }
		public boolean isFinal() { return false; }
		public int getKind() { return IScannerContext.ContextKind.SENTINEL; }
		public void close() { }
	}
	private final IParserLogService log;
	private int current_size = 8;
	
	private int lastFileContext = 0;
	 
	private IScannerContext [] cs = new IScannerContext[current_size];
	private int cs_pos = 0;
	
	private int currentInclusionArraySize  = 16;
	private int currentInclusionIndex = 0;
	private String [] fileNames = new String[ currentInclusionArraySize ];
	private static final String EMPTY_STRING = "";  //$NON-NLS-1$
	
	
	public final String getInclusionFilename( int index )
	{
		try
		{
			return fileNames[ index ];
		}
		catch( ArrayIndexOutOfBoundsException aioobe )
		{
			return EMPTY_STRING;
		}
	}
	
	private final void addInclusionFilename( String filename )
	{
		try
		{
			fileNames[ currentInclusionIndex++ ] = filename; 
		}
		catch( ArrayIndexOutOfBoundsException aioobe )
		{
			int newSize = currentInclusionArraySize * 2;
			String newFileNames [] = new String[ newSize ];
			System.arraycopy( fileNames, 0, newFileNames, 0, fileNames.length );
			newFileNames[ currentInclusionArraySize++ ] = filename;
			currentInclusionArraySize = newSize;
			fileNames = newFileNames;
		}
	}
	
	private static IScannerContext sentinel = new SentinelContext();
	 
	private IScanner scanner;

	public final void cs_push(IScannerContext c) {
		try {
			cs[cs_pos++] = c;
		}
		catch (ArrayIndexOutOfBoundsException a)
		{
			int new_size = current_size*2;
			IScannerContext [] new_cs = new IScannerContext[new_size];
			System.arraycopy( cs, 0, new_cs, 0, cs.length );			
			new_cs[current_size] = c;
			current_size = new_size;
			cs = new_cs;
		}
		scanner.setScannerContext(c);
	}
	public final IScannerContext cs_pop() {
		IScannerContext context = cs[--cs_pos];
		scanner.setScannerContext((cs_pos == 0) ? sentinel : cs[cs_pos -1]);
		return context;
	}
	
	public ContextStack( IScanner scanner, IParserLogService l ) {
		log = l;
		this.scanner = scanner;
		cs_push(sentinel);
		scanner.setScannerContext(sentinel);
	}

    public void updateInclusionContext(CodeReader code, IASTInclusion inclusion, ISourceElementRequestor requestor) throws ContextException {
    	addInclusionFilename( new String( code.filename ));
    	ScannerContextInclusion context = new ScannerContextInclusion( code, inclusion, currentInclusionIndex - 1 );
    	
    	if( isCircularInclusion( context.getContextName() ) )
			throw new ContextException( IProblem.PREPROCESSOR_CIRCULAR_INCLUSION );
		
		TraceUtil.outputTrace(log, "Scanner::ContextStack: entering inclusion ", null, context.getContextName(), null, null ); //$NON-NLS-1$
		context.getExtension().enterScope( requestor, null );	
		cs_push(context);
		lastFileContext++;
    }
  
	public void updateMacroContext(String reader, String filename, ISourceElementRequestor requestor, int macroOffset, int macroLength) throws ContextException 
    {		
        // If we expand a macro within a macro, then keep offsets of the top-level one,
        // as only the top level macro identifier is properly positioned    
        if (getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION) {
            macroOffset = ((ScannerContextMacro)getCurrentContext()).getOffset();
            macroLength = ((ScannerContextMacro)getCurrentContext()).getMacroLength();
        }

		cs_push(new ScannerContextMacro( 
				reader, 
				filename, 
				macroOffset, macroLength));	
	}
	
	protected void pushInitialContext( IScannerContext context ) throws ContextException
	{
		addInclusionFilename( context.getContextName() );
		lastFileContext++;
		cs_push(context);
	}
	
	public boolean rollbackContext(ISourceElementRequestor requestor) {
		IScannerContext context = getCurrentContext();
			context.close();

		if( context.getKind() == IScannerContext.ContextKind.INCLUSION )
		{
			TraceUtil.outputTrace(log, "Scanner::ContextStack: ending inclusion ", null, context.getContextName(), null, null); //$NON-NLS-1$
			((ScannerContextInclusion)context).getExtension().exitScope( requestor, null );
			lastFileContext--;
		}
		cs_pop();
		return cs_pos != 0;
	}
	
	public void undoRollback( IScannerContext undoTo, ISourceElementRequestor requestor ) {
		IScannerContext context;
		while (getCurrentContext() != undoTo ) {
			context = cs[cs_pos++];
			if(context.getKind() == IScannerContext.ContextKind.INCLUSION)
				lastFileContext++;
			scanner.setScannerContext(context);
		}
	}
	
	/**
	 * 
	 * @param symbol
	 * @return boolean, whether or not we should expand this definition
	 * 
	 * 16.3.4-2 If the name of the macro being replaced is found during 
	 * this scan of the replacement list it is not replaced.  Further, if 
	 * any nested replacements encounter the name of the macro being replaced,
	 * it is not replaced. 
	 */
	protected boolean shouldExpandDefinition( String symbol )
	{
		for(int i = cs_pos-1; i >= 0; i--)
			if (cs[i].getKind() == IScannerContext.ContextKind.MACROEXPANSION
					&& cs[i].getContextName().equals(symbol))
				return false;
		return true;
	}
	
	protected boolean isCircularInclusion( String symbol )
	{
		for(int i = cs_pos-1; i >= 0; i--)
			if (cs[i].getKind() == IScannerContext.ContextKind.INCLUSION &&
					cs[i].getContextName().equals(symbol))
				return true;
		return false;
	}
	
	public final IScannerContext getCurrentContext(){
		return cs[cs_pos -1];
	}

	public ScannerContextInclusion getMostRelevantFileContext()
	{
		if( cs[lastFileContext] != null && cs[lastFileContext] instanceof ScannerContextInclusion )
			return (ScannerContextInclusion)cs[lastFileContext];
		return null;
	}
	
	public int getMostRelevantFileContextIndex()
	{
		return getMostRelevantFileContext().getFilenameIndex();
	}
	public int getCurrentLineNumber()
	{
		
		ScannerContextInclusion mostRelevantFileContext = getMostRelevantFileContext();
		if( mostRelevantFileContext != null )
			return mostRelevantFileContext.getLine();
		return -1;
	}

}
