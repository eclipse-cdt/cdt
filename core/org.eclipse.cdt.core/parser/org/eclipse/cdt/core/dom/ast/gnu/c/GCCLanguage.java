/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.ast.gnu.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.pdom.PDOMCodeReaderFactory;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author Doug Schaefer
 *
 */
public class GCCLanguage extends PlatformObject implements ILanguage {

	protected static final GCCScannerExtensionConfiguration C_GNU_SCANNER_EXTENSION = new GCCScannerExtensionConfiguration();
	// Must match the id in the extension
	public static final String ID = CCorePlugin.PLUGIN_ID + ".gcc"; //$NON-NLS-1$ 

	public String getId() {
		return ID; 
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == IPDOMLinkageFactory.class)
			return new PDOMCLinkageFactory();
		else
			return super.getAdapter(adapter);
	}
	
	public IASTTranslationUnit getTranslationUnit(IStorage file, IProject project, int style) {
		return getTranslationUnit(file.getFullPath().toOSString(), project, project, style, null);
	}
	
	public IASTTranslationUnit getTranslationUnit(IFile file, int style) {
		return getTranslationUnit(file.getLocation().toOSString(), file.getProject(), file, style, null);
	}
	
	public IASTTranslationUnit getTranslationUnit(IWorkingCopy workingCopy, int style) {
		IFile file = (IFile)workingCopy.getResource();
		String path = file.getLocation().toOSString();
		CodeReader reader = new CodeReader(path, workingCopy.getContents());
		return getTranslationUnit(path, file.getProject(), file, style, reader);
	}
	
	protected IASTTranslationUnit getTranslationUnit(String path, IProject project, IResource infoResource, int style,
			CodeReader reader) {
		IScannerInfo scanInfo = null;
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null){
			IScannerInfo buildScanInfo = provider.getScannerInformation(infoResource);
			if (buildScanInfo != null)
				scanInfo = buildScanInfo;
			else if ((style & ILanguage.AST_SKIP_IF_NO_BUILD_INFO) != 0)
				return null;
			else
				scanInfo = new ScannerInfo();
		}
		
		IPDOM pdom = PDOM.getPDOM(project);
		ICodeReaderFactory fileCreator;
		if ((style & ILanguage.AST_SKIP_INDEXED_HEADERS) != 0)
			fileCreator = new PDOMCodeReaderFactory((PDOMDatabase)pdom);
		else
			fileCreator = SavedCodeReaderFactory.getInstance();

		if (reader == null) {
			reader = fileCreator.createCodeReaderForTranslationUnit(path);
	        if( reader == null )
	            return null;
		}
		
	    IScannerExtensionConfiguration scannerExtensionConfiguration =
	       scannerExtensionConfiguration = C_GNU_SCANNER_EXTENSION;
	    
		IScanner scanner = new DOMScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE,
                ParserLanguage.C, ParserFactory.createDefaultLogService(), scannerExtensionConfiguration, fileCreator );
	    //assume GCC
		ISourceCodeParser parser = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(),
				new GCCParserExtensionConfiguration()  );

	    // Parse
		IASTTranslationUnit ast = parser.parse();

		if ((style & AST_USE_INDEX) != 0) 
			ast.setIndex(pdom);

		return ast;
	}
	
	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy, int offset) {
		return null;
	}

	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		// Use the default CDT model builder
		return null;
	}
}
