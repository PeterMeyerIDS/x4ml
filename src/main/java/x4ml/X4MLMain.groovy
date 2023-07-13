package x4ml

import java.awt.EventQueue
import java.nio.file.Files

import javax.swing.UIManager
import javax.swing.UIManager.LookAndFeelInfo

import groovy.json.JsonOutput
import groovy.json.JsonSlurper



/**
 * 
 *	
 *
    Usernamen dürfen nicht mit __ beginnen; reserviert sind
	__xxxx	im Servermode als Admin -- an Kommandozeile festgelegt
	__DESKTOP__   als einziger Nutzer im Desktopmodus

 *
 */
class X4MLMain {
	
	
	public static final String VERSION = "0.9.70 Gerd Hentschel edition"
	
	// this directory is always there and opened first by default
	public static final String DEFAULTDIR = 'playground'
	
	// the only allowable username in Desktop mode 
	public static final String DESKTOPUSER = '__DESKTOP__'
	
	public static boolean SERVERMODE
	
	/*
	 * in Desktop mode: internally used temporary dir, remains empty
	 * in Server mode: user-chosen general data directory, contains config DB and user workdirs
	 */
	public static String X4MLDIR
	
	
	public static String desktopModeProjectDir = null
	
	public static Map<String, String> suffixToMIMEAndBinary = [
		'html': ['text/html', false],
		'css': ['text/css', false],
		'js': ['text/javascript', false],
		'woff2': ['font/woff2', true],
		'jpg': ['image/jpeg', true],
		'png': ['image/png', true],
		'tif': ['image/tiff', true],
		'tiff': ['image/tiff', true],
		'mp3': ['audio/mpeg', true],
		'mp4': ['video/mp4', true]
	]
	
	
	
	/**
	 *

"username|directoryname" --> [uuid: "12345", prefs: $dictObject, procs: $procObject]

$dictObject (wird als JSON persistiert) ist:
{
      "lemmapath": "/entry/lemma",
      "dictname": "My first dictionary"
}

$procObject (temporäre Zustände für Prozessoren) ist:
{
      "lastFetchedOtherFileName": "bla.dtd",
      "mostRecentHTML": "<html>...",
      "mostRecentRealHTML": "<html>...",
      "mostRecentXML": Slurperergebnis,
      "mostRecentXMLString": "<xml>...",
      "mostRecentNotWellFormed": false
}




	 */
	public static Map WORKDIRSESSIONS = [:]
	
	
	// only in server mode
	public static String ADMINNAME = null
	public static String ADMINPW = null
	
