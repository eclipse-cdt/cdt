/*
 * Created on Aug 16, 2004
 *
 * Copyright 2004, QNX Software Systems Ltd. All Rights Reserved.
 *
 * This source code may contain confidential information of QNX Software
 * Systems Ltd.  (QSSL) and its licensors. Any use, reproduction,
 * modification, disclosure, distribution or transfer of this software,
 * or any software which includes or is based upon any of this code, is
 * prohibited unless expressly authorized by QSSL by written agreement. For
 * more information (including whether this source code file has been
 * published) please email licensing@qnx.com.
 */
package org.eclipse.cdt.internal.ui.text;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.CodeFormatterConstants;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * @author AChapiro
 */
public class CFormattingStrategy extends ContextBasedFormattingStrategy {
	

	/** Documents to be formatted by this strategy */
	private final LinkedList fDocuments= new LinkedList();
	/** Partitions to be formatted by this strategy */
	private final LinkedList fPartitions= new LinkedList();

	/**
	 * Creates a new java formatting strategy.
 	 */
	public CFormattingStrategy() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#format()
	 */
	public void format() {
		super.format();
		
		final IDocument document= (IDocument)fDocuments.removeFirst();
		final TypedPosition partition= (TypedPosition)fPartitions.removeFirst();
		
		if (document != null && partition != null) {
			Map options = getPreferences();
			try {
				
				final TextEdit edit= CodeFormatterUtil.format(CodeFormatter.K_COMPILATION_UNIT, 
						document.get(), 
						partition.getOffset(), 
						partition.getLength(), 
						0, 
						TextUtilities.getDefaultLineDelimiter(document), 
						getPreferences());
				
				if (edit != null)
					edit.apply(document);
				
			} catch (MalformedTreeException exception) {
				CUIPlugin.getDefault().log(exception);
			} catch (BadLocationException exception) {
				// Can only happen on concurrent document modification - log and bail out
				CUIPlugin.getDefault().log(exception);
			}
		}
 	}		

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
	 */
	public void formatterStarts(final IFormattingContext context) {
	    prepareFormattingContext(context);
		super.formatterStarts(context);
		
		fPartitions.addLast(context.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
		fDocuments.addLast(context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops()
	 */
	public void formatterStops() {
		super.formatterStops();

		fPartitions.clear();
		fDocuments.clear();
	}

	private IFile getActiveFile() {
		IFile file = null;
		IEditorPart editor = 
			CUIPlugin.getDefault().getWorkbench().
				getActiveWorkbenchWindow().
					getActivePage().getActiveEditor();
			IEditorInput input = editor.getEditorInput();
			if(input instanceof IFileEditorInput) {
			    file = ((IFileEditorInput)input).getFile();
			}		
		return file;
	}
	
	private void prepareFormattingContext(final IFormattingContext context) {
	    Map preferences;
		ParserLanguage language = ParserLanguage.CPP;
		IFile activeFile = getActiveFile();
		if(null != activeFile) {
			IProject currentProject = activeFile.getProject();
			Assert.isNotNull(currentProject);
			// pick the language
			if (CoreModel.hasCCNature(currentProject)) {
				language = ParserLanguage.CPP;
			} else {
				// for C project try to guess.
				ICFileType type = CCorePlugin.getDefault().getFileType(currentProject, 
						activeFile.getFullPath().lastSegment());
				String lid = type.getLanguage().getId();
				if(lid != null && lid.equals(ICFileTypeConstants.LANG_C))
					language = ParserLanguage.C;
			}
	        preferences= new HashMap(CoreModel.getDefault().create(
	                activeFile.getProject()).getOptions(true));
		} else
            preferences= new HashMap(CCorePlugin.getOptions());

        preferences.put(CodeFormatterConstants.FORMATTER_LANGUAGE, language);
		preferences.put(CodeFormatterConstants.FORMATTER_CURRENT_FILE, activeFile);
	          
		context.storeToMap(CUIPlugin.getDefault().getPreferenceStore(), preferences, false);
        context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, preferences);

        return;
	}
	    
}
