package org.eclipse.cdt.releng;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;

/**
 * This application ftps the test results up to the server. 
 * @see IPlatformRunnable
 */
public class PostResults implements IPlatformRunnable {
	
	private static final String ftpHost = "download.eclipse.org";
	private static final String ftpPath = "cdt/updates/builds/1.2";
	private static final String ftpUser = System.getProperty("cdt.build.user");
	private static final String ftpPassword = System.getProperty("cdt.build.passwd");

	public PostResults() {
	}

	/**
	 * @see IPlatformRunnable#run
	 */
	public Object run(Object args) throws Exception {
		// Get the goodies out of the argument list
		String version = "unversioned";
		List logs = new ArrayList();
		
		String[] strargs = (String[])args;
		for (int i = 0; i < strargs.length; ++i)
			if (strargs[i].equals("-version")) {
				version = strargs[++i];
			} else if (strargs[i].equals("-logs")) {
				for (i++; i < strargs.length; ++i) {
					logs.add(strargs[i]);
				}
			}

		IPath resultDir = Platform.getLocation().removeLastSegments(1).append("results");

		// Open ftp connection
		FTPClient ftp = new FTPClient(ftpHost);
		ftp.setConnectMode(FTPConnectMode.ACTIVE);
		ftp.login(ftpUser, ftpPassword);
		ftp.chdir(ftpPath);
		
		// Upload the html files
		ftp.put(new FileInputStream(resultDir.append("index.html").toOSString()), "index.html");
		
		ftp.chdir("logs");
		ftp.mkdir(version);
		ftp.chdir(version);
		
		for (int i = 0; i < logs.size(); ++i) {
			String log = (String)logs.get(i);
			ftp.put(new FileInputStream(resultDir.append(log).toOSString()), log);
		}
		
		ftp.quit();
		return null;
	}
}
