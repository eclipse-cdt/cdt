package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.index.TagFlags;
import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.ui.IFunctionSummary;

public class DefaultCEditorTextHover implements ITextHover 
{
	protected IEditorPart fEditor;
	
	/**
	 * Constructor for DefaultCEditorTextHover
	 */
	public DefaultCEditorTextHover( IEditorPart editor ) 
	{
		fEditor = editor;
	}

	/**
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo( ITextViewer viewer, IRegion region ) 
	{
		String result = null;
		String expression = null;
		
		if(fEditor == null) 
			return null;
		try
		{
			expression = viewer.getDocument().get( region.getOffset(), region.getLength() );
			expression = expression.trim();
			if ( expression.length() == 0 )
				return null; 

			// We are just doing some C, call the Help to get info

			IFunctionSummary fs = CCompletionContributorManager.getFunctionInfo(expression);
			if(fs != null) {
				StringBuffer s = new StringBuffer();
				s.append(expression + "() - " + fs.getSummary() + "\n\n" + fs.getSynopsis());
				int i;
				for(i = 0; i < s.length(); i++) {
					if(s.charAt(i) == '\\') {
						if((i + 1 < s.length()) && s.charAt(i+1) == 'n') {
							s.replace(i, i + 2, "\n");
						}
					}
				}
				i = s.length();
				// Eat the last cariage return for nicer looking text
				if(i != 0 && s.charAt(i - 1) == '\n') {
					s.replace(i - 1, i, "");
				}
				return s.toString();
			} else {
				// Query the C model
				IndexModel model = IndexModel.getDefault();
				IEditorInput input = fEditor.getEditorInput();
				if(input instanceof IFileEditorInput) {
					IProject project = ((IFileEditorInput)input).getFile().getProject();

					// Bail out quickly, if the project was deleted.
					if (!project.exists())
						throw new CoreException(new Status(0, "", 0, "", null));

					IProject[] refs = project.getReferencedProjects();
					
					ITagEntry[] tags= model.query(project, expression, false, true);
					
					if(tags == null || tags.length == 0) {
						for ( int j= 0; j < refs.length; j++ ) {
							if (!refs[j].exists())
								continue;
							tags= model.query(refs[j], expression, false, true);
							if(tags != null && tags.length > 0) 
								break;
						}
					}
					
					if(tags != null && tags.length > 0) {
						ITagEntry selectedTag = selectTag(tags);
						// Show only the first element
						StringBuffer s = new StringBuffer();
						s.append(expression + "() - " + selectedTag.getIFile().getFullPath().toString() + "[" + selectedTag.getLineNumber()+"]" );
						// Now add the pattern
						s.append("\n\n" + selectedTag.getPattern());
						return s.toString();
					}	
				}
			}
		}
		catch( BadLocationException x )
		{
			// ignore
		}
		catch( CoreException x )
		{
			// ignore
		}
		if ( expression != null && result != null )
			return expression + " = " + result;
		return null;
	}

	/**
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion( ITextViewer viewer, int offset ) 
	{
		Point selectedRange = viewer.getSelectedRange();
		if ( selectedRange.x >= 0 && 
			 selectedRange.y > 0 &&
			 offset >= selectedRange.x &&
			 offset <= selectedRange.x + selectedRange.y )
			return new Region( selectedRange.x, selectedRange.y );
		if ( viewer != null )
			return CWordFinder.findWord( viewer.getDocument(), offset );
		return null;
	}
	
	private ITagEntry selectTag(ITagEntry[] tags) {
		// Rules are to return a function prototype/declaration, and if
		// not found first entry
		for(int i = 0; i < tags.length; i++) {
			if(tags[i].getKind() == TagFlags.T_PROTOTYPE) {
				return tags[i];
			}
		}
		for(int i = 0; i < tags.length; i++) {
			if(tags[i].getKind() == TagFlags.T_FUNCTION) {
				return tags[i];
			}
		}
		return tags[0];
	}
}

