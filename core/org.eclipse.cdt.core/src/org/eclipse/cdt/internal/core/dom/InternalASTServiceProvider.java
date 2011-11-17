/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IParserConfiguration;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @deprecated
 */
@Deprecated
public class InternalASTServiceProvider implements IASTServiceProvider {

	protected static final GCCScannerExtensionConfiguration C_GNU_SCANNER_EXTENSION = GCCScannerExtensionConfiguration.getInstance();
    protected static final GPPScannerExtensionConfiguration CPP_GNU_SCANNER_EXTENSION = GPPScannerExtensionConfiguration.getInstance();
    private static final String[] dialects = {
   		"C99",   //$NON-NLS-1$
   		"C++98", //$NON-NLS-1$
   		"GNUC",  //$NON-NLS-1$
    	"GNUC++" //$NON-NLS-1$
   	};

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getName()
     */
    public String getName() {
    	// TODO is this a name or an id?
        return "CDT AST Service"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit()
     */
    @Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse) throws UnsupportedDialectException {
        return getTranslationUnit( fileToParse.getLocation().toOSString(), fileToParse, SavedCodeReaderFactory.getInstance(), null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.cdt.core.dom.ICodeReaderFactory)
     */
    @Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
        return getTranslationUnit( fileToParse.getLocation().toOSString(), fileToParse, fileCreator, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.cdt.core.dom.ICodeReaderFactory, org.eclipse.cdt.core.dom.IParserConfiguration)
     */
    @Override
	public IASTTranslationUnit getTranslationUnit(
            IFile fileToParse, ICodeReaderFactory fileCreator, IParserConfiguration configuration) throws UnsupportedDialectException {
		return getTranslationUnit( fileToParse.getLocation().toOSString(), fileToParse, fileCreator, configuration);
    }

    public IASTTranslationUnit getTranslationUnit(
            String filename, IResource infoProvider, ICodeReaderFactory fileCreator, IParserConfiguration configuration) throws UnsupportedDialectException
    {
        IProject project = infoProvider.getProject();
		IScannerInfo scanInfo = null;

		if( configuration == null )
		{
			IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
			if (provider != null){
                IScannerInfo buildScanInfo = provider.getScannerInformation(infoProvider);

                if (buildScanInfo != null)
                    scanInfo = buildScanInfo;
                else
                    scanInfo = new ScannerInfo();
			}
		}
		else
		    scanInfo = configuration.getScannerInfo();

		CodeReader reader = fileCreator.createCodeReaderForTranslationUnit(filename);
        if( reader == null )
            return null;
		IScanner scanner = null;
		ISourceCodeParser parser = null;
		boolean[] isSource= {false};

		if( configuration == null || configuration.getParserDialect() == null)
		{
		    ParserLanguage l = getLanguage(filename, project, isSource);
		    IScannerExtensionConfiguration scannerExtensionConfiguration;
		    if( l == ParserLanguage.CPP )
		       scannerExtensionConfiguration = CPP_GNU_SCANNER_EXTENSION;
		    else
		       scannerExtensionConfiguration = C_GNU_SCANNER_EXTENSION;
			scanner= createScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE, l, ParserFactory.createDefaultLogService(),
					scannerExtensionConfiguration, fileCreator);
		    //assume GCC
		    if( l == ParserLanguage.C )
		        parser = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(), new GCCParserExtensionConfiguration()  );
		    else
		        parser = new GNUCPPSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(), new GPPParserExtensionConfiguration() );
		}
		else
		{
		    String dialect = configuration.getParserDialect();
		    if( dialect.equals( dialects[0]) || dialect.equals( dialects[2]))
				scanner= createScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE, ParserLanguage.C,
						ParserUtil.getScannerLogService(), C_GNU_SCANNER_EXTENSION, fileCreator);
		    else if( dialect.equals( dialects[1] ) || dialect.equals( dialects[3] ))
			    scanner = createScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP,
						ParserUtil.getScannerLogService(), CPP_GNU_SCANNER_EXTENSION, fileCreator);
		    else
		        throw new UnsupportedDialectException();

		    if( dialect.equals( dialects[0]))
		    {
		        ICParserExtensionConfiguration config = new ANSICParserExtensionConfiguration();
		        parser = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(), config );
		    }
		    else if( dialect.equals( dialects[1] ))
		    {
		        ICPPParserExtensionConfiguration config = new ANSICPPParserExtensionConfiguration();
		        parser = new GNUCPPSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(), config );
		    }
		    else if( dialect.equals( dialects[2]))
		    {
		        ICParserExtensionConfiguration config = new GCCParserExtensionConfiguration();
		        parser = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(), config );
		    }
		    else if( dialect.equals( dialects[3]))
		    {
		        ICPPParserExtensionConfiguration config = new GPPParserExtensionConfiguration();
		        parser = new GNUCPPSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(), config );
		    }
		}
		// Parse
		if (parser != null) {
			IASTTranslationUnit ast= parser.parse();
			ast.setIsHeaderUnit(!isSource[0]);
			return ast;
		}
		return null;
    }

	@Override
	public IASTCompletionNode getCompletionNode(IStorage fileToParse, IProject project, int offset,
            ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
        return getCompletionNode(fileToParse.getFullPath().toOSString(), project, offset, fileCreator);
    }

    @Override
	public IASTCompletionNode getCompletionNode(IFile fileToParse, int offset,
            ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
        return getCompletionNode(fileToParse.getLocation().toOSString(), fileToParse, offset, fileCreator);
    }

    public IASTCompletionNode getCompletionNode(String filename, IResource infoProvider, int offset,
            ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
		// Get the scanner info
		IScannerInfo scanInfo = null;
        IProject project = infoProvider.getProject();

		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null) {
			IScannerInfo buildScanInfo = provider.getScannerInformation(infoProvider);
			if (buildScanInfo != null)
				scanInfo = buildScanInfo;
			else
				scanInfo = new ScannerInfo();
		}

		CodeReader reader = fileCreator.createCodeReaderForTranslationUnit(filename);

		boolean[] isSource= {false};
		ParserLanguage l = getLanguage(filename, project, isSource);
		IScannerExtensionConfiguration scannerExtensionConfiguration = null;
		if (l == ParserLanguage.CPP)
			scannerExtensionConfiguration = CPP_GNU_SCANNER_EXTENSION;
		else
			scannerExtensionConfiguration = C_GNU_SCANNER_EXTENSION;

		IScanner scanner= createScanner(reader, scanInfo, ParserMode.COMPLETION_PARSE, l,
				ParserFactory.createDefaultLogService(), scannerExtensionConfiguration, fileCreator);
		scanner.setContentAssistMode(offset);

		// assume GCC
		ISourceCodeParser parser = null;
		if (l == ParserLanguage.C)
			parser = new GNUCSourceParser(scanner, ParserMode.COMPLETION_PARSE,
					ParserUtil.getParserLogService(),
					new GCCParserExtensionConfiguration());
		else
			parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETION_PARSE,
					ParserUtil.getParserLogService(),
					new GPPParserExtensionConfiguration());

		// Run the parse and return the completion node
		parser.parse();
		IASTCompletionNode node = parser.getCompletionNode();
		return node;
	}

	private IScanner createScanner(CodeReader reader, IScannerInfo scanInfo,
			ParserMode mode, ParserLanguage lang, IParserLogService log,
			IScannerExtensionConfiguration scanConfig, ICodeReaderFactory fileCreator) {
		return new CPreprocessor(FileContent.adapt(reader), scanInfo, lang, log, scanConfig, IncludeFileContentProvider.adapt(fileCreator));
	}

    /*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getSupportedDialects()
	 */
    public String[] getSupportedDialects() {
        return dialects;
    }

    private ParserLanguage getLanguage( String filename, IProject project, boolean[] isSource ) {
    	//FIXME: ALAIN, for headers should we assume CPP ??
    	// The problem is that it really depends on how the header was included.
    	String id = null;
    	isSource[0]= false;
    	IContentType contentType = CCorePlugin.getContentType(project, filename);
    	if (contentType != null) {
    		id = contentType.getId();
    	}
    	if (id != null) {
    		if (CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(id)) {
    			isSource[0]= true;
    			return ParserLanguage.CPP;
    		} else if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)) {
    			return ParserLanguage.CPP;
    		} else if (CCorePlugin.CONTENT_TYPE_CHEADER.equals(id)) {
    			return ParserLanguage.C;
    		} else if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id)) {
    			isSource[0]= true;
    			return ParserLanguage.C;
    		} else if (CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(id)) {
    			// ???
    			// What do we do here ?
    		}
    	}
		return ParserLanguage.CPP;
    }

    @Override
	public IASTTranslationUnit getTranslationUnit(IStorage fileToParse, IProject project, ICodeReaderFactory fileCreator) throws UnsupportedDialectException{
        return getTranslationUnit( fileToParse.getFullPath().toOSString(), project, fileCreator, null);
    }

    @Override
	public IASTTranslationUnit getTranslationUnit(IStorage fileToParse, IProject project) throws UnsupportedDialectException {
        return getTranslationUnit( fileToParse.getFullPath().toOSString(), project, SavedCodeReaderFactory.getInstance(), null);
    }

	@Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse, boolean parseComments) throws UnsupportedDialectException {
		return getTranslationUnit( fileToParse.getLocation().toOSString(), fileToParse, SavedCodeReaderFactory.getInstance(), null);
	}

	@Override
	public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator, boolean parseComments) throws UnsupportedDialectException {
		return getTranslationUnit( fileToParse.getLocation().toOSString(), fileToParse, fileCreator, null);
	}
}
