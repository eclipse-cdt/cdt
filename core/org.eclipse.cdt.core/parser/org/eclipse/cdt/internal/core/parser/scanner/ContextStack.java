/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.IOException;
import java.io.Reader;

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
		public int read() throws IOException { return '\n'; }
		public String getFilename() { return ""; } //$NON-NLS-1$
		public int getMacroOffset() { return -1; }
		public int getMacroLength() { return -1; }
		public int getOffset() { return 0; }
		public int getRelativeOffset() { return 0; }
		public Reader getReader() { return null; }
		public void pushUndo(int undo) { }
		public int getKind() { return IScannerContext.ContextKind.SENTINEL; }
		public void setKind(int kind) { }
		public IASTInclusion getExtension() { return null; }
		public void setExtension(IASTInclusion ext) { }
		public int getLine() { return -1; }
		public int undoStackSize() { return 0; }  
		public int popUndo() { return '\n'; }
	}
	private final IParserLogService log;
	private int current_size = 8;

	private IScannerContext [] cs = new IScannerContext[current_size];;
	private int cs_pos = 0;
	
	
	private static IScannerContext sentinel = new SentinelContext();
	 
	private IScanner scanner;

	private final void cs_push(IScannerContext c) {
		try {
			cs[cs_pos++] = c;;
		}
		catch (ArrayIndexOutOfBoundsException a)
		{
			int new_size = current_size*2;
			IScannerContext [] new_cs = new IScannerContext[new_size];
			
			for (int i = 0; i < current_size; i++) {
				new_cs[i] = cs[i];
			}
			
			new_cs[current_size] = c;
			current_size = new_size;
			cs = new_cs;
		}
		scanner.setScannerContext(c);
	}
	private final IScannerContext cs_pop() {
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

    public void updateContext(Reader reader, String filename, int type, IASTInclusion inclusion, ISourceElementRequestor requestor) throws ContextException {
        updateContext(reader, filename, type, inclusion, requestor, -1, -1);
    }
  
	public void updateContext(Reader reader, String filename, int type, IASTInclusion inclusion, ISourceElementRequestor requestor, int macroOffset, int macroLength) throws ContextException 
    {
		int startLine = 1;
		
        // If we expand a macro within a macro, then keep offsets of the top-level one,
        // as only the top level macro identifier is properly positioned    
        if (type == IScannerContext.ContextKind.MACROEXPANSION) {
            if (getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION) {
                macroOffset = getCurrentContext().getMacroOffset();
                macroLength = getCurrentContext().getMacroLength();
            }
            
			startLine = getCurrentContext().getLine();
        }

		IScannerContext context = new ScannerContext( reader, filename, type, null, macroOffset, macroLength, startLine );
		context.setExtension(inclusion); 
		push( context, requestor );	
	}
	
	protected void push( IScannerContext context, ISourceElementRequestor requestor ) throws ContextException
	{
		if( context.getKind() == IScannerContext.ContextKind.INCLUSION ) {
			if( isCircularInclusion( context.getFilename() ) )
				throw new ContextException( IProblem.PREPROCESSOR_CIRCULAR_INCLUSION );
			
			TraceUtil.outputTrace(log, "Scanner::ContextStack: entering inclusion ", null, context.getFilename(), null, null ); //$NON-NLS-1$
			context.getExtension().enterScope( requestor );				
		} 
		
//		This could be replaced with a check for shouldExpandMacro -- but it is called by 
//		the scanner before this point
//		else if( context.getKind() == IScannerContext.ContextKind.MACROEXPANSION )
//		{
//			if( !defines.add( context.getFilename() ) )
//				throw new ContextException( IProblem.PREPROCESSOR_INVALID_MACRO_DEFN );
//		}
		cs_push(context);
	}
	
	public boolean rollbackContext(ISourceElementRequestor requestor) {
		IScannerContext context = getCurrentContext();
		try {
			context.getReader().close();
		} catch (IOException ie) {
			TraceUtil.outputTrace( log, "ContextStack : Error closing reader "); //$NON-NLS-1$
		}

		if( context.getKind() == IScannerContext.ContextKind.INCLUSION )
		{
			TraceUtil.outputTrace(log, "Scanner::ContextStack: ending inclusion ", null, context.getFilename(), null, null); //$NON-NLS-1$
			context.getExtension().exitScope( requestor );
		}
		cs_pop();
		return cs_pos != 0;
	}
	
	public void undoRollback( IScannerContext undoTo, ISourceElementRequestor requestor ) {
		while (getCurrentContext() != undoTo ) {
			//cs_pos++;
			scanner.setScannerContext(cs[cs_pos++]);
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
					&& cs[i].getFilename().equals(symbol))
				return false;
		return true;
	}
	
	protected boolean isCircularInclusion( String symbol )
	{
		for(int i = cs_pos-1; i >= 0; i--)
			if (cs[i].getKind() == IScannerContext.ContextKind.INCLUSION &&
					cs[i].getFilename().equals(symbol))
				return true;
		return false;
	}
	
	public final IScannerContext getCurrentContext(){
		//return (cs_pos == 0) ? sentinel : cs[cs_pos -1];
		return cs[cs_pos -1];
	}

	public IScannerContext getMostRelevantFileContext()
	{
		IScannerContext context = sentinel;
		for( int i = cs_pos - 1; i >= 0; --i )
		{
			context = cs[i];
			if( context.getKind() == IScannerContext.ContextKind.INCLUSION 
					|| context.getKind() == IScannerContext.ContextKind.TOP )
				break;
		}
		return context;
	}
	
	public int getCurrentLineNumber()
	{
		return getMostRelevantFileContext().getLine();
	}

}
