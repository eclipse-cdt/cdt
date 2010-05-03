/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;


import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;

import junit.framework.TestCase;


public class AbstractLRHangingTest  extends TestCase{
	

	
	
	public AbstractLRHangingTest() {
	}

	public AbstractLRHangingTest(String name) {
		super(name);
	}
	
	
	

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}
	
	protected void runThreadByLimitedTime(long limitTime, Thread testThread)throws Exception{
		testThread.start();
		testThread.join(limitTime);

		if (testThread.isAlive()) {
			testThread.interrupt();
			fail("timeout by " + limitTime / 1000 + " seconds"); //$NON-NLS-2$
		} else {
			// Finished
		}
	}
	
	// 1mins
	public static long THREAD_TIMEOUT_LIMIT = 1 * 60 * 1000;
	public static String CONTENT_ASIST_CURSOR =" /*<ctrl-space>*/ ";
	
	private String errMsg="";
	
	public void resetErrMsg(){
		errMsg="";
	}
	
	public void setErrMsg(String errMsg){
		this.errMsg=errMsg;
	}
	
	
	protected void runTestCase(final String code, final ILanguage language)throws Exception{
		
		
		Thread testThread = new Thread() {
			
			
			public void run() {
				
				String errMsg="";
				resetErrMsg();
				String msg=null;
				int offset = code.indexOf(CONTENT_ASIST_CURSOR);
				int index=0;
				while(offset >=0){
					
					IASTCompletionNode node = null;
					try {
						node = ParseHelper.getCompletionNode(code, language, offset);
					} catch (Exception e) {
						if(errMsg.length()==0){
							errMsg = "caught an exception when the code is parsed for cursor number " + index;
						}else{
							errMsg = errMsg + "\n" +  "caught an exception when the code is parsed for cursor number " + index;
						}
	
					}
					if(node == null){
						if(errMsg.length()==0){
							errMsg = "return completion node is null when the code is parsed for cursor number " + index;
						}else{
							errMsg = errMsg + "\n" + "return completion node is null when the code is parsed for cursor number " + index;
						}
						
					}
					offset = code.indexOf(CONTENT_ASIST_CURSOR, offset + 1);
					index++;
				}
				setErrMsg(errMsg);
				
			}

		};

		runThreadByLimitedTime(THREAD_TIMEOUT_LIMIT, testThread);
		if(errMsg.length()>0){
			fail(errMsg);
		}
		
	}
	
	
	

	
	

}
