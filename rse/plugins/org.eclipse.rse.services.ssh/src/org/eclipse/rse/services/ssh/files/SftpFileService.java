/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation 
 *******************************************************************************/

package org.eclipse.rse.services.ssh.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile; 
import org.eclipse.rse.services.ssh.ISshService;
import org.eclipse.rse.services.ssh.ISshSessionProvider;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;

public class SftpFileService extends AbstractFileService implements IFileService, ISshService
{
	//private SshConnectorService fConnector;
	private ISshSessionProvider fSessionProvider;
	private ChannelSftp fChannelSftp;
	private String fUserHome;
	
//	public SftpFileService(SshConnectorService conn) {
//		fConnector = conn;
//	}

	public SftpFileService(ISshSessionProvider sessionProvider) {
		fSessionProvider = sessionProvider;
	}
	
	public String getName() {
		return "Ssh / Sftp File Service";
	}
	
	public String getDescription() {
		return "Access a remote file system via Ssh / Sftp protocol";
	}
	
	public void connect() throws Exception {
		Session session = fSessionProvider.getSession();
	    Channel channel=session.openChannel("sftp"); //$NON-NLS-1$
	    channel.connect();
	    fChannelSftp=(ChannelSftp)channel;
		fUserHome = fChannelSftp.pwd();
	}
	
	public void disconnect() {
		fChannelSftp.disconnect();
		fChannelSftp = null;
	}
	
	public IHostFile getFile(IProgressMonitor monitor, String remoteParent, String fileName)
	{
		//TODO getFile() must return a dummy even for non-existent files,
		//or the move() operation will fail. This needs to be described in 
		//the API docs.
		SftpHostFile node = null;
		SftpATTRS attrs = null;
		try {
			attrs = fChannelSftp.stat(remoteParent+'/'+fileName);
		} catch(Exception e) {}
		if (attrs!=null) {
			node = makeHostFile(remoteParent, fileName, attrs);
		} else {
			node = new SftpHostFile(remoteParent, fileName, false, false, false, 0, 0);
			node.setExists(false);
		}
		return node;
	}
	
	public boolean isConnected() {
		return fChannelSftp.isConnected();
	}
	
