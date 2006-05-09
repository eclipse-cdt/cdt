/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Qnx Software System
 *     Anton Leherbauer (Wind River Systems) - Fixed bug 126617
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.Strings;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;


/**
 * A template editor using the C/C++ formatter to format a template buffer.
 */
public class CFormatter {

//	private static final String CURSOR= "cursor"; //$NON-NLS-1$
//	private static final String MARKER= "/*${cursor}*/"; //$NON-NLS-1$

	/** The line delimiter to use if code formatter is not used. */
	private final String fLineDelimiter;
	/** The initial indent level */
	private final int fInitialIndentLevel;
	
	/** Flag indicating whether to use the code formatter or not. */
	private boolean fUseCodeFormatter;

	public CFormatter(String lineDelimiter, int initialIndentLevel, boolean useCodeFormatter) {
		fLineDelimiter= lineDelimiter;
		fUseCodeFormatter= useCodeFormatter;
		fInitialIndentLevel= initialIndentLevel;
	}

	public void edit(TemplateBuffer buffer, CContext context, int indentationLevel) throws BadLocationException {		
		try {
			if (fUseCodeFormatter)
				// try to format and fall back to indenting
				try {
					format(buffer, context);
				} catch (BadLocationException e) {
					indent(buffer);
				} catch (MalformedTreeException e) {
					indent(buffer);
				}
			else {
				indent(buffer);
			}

			// don't trim the buffer if the replacement area is empty
			// case: surrounding empty lines with block
			if (context.getStart() == context.getCompletionOffset())
				if (context.getDocument().get(context.getStart(), context.getEnd() - context.getEnd()).trim().length() == 0)
					return;
			
			trimBegin(buffer);
		} catch (MalformedTreeException e) {
			throw new BadLocationException();
		}
	}

//	private static int getCaretOffset(TemplateVariable[] variables) {
//	    for (int i= 0; i != variables.length; i++) {
//	        TemplateVariable variable= variables[i];
//	        
//	        if (variable.getName().equals(CURSOR)) {
//	        	return variable.getOffsets()[0];
//	        }
//	    }
//	    
//	    return -1;
//	}

//	private boolean isInsideCommentOrString(String string, int offset) {
//
//		IDocument document= new Document(string);
//		CUIPlugin.getDefault().getTextTools().setupCDocument(document);
//
//		try {		
//			ITypedRegion partition= document.getPartition(offset);
//			String partitionType= partition.getType();
//		
//			return partitionType != null && (
//				partitionType.equals(ICPartitions.C_MULTILINE_COMMENT) ||
//				partitionType.equals(ICPartitions.C_SINGLE_LINE_COMMENT) ||
//				partitionType.equals(ICPartitions.C_STRING));
//		} catch (BadLocationException e) {
//			return false;	
//		}
//	}

	private void format(TemplateBuffer templateBuffer, CContext context) throws BadLocationException {
		// XXX 4360, 15247
		// workaround for code formatter limitations
		// handle a special case where cursor position is surrounded by whitespaces		

//		String string= templateBuffer.getString();
//		TemplateVariable[] variables= templateBuffer.getVariables();
//
//		int caretOffset= getCaretOffset(variables);
//		if ((caretOffset > 0) && Character.isWhitespace(string.charAt(caretOffset - 1)) &&
//			(caretOffset < string.length()) && Character.isWhitespace(string.charAt(caretOffset)) &&
//			! isInsideCommentOrString(string, caretOffset))
//		{
//			List positions= variablesToPositions(variables);
//
//		    TextEdit insert= new InsertEdit(caretOffset, MARKER);
//		    string= edit(string, positions, insert);
//			positionsToVariables(positions, variables);
//		    templateBuffer.setContent(string, variables);
//
//			plainFormat(templateBuffer, context);			
//
//			string= templateBuffer.getString();
//			variables= templateBuffer.getVariables();
//			caretOffset= getCaretOffset(variables);
//
//			positions= variablesToPositions(variables);
//			TextEdit delete= new DeleteEdit(caretOffset, MARKER.length());
//		    string= edit(string, positions, delete);
//			positionsToVariables(positions, variables);		    
//		    templateBuffer.setContent(string, variables);
//	
//		} else {
//			plainFormat(templateBuffer, context);			
//		}	    
		plainFormat(templateBuffer, context);			
	}
	