	private static X4MLWebApp webServer
	private static X4MLXMLDB xmlDatabase
	
	
	/**
	 * 
	 * global app parameters
	 * 
	 * server mode params:
	 * 			IP port
	 * 			main directory
	 * 			admin user name (must start with __)
	 * 			admin password
	 * 
	 */
	public static void main(String[] args) {
		
		Integer webAppPort
		
		if (args.length == 0) {
			SERVERMODE = false
			File tmpDirFile = null
			try {
				tmpDirFile = Files.createTempDirectory("x4mlDir").toFile()
				tmpDirFile.deleteOnExit()
			} catch (IOException e1) {
				println("Could not create temp dir X4MLDIR; exiting")
				e1.printStackTrace()
				System.exit(-1)
			}
			X4MLDIR = tmpDirFile.getAbsolutePath()
			webAppPort = null // "random available port" 
			
			println('Program starts in desktop mode. Use parameter help for infos on starting in server mode.')
			
		} else if (args.length == 1 && args[0] == 'help') {
			println("""Usage: java -jar x4ml.jar [params]
params for desktop mode: none
params for server  mode: IP port, main directory, admin user name, admin password""")
			System.exit(0)
		} else  if (args.length == 4) {
			
			SERVERMODE = true
			
			if (!args[0].isInteger()) {
				println("first argument must be a port number; exiting.")
				System.exit(-1)
			}
			
			webAppPort = args[0].toInteger()
			
			X4MLDIR = args[1]
			if (!(new File(X4MLDIR).exists())) {
				println("base directory does not exist; exiting.")
				System.exit(-1)
			}
			
			ADMINNAME = args[2]
			if (!(ADMINNAME.startsWith('__'))) {
				println("admin username does not start with '__'; exiting.")
				System.exit(-1)
			}
			
			ADMINPW = args[3]
			
			
		} else {
			println("Invalid # of params; exiting.")
			println("Use parameter help for infos on starting in server mode.")
			System.exit(-1)
		}
		
		
		webServer = new X4MLWebApp(webAppPort)
		
		// initialize BaseX, set directories; must be done in desktop and server mode
		// see X4MLXMLDB constructor!
		xmlDatabase = new X4MLXMLDB(X4MLDIR)
		
		if (!SERVERMODE) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
							if ("Nimbus".equals(info.getName())) {
								UIManager.setLookAndFeel(info.getClassName())
								break
							}
						}
						X4MLGUI window = new X4MLGUI()
						window.frmX4ml.setVisible(true)
						
						
					} catch (Exception e) {
						e.printStackTrace()
						println("GUI for Desktop Mode could not be started; exiting.")
						System.exit(-1)
					}
				}
			})
		}
		
	}
	
	public static URI getDesktopModeURL(String username) {
		webServer.getDesktopModeURL(username)
	}
	
	public static List<String> getServerUsers() {
		def users = xmlDatabase.returnReadOnlyXQueryAsString("_ROOT_/user/@name/string()")
		(users.split('\n') as List).sort()
	}

	
	// normaler username darf nicht mit __ anfangen
	public static boolean addServerUser(String username, String password) {
		// <user name="test" password="pw"></user>
		
		if (username.startsWith('__')) {
			return false
		}
		
		def user = xmlDatabase.returnReadOnlyXQueryAsString("_ROOT_/user[@name='${username}']")
		if (user) {
			return false
		}
		xmlDatabase.returnXQueryAsString("insert node <user name='$username' password='${escape4XML(password)}' /> into _ROOT_")
		return true
	}
	
	public static void removeServerUser(String username) {
		// <user name="test" password="pw"></user>
		xmlDatabase.returnXQueryAsString("delete node _ROOT_/user[@name=$username]")
	}

	public static void changeServerUserPassword(String username, String password) {
		if (username.startsWith('__')) {
			return
		}
		xmlDatabase.returnXQueryAsString("replace value of node _ROOT_/user[@name=$username]/@password with '${escape4XML(password)}'")
	}
	
	// Sonderfall __DESKTOP__ = DESKTOPUSER
	// mit DB abgleichen: <user name="test" password="pw"></user>
	public static boolean authenticateUser(String username, String password) {
		if (username == DESKTOPUSER) {
			return !SERVERMODE
		}
		def pwInDB = unescape4XML(
			xmlDatabase.returnReadOnlyXQueryAsString("_ROOT_/user[@name='${username}']/@password/string()")
		)
		return pwInDB == password
	}

	public static boolean authenticateAdminUser(String username, String password) {
		//println("authenticating admin: username $username, password $password; should be ADMINNAME: $ADMINNAME; ADMINPW: $ADMINPW")
		def authenticated = (username == ADMINNAME && password == ADMINPW)
		//println("authenticated: $authenticated")
		return authenticated
	}
	
	// holt [uuid: "12345", dict: $dictObject, procs: $procObject] aus der WORKDIRSESSIONS-Map
	public static Map getWorkdirSessionObject(String username, String workdir) {
		WORKDIRSESSIONS["$username|$workdir"]
	}

	// erzeugt und persistiert ggf. [uuid: "12345", prefs: $dictObject, procs: $procObject]
	// für eine Kombi username + workdir
	// und legt sie in WORKDIRSESSIONS-Map
	// uuid liefert der Browser!!!!
	// returns [username: '...', workdir: '...', files: ['bla.xml', ....]]
	public static Map startNewSession(String username, String workdir, String uuid) {
		
		if (!username || !workdir) {
			throw new Exception('session creation went wrong!');
		}
		
		def prefsObject = fetchPersistedPrefs(username, workdir)
		boolean isNewWorkdir4User = !prefsObject 
		
		if (isNewWorkdir4User) {
			prefsObject = [
				lemmapath: '',
				dictname: '',
				namespaces: []  // list of Maps: [mnemo: 'svg', ns: 'http://www.w3.org/2000/svg'] for future namespace support
			]
		}
		
		def sessionObject = [
			uuid: uuid,
			prefs: prefsObject,
			procs: [
				lastFetchedOtherFileName: "",
				mostRecentHTML: "",
				mostRecentRealHTML: "",
				mostRecentXML: null,
				mostRecentXMLString: "",
				mostRecentNotWellFormed: false
			]
		]
		
		WORKDIRSESSIONS["$username|$workdir"] = sessionObject
		
		// println("sessionObject: $sessionObject")
		
		if (isNewWorkdir4User) {
			persistPrefs(username, workdir)
		}
		
		def projectDirInfo4Browser = SERVERMODE ? 'server mode' : desktopModeProjectDir
		
		
		return [
			username: username,
			projectdir: projectDirInfo4Browser,
			files: listWorkdirFiles(username, workdir)
		]
		
	}
	
	

	// aus browser session den username rausholen: ctx.sessionAttribute('x4mluser')
	// aus Query-Params oder POST-Payload die Browser-UUID und das workdir rausholen
	// Methode prüft, ob UUID des Browserfenster noch aktuell für username+workdir
	public static boolean checkIsCurrentWorkdirSession(String username, String workdir, String uuid) {
		getWorkdirSessionObject(username, workdir).uuid == uuid
	}
	
	private static Map getPrefsObject(String username, String workdir) {
		getWorkdirSessionObject(username, workdir)?.prefs ?: [:]
	}
	
	/*
	 * returns empty string if no such pref
	 */
	public static String getPref(String username, String workdir, String key) {
		getPrefsObject(username, workdir)[key] ?: ''
	}
	
	public static Map getProcsObject(String username, String workdir) {
		getWorkdirSessionObject(username, workdir).procs
	}
	
	
	
	public static void setPref(String username, String workdir, String key, String value) {
		def prefs = getPrefsObject(username, workdir)
		prefs[key] = value
		persistPrefs(username, workdir)
	}
	
	public static void removeWorkdirAndPersist(String username, String workdir) {
		WORKDIRSESSIONS.remove("$username|$workdir")
		if (SERVERMODE) {
			// delete node
			String workdirEscaped = escape4XML(workdir)
			xmlDatabase.returnXQueryAsString("delete node _ROOT_/dir[@user='${username}' and @dirname='$workdirEscaped']")
		} else {
			X4MLPreferences4DesktopMode.removeWorkdirFromPrefsObjects4Workdirs(desktopModeProjectDir, workdir)
		}
		deleteWorkdir(username, workdir)
	}
	
	public static void persistPrefs(String username, String workdir) {
		// getWorkdirSessionObject(username, workdir).prefs als JSON serialisieren und persistieren:
		//    Server: <dir user="username" dirname="playground">$dictObject = prefs</dir>
		//    Desktop: hier fließt das globale project directory mit ein:
		//        workdir|directoryname --> $dictObject
		
		def prefs = getPrefsObject(username, workdir)
		
		
		if (SERVERMODE) {
			def prefsSerialized = escape4XML(
				JsonOutput.toJson(prefs)
			).replace('{', '&#x7B;').replace('}', '&#x7D;')
			// <dir user="username" dirname="playground">$prefsObject</dir>
			String workdirEscaped = escape4XML(workdir)
			def dirElement = xmlDatabase.returnReadOnlyXQueryAsString("_ROOT_/dir[@user='${username}' and @dirname='$workdirEscaped']")
			if (dirElement) {
				xmlDatabase.returnXQueryAsString("replace value of node _ROOT_/dir[@user='${username}' and @dirname='$workdirEscaped'] with '${prefsSerialized}'")
			} else {
				xmlDatabase.returnXQueryAsString("insert node <dir user='$username' dirname='${workdirEscaped}' >$prefsSerialized</dir> into _ROOT_")
			}
			
			
		} else {
			X4MLPreferences4DesktopMode.setWorkDirPrefsObject(desktopModeProjectDir, workdir, prefs)
		}
		
	}
	
	public static Map fetchPersistedPrefs(String username, String workdir) {
		
		if (SERVERMODE) {
			String workdirEscaped = escape4XML(workdir)
			String prefsStringSerialized = xmlDatabase.returnReadOnlyXQueryAsString("_ROOT_/dir[@user='${username}' and @dirname='$workdirEscaped']/text()")
			if (!prefsStringSerialized) return [:]
			def jsonSlurper = new JsonSlurper()
			return jsonSlurper.parseText(unescape4XML(prefsStringSerialized))
		} else {
			return X4MLPreferences4DesktopMode.getWorkDirPrefsObject(desktopModeProjectDir, workdir)
		}
		
	}
	
