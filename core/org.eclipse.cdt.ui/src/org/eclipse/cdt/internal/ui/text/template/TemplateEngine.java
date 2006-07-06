/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;

import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.corext.template.c.TranslationUnitContext;
import org.eclipse.cdt.internal.corext.template.c.TranslationUnitContextType;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.text.c.hover.SourceViewerInformationControl;

public class TemplateEngine implements ICompletionContributor {

	private TemplateContextType fContextType;	
	private ArrayList fProposals= new ArrayList();

	public class CTemplateProposal extends TemplateProposal implements ICCompletionProposal {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getInformationControlCreator()
		 */
		public IInformationControlCreator getInformationControlCreator() {
			return new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					int shellStyle= SWT.RESIZE;
					int style= SWT.V_SCROLL | SWT.H_SCROLL;				
					return new SourceViewerInformationControl(parent, shellStyle, style);
				}
			};
		}
		/**
		 * @param template
		 * @param context
		 * @param region
		 * @param image
		 */
		public CTemplateProposal(Template template, TemplateContext context, IRegion region, Image image) {
			super(template, context, region, image, 90);
		}

        public String getIdString() {
            return getDisplayString();
        }
        
	}
	/**
	 * Creates the template engine for a particular context type.
	 * See <code>TemplateContext</code> for supported context types.
	 */
	public TemplateEngine(TemplateContextType contextType) {
		Assert.isNotNull(contextType);
		fContextType= contextType;
	}

	/**
	 * This is the default constructor used by the new content assist extension point
	 */
	public TemplateEngine() {
		fContextType = CUIPlugin.getDefault().getTemplateContextRegistry().getContextType(CContextType.CCONTEXT_TYPE);			
		if (fContextType == null) {
			fContextType= new CContextType();
			CUIPlugin.getDefault().getTemplateContextRegistry().addContextType(fContextType);
		}
	}
	
	/**
	 * Empties the collector.
	 */
	public void reset() {
		fProposals.clear();
	}

	/**
	 * Returns the array of matching templates.
	 */
	public List getResults() {
		//return (TemplateProposal[]) fProposals.toArray(new TemplateProposal[fProposals.size()]);
		return fProposals;
	}

	/**
	 * Inspects the context of the compilation unit around <code>completionPosition</code>
	 * and feeds the collector with proposals.
	 * @param viewer the text viewer
	 * @param completionPosition the context position in the document of the text viewer
	 * @param translationUnit the translation unit (may be <code>null</code>)
	 */
	public void complete(ITextViewer viewer, int completionPosition, ITranslationUnit translationUnit)
	{
	    IDocument document= viewer.getDocument();
	    
		if (!(fContextType instanceof TranslationUnitContextType))
			return;

		Point selection= viewer.getSelectedRange();

		((TranslationUnitContextType) fContextType).setContextParameters(document.get(), completionPosition, translationUnit);		
		TranslationUnitContext context= ((TranslationUnitContextType) fContextType).createContext(document, completionPosition, selection.y, translationUnit);
		int start= context.getStart();
		int end= context.getEnd();
		IRegion region= new Region(start, end - start);

		Template[] templates= CUIPlugin.getDefault().getTemplateStore().getTemplates();
		for (int i= 0; i != templates.length; i++)
			if (context.canEvaluate(templates[i]))
				fProposals.add(new CTemplateProposal(templates[i], context, region, CPluginImages.get(CPluginImages.IMG_OBJS_TEMPLATE)));
	}
	
	public void contributeCompletionProposals(ITextViewer viewer,
											  int offset,
											  IWorkingCopy workingCopy,
											  ASTCompletionNode completionNode,
                                              String prefix,
											  List proposals)
	{
		// TODO We should use the completion node to determine the proper context for the templates
		// For now we just keep the current behavior
		complete(viewer, offset, workingCopy);
		proposals.addAll(fProposals);
	}
}