	private void plainFormat(TemplateBuffer templateBuffer, CContext context) throws BadLocationException {
		
		IDocument doc= new Document(templateBuffer.getString());
		
		TemplateVariable[] variables= templateBuffer.getVariables();
		
		List offsets= variablesToPositions(variables);
		
		Map options;
		if (context.getTranslationUnit() != null)
			options= context.getTranslationUnit().getCProject().getOptions(true); 
		else
			options= CCorePlugin.getOptions();
		
		TextEdit edit= CodeFormatterUtil.format(CodeFormatter.K_UNKNOWN, doc.get(), fInitialIndentLevel, fLineDelimiter, options);
		if (edit == null)
			throw new BadLocationException(); // fall back to indenting
		
		MultiTextEdit root;
		if (edit instanceof MultiTextEdit)
			root= (MultiTextEdit) edit;
		else {
			root= new MultiTextEdit(0, doc.getLength());
			root.addChild(edit);
		}
		for (Iterator it= offsets.iterator(); it.hasNext();) {
			TextEdit position= (TextEdit) it.next();
			try {
				root.addChild(position);
			} catch (MalformedTreeException e) {
				// position conflicts with formatter edit
				// ignore this position
			}
		}
		
		root.apply(doc, TextEdit.UPDATE_REGIONS);
		
		positionsToVariables(offsets, variables);
		
		templateBuffer.setContent(doc.get(), variables);	    
	}	

	private void indent(TemplateBuffer templateBuffer) throws BadLocationException, MalformedTreeException {

		TemplateVariable[] variables= templateBuffer.getVariables();
		List positions= variablesToPositions(variables);
		
		IDocument document= new Document(templateBuffer.getString());
		MultiTextEdit root= new MultiTextEdit(0, document.getLength());
		root.addChildren((TextEdit[]) positions.toArray(new TextEdit[positions.size()]));
		
		IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
		boolean useSpaces = store.getBoolean(CEditor.SPACES_FOR_TABS);
		int tabWidth = store
				.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
		int indentWidth = tabWidth;
		String indent = createIndentString(fInitialIndentLevel * indentWidth, tabWidth, useSpaces);

		// first line
		int offset= document.getLineOffset(0);
		TextEdit edit= new InsertEdit(offset, indent);
		root.addChild(edit);
		root.apply(document, TextEdit.UPDATE_REGIONS);
		root.removeChild(edit);
		formatDelimiter(document, root, 0);
		
		// following lines
	    int lineCount= document.getNumberOfLines();
	    
	    for (int line= 1; line < lineCount; line++) {
			IRegion region= document.getLineInformation(line);
			String lineContent= document.get(region.getOffset(), region.getLength());
			String lineIndent= Strings.getIndentString(lineContent, tabWidth);
			int lineIndentLevel= Strings.computeIndent(lineIndent, tabWidth);
			indent= createIndentString((fInitialIndentLevel + lineIndentLevel) * indentWidth, tabWidth, useSpaces);
	    	edit= new ReplaceEdit(region.getOffset(), lineIndent.length(), indent);
			root.addChild(edit);
			root.apply(document, TextEdit.UPDATE_REGIONS);
			root.removeChild(edit);

			formatDelimiter(document, root, line);
	    }
	    
		positionsToVariables(positions, variables);
		templateBuffer.setContent(document.get(), variables);
	}

