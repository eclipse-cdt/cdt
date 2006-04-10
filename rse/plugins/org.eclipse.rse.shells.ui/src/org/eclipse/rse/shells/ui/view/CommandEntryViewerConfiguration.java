/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.shells.ui.view;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;


/**
 * Source viewer configuration class for command line content assist.
 * 
 */
public class CommandEntryViewerConfiguration extends SourceViewerConfiguration
{

	private CommandEntryContentAssistProcessor _contentAssistantProcessor;
	private ContentAssistant _contentAssistant;
	/**
	 * Constructor 
	 */
	public CommandEntryViewerConfiguration()
	{
		super();
		_contentAssistantProcessor = new CommandEntryContentAssistProcessor(this);

	}

	public void setRemoteCommand(IRemoteCommandShell cmd)
	{
		_contentAssistantProcessor.setRemoteCommand(cmd);
	}

	public IRemoteCommandShell getRemoteCommand()
	{
		return _contentAssistantProcessor.getRemoteCommand();
	}

	/**
	 * Parent override.
	 * Returns the content assistant ready to be used with the given source viewer.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a content assistant or <code>null</code> if content assist should not be supported
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
	{
		if (_contentAssistant == null)
		{
			_contentAssistant = new ContentAssistant();
			_contentAssistant.setContentAssistProcessor(_contentAssistantProcessor, IDocument.DEFAULT_CONTENT_TYPE);
			_contentAssistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
			//_contentAssistant.enableAutoActivation(true);
			_contentAssistant.enableAutoInsert(true);

			_contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		}
		return _contentAssistant;
	}

	/**
	 * Return the characters which trigger the auto-display of the list
	 * substitution variables. We return '$' by default, but this can be
	 * overridden.
	 */
	protected char[] getCompletionProposalAutoActivationCharacters()
	{
		return new char[] { '/', '\\', '$', '%'};
	}
	
}