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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.model.ICProject;
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
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.PDOMCodeReaderFactory;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
	
	public IASTTranslationUnit getASTTranslationUnit(ITranslationUnit file, int style) {
		IResource resource = file.getResource();
		ICProject project = file.getCProject();
		IProject rproject = project.getProject();
		
		IScannerInfo scanInfo = null;
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(rproject);
		if (provider != null){
			IResource infoResource = resource != null ? resource : rproject; 
			IScannerInfo buildScanInfo = provider.getScannerInformation(infoResource);
			if (buildScanInfo != null)
				scanInfo = buildScanInfo;
			else if ((style & ILanguage.AST_SKIP_IF_NO_BUILD_INFO) != 0)
				return null;
			else
				scanInfo = new ScannerInfo();
		}
		
		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project).getAdapter(PDOM.class);
		ICodeReaderFactory fileCreator;
		if ((style & (ILanguage.AST_SKIP_INDEXED_HEADERS | ILanguage.AST_SKIP_ALL_HEADERS)) != 0)
			fileCreator = new PDOMCodeReaderFactory(pdom);
		else
			fileCreator = SavedCodeReaderFactory.getInstance();

		CodeReader reader;
		IFile rfile = (IFile)file.getResource();
		if (file instanceof IWorkingCopy) {
			// get the working copy contents
			reader = new CodeReader(((IWorkingCopy)file).getOriginalElement().getPath().toOSString(), file.getContents());
		} else {
			String path
				= rfile != null
				? rfile.getLocation().toOSString()
				: file.getPath().toOSString();
			reader = fileCreator.createCodeReaderForTranslationUnit(path);
			if (reader == null)
				return null;
		}
		
	    IScannerExtensionConfiguration scannerExtensionConfiguration
	    	= C_GNU_SCANNER_EXTENSION;
	    
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
		IResource resource = workingCopy.getResource();
		ICProject project = workingCopy.getCProject();
		IProject rproject = project.getProject();
		
		IScannerInfo scanInfo = null;
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(rproject);
		if (provider != null){
			IResource infoResource = resource != null ? resource : rproject; 
			IScannerInfo buildScanInfo = provider.getScannerInformation(infoResource);
			if (buildScanInfo != null)
				scanInfo = buildScanInfo;
			else
				scanInfo = new ScannerInfo();
		}
		
		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project).getAdapter(PDOM.class);
		ICodeReaderFactory fileCreator = new PDOMCodeReaderFactory(pdom);

		String path
			= resource != null
			? resource.getLocation().toOSString()
			: workingCopy.getOriginalElement().getPath().toOSString();
		CodeReader reader = new CodeReader(path, workingCopy.getContents());
	    IScannerExtensionConfiguration scannerExtensionConfiguration
	    	= C_GNU_SCANNER_EXTENSION;
		IScanner scanner = new DOMScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE,
                ParserLanguage.C, ParserFactory.createDefaultLogService(), scannerExtensionConfiguration, fileCreator );
		scanner.setContentAssistMode(offset);
		
		ISourceCodeParser parser = new GNUCSourceParser(
				scanner,
				ParserMode.COMPLETION_PARSE,
				ParserUtil.getParserLogService(),
				new GCCParserExtensionConfiguration());
		
		// Run the parse and return the completion node
		parser.parse();
		ASTCompletionNode node = parser.getCompletionNode();
		if (node != null) {
			node.count = scanner.getCount();
		}
		return node;
	}

	private static class NameCollector extends CASTVisitor {
        {
            shouldVisitNames = true;
        }
        private List nameList = new ArrayList();
        public int visit( IASTName name ){
            nameList.add( name );
            return PROCESS_CONTINUE;
        }
        public IASTName[] getNames() {
        	return (IASTName[])nameList.toArray(new IASTName[nameList.size()]);
        }
	}
	
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		IASTNode selectedNode = ast.selectNodeForLocation(ast.getFilePath(), start, length);
		
		if (selectedNode == null)
			return new IASTName[0];
		
		if (selectedNode instanceof IASTName)
			return new IASTName[] { (IASTName)selectedNode };
		
		NameCollector collector = new NameCollector();
		selectedNode.accept(collector);
		return collector.getNames();
	}
	
	public IContributedModelBuilder createModelBuilder(ITranslationUnit tu) {
		// Use the default CDT model builder
		return null;
	}
}