	protected IHostFile[] internalFetch(IProgressMonitor monitor, String parentPath, String fileFilter, int fileType)
	{
		if (fileFilter == null) {
			fileFilter = "*"; //$NON-NLS-1$
		}
		NamePatternMatcher filematcher = new NamePatternMatcher(fileFilter, true, true);
		List results = new ArrayList();
		try {
		    java.util.Vector vv=fChannelSftp.ls(parentPath);
		    for(int ii=0; ii<vv.size(); ii++) {
		    	Object obj=vv.elementAt(ii);
		    	if(obj instanceof ChannelSftp.LsEntry){
		    		ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry)obj;
		    		String fileName = lsEntry.getFilename();
		    		if (".".equals(fileName) || "..".equals(fileName)) { //$NON-NLS-1$ //$NON-NLS-2$
		    			//don't show the trivial names
		    			continue;
		    		}
		    		if (filematcher.matches(fileName)) {
		    			SftpHostFile node = makeHostFile(parentPath, fileName, lsEntry.getAttrs());
		    			if (isRightType(fileType, node)) {
		    				results.add(node);
		    			}
		    		}
		    	}
		    }
		} catch(Exception e) {
			//TODO throw new SystemMessageException.
			//We get a "2: No such file" exception when we try to get contents
			//of a symbolic link that turns out to point to a file rather than
			//a directory. In this case, the result is probably expected.
			//We should try to classify symbolic links as "file" or "dir" correctly.
			e.printStackTrace();
		}
		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}

	private SftpHostFile makeHostFile(String parentPath, String fileName, SftpATTRS attrs) {
		SftpHostFile node = new SftpHostFile(parentPath, fileName, attrs.isDir(), false, attrs.isLink(), 1000L * attrs.getMTime(), attrs.getSize());
		if (attrs.getExtended()!=null) {
			node.setExtendedData(attrs.getExtended());
		}
		//TODO remove comments as soon as jsch-0.1.29 is available 
		//if (node.isLink()) {
		//	try {
		//		//Note: readlink() is supported only with jsch-0.1.29 or higher.
		//		//By catching the exception we remain backward compatible.
		//		String linkTarget=fChannelSftp.readlink(node.getAbsolutePath());
		//		node.setLinkTarget(linkTarget);
		//		//TODO: Classify the type of resource linked to as file, folder or broken link
		//	} catch(Exception e) {}
		//}
		return node;
	}
	
	public String getSeparator() {
		return "/"; //$NON-NLS-1$
	}
	
	public boolean upload(IProgressMonitor monitor, File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding)
	{
		//TODO what to do with isBinary? 
		try {
			SftpProgressMonitor sftpMonitor=new MyProgressMonitor(monitor);
			int mode=ChannelSftp.OVERWRITE;
			String dst = remoteParent;
			if( remoteFile!=null ) {
				if (!dst.endsWith("/")) { //$NON-NLS-1$
					dst += '/';
				}
				dst += remoteFile;
			}
			fChannelSftp.put(localFile.getAbsolutePath(), dst, sftpMonitor, mode); 
		}
		catch (Exception e) {
			//TODO See download
			//e.printStackTrace();
			//throw new RemoteFileIOException(e);
			return false;
		}
		return true;
	}

	  public static class MyProgressMonitor implements SftpProgressMonitor
	  {
		  IProgressMonitor fMonitor;
		  
		  public MyProgressMonitor(IProgressMonitor monitor) {
			  fMonitor = monitor;
		  }
		  public void init(int op, String src, String dest, long max){
			  String desc = ((op==SftpProgressMonitor.PUT)? 
                      "put" : "get")+": "+src; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			  //TODO avoid cast from long to int
			  fMonitor.beginTask(desc, (int)max);
		  }
		  public boolean count(long count){
		      fMonitor.worked((int)count);
		      return !(fMonitor.isCanceled());
		  }
		  public void end(){
			  fMonitor.done();
		  }
	 };

	public boolean upload(IProgressMonitor monitor, InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding) throws SystemMessageException
	{
		//TODO hack for now
		try
		{
			BufferedInputStream bis = new BufferedInputStream(stream);
			File tempFile = File.createTempFile("ftp", "temp"); //$NON-NLS-1$ //$NON-NLS-2$
			FileOutputStream os = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
	
			 byte[] buffer = new byte[1024];
			 int readCount;
			 while( (readCount = bis.read(buffer)) > 0) 
			 {
			      bos.write(buffer, 0, readCount);
			 }
			 bos.close();
			 upload(monitor, tempFile, remoteParent, remoteFile, isBinary, "", hostEncoding); //$NON-NLS-1$
		}
		catch (Exception e) {
			//TODO See download
			//e.printStackTrace();
			//throw new RemoteFileIOException(e);
			return false;
		}
		return true;
	}
	
	public boolean download(IProgressMonitor monitor, String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding) throws SystemMessageException
	{
		try {
			if (!localFile.exists()) {
				File localParentFile = localFile.getParentFile();
				if (!localParentFile.exists()) {
					localParentFile.mkdirs();
				}
				//localFile.createNewFile();
			}
			//TODO Ascii/binary?
			String remotePath = remoteParent+'/'+remoteFile;
			int mode=ChannelSftp.OVERWRITE;
			MyProgressMonitor sftpMonitor = new MyProgressMonitor(monitor);
			fChannelSftp.get(remotePath, localFile.getAbsolutePath(), sftpMonitor, mode);
		}
		catch (Exception e) {
			//TODO handle exception properly: happens e.g. when trying to download a symlink.
			//Messages from Jsch are mostly not useful, especially when the server version is
			//<=3 (e.g. "4: Failure"). therefore it is better for now to just return false.
			//e.printStackTrace();
			//throw new RemoteFileIOException(e);
			return false;
		}
		return true;
	}
	
	public IHostFile getUserHome() 	{
		//TODO assert: this is only called after we are connected
		int lastSlash = fUserHome.lastIndexOf('/');
		String name = fUserHome.substring(lastSlash + 1);	
		String parent = fUserHome.substring(0, lastSlash);
		return getFile(null, parent, name);
	}

	public IHostFile[] getRoots(IProgressMonitor monitor) {
		IHostFile root = new SftpHostFile("/", "/", true, true, false, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
		return new IHostFile[] { root };
	}
	
	// TODO
	/********************************************************
	 * 
	 *    The following APIs need to be implemented
	 * 
	 ********************************************************/
	
	public IHostFile createFile(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException 
	{
		IHostFile result = null;
		try {
			String fullPath = remoteParent + '/' + fileName;
			OutputStream os = fChannelSftp.put(fullPath);
			os.close();
			SftpATTRS attrs = fChannelSftp.stat(fullPath);
			result = makeHostFile(remoteParent, fileName, attrs);
		} catch (Exception e) {
			e.printStackTrace();
		// DKM commenting out because services don't know about this class
			// throw new RemoteFileIOException(e);
		}
		return result;
	}

	public IHostFile createFolder(IProgressMonitor monitor, String remoteParent, String folderName) throws SystemMessageException
	{
		IHostFile result = null;
		try {
			String fullPath = remoteParent + '/' + folderName;
			fChannelSftp.mkdir(fullPath);
			SftpATTRS attrs = fChannelSftp.stat(fullPath);
			result = makeHostFile(remoteParent, folderName, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			// DKM commenting out because services don't know about this class
			//throw new RemoteFileIOException(e);
		}
		return result;
	}

	public boolean delete(IProgressMonitor monitor, String remoteParent, String fileName) throws SystemMessageException
	{
		boolean ok=false;
		try {
			String fullPath = remoteParent + '/' + fileName;
			SftpATTRS attrs = fChannelSftp.stat(fullPath);
			if (attrs==null) {
				//doesn't exist, nothing to do
			} else if (attrs.isDir()) {
				fChannelSftp.rmdir(fullPath);
			} else {
				fChannelSftp.rm(fullPath);
			}
			ok=true;
		} catch (Exception e) {
			e.printStackTrace();
			// DKM commenting out because services don't know about this class
			//throw new RemoteFileIOException(e);
		}
		return ok;
	}

	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName) throws SystemMessageException
	{
		boolean ok=false;
		try {
			String fullPathOld = remoteParent + '/' + oldName;
			String fullPathNew = remoteParent + '/' + newName;
			fChannelSftp.rename(fullPathOld, fullPathNew);
			ok=true;
		} catch (Exception e) {
			e.printStackTrace();
			// DKM commenting out because services don't know about this class
			//throw new RemoteFileIOException(e);
		}
		return ok;
	}
	
	public boolean rename(IProgressMonitor monitor, String remoteParent, String oldName, String newName, IHostFile oldFile) throws SystemMessageException {
		// TODO dont know how to update
		return rename(monitor, remoteParent, oldName, newName);
	}

	private boolean progressWorked(IProgressMonitor monitor, int work) {
		boolean cancelRequested = false;
		if (monitor!=null) {
			monitor.worked(work);
			cancelRequested = monitor.isCanceled();
		}
		return cancelRequested;
	}
	
	public int runCommand(IProgressMonitor monitor, String command) throws SystemMessageException
	{
		int result = -1;
		if (monitor!=null) {
			monitor.beginTask(command, 20);
		}
		Channel channel = null;
		try {
			channel=fSessionProvider.getSession().openChannel("exec"); //$NON-NLS-1$
			((ChannelExec)channel).setCommand(command);

			//No user input
			channel.setInputStream(null);
			//TODO capture error output for exception
			((ChannelExec)channel).setErrStream(System.err);
			InputStream in=channel.getInputStream();
			channel.connect();
			byte[] tmp=new byte[1024];
			while(!channel.isClosed()){
				if( progressWorked(monitor,1) ) {
					break;
				}
				while(in.available()>0){
					int i=in.read(tmp, 0, 1024);
					if(i<0)break;
					//System.out.print(new String(tmp, 0, i));
				}
				try{Thread.sleep(1000);}catch(Exception ee){}
			}
			result = channel.getExitStatus();
		} catch(Exception e) {
			// DKM
			//  not visible to this plugin
			// throw new RemoteFileIOException(e);
		} finally {
			if (monitor!=null) {
				monitor.done();
			}
			if (channel!=null) {
				channel.disconnect();
			}
		}
		return result;
	}
	
	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException
	{
		// move is not supported by sftp directly. Use the ssh shell instead.
		// TODO check if newer versions of sftp support move directly
		// TODO Interpret some error messages like "command not found" (use ren instead of mv on windows)
		String fullPathOld = srcParent + '/' + srcName;
		String fullPathNew = tgtParent + '/' + tgtName;
		//TODO quote pathes if necessary
		int rv = runCommand(monitor, "mv "+fullPathOld+' '+fullPathNew); //$NON-NLS-1$
		return (rv==0);
	}

	public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException {
		// move is not supported by sftp directly. Use the ssh shell instead.
		// TODO check if newer versions of sftp support move directly
		// TODO Interpret some error messages like "command not found" (use (x)copy instead of cp on windows)
		String fullPathOld = srcParent + '/' + srcName; //$NON-NLS-1$
		String fullPathNew = tgtParent + '/' + tgtName; //$NON-NLS-1$
		//TODO quote pathes if necessary
		int rv = runCommand(monitor, "cp "+fullPathOld+' '+fullPathNew); //$NON-NLS-1$
		return (rv==0);
	}
	
	public boolean copyBatch(IProgressMonitor monitor, String[] srcParents, String[] srcNames, String tgtParent) throws SystemMessageException 
	{
		boolean ok = true;
		for (int i = 0; i < srcParents.length; i++)
		{
			ok = ok && copy(monitor, srcParents[i], srcNames[i], tgtParent, srcNames[i]);
		}
		return ok;
	}

	public void initService(IProgressMonitor monitor) {
		try
		{
			connect();
		}
		catch (Exception e)
		{			
		}
	}
	
	public void uninitService(IProgressMonitor monitor) {
		disconnect();
	}

	public boolean isCaseSensitive() {
		//TODO find out whether remote is case sensitive or not
		return true;
	}

}