	/**
	 * Changes the delimiter to the configured line delimiter.
	 * 
	 * @param document the temporary document being edited
	 * @param root the root edit containing all positions that will be updated along the way
	 * @param line the line to format
	 * @throws BadLocationException if applying the changes fails
	 */
	private void formatDelimiter(IDocument document, MultiTextEdit root, int line) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		String lineDelimiter= document.getLineDelimiter(line);
		if (lineDelimiter != null) {
			TextEdit edit= new ReplaceEdit(region.getOffset() + region.getLength(), lineDelimiter.length(), fLineDelimiter);
			root.addChild(edit);
			root.apply(document, TextEdit.UPDATE_REGIONS);
			root.removeChild(edit);
		}
	}

	private static void trimBegin(TemplateBuffer templateBuffer) throws BadLocationException {
		String string= templateBuffer.getString();
		TemplateVariable[] variables= templateBuffer.getVariables();

		List positions= variablesToPositions(variables);

		int i= 0;
		while ((i != string.length()) && Character.isWhitespace(string.charAt(i)))
			i++;

		string= edit(string, positions, new DeleteEdit(0, i));
		positionsToVariables(positions, variables);

		templateBuffer.setContent(string, variables);
	}
	
	private static String edit(String string, List positions, TextEdit edit) throws BadLocationException {
		MultiTextEdit root= new MultiTextEdit(0, string.length());
		root.addChildren((TextEdit[]) positions.toArray(new TextEdit[positions.size()]));
		root.addChild(edit);
		IDocument document= new Document(string);
		root.apply(document);
		
		return document.get();		
	}

	/**
	 * Create an indent string suitable as line prefix using the given
	 * formatting options.
	 * 
	 * @param displayedWidth
	 *            the desired displayed width (in char units)
	 * @param tabWidth
	 *            the displayed tab width
	 * @param useSpaces
	 *            if <code>true</code>, only spaces are used for the indent
	 *            string, otherwise, the indent string will contain mixed tabs
	 *            and spaces.
	 * @return the new indent string
	 */
	private static String createIndentString(int displayedWidth,
			int tabWidth, boolean useSpaces) {
		return appendIndentString(new StringBuffer(displayedWidth),
				displayedWidth, tabWidth, useSpaces, 0).toString();
	}

	/**
	 * Append an indent string to the given buffer so that the resulting string
	 * ends at the desired column.
	 * 
	 * @param buffer
	 *            the StringBuffer to append to
	 * @param displayedWidth
	 *            the desired displayed width (in char units)
	 * @param tabWidth
	 *            the displayed tab width
	 * @param useSpaces
	 *            if <code>true</code>, only spaces are used for the indent
	 *            string, otherwise, the indent string will contain mixed tabs
	 *            and spaces.
	 * @param startColumn
	 *            the displayed starting column to correctly identify the tab
	 *            stops
	 * @return the same StringBuffer as provided
	 */
	private static StringBuffer appendIndentString(StringBuffer buffer,
			int displayedWidth, int tabWidth, boolean useSpaces, int startColumn) {
		int tabStop= startColumn - startColumn % tabWidth;
		int tabs= useSpaces ? 0 : (displayedWidth - tabStop) / tabWidth;
		for (int i= 0; i < tabs; ++i) {
			buffer.append('\t');
			tabStop += tabWidth;
			startColumn= tabStop;
		}
		int spaces= displayedWidth - startColumn;
		for (int i= 0; i < spaces; ++i) {
			buffer.append(' ');
		}
		return buffer;
	}

	private static List variablesToPositions(TemplateVariable[] variables) {
   		List positions= new ArrayList(5);
		for (int i= 0; i != variables.length; i++) {
		    int[] offsets= variables[i].getOffsets();
		    
		    // trim positions off whitespace
		    String value= variables[i].getDefaultValue();
		    int wsStart= 0;
		    while (wsStart < value.length() && Character.isWhitespace(value.charAt(wsStart)) && !Strings.isLineDelimiterChar(value.charAt(wsStart)))
		    	wsStart++;
		    
		    variables[i].getValues()[0]= value.substring(wsStart);
		    
		    for (int j= 0; j != offsets.length; j++) {
		    	offsets[j] += wsStart;
				positions.add(new RangeMarker(offsets[j], 0));
		    }
		}
		return positions;	    
	}
	
	private static void positionsToVariables(List positions, TemplateVariable[] variables) {
		Iterator iterator= positions.iterator();
		
		for (int i= 0; i != variables.length; i++) {
		    TemplateVariable variable= variables[i];
		    
			int[] offsets= new int[variable.getOffsets().length];
			for (int j= 0; j != offsets.length; j++)
				offsets[j]= ((TextEdit) iterator.next()).getOffset();
			
		 	variable.setOffsets(offsets);   
		}
	}	

}

