/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.internal.ui.editor.asm.AsmSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextEditor;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextTools;
import org.eclipse.cdt.internal.ui.text.CAnnotationHover;
import org.eclipse.cdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Enter type comment.
 * 
 * @since May 5, 2003
 */
public class DisassemblySourceViewerConfiguration extends AsmSourceViewerConfiguration
{
	public DisassemblySourceViewerConfiguration( AsmTextTools tools, AsmTextEditor editor )
	{
		super( tools, editor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
	{
		return new CAnnotationHover();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationControlCreator(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IInformationControlCreator getInformationControlCreator( ISourceViewer sourceViewer )
	{
		return getInformationControlCreator( sourceViewer, true );
	}

	public IInformationControlCreator getInformationControlCreator( ISourceViewer sourceViewer, final boolean cutDown )
	{
		return new IInformationControlCreator()
		{
			public IInformationControl createInformationControl( Shell parent )
			{
				int style = cutDown ? SWT.NONE : ( SWT.V_SCROLL | SWT.H_SCROLL );
				return new DefaultInformationControl( parent, style, new HTMLTextPresenter( cutDown ) );
			}
		};
	}
}
