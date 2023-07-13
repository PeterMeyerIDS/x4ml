package x4ml

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.nio.file.Files
import java.nio.file.Path
import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences

class X4MLPreferences4DesktopMode {
	
	
	
	private static final String X4MLPREFSPATHPREFIX = "/org/ids/pm/x4ml"
	
	

	public static File getProjectDirOrNull() {
		
		Preferences x4mlPrefs = Preferences.userRoot().node(X4MLPREFSPATHPREFIX)
		
		
		String projectDirPathString = null;
		try {
			projectDirPathString = x4mlPrefs.get("projectdir", null)
		} catch (Exception e) {
			println("No prefs found: " + e.getLocalizedMessage())
		}
		if (projectDirPathString != null) {
			File projectDir = new File(projectDirPathString)
			Path projectDirPath = projectDir.toPath()
			if (projectDir.exists() && Files.isReadable(projectDirPath) && Files.isWritable(projectDirPath)) {
				return projectDir
			} else {
				return null
			}
		} else {
			return null
		}
	}
	
	
	public static void setProjectDir(File f) throws BackingStoreException {
		
		Preferences x4mlPrefs = Preferences.userRoot().node(X4MLPREFSPATHPREFIX)
		
		x4mlPrefs.put("projectdir", f.getAbsolutePath())
		x4mlPrefs.flush();
		
	}
	
	private static getConfigFile4ProjectDir(String projectdir) {
		new File(new File(projectdir), 'x4ml-config')
	} 
	
	/**
	 * 
	 * returns Map [workdir1: prefsObj1, workdir2: prefsObj2, ...]
	 */
	private static Map getPrefsObjects4Workdirs(String projectdir) {
		File configFile = getConfigFile4ProjectDir(projectdir)
		if (!configFile.exists()) {
			return [:]
		}
		def jsonContent = configFile.getText('UTF-8')
		return new JsonSlurper().parseText(jsonContent)
	}
	
	public static Map removeWorkdirFromPrefsObjects4Workdirs(String projectdir, String workdir) {
		File configFile = getConfigFile4ProjectDir(projectdir)
		if (!configFile.exists()) {
			return [:]
		}
		def jsonContent = configFile.getText('UTF-8')
		Map po = new JsonSlurper().parseText(jsonContent)
		po.remove(workdir)
		String newJson = JsonOutput.toJson(po)
		configFile.setText(newJson, 'UTF-8')
	}
	
	public static void setWorkDirPrefsObject(String projectdir, String workdir, Map prefsObject) {
		File configFile = getConfigFile4ProjectDir(projectdir)
		Map config = getPrefsObjects4Workdirs(projectdir)
		config[workdir] = prefsObject
		configFile.setText(JsonOutput.toJson(config))
	}
	
	public static Map getWorkDirPrefsObject(String projectdir, String workdir) {
		getPrefsObjects4Workdirs(projectdir)[workdir]
	}
	
	public static List getAllWorkdirs(String projectdir) {
		def workdirs = getPrefsObjects4Workdirs(projectdir).keySet() as List
		workdirs.sort()
	}
	


}
