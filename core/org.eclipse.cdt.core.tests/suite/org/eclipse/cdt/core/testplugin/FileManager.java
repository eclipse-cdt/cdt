/*
 * Created on Sep 8, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.core.testplugin;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FileManager {
	ArrayList fileHandles;
	
	public FileManager(){
		fileHandles = new ArrayList();
	}
	
	public void addFile(IFile file){
		fileHandles.add(file);
	}
	
	public void closeAllFiles() throws CoreException{
		Iterator iter = fileHandles.iterator();
		while (iter.hasNext()){
			IFile tempFile = (IFile) iter.next();
			tempFile.refreshLocal(IResource.DEPTH_INFINITE,null);
			
			try {
				tempFile.delete(true,null);
			} catch (CoreException e) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {

				}
				finally{
					tempFile.delete(true,null);
				}

			}
			
		}
	}
}