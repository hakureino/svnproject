package maventest.svn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SVNUtil {
	private String userName = "";
	private String password = "";
	private String urlString = "";
	private String filePrefix = "";
	// private String urlStringfile = "http://svn.isid.co.jp/svn/istd";
	// private String userName = "testUserName";
	// private String password = "testPassword";
	//private String urlString = "testUrlString";
	private String urlStringfile = "testUrlStringfile";
	boolean readonly = true;
	private DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(readonly);

	private SVNRepository repos;
	private ISVNAuthenticationManager authManager;
	private static SVNClientManager clientManager;
	
	public void readProperties() {
		Properties properties = new Properties();
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream("D:\\systemparam.properties");
			properties.load(inputStream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.userName = properties.getProperty("userName");
		this.password = properties.getProperty("password");
		this.urlString = properties.getProperty("urlString");
		this.filePrefix = properties.getProperty("filePrefix");
	}

	public SVNUtil() {
		try {
			readProperties();
			init();
		} catch (SVNException e) {
			System.out.println("初始化失败");
			e.printStackTrace();
		}
	}

	public void init() throws SVNException {
		authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password.toCharArray());
		options.setDiffCommand("-x -w");
		repos = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(urlString));
		repos.setAuthenticationManager(authManager);
		clientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, authManager);
	}
	
	public List<SVNLogEntryPath> getChangeFileList(long version) throws SVNException {
		List<SVNLogEntryPath> result = new ArrayList<>();
		SVNLogClient logClient = new SVNLogClient(authManager, options);
		SVNURL url = SVNURL.parseURIEncoded(urlString);
		// 相对于目标URL的路径数组
		String[] paths = { "." };
		// PegRevision表示在这个版本下定位那个元素,这一定是唯一的
		SVNRevision pegRevision = SVNRevision.create(version);
		SVNRevision startRevision = SVNRevision.create(version);
		SVNRevision endRevision = SVNRevision.create(version);
		// 遍历历史记录时不交叉复制，否则副本历史记录也将包括在处理中。
		boolean stopOnCopy = false;
		// 报告正在处理的每个修订的所有更改路径（这些路径将通过调用org.tmateSoft.svn.core.svnlogentry.getChangedPaths（）调用）
		boolean discoverChangedPaths = true;
		// 限制最大处理的日志数
		long limit = 9999l;
		
		ISVNLogEntryHandler handler = new ISVNLogEntryHandler() {
			@Override
			public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
				System.out.println("提交者: " + logEntry.getAuthor());
				System.out.println("提交时间: " + logEntry.getDate());
				System.out.println("提交信息: " + logEntry.getMessage());
				System.out.println("版本: " + logEntry.getRevision());
				System.out.println("---------版本为" + logEntry.getRevision() + "的修改记录start--------");
				Map<String, SVNLogEntryPath> maps = logEntry.getChangedPaths();
				Set<Map.Entry<String, SVNLogEntryPath>> entries = maps.entrySet();
				for (Map.Entry<String, SVNLogEntryPath> entry : entries) {
					// System.out.println(entry.getKey());
					SVNLogEntryPath entryPath = entry.getValue();
					result.add(entryPath);
					System.out.println(entryPath.getType() + " " + entryPath.getPath());
				}
				System.out.println("---------版本为" + logEntry.getRevision() + "的修改记录end--------");
			} 
		};
		try {
			logClient.doLog(url, paths, pegRevision, startRevision, endRevision, stopOnCopy, discoverChangedPaths, limit, handler);
		} catch (SVNException e) {
			System.out.println("Error in doLog() ");
			e.printStackTrace();
		}
		return result;
	}
	
	public SVNLogEntry getonepathSvninfo(String path) throws SVNException {
		SVNURL epositoryBaseUrl = SVNURL.parseURIEncoded(urlString);
		long startRevision = 0;
		// 开始
		long endRevision = -1;// 为-1时 表示最后一个版本
		Collection<SVNLogEntry> logEntries = repos.log(new String[] { path }, null, startRevision, endRevision, true,
				true);
		SVNLogEntry[] svnLogEntries = logEntries.toArray(new SVNLogEntry[0]);
		return svnLogEntries[svnLogEntries.length - 1];
	}
	
	public static void main(String[] args) throws Exception {
		SVNUtil demo = new SVNUtil();
		try {
			//System.out.println(demo.getChangeFileList(55881l));
			System.out.println("版本号"+demo.getonepathSvninfo("/branches/develop/maintenance/web/istandard/src/main/java/jp/co/isid/istandard/hc/logic/hc0302/impl/HC03020401_R01ReportPrevLogicImpl.java").getRevision());
		} catch (SVNException e) {
			e.printStackTrace();
		}
	}

}
