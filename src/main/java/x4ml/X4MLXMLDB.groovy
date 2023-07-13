package x4ml

import org.basex.core.Context
import org.basex.core.cmd.XQuery
import groovy.transform.Synchronized


/**
 * 
 * 
 * 
 * 
 * persistence for server mode: User data; persistent data for user dirs
 * 
 * 	<user name="test" password="pw"></user>
	<dir user="username" dirname="playground">$dictObject</dir>

	$dictObject ist derzeit:
	{
      "lemmapath": "/entry/lemma",
      "dictname": "My first dictionary"
	}

 *
 */
class X4MLXMLDB {
	
	
	private Context context = null
	public final dbWriteLock = new Object()
	
	private final String DBNAME = 'x4mldb'
	
	public X4MLXMLDB(String dbDirectory) {
		
		System.setProperty("org.basex.DBPATH", dbDirectory + File.separator + 'db')
		System.setProperty("org.basex.path", dbDirectory)
		context = new Context(false) // false: no file used for context info!
		
		println("Base directory set to $dbDirectory")
		
		if (X4MLMain.SERVERMODE) {
			String dbExists = returnReadOnlyXQueryAsString("""db:exists("$DBNAME")""")
			if (dbExists != 'true') {
				returnXQueryAsString("""db:create('$DBNAME', <root/>, 'root.xml', map {'textindex': false(), 'attrindex': false(), 'tokenindex': false(), 'updindex': false(), 'autooptimize': false()})""")
			}
		}
		
		
	}
	
	
	/**
	 * XPath-Präfix für "Dokumentelemente" einer DB (convenience)
	 * @return db:open('x4mldb')/root
	 */
	public String db() {
		"db:open('${DBNAME}')/root"
	}

	
	@Synchronized("dbWriteLock")
	public String returnXQueryAsString(String query) {
		def res
		String expandedQuery = query.replace('_ROOT_', db())
		try {
			res = new XQuery(expandedQuery).execute(context)
		} catch (e) {
			println "\n\n$expandedQuery\n"
			println("FEHLER: ${e.message}")
			throw new Exception(e.message)
		}
		res
	}
	
	// Nurlese-Op muss nicht synchronisiert werden
	public String returnReadOnlyXQueryAsString(String query) {
		String expandedQuery = query.replace('_ROOT_', db())
		def res = new XQuery(expandedQuery).execute(context)
		res
	}
	
	/**
	 *
	 * alle Argumente müssen XML-kompatibel sein (escape4XML bereits angewendet)
	 * TODO Prüfen, ob User vorhanden?
	 *
	 */
	@Synchronized("dbWriteLock")
	public boolean addUser(String name, String password) {
		
		returnXQueryAsString("""insert node <user name="$name" hash="$passwordHash" salt="$salt" role="$role"/> into ${db('USER', true)}""")

		true
	}

	
	// TODO
	@Synchronized("dbWriteLock")
	public String changePassword(String name, String passwordHash, String salt, String role=null) {
		
		try {
			returnXQueryAsString("""replace value of node ${db('USER')}[@name="$name"]/@hash with '$passwordHash'""")
			returnXQueryAsString("""replace value of node ${db('USER')}[@name="$name"]/@salt with '$salt'""")
			if (role) {
				returnXQueryAsString("""replace value of node ${db('USER')}[@name="$name"]/@role with '$role'""")
			}
		} catch (e) {
			return '_error_'
		}
		return 'ok'
		
	}

	
}
