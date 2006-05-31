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
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.ssh.Activator;
import org.eclipse.rse.services.ssh.ISshService;
import org.eclipse.rse.services.ssh.ISshSessionProvider;
import org.eclipse.swt.widgets.Display;

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
		Activator.trace("SftpFileService.connecting..."); //$NON-NLS-1$
		try {
			Session session = fSessionProvider.getSession();
		    Channel channel=session.openChannel("sftp"); //$NON-NLS-1$
		    channel.connect();
		    fChannelSftp=(ChannelSftp)channel;
			fUserHome = fChannelSftp.pwd();
			Activator.trace("SftpFileService.connected"); //$NON-NLS-1$
		} catch(Exception e) {
			Activator.trace("SftpFileService.connecting failed: "+e.toString()); //$NON-NLS-1$
			throw e;
		}
	}
	
	protected ChannelSftp getChannel(String task) throws Exception
	{
		Activator.trace(task);
		if (fChannelSftp==null || !fChannelSftp.isConnected()) {
			Activator.trace(task + ": channel not connected: "+fChannelSftp); //$NON-NLS-1$
			Session session = fSessionProvider.getSession();
			if (session!=null) {
				if (!session.isConnected()) {
					//notify of lost session. May reconnect asynchronously later.
					fSessionProvider.handleSessionLost();
					//dont throw an exception here, expect jsch to throw something useful 
				} else {
					//session connected but channel not: try to reconnect
					//(may throw Exception)
					connect();
				}
			}
			//TODO might throw NPE if session has been disconnected
		}
		return fChannelSftp;
	}
	
	public void disconnect() {
		try {
			getChannel("SftpFileService.disconnect").disconnect(); //$NON-NLS-1$
		} catch(Exception e) {
			/*nothing to do*/
		}
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
			attrs = getChannel("SftpFileService.getFile").stat(remoteParent+'/'+fileName); //$NON-NLS-1$
			Activator.trace("SftpFileService.getFile done"); //$NON-NLS-1$
		} catch(Exception e) {
			Activator.trace("SftpFileService.getFile failed: "+e.toString()); //$NON-NLS-1$
		}
		if (attrs!=null) {
			node = makeHostFile(remoteParent, fileName, attrs);
		} else {
			node = new SftpHostFile(remoteParent, fileName, false, false, false, 0, 0);
			node.setExists(false);
		}
		return node;
	}
	
	public boolean isConnected() {
		try {
			return getChannel("SftpFileService.isConnected()").isConnected(); //$NON-NLS-1$
		} catch(Exception e) {
			/*cannot be connected when we cannot get a channel*/
		}
		return false;
	}
	
	protected IHostFile[] internalFetch(IProgressMonitor monitor, String parentPath, String fileFilter, int fileType)
	{
		if (fileFilter == null) {
			fileFilter = "*"; //$NON-NLS-1$
		}
		NamePatternMatcher filematcher = new NamePatternMatcher(fileFilter, true, true);
		List results = new ArrayList();
		try {
		    java.util.Vector vv=getChannel("SftpFileService.internalFetch").ls(parentPath); //$NON-NLS-1$
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
			Activator.trace("SftpFileService.internalFetch ok"); //$NON-NLS-1$
		} catch(Exception e) {
			//TODO throw new SystemMessageException.
			//We get a "2: No such file" exception when we try to get contents
			//of a symbolic link that turns out to point to a file rather than
			//a directory. In this case, the result is probably expected.
			//We should try to classify symbolic links as "file" or "dir" correctly.
			Activator.trace("SftpFileService.internalFetch failed: "+e.toString()); //$NON-NLS-1$
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
		//		String linkTarget=getChannel("makeHostFile.readlink").readlink(node.getAbsolutePath()); //$NON-NLS-1$
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
			getChannel("SftpFileService.upload "+remoteFile).put(localFile.getAbsolutePath(), dst, sftpMonitor, mode); //$NON-NLS-1$ 
			Activator.trace("SftpFileService.upload "+remoteFile+ " ok"); //$NON-NLS-1$ //$NON-NLS-1$
		}
		catch (Exception e) {
			//TODO See download
			//e.printStackTrace();
			//throw new RemoteFileIOException(e);
			Activator.trace("SftpFileService.upload "+remoteFile+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
			getChannel("SftpFileService.download "+remoteFile).get(remotePath, localFile.getAbsolutePath(), sftpMonitor, mode); //$NON-NLS-1$
			Activator.trace("SftpFileService.download "+remoteFile+ " ok"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (Exception e) {
			//TODO handle exception properly: happens e.g. when trying to download a symlink.
			//Messages from Jsch are mostly not useful, especially when the server version is
			//<=3 (e.g. "4: Failure"). therefore it is better for now to just return false.
			//e.printStackTrace();
			//throw new RemoteFileIOException(e);
			Activator.trace("SftpFileService.download "+remoteFile+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
			OutputStream os = getChannel("SftpFileService.createFile").put(fullPath); //$NON-NLS-1$
			os.close();
			SftpATTRS attrs = getChannel("SftpFileService.createFile.stat").stat(fullPath); //$NON-NLS-1$
			result = makeHostFile(remoteParent, fileName, attrs);
			Activator.trace("SftpFileService.createFile ok"); //$NON-NLS-1$
		} catch (Exception e) {
			Activator.trace("SftpFileService.createFile failed: "+e.toString()); //$NON-NLS-1$
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
			getChannel("SftpFileService.createFolder").mkdir(fullPath); //$NON-NLS-1$
			SftpATTRS attrs = getChannel("SftpFileService.createFolder.stat").stat(fullPath); //$NON-NLS-1$
			result = makeHostFile(remoteParent, folderName, attrs);
			Activator.trace("SftpFileService.createFolder ok"); //$NON-NLS-1$
		} catch (Exception e) {
			Activator.trace("SftpFileService.createFolder failed: "+e.toString()); //$NON-NLS-1$
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
			SftpATTRS attrs = getChannel("SftpFileService.delete").stat(fullPath); //$NON-NLS-1$
			if (attrs==null) {
				//doesn't exist, nothing to do
			} else if (attrs.isDir()) {
				getChannel("SftpFileService.delete.rmdir").rmdir(fullPath); //$NON-NLS-1$
			} else {
				getChannel("SftpFileService.delete.rm").rm(fullPath); //$NON-NLS-1$
			}
			ok=true;
			Activator.trace("SftpFileService.delete ok"); //$NON-NLS-1$
		} catch (Exception e) {
			Activator.trace("SftpFileService.delete: "+e.toString()); //$NON-NLS-1$
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
			getChannel("SftpFileService.rename").rename(fullPathOld, fullPathNew); //$NON-NLS-1$
			ok=true;
			Activator.trace("SftpFileService.rename ok"); //$NON-NLS-1$
		} catch (Exception e) {
			Activator.trace("SftpFileService.rename failed: "+e.toString()); //$NON-NLS-1$
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
		Activator.trace("SftpFileService.runCommand "+command); //$NON-NLS-1$ //$NON-NLS-2$
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
			Activator.trace("SftpFileService.runCommand ok, result: "+result); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(Exception e) {
			// DKM
			//  not visible to this plugin
			// throw new RemoteFileIOException(e);
			Activator.trace("SftpFileService.runCommand failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	private static Pattern fValidShellPattern = Pattern.compile("[a-zA-Z0-9._/]*"); //$NON-NLS-1$
	/**
	 * Quotes a string such that it can be used in a remote UNIX shell.
	 * On Windows, special characters likes quotes and dollar sign. and
	 * - most importantly - the backslash will not be quoted correctly.
	 * 
	 * Newline is only quoted correctly in tcsh. But since this is mainly
	 * intended for file names, it should work OK in almost every case.
	 * 
	 * @param s String to be quoted
	 * @return quoted string, or original if no quoting was necessary.
	 */
	public static String enQuote(String s) {
		if(fValidShellPattern.matcher(s).matches()) {
			return s;
		} else {
			StringBuffer buf = new StringBuffer(s.length()+16);
			buf.append('"');
			for(int i=0; i<s.length(); i++) {
				char c=s.charAt(i);
				switch(c) {
				case '$':
					//Need to treat specially to work in both bash and tcsh:
					//close the quote, insert quoted $, reopen the quote
					buf.append('"');
					buf.append('\\');
					buf.append('$');
					buf.append('"');
					break;
				case '"':
				case '\\':
				case '\'':
				case '`':
				case '\n':
					//just quote it. The newline will work in tcsh only -
					//bash replaces it by the empty string. But newlines
					//in filenames are an academic issue, hopefully.
					buf.append('\\');
					buf.append(c);
					break;
				default:
					buf.append(c);
				}
			}
			buf.append('"');
			return buf.toString();
		}
	}
	
	public boolean move(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException
	{
		// move is not supported by sftp directly. Use the ssh shell instead.
		// TODO check if newer versions of sftp support move directly
		// TODO Interpret some error messages like "command not found" (use ren instead of mv on windows)
		// TODO mimic by copy if the remote does not support copying between file systems?
		String fullPathOld = enQuote(srcParent + '/' + srcName);
		String fullPathNew = enQuote(tgtParent + '/' + tgtName);
		int rv = runCommand(monitor, "mv "+fullPathOld+' '+fullPathNew); //$NON-NLS-1$
		return (rv==0);
	}

	public boolean copy(IProgressMonitor monitor, String srcParent, String srcName, String tgtParent, String tgtName) throws SystemMessageException {
		// move is not supported by sftp directly. Use the ssh shell instead.
		// TODO check if newer versions of sftp support move directly
		// TODO Interpret some error messages like "command not found" (use (x)copy instead of cp on windows)
		String fullPathOld = enQuote(srcParent + '/' + srcName); //$NON-NLS-1$
		String fullPathNew = enQuote(tgtParent + '/' + tgtName); //$NON-NLS-1$
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
