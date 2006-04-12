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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.rse.core.subsystems.RemoteChildrenContentsType;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteCmdSubSystem;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileChildrenContentsType;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFolderChildrenContentsType;
import org.eclipse.rse.subsystems.shells.core.model.ISystemOutputRemoteTypes;
import org.eclipse.rse.subsystems.shells.core.subsystems.ICandidateCommand;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * Class that implements the content assist processor interface for the command line.
 */
public class CommandEntryContentAssistProcessor implements IContentAssistProcessor
{

	private class CompletionResults
	{
		private List _nameResults;
		private List _proposalResults;
		private List _proposalHelpResults;
		private List _images;

		public CompletionResults()
		{
			_nameResults = new ArrayList();
			_proposalResults = new ArrayList();
			_proposalHelpResults = new ArrayList();
			_images = new ArrayList();
		}

		public int size()
		{
			return _nameResults.size();
		}

		public void addResult(String name, String text, String help, Image image)
		{
			_nameResults.add(name);
			_proposalResults.add(text);
			_proposalHelpResults.add(help);
			_images.add(image);
		}

		public String getNameAt(int i)
		{
			return (String) _nameResults.get(i);
		}

		public String getTextAt(int i)
		{
			return (String) _proposalResults.get(i);
		}

		public String getHelpAt(int i)
		{
			return (String) _proposalHelpResults.get(i);
		}

		public Image getImageAt(int i)
		{
			return (Image) _images.get(i);
		}
	}

	private CommandEntryViewerConfiguration _configurator;
	private SystemTableViewProvider _provider;
	private HashMap _imageMap;
	private IRemoteCommandShell _remoteCommand;
	private char _fileSeparator;
	private char _foreignFileSeparator;
	private IRemoteFile _lastFolderContext;
	private boolean _isWindows;
	private Image _envImage;

	private static int FILES_ONLY = 0;
	private static int FOLDERS_ONLY = 1;
	private static int FILES_AND_FOLDERS = 2;

	/**
	 * Constructor
	 */
	public CommandEntryContentAssistProcessor(CommandEntryViewerConfiguration configurator)
	{
		_configurator = configurator;
		_provider = new SystemTableViewProvider();
		_imageMap = new HashMap();
	}

	public void setRemoteCommand(IRemoteCommandShell cmd)
	{
		if (cmd != _remoteCommand)
		{

			_remoteCommand = cmd;
			if (_remoteCommand != null)
			{
				RemoteCmdSubSystem cmdSubsystem = (RemoteCmdSubSystem) _remoteCommand.getCommandSubSystem();
				String type = cmdSubsystem.getHost().getSystemType();
				if (cmdSubsystem.isWindows())
				{
					_isWindows = true;
					_fileSeparator = '\\';
					_foreignFileSeparator = '/';
				}
				else
				{
					_isWindows = false;
					_fileSeparator = '/';
					_foreignFileSeparator = '\\';
				}

				_remoteCommand.getCandidateCommands();
			}
		}
	}

	public IRemoteCommandShell getRemoteCommand()
	{
		return _remoteCommand;
	}

	private boolean isDeliminator(char c)
	{
		if (c == ' ' || c == ';' || c == ':' || c == '&' || c == '|' || c == '=')
			return true;
		return false;
	}

	private String getCurrentText(ITextViewer viewer, int documentOffset)
	{
		StringBuffer currentText = new StringBuffer();
		String text = viewer.getDocument().get();
		if (documentOffset > -1)
		{
			boolean noWhiteSpace = true;
			int firstChar = documentOffset - 1;
			while (firstChar > -1 && noWhiteSpace)
			{
				char c = text.charAt(firstChar);
				if (isDeliminator(c))
				{
					noWhiteSpace = false;
				}
				else
				{
					currentText.insert(0, c);
				}
				firstChar--;
			}
		}
		return currentText.toString();
	}

	private boolean isAtFirstToken(IDocument document)
	{
		String text = document.get();
		if (text.length() > 0)
		{
			int spaceIndex = text.indexOf(' ');
			if (spaceIndex > 0)
			{
				return false;
			}
		}

		return true;
	}

