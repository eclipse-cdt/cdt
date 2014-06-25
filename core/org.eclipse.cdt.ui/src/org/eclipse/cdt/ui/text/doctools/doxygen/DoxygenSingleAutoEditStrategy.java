/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * Martin Stumpf - adapted orginal to cope with single line comments
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.doxygen;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.ui.text.ICPartitions;

/**
 * {@link IAutoEditStrategy} for adding Doxygen tags for comments.
 *
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DoxygenSingleAutoEditStrategy extends DoxygenMultilineAutoEditStrategy {
    private static final String SLASH_COMMENT = "///";  //$NON-NLS-1$
    private static final String EXCL_COMMENT = "//!"; //$NON-NLS-1$
    private static String fgDefaultLineDelim = "\n"; //$NON-NLS-1$


    public DoxygenSingleAutoEditStrategy() {
    }

    /**
     * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
     */
    @Override
	public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
        fgDefaultLineDelim = TextUtilities.getDefaultLineDelimiter(doc);
        if(doc instanceof IDocumentExtension4) {
            boolean forNewLine= cmd.length == 0 && cmd.text != null && endsWithDelimiter(doc, cmd.text);

            if(forNewLine ) {
                IDocumentExtension4 ext4= (IDocumentExtension4) doc;
                DocumentRewriteSession drs= ext4.startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED_SMALL);
                try {
                    customizeDocumentAfterNewLine(doc, cmd);
                } finally {
                    ext4.stopRewriteSession(drs);
                }
            }
        }
    }

    @Override
	public void customizeDocumentAfterNewLine(IDocument doc, final DocumentCommand c) {
        int offset= c.offset;
        if (offset == -1 || doc.getLength() == 0)
            return;

        final StringBuilder buf= new StringBuilder(c.text);
        try {
            // find start of line
            IRegion line= doc.getLineInformationOfOffset(c.offset);
            int lineDelimiterLength = doc.getLineDelimiter(doc.getLineOfOffset(c.offset)).length();
            int lineStart= line.getOffset();
            int firstNonWS= findEndOfWhiteSpaceAt(doc, lineStart, c.offset);

            IRegion prefix= findPrefixRange(doc, line);
            String indentation = doc.get(prefix.getOffset(), firstNonWS - prefix.getOffset());
            String commentPrefix = getCommentPrefix(doc, firstNonWS);
            String lineContent = doc.get(prefix.getOffset(), prefix.getLength());            
            int lengthToAdd= Math.min(offset - prefix.getOffset(), prefix.getLength());
            //String lineTail = lineContent.substring(lengthToAdd);
 
            buf.append(lineContent.substring(0, lengthToAdd));


            boolean commentAtStart = firstNonWS < c.offset && doc.getChar(firstNonWS) == '/';
            boolean commentFollows = false;
            boolean commentAhead = false;
            boolean firstLineContainsText = doc.get(line.getOffset(), line.getLength()).trim().length() > commentPrefix.length();

            if (commentAtStart) {
                if (line.getOffset() + line.getLength() + lineDelimiterLength < doc.getLength())
                {
                    IRegion nextLine = doc.getLineInformationOfOffset(line.getOffset() + line.getLength() + lineDelimiterLength);
                    //int firstNonWSofNextLine = findEndOfWhiteSpaceAt(doc, nextLine.getOffset(), nextLine.getOffset() + nextLine.getLength());
                    commentFollows = doc.get(nextLine.getOffset(), nextLine.getLength()).trim().startsWith(commentPrefix);

                    if (line.getOffset() >= lineDelimiterLength)
                    {
                        IRegion previousLine = doc.getLineInformationOfOffset(line.getOffset() - lineDelimiterLength);
                        //int firstNonWSofPreviousLine = findEndOfWhiteSpaceAt(doc, previousLine.getOffset(), previousLine.getOffset() + previousLine.getLength());
                        commentAhead = doc.get(previousLine.getOffset(), previousLine.getLength()).trim().startsWith(commentPrefix);
                    }
                }
                // comment started on this line
                buf.append(" "); //$NON-NLS-1$
            }

            c.shiftsCaret= false;
            c.caretOffset= c.offset + buf.length();

            if(commentAtStart && !commentFollows && !commentAhead) {
                try {
                    // as we are auto-closing, the comment becomes eligible for auto-doc'ing
                    IASTDeclaration dec= null;
                    IASTTranslationUnit ast= getAST();

                    if(ast != null) {
                        dec= findFollowingDeclaration(ast, offset);
                        if(dec == null) {
                            IASTNodeSelector ans= ast.getNodeSelector(ast.getFilePath());
                            IASTNode node= ans.findEnclosingNode(offset, 0);
                            if(node instanceof IASTDeclaration) {
                                dec= (IASTDeclaration) node;
                            }
                        }
                    }

                    StringBuilder content = null;
                    if(dec!=null) {
                        ITypedRegion partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING /* this! */, offset, false);
                        content = customizeAfterNewLineForDeclaration(doc, dec, partition);
                    }

                    if (content == null || content.toString().trim().length() == 0)
                    {
                        buf.setLength(0);
                        buf.append(fgDefaultLineDelim);
                        buf.append(indentation + commentPrefix + " "); //$NON-NLS-1$
                        c.shiftsCaret= false;
                        c.caretOffset= c.offset + buf.length();
                    } else {
                        if (!firstLineContainsText)
                        {
                            c.shiftsCaret= false;
                            c.caretOffset= c.offset + 1;
                            buf.setLength(0);
                            buf.append(indent(content, indentation + commentPrefix + " ", fgDefaultLineDelim).substring(indentation.length() + commentPrefix.length())); //$NON-NLS-1$
                        }
                        else
                        {
                            buf.append(fgDefaultLineDelim);
                            buf.append(indent(content, indentation + commentPrefix + " ", fgDefaultLineDelim)); //$NON-NLS-1$
                        }

                        buf.setLength(buf.length() - fgDefaultLineDelim.length());
                    }
                } catch(BadLocationException ble) {
                    ble.printStackTrace();
                }
            }

            c.text= buf.toString();

        } catch (BadLocationException excp) {
        	System.err.println(excp.getMessage());
        	excp.printStackTrace();
            // stop work
        }
    }

    private String getCommentPrefix(IDocument doc, int offset) throws BadLocationException {
        if (doc.get(offset, SLASH_COMMENT.length()).equals(SLASH_COMMENT))
        {
            return SLASH_COMMENT;
        }
        else
        {
            return EXCL_COMMENT;
        }
    }
    
	/**
	 * Returns the range of the comment prefix on the given line in
	 * <code>document</code>. The prefix greedily matches the following regex
	 * pattern: <code>\s*\/\/[\/!]\S*\s*</code>, that is, any number of whitespace
	 * characters, followed by an comment ('///' or '//!'), followed by any number of
	 * non-whitespace characters, followed by any number of whitespace characters.
	 *
	 * @param document the document to which <code>line</code> refers
	 * @param line the line from which to extract the prefix range
	 * @return an <code>IRegion</code> describing the range of the prefix on
	 *         the given line
	 * @throws BadLocationException if accessing the document fails
	 */
	protected static IRegion findPrefixRange(IDocument document, IRegion line) throws BadLocationException {
		int lineOffset= line.getOffset();
		int lineEnd= lineOffset + line.getLength();
		int indentEnd= findEndOfWhiteSpaceAt(document, lineOffset, lineEnd);
		if (indentEnd < lineEnd-2 && document.getChar(indentEnd) == '/' && document.getChar(indentEnd+1) == '/' && (
				document.getChar(indentEnd+2) == '/' || document.getChar(indentEnd+2) == '!')) {
			indentEnd += 3;
			while (indentEnd < lineEnd && !isWhitespace(document.getChar(indentEnd)))
				indentEnd++;
			while (indentEnd < lineEnd && isWhitespace(document.getChar(indentEnd)))
				indentEnd++;
		}
		return new Region(lineOffset, indentEnd - lineOffset);
	}
	
	private static boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\t';
	}
}
