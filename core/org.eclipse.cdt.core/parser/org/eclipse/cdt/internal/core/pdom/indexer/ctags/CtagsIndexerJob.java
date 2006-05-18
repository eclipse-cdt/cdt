/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.ctags;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public abstract class CtagsIndexerJob extends Job {

	protected final CtagsIndexer indexer;
	protected final PDOM pdom;
	
	public CtagsIndexerJob(CtagsIndexer indexer) throws CoreException {
		super("ctags Indexer: " + indexer.getProject().getElementName());
		this.indexer = indexer;
		this.pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM();
		setRule(CCorePlugin.getPDOMManager().getIndexerSchedulingRule());
	}

	// Indexing functions
	void runCtags(IPath sourcePath) {
		String ctagsFileName = indexer.getResolvedCtagsFileName();
		File ctagsFile = new File(ctagsFileName);
		if (ctagsFile.exists())
			ctagsFile.delete();
		
    	String[] cmd = new String[] {
    			indexer.getResolvedCtagsCommand(),
    			"--excmd=number", //$NON-NLS-1$
		        "--format=2", //$NON-NLS-1$
				"--sort=no",  //$NON-NLS-1$
				"--fields=aiKlmnsSz", //$NON-NLS-1$
				"--c-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--c++-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--languages=c,c++", //$NON-NLS-1$
				"-f", //$NON-NLS-1$
				ctagsFileName,
				"-R",  //$NON-NLS-2$
				sourcePath.toOSString()  // Give absolute path so that tag file entries will be absolute
    	};
    	
    	try {
    		// Run ctags
    		Process p = Runtime.getRuntime().exec(cmd);
    		p.waitFor();
    		
    		// Parse the ctags file
    		pdom.acquireWriteLock();
    		try {
    			processCtagsFile(ctagsFileName);
    		} finally {
    			pdom.releaseWriteLock();
    		}
    	} catch (InterruptedException e) {
    	    return;
        } catch (IOException e) {
        	CCorePlugin.log(e);
        	return;
        } catch (CoreException e) {
        	CCorePlugin.log(e);
        	return;
        }    	
	}
	
	private void processCtagsFile(String ctagsFileName) throws IOException, CoreException {
		BufferedReader reader = new BufferedReader(new FileReader(ctagsFileName));
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (line.charAt(0) == '!')
				// skip over header
				continue;
			
			String elementName = null;
			String fileName = null;
			int lineNum = -1;
			Map fields = new HashMap();
			
			StringTokenizer tokenizer = new StringTokenizer(line, "\t"); //$NON-NLS-1$
			for (int state = 0; tokenizer.hasMoreTokens(); ++state) {
				String token = tokenizer.nextToken();
				switch (state) {
				case 0:
					// element name
					elementName = token;
					break;
				case 1:
					// file name
					fileName = token;
					break;
				case 2:
					// line number
					try {
						token = token.trim();
						int i = token.indexOf(';');
						lineNum = Integer.parseInt(token.substring(0, i)) - 1; // Make it 0 based
					} catch (NumberFormatException e) {
						// Not sure what the line number is.
						lineNum = -1;
					}
					break;

				default:
					// extension field
					int i = token.indexOf(':');
					if (i != -1) {
						String key = token.substring(0, i);
						String value = token.substring(i + 1);
						fields.put(key, value);
					}
				}
			}
			
			if (elementName != null && fileName != null) {
				PDOMFile file = pdom.addFile(fileName);
				String languageName = (String)fields.get("language"); //$NON-NLS-1$
				if (languageName.equals("C++")) { //$NON-NLS-1$
					PDOMLinkage linkage = pdom.getLinkage(new GPPLanguage());
					new CtagsCPPName(linkage, fileName, lineNum, elementName, fields).addToPDOM(file);
				} else {
					PDOMLinkage linkage = pdom.getLinkage(new GCCLanguage());
					new CtagsCName(linkage, fileName, lineNum, elementName, fields).addToPDOM(file);
				}
			}
		}
	}

}