//	public static boolean createWorkdirIfNotExists(String username, String workdir) {
//		File workdirFolder = getWorkdirFolder(username, workdir)
//		if (!workdirFolder.exists()) {
//			workdirFolder.mkdirs()
//		}
//	}
	
	private static boolean deleteWorkdir(String username, String workdir) {
		File workdirFolder = getWorkdirFolder(username, workdir)
		workdirFolder.deleteDir()
	}
	
	public static File getFileInWorkdir(String filename, String username, String workdir, boolean isResource = false) {
		File folder = isResource ? getWorkdirResourcesFolder(username, workdir) : getWorkdirFolder(username, workdir)
		def thefile = new File(folder, filename)
		// println("getFileInWorkdir returns: ${thefile.getAbsolutePath()}")
		thefile
	}

	public static void deleteFileInWorkdir(String filename, String username, String workdir, boolean isResource = false) {
		getFileInWorkdir(filename, username, workdir, isResource).delete()
	}
	
	private static String createFileInWorkdir(String fname, String username, String workdir) {
		if (!listWorkdirFiles(username, workdir).contains(fname)) {
			boolean success = getFileInWorkdir(fname, username, workdir).createNewFile()
			return success ? null : 'File creation failed for unknown reasons.';
		} else {
			return 'File creation failed. Use an allowed suffix; make sure the file does not exist yet.'
		}
	}
	
	public static String getFileContentInWorkdir(String filename, String username, String workdir, boolean isResource = false) {
		if (!filename) {
			return ''
		} else {
			return getFileInWorkdir(filename, username, workdir, isResource).getText('UTF-8')
		}
		
	}
	
	public static void setFileContentInWorkdir(String filename, String content, String username, String workdir, boolean isResource = false) {
		getFileInWorkdir(filename, username, workdir, isResource).setText(content, 'UTF-8')
	}
	
	
	public static Map getAllUserFiles(String username) {
		def result = [:]
		
		getAllWorkdirs(username).each {
			workdir ->
			result[workdir] = [
				files: listWorkdirFiles(username, workdir),
				resfiles: listWorkdirFiles(username, workdir, true)
			]
		}
		
		result
	}
	
	public static List getAllWorkdirs(String username) {
		if (SERVERMODE) {
			xmlDatabase.returnReadOnlyXQueryAsString("_ROOT_/dir[@user='${username}']/@dirname/string()")
				.split('\n')
				.sort()
		} else {
			X4MLPreferences4DesktopMode.getAllWorkdirs(desktopModeProjectDir)
		}
	}

	/*
	 * returns workdir File object regardless of whether the folder exists previously
	 */
	private static File getWorkdirFolder(String username, String workdir) {
		File folder
		if (SERVERMODE) {
			folder = new File(X4MLDIR + File.separator + 'userdata' + File.separator + username + File.separator + workdir)
		} else {
			folder = new File(desktopModeProjectDir + File.separator + workdir)
		}
		
		folder.mkdirs()
		return folder

	}
	
	public static List<String> listWorkdirFiles(String username, String workdir, boolean resourcesFolder = false) {
		File folder = resourcesFolder ? getWorkdirResourcesFolder(username, workdir) : getWorkdirFolder(username, workdir)
		def suffixes = resourcesFolder ? UpdateManager.knownResourcesSuffixes : UpdateManager.knownSuffixes
		folder
			.listFiles()
			.grep{Files.isWritable(it.toPath())}
			.collect{it.name}
			.grep{String fname -> suffixes.any{fname.endsWith('.' + it)}}
			.sort()
	}

	// für File-Upload nach /resources; eigentliches Speichern in WebApp mit
	//   FileUtil.streamToFile(uploadedFile.content(), "resources/" + uploadedFile.filename()));
	public static File getWorkdirResourcesFolder(String username, String workdir) {
		
		File workdirFolder = getWorkdirFolder(username, workdir)
		File resourcesDir = new File(workdirFolder, 'resources')
		resourcesDir.mkdirs()
		return resourcesDir
		
	}
	


	private static String escapeQuotes(String text) {

		text.replace("'", "''")

	}
	
	private static String escape4XML(String raw) {
		raw
			.replace('&', '&amp;')
			.replace('<', '&lt;')
			.replace('>', '&gt;')
			.replace('"', '&quot;')
			.replace('\'', '&apos;')
			
	}
	
	
	private static String unescape4XML(String escaped) {
		escaped
			.replace('&lt;', '<')
			.replace('&gt;', '>')
			.replace('&quot;', '"')
			.replace('&apos;', '\'')
			.replace('&amp;', '&')
			
	}


	
	
}
