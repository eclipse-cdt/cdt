/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;

import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/**
 * A proposal computer for include directives.
 *
 * @since 5.0
 */
public class InclusionProposalComputer implements ICompletionProposalComputer {

	private String fErrorMessage;

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		List<ICompletionProposal> proposals= Collections.emptyList();
		fErrorMessage= null;
		
		if (context instanceof CContentAssistInvocationContext) {
			CContentAssistInvocationContext cContext= (CContentAssistInvocationContext) context;
			if (inIncludeDirective(cContext)) {
				// add include file proposals
				proposals= new ArrayList<ICompletionProposal>();
				try {
					addInclusionProposals(cContext, proposals);
				} catch (Exception exc) {
					fErrorMessage= exc.getMessage();
					CUIPlugin.log(exc);
				}
			}
		}
		return proposals;
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return fErrorMessage;
	}

	@Override
	public void sessionEnded() {
	}

	@Override
	public void sessionStarted() {
	}

	/**
	 * Test whether the invocation offset is inside the file name part if an include directive.
	 * 
	 * @param context  the invocation context
	 * @return <code>true</code> if the invocation offset is inside or before the directive keyword
	 */
	private boolean inIncludeDirective(CContentAssistInvocationContext context) {
		IDocument doc = context.getDocument();
		int offset = context.getInvocationOffset();
		
		try {
			final ITypedRegion partition= TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING, offset, true);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				String ppPrefix= doc.get(partition.getOffset(), offset - partition.getOffset());
				if (ppPrefix.matches("\\s*#\\s*include\\s*[\"<][^\">]*")) { //$NON-NLS-1$
					// we are inside the file name part of the include directive
					return true;
				}
			}
			
		} catch (BadLocationException exc) {
		}
		return false;
	}

	private void addInclusionProposals(CContentAssistInvocationContext context, List<ICompletionProposal> proposals) throws Exception {
		if (context.isContextInformationStyle()) {
			return;
		}
		final ITranslationUnit tu= context.getTranslationUnit();
		if (tu == null) {
			return;
		}
		String prefix;
		boolean angleBrackets= false;
		prefix = computeIncludePrefix(context);
		if (prefix.length() > 0) {
			angleBrackets= prefix.charAt(0) == '<';
			prefix= prefix.substring(1);
		}
		IPath prefixPath= new Path(prefix);
		String[] potentialIncludes= collectIncludeFiles(tu, prefixPath, angleBrackets);
		if (potentialIncludes.length > 0) {
			IInclude[] includes= tu.getIncludes();
			Set<String> alreadyIncluded= new HashSet<String>();
			for (IInclude includeDirective : includes) {
				alreadyIncluded.add(includeDirective.getElementName());
			}
			Image image = getImage(CElementImageProvider.getIncludeImageDescriptor());
			for (String include : potentialIncludes) {
				if (alreadyIncluded.add(include)) {
					final char openingBracket= angleBrackets ? '<' : '"';
					final char closingBracket= angleBrackets ? '>' : '"';
					String repString= openingBracket + include;
					final String dispString= repString + closingBracket;
					int repLength = prefix.length() + 1;
					int repOffset= context.getInvocationOffset() - repLength;
					final boolean needClosingBracket= context.getDocument().getChar(repOffset + repLength) != closingBracket;
					if (needClosingBracket) {
						repString += closingBracket;
					}
					final boolean isDir= include.endsWith("/"); //$NON-NLS-1$
					final int relevance= computeRelevance(prefix, include) + (isDir ? 0 : 1);
					final CCompletionProposal proposal= createProposal(repOffset, repLength, repString, dispString, image, relevance, context);
					if (!isDir && !needClosingBracket) {
						// put cursor behind closing bracket
						proposal.setCursorPosition(repString.length() + 1);
					}
					proposals.add(proposal);
				}
			}
		}
	}
	
	/**
	 * Collect potential include files for the given translation unit.
	 * 
	 * @param tu  the translation unit to include the file
	 * @param prefixPath  the path part to match the sub-directory and file name
	 * @param angleBrackets  whether angle brackets enclose the include file name
	 * @return an array of incude file names
	 * @throws CoreException
	 */
	private String[] collectIncludeFiles(final ITranslationUnit tu, IPath prefixPath, boolean angleBrackets) throws CoreException {
		final List<String> includeFiles= new ArrayList<String>();
		if (!angleBrackets) {
			// search in current directory
			IResource resource= tu.getResource();
			if (resource != null) {
				IContainer parent= resource.getParent();
				collectIncludeFilesFromContainer(tu, parent, prefixPath, includeFiles);
			} else {
				IPath location= tu.getLocation();
				if (location != null) {
					collectIncludeFilesFromDirectory(tu, location.removeLastSegments(1), prefixPath, includeFiles);
				}
			}
		}
		IScannerInfo info= tu.getScannerInfo(true);
		if (info != null) {
			collectIncludeFilesFromScannerInfo(tu, info, prefixPath, angleBrackets, includeFiles);
		}
		return includeFiles.toArray(new String[includeFiles.size()]);
	}

	/**
	 * @param tu  the translation unit to include the file
	 * @param info  the scanner info for this translation unit
	 * @param prefixPath  the path part to match the sub-directory and file name
	 * @param angleBrackets  whether angle brackets enclose the include file name
	 * @param includeFiles  the result list
	 */
	private void collectIncludeFilesFromScannerInfo(ITranslationUnit tu, IScannerInfo info, IPath prefixPath, boolean angleBrackets, List<String> includeFiles) {
		if (!angleBrackets && info instanceof IExtendedScannerInfo) {
			IExtendedScannerInfo extendedInfo= (IExtendedScannerInfo) info;
			String[] quoteIncludes= extendedInfo.getLocalIncludePath();
			
			if (quoteIncludes != null) {
				for (String quoteInclude : quoteIncludes) {
					IPath includeDir= new Path(quoteInclude);
					collectIncludeFilesFromDirectory(tu, includeDir, prefixPath, includeFiles);
				}
			}
		}
		
		String[] allIncludes= info.getIncludePaths();
		for (String allInclude : allIncludes) {
			IPath includeDir= new Path(allInclude);
			collectIncludeFilesFromDirectory(tu, includeDir, prefixPath, includeFiles);
		}
	}

	/**
	 * Collect include files from the given file system directory.
	 * 
	 * @param tu  the translation unit to include the file
	 * @param directory  the file system path of the directory
	 * @param prefixPath  the path part to match the sub-directory and file name
	 * @param includeFiles  the result list
	 */
	private void collectIncludeFilesFromDirectory(ITranslationUnit tu, IPath directory, IPath prefixPath, List<String> includeFiles) {
		final String namePrefix;
		if (prefixPath.segmentCount() == 0) {
			namePrefix= ""; //$NON-NLS-1$
		} else if (prefixPath.hasTrailingSeparator()) {
			namePrefix= ""; //$NON-NLS-1$
			prefixPath= prefixPath.removeTrailingSeparator();
			directory= directory.append(prefixPath);
		} else {
			namePrefix= prefixPath.lastSegment();
			prefixPath= prefixPath.removeLastSegments(1);
			if (prefixPath.segmentCount() > 0) {
				directory= directory.append(prefixPath);
			}
		}
		final File fileDir = directory.toFile();
		if (!fileDir.exists()) {
			return;
		}
		final int prefixLength = namePrefix.length();
		final IProject project= tu.getCProject().getProject();
		File[] files= fileDir.listFiles();
		if (files == null) {
			return;
		}
		IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(namePrefix);
		for (File file : files) {
			final String name= file.getName();
			if (name.length() >= prefixLength && matcher.match(name.toCharArray())) {
				if (file.isFile()) {
					if (CoreModel.isValidCXXHeaderUnitName(project, name) || CoreModel.isValidCHeaderUnitName(project, name)) {
						includeFiles.add(prefixPath.append(name).toString());
					}
				} else if (file.isDirectory()) {
					includeFiles.add(prefixPath.append(name).addTrailingSeparator().toString());
				}
			}
		}
	}

	/**
	 * Collect include files from the given resource container.
	 * 
	 * @param tu  the translation unit to include the file
	 * @param parent  the resource container
	 * @param prefixPath  the path part to match the sub-directory and file name
	 * @param includeFiles  the result list
	 * @throws CoreException
	 */
	private void collectIncludeFilesFromContainer(final ITranslationUnit tu, IContainer parent, IPath prefixPath, final List<String> includeFiles) throws CoreException {
		final String namePrefix;
		if (prefixPath.segmentCount() == 0) {
			namePrefix= ""; //$NON-NLS-1$
		} else if (prefixPath.hasTrailingSeparator()) {
			namePrefix= ""; //$NON-NLS-1$
			prefixPath= prefixPath.removeTrailingSeparator();
		} else {
			namePrefix= prefixPath.lastSegment();
			prefixPath= prefixPath.removeLastSegments(1);
		}
		if (prefixPath.segmentCount() > 0) {
			IPath parentPath = parent.getFullPath().append(prefixPath);
			if (parentPath.segmentCount() > 1) {
				parent = parent.getFolder(prefixPath);
			} else if (parentPath.segmentCount() == 1) {
				parent = ResourcesPlugin.getWorkspace().getRoot().getProject(parentPath.lastSegment());
			} else {
				return;
			}
		}
		if (!parent.exists()) {
			return;
		}
		final IPath cPrefixPath= prefixPath;
		final int prefixLength = namePrefix.length();
		final IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(namePrefix);
		final IProject project= tu.getCProject().getProject();
		parent.accept(new IResourceProxyVisitor() {
			boolean fFirstVisit= true;
			@Override
			public boolean visit(IResourceProxy proxy) throws CoreException {
				final int type= proxy.getType();
				final String name= proxy.getName();
				if (fFirstVisit) {
					fFirstVisit= false;
					return true;
				}
				if (name.length() >= prefixLength && matcher.match(name.toCharArray())) {
					if (type == IResource.FILE) {
						if (CoreModel.isValidCXXHeaderUnitName(project, name) || CoreModel.isValidCHeaderUnitName(project, name)) {
							includeFiles.add(cPrefixPath.append(name).toString());
						}
					} else if (type == IResource.FOLDER) {
						includeFiles.add(cPrefixPath.append(name).addTrailingSeparator().toString());
					}
				}
				return false;
			}}, IResource.DEPTH_ONE);
	}

	/**
	 * Compute the file name portion in an incomplete include directive.
	 * 
	 * @param context
	 * @return the file name portion including the opening bracket or quote
	 * @throws BadLocationException
	 */
	private String computeIncludePrefix(CContentAssistInvocationContext context) throws BadLocationException {
		IDocument document= context.getDocument();
		if (document == null)
			return null;
		int end= context.getInvocationOffset();
		int start= end;
		while (--start >= 0) {
			final char ch= document.getChar(start);
			if (ch == '"' || ch == '<')
				break;
		}
		return document.get(start, end - start);
	}


	/**
	 * Compute base relevance depending on quality of name / prefix match.
	 * 
	 * @param prefix  the completion pefix
	 * @param match  the matching identifier
	 * @return a relevance value inidicating the quality of the name match
	 */
	protected int computeRelevance(String prefix, String match) {
		int baseRelevance= 0;
		boolean caseMatch= prefix.length() > 0 && match.startsWith(prefix);
		if (caseMatch) {
			baseRelevance += RelevanceConstants.CASE_MATCH_RELEVANCE;
		}
		return baseRelevance;
	}

	private CCompletionProposal createProposal(int repOffset, int repLength, String repString, String dispString, Image image, int relevance, CContentAssistInvocationContext context) {
		return new CCompletionProposal(repString, repOffset, repLength, image, dispString, dispString, relevance, context.getViewer());
	}

	private Image getImage(ImageDescriptor desc) {
		return desc != null ? CUIPlugin.getImageDescriptorRegistry().get(desc) : null;
	}
	
}
