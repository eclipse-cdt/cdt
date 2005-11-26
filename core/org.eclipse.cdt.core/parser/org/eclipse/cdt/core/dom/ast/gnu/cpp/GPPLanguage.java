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

package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ILanguage;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.pdom.dom.PDOMName;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class GPPLanguage implements ILanguage {

	protected static final GPPScannerExtensionConfiguration CPP_GNU_SCANNER_EXTENSION = new GPPScannerExtensionConfiguration();

	public int getId() {
		return GPP_ID;
	}
	
	public IASTTranslationUnit getTranslationUnit(ITranslationUnit tu, int style) {
		IFile file = (IFile)tu.getResource();
        IProject project = file.getProject();
		IScannerInfo scanInfo = null;
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null){
			IScannerInfo buildScanInfo = provider.getScannerInformation(file);
			if (buildScanInfo != null)
				scanInfo = buildScanInfo;
			else
				scanInfo = new ScannerInfo();
		}
		
		// TODO - use different factories if we are working copy, or style
		// is skip headers.
		ICodeReaderFactory fileCreator = SavedCodeReaderFactory.getInstance();
		CodeReader reader = fileCreator.createCodeReaderForTranslationUnit(tu);
        if( reader == null )
            return null;

	    IScannerExtensionConfiguration scannerExtensionConfiguration = CPP_GNU_SCANNER_EXTENSION;
	    
		IScanner scanner = new DOMScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE,
                ParserLanguage.CPP, ParserFactory.createDefaultLogService(), scannerExtensionConfiguration, fileCreator );
		ISourceCodeParser parser = new GNUCPPSourceParser( scanner, ParserMode.COMPLETE_PARSE, ParserUtil.getParserLogService(),
				new GPPParserExtensionConfiguration()  );

	    // Parse
		IASTTranslationUnit ast = parser.parse();

		if ((style & AST_USE_INDEX) != 0) 
			ast.setIndex(tu.getCProject().getIndex());
		
		return ast;
	}

	public ASTCompletionNode getCompletionNode(IWorkingCopy workingCopy,
			int offset) {
		return null;
	}

	public PDOMBinding getPDOMBinding(PDOMDatabase pdom, IASTName name) throws CoreException {
		try {
			IBinding binding = name.resolveBinding();
			if (binding == null)
				return null;
			
			IScope scope = binding.getScope();
			if (scope == null)
				return null;
	
			PDOMBinding pdomBinding = null;
			IASTName scopeName = scope.getScopeName();
			
			if (scopeName == null) {
				pdomBinding = new PDOMBinding(pdom, name, binding);
				new PDOMName(pdom, name, pdomBinding);
			} else {
				IBinding scopeBinding = scopeName.resolveBinding();
				if (scopeBinding instanceof IType) {
					pdomBinding = new PDOMBinding(pdom, name, binding);
					new PDOMName(pdom, name, pdomBinding);
				} 
			}
			return null;
		} catch (DOMException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					CCorePlugin.PLUGIN_ID, 0, "DOM Exception", e));
		}
	}
	
	public PDOMBinding createPDOMBinding(PDOMDatabase pdom, int bindingType) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