	private String getFirstToken(IDocument document)
	{
		StringBuffer buf = new StringBuffer();
		String text = document.get();
		int index = 0;
		while (index < text.length())
		{
			char c = text.charAt(index);
			if (c == ' ')
			{
				return buf.toString();
			}
			else
			{
				buf.append(c);
			}
			index++;
		}
		return buf.toString();
	}

	/**
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset)
	{
		String text = viewer.getDocument().get();
		boolean atFirstToken = isAtFirstToken(viewer.getDocument());
		String firstToken = getFirstToken(viewer.getDocument());
		String currentText = getCurrentText(viewer, documentOffset);

		CompletionResults completions = getCompletions(firstToken, currentText, atFirstToken);
		if (completions != null && completions.size() > 0)
		{
			ICompletionProposal[] proposalList = new ICompletionProposal[completions.size()];

			int replacementOffset = documentOffset - currentText.length();
			int replacementLength = 0;

			replacementLength = documentOffset - replacementOffset;

			for (int idx = 0; idx < proposalList.length; idx++)
			{
				String name = completions.getNameAt(idx);
				String proposal = completions.getTextAt(idx);
				String help = completions.getHelpAt(idx);
				Image image = completions.getImageAt(idx);

				// @param replacementString the actual string to be inserted into the document
				// @param replacementOffset the offset of the text to be replaced
				// @param replacementLength the length of the text to be replaced
				// @param cursorPosition the position of the cursor following the insert relative to replacementOffset
				// @param image the image to display for this proposal
				// @param displayString the string to be displayed for the proposal
				// @param contentInformation the context information associated with this proposal
				// @param additionalProposalInfo the additional information associated with this proposal
				ContextInformation info = new ContextInformation(proposal, help);
				proposalList[idx] = new CompletionProposal(proposal, replacementOffset, replacementLength, documentOffset + proposal.length(), image, name, info, help);

			}
			return proposalList;
		}
		return new ICompletionProposal[0];
	}

	private Image getImageFor(Object object)
	{
		ImageDescriptor descriptor = null;
		Image image = null;
		if (object instanceof ICandidateCommand)
		{
			ICandidateCommand cmd = (ICandidateCommand) object;
			descriptor = cmd.getImageDescriptor();
			if (descriptor == null)
			{
				String type = cmd.getType();
				if (type.equals(ISystemOutputRemoteTypes.TYPE_FILE))
				{
					descriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE); 
				}
				else
				{
				    descriptor = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_RUN_ID);
				}
			}
		}
		else if (object instanceof IAdaptable)
		{
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) object).getAdapter(ISystemViewElementAdapter.class);
			descriptor = adapter.getImageDescriptor(object);
		}

		image = (Image) _imageMap.get(descriptor);
		if (image == null)
		{
			image = descriptor.createImage();
			_imageMap.put(descriptor, image);
		}
		return image;
	}

	private Image getEnvironmentVariableImage()
	{
		if (_envImage == null)
		{
			_envImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_ENVVAR_ID).createImage();
		}
		return _envImage;
	}

	private CompletionResults getCompletions(String firstToken, String currentText, boolean atFirstToken)
	{
		CompletionResults results = new CompletionResults();
		if (_remoteCommand != null && _remoteCommand.isActive())
		{		   		            		            
		    Object context =  _remoteCommand.getContext();
		    if (context instanceof IRemoteFile)
		    {
	
		        IRemoteFile workingDirectory = (IRemoteFile)context;
				if (workingDirectory != null)
				{
					int separatorIndex = currentText.lastIndexOf(_fileSeparator);
					int foreignseparatorIndex = currentText.lastIndexOf(_foreignFileSeparator);
					if (foreignseparatorIndex > separatorIndex)
					{
						separatorIndex = foreignseparatorIndex;
					}
	
					if (separatorIndex > 0)
					{
						String previousText = currentText.substring(0, separatorIndex + 1).replace(_foreignFileSeparator, _fileSeparator);
						String nextText = "";
						if (separatorIndex < currentText.length())
						{
							nextText = currentText.substring(separatorIndex + 1, currentText.length());
						}
	
						IRemoteFile contextDirectory = null;
						String contextPath = null;
						if (!_isWindows && currentText.charAt(0) == _fileSeparator)
						{
							contextPath = previousText;
						}
						else
						{
							contextPath = workingDirectory.getAbsolutePath() + _fileSeparator + previousText;
						}
	
						if (_lastFolderContext != null && _lastFolderContext.getAbsolutePath().equals(contextPath))
						{
							contextDirectory = _lastFolderContext;
						}
						else
						{
							try
							{
								contextDirectory = RemoteFileUtility.getFileSubSystem(_remoteCommand.getCommandSubSystem().getHost()).getRemoteFileObject(contextPath);
								_lastFolderContext = contextDirectory;
							}
							catch (Exception e)
							{
							}
						}
						getFileCompletions(contextDirectory, previousText, nextText, results, FILES_AND_FOLDERS);
	
					}
					else
					{
						if (atFirstToken)
						{
							if (!_isWindows && currentText.length() > 0 && currentText.charAt(0) == _fileSeparator)
							{
								getAbsoluteFileCompletions("", currentText, results, FILES_AND_FOLDERS);
							}
							else
							{
								getFileCompletions(workingDirectory, "", currentText, results, FILES_ONLY);
								getCommandCompletions(currentText, results);
							}
						}
						else
						{
							int flag = FILES_AND_FOLDERS;
	
							if (firstToken.equals("cd"))
							{
								flag = FOLDERS_ONLY;
							}
							else
							{
								if (_isWindows)
								{
									if (firstToken.equals("set") || currentText.startsWith("%"))
									{
										getEnvironmentCompletions(currentText, results);
										return results;
									}
								}
								else if (firstToken.equals("export") || currentText.startsWith("$"))
								{
									getEnvironmentCompletions(currentText, results);
									return results;
								}
							}
	
							getFileCompletions(workingDirectory, "", currentText, results, flag);
						}
					}
				}
		    }
		    else
		    {
		       getCommandCompletions(currentText, results);
		    }
		}

		return results;
	}

	private void getEnvironmentCompletions(String currentText, CompletionResults results)
	{
		IRemoteCmdSubSystem subsystem = _remoteCommand.getCommandSubSystem();
		if (subsystem instanceof IRemoteCmdSubSystem)
		{
			IRemoteCmdSubSystem cmdSubsystem = (IRemoteCmdSubSystem) subsystem;
			List vars = cmdSubsystem.getHostEnvironmentVariables();
			if (vars != null)
			{
				for (int i = 0; i < vars.size(); i++)
				{
					String var = (String) vars.get(i);
					int eqSepIndex = var.indexOf('=');
					String name = var.substring(0, eqSepIndex);
					String value = var.substring(eqSepIndex + 1, var.length());
					boolean hasDollars = currentText.startsWith("$");
					String compareName = name;
					if (hasDollars)
					{
						compareName = "$" + name;
					}
					boolean hasPercent = currentText.startsWith("%");
					if (hasPercent)
					{
						compareName = "%" + name + "%";
					}

					if (compareName.toLowerCase().startsWith(currentText.toLowerCase()))
					{

						results.addResult(name, compareName, value, getEnvironmentVariableImage());
					}
				}
			}
		}
	}

	private void getCommandCompletions(String currentText, CompletionResults results)
	{
		IRemoteCmdSubSystem subsystem = _remoteCommand.getCommandSubSystem();
		if (subsystem instanceof IRemoteCmdSubSystem)
		{
			IRemoteCmdSubSystem cmdSubsystem = (IRemoteCmdSubSystem) subsystem;
			ICandidateCommand[] cmds = cmdSubsystem.getCandidateCommands(_remoteCommand);
			if (cmds != null)
			{
				for (int i = 0; i < cmds.length; i++)
				{
					ICandidateCommand cmd = cmds[i];
					String name = cmd.getName();
					if (name.toLowerCase().startsWith(currentText.toLowerCase()))
					{

						results.addResult(name, name, cmd.getDescription(), getImageFor(cmd));
					}
				}
			}
		}
	}

	private void getAbsoluteFileCompletions(String prefix, String currentText, CompletionResults results, int flag)
	{
		if (!_isWindows)
		{
			int lastSlashIndex = currentText.lastIndexOf("/");
			String parentPath = currentText.substring(0, lastSlashIndex);
			IRemoteFileSubSystem fs = RemoteFileUtility.getFileSubSystem(_remoteCommand.getCommandSubSystem().getHost());
			try
			{
				IRemoteFile parent = fs.getRemoteFileObject(parentPath);
				
				 Object[] fileList = null;
				 
				 if (parent.hasContents(RemoteChildrenContentsType.getInstance()))
				 {
				 	fileList = parent.getContents(RemoteChildrenContentsType.getInstance());
				 }
				 else
				 {
				 	fileList = parent.getParentRemoteFileSubSystem().listFoldersAndFiles(parent, currentText + "*");
				 }
			

				for (int f1 = 0; f1 < fileList.length; f1++)
				{
					if (fileList[f1] instanceof IRemoteFile)
					{
						RemoteFile file = (RemoteFile) fileList[f1];
						if ((flag == FILES_ONLY && file.isFile()) || (flag == FOLDERS_ONLY && file.isDirectory()) || (flag == FILES_AND_FOLDERS))
						{
							String name = file.getName();
							String proposal = prefix + name;

							if (name.startsWith(currentText))
							{
								results.addResult(name, proposal, file.getAbsolutePath(), getImageFor(file));
							}
							else if (_isWindows && name.toLowerCase().startsWith(currentText.toLowerCase()))
							{
								results.addResult(name, proposal, file.getAbsolutePath(), getImageFor(file));
							}
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	private void getFileCompletions(IRemoteFile workingDirectory, String prefix, String currentText, CompletionResults results, int flag)
	{
		
		 Object[] fileList = null;
		 
		 if (workingDirectory.hasContents(RemoteChildrenContentsType.getInstance()) && !workingDirectory.isStale())
		 {
			if (flag == FILES_ONLY)
			{
				fileList = workingDirectory.getContents(RemoteFileChildrenContentsType.getInstance());
			}			
			else if (flag == FOLDERS_ONLY)
			{
				fileList = workingDirectory.getContents(RemoteFolderChildrenContentsType.getInstance());
			}
			else
			{
				fileList = workingDirectory.getContents(RemoteChildrenContentsType.getInstance());
			}
		 }
		 else
		 {
		 	fileList = workingDirectory.getParentRemoteFileSubSystem().listFoldersAndFiles(workingDirectory, currentText + "*");
		 }
			//_provider.getChildren(workingDirectory);

		if (fileList != null && fileList.length > 0)
		{
			for (int f1 = 0; f1 < fileList.length; f1++)
			{
				if (fileList[f1] instanceof IRemoteFile)
				{
					RemoteFile file = (RemoteFile) fileList[f1];
					if ((flag == FILES_ONLY && file.isFile()) || (flag == FOLDERS_ONLY && file.isDirectory()) || (flag == FILES_AND_FOLDERS))
					{
						String name = file.getName();
						String proposal = prefix + name;

						if (name.startsWith(currentText))
						{
							results.addResult(name, proposal, file.getAbsolutePath(), getImageFor(file));
						}
						else if (_isWindows && name.toLowerCase().startsWith(currentText.toLowerCase()))
						{
							results.addResult(name, proposal, file.getAbsolutePath(), getImageFor(file));
						}
					}
				}
			}
		}
	}

	/*
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset)
	{
		return null;
	}

	/*
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters()
	{
		return _configurator.getCompletionProposalAutoActivationCharacters();
	}

	/*
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters()
	{
		return null;
	}

	/*
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage()
	{
		return null;
	}

	/*
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator()
	{
		return null;
	}

}