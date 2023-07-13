package x4ml

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.util.FileUtil


class X4MLWebApp {
	
	private Javalin app
	private int javalinPort
	
	private final String SESSIONUSERNAMEATTRIBUTEKEY = 'x4mluser'
	
	
	
	
	public X4MLWebApp(Integer port) {
		
		startServer(port ?: 0) // 0 = will be assigned randomly on startup (for Desktop Mode)
		javalinPort = app.port()
	}
	
	public URI getDesktopModeURL(String username) {
		new URI("http://localhost:" + javalinPort + "/mainpage?workdir=playground&username=$username")
	}
	
	
	private void stopServer() {
		app.stop()
	}
	
	
	
	private void startServer(int port) {
		
		
		
		if (app) {
			return
		}
		
		System.addShutdownHook {
			stopServer()
		}
		
		app = Javalin.create({
			config ->
			config.showJavalinBanner = false
			//config.enableDevLogging()
		}).start(port)
		
		
//		app.before {
//			ctx ->
//			println ('request path is ' + ctx.path())
//			println ('request method is ' + ctx.method())
//		}
		
		app.get("/", {
			ctx ->
			if (X4MLMain.SERVERMODE) {
				String username = getSessionUser(ctx)
				if (username) {
					ctx.redirect('/mainpage?username=' + username + '&workdir=' + X4MLMain.DEFAULTDIR)
				} else {
					ctx.redirect('/login')
				}
				
			} else {
				ctx.result('ERROR: Not supported in Desktop mode.')
			}
			
		})
		
		// ?workdir=…&username=…
		app.get("/mainpage", {
			ctx ->
			if (X4MLMain.SERVERMODE && !getSessionUser(ctx)) {
				ctx.redirect('/login')
			} else {
				if (!X4MLMain.SERVERMODE) {
					// no login page for Desktop mode; therefore, session att. username is set here
					ctx.sessionAttribute(SESSIONUSERNAMEATTRIBUTEKEY, X4MLMain.DESKTOPUSER)
				}
				fetchAssets(ctx, 'assets', 'mainpage.html')
			}
			
			
		})
		
		
		// ?username=...
		app.get("/workdirmanager", {
			ctx ->
			if (!getSessionUser(ctx)) {
				ctx.status(404)
				return
			}
			fetchAssets(ctx, 'assets', 'workdirmanager.html')
			
		})
		
		
		app.get("/login", {
			ctx ->
			if (X4MLMain.SERVERMODE) {
				fetchAssets(ctx, 'assets', 'loginpage.html')
			} else {
				ctx.result('ERROR: Not supported in Desktop mode.')
			}
		})
		
		
		
		// nur im Servermode
		app.post("/login", {
			ctx ->
			def user = ctx.formParam('user')
			def pw = ctx.formParam('pw')
			if (X4MLMain.authenticateAdminUser(user, pw)) {
				ctx.sessionAttribute(SESSIONUSERNAMEATTRIBUTEKEY, user)
				ctx.redirect('/users')
			} else if (X4MLMain.authenticateUser(user, pw)) {
				ctx.sessionAttribute(SESSIONUSERNAMEATTRIBUTEKEY, user)
				ctx.redirect('/')
			} else {
				ctx.redirect('/login')
			}
		})

		
		app.post("/deleteFile", {
			ctx ->
			def username = getSessionUser(ctx)
			if (!username) {
				ctx.status(404)
			}
			def requestData = new JsonSlurper().parseText(ctx.body())
			
			if (requestData.file == null) {
				X4MLMain.removeWorkdirAndPersist(username, requestData.workspace)
			} else {
				X4MLMain.deleteFileInWorkdir(requestData.file, username, requestData.workspace, requestData.isResource)
			}
			
			
		})
		
		app.get("/users", {
			ctx ->
			def username = getSessionUser(ctx)
			if (!username || username != X4MLMain.ADMINNAME) {
				ctx.redirect('/')
			} else {
				def htmlUsersList = X4MLMain.getServerUsers().collect {
					"<li>$it</it>"
				}.join('\n')
				ctx.html("""<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Administration</title>
  </head>
  <body>
	<h1>User Administration</h1>
	<p>Existing user accounts:</p>
    <ul>
		$htmlUsersList
	</ul>
	<p onclick='execcmd()'>Click here to execute command.</p>
<script>
	function execcmd() {
		var cmd = window.prompt("Enter a command: adduser|changepw username 'password'; deleteuser username - usernames should only have ASCII chars!");
	    fetch('/user', {
         	    	  method: 'POST',
         	    	  body: cmd 
        })
        .then(x => x.text())
        .then(data => {window.alert('Done. Refresh window to see results.');});
    }
</script>
  </body>
</html>""")
			}
			
		})
		
		app.post("/user", {
			ctx ->
			def username = getSessionUser(ctx)
			if (!username || username != X4MLMain.ADMINNAME) {
				ctx.status('404')
			} else {
				String cmd = ctx.body().trim()
				// adduser|changepw username 'password'; deleteuser username
				def cmdparts = cmd.split('\\s+', 3)
				switch (cmdparts[0]) {
					case 'adduser':
						def user = cmdparts[1]
						def password = cmdparts[2][1..-2] // strip enclosing quotes
						X4MLMain.addServerUser(user, password)
						break
					case 'changepw':
						def user = cmdparts[1]
						def password = cmdparts[2][1..-2] // strip enclosing quotes
						X4MLMain.changeServerUserPassword(user, password)
						break
					case 'deleteuser':
						def user = cmdparts[1]
						X4MLMain.removeServerUser(user)
						

				}
			}
		})

		// generic assets; no auth required
		app.get("/assets/{fname}", {
			ctx ->
			
			def fname = ctx.pathParam('fname')
			fetchAssets(ctx, 'assets', fname)
			
			
		})

		/*
		 * im einzigen "zugelassenen" Unterordner resources im working dir
		 * nur bestimmte binäre Ressourcen zugelassen, siehe suffixToMIME
		 * wir können hier auf Prüfung der Aktualität der workdir-Session verzichten,
		 * da vorher immer schon die Prüfung bei /entry/{workdir} vorgenommen wird
		 * ACHTUNG: Im HTML muss es immer src="resources/xxx.jpg" sein,
		 * 		also relativer Pfad! nicht: /resources !
		 */
		app.get("/entry/{workdir}/resources/{fname}", {
			Context ctx ->
			def username = getSessionUser(ctx) 
			def fname = ctx.pathParam('fname')
			def workdir = ctx.pathParam('workdir')
			def suffixpos = fname.lastIndexOf('.')
			def suffix = fname[(suffixpos+1)..-1]
			def mimeAndBinary = X4MLMain.suffixToMIMEAndBinary[suffix]
			if (!mimeAndBinary) {
				ctx.status(404)
			} else {
				File rsrc = X4MLMain.getFileInWorkdir(fname, username, workdir, true)
				if (!rsrc.exists()) {
					ctx.status(404)
				} else {
					ctx.header('Content-Type', "${mimeAndBinary[0]}; charset=utf-8")
					if (!mimeAndBinary[1]) {
						// Text
						ctx.result(rsrc.getText('UTF-8'))
					} else {
						ctx.result(rsrc.getBytes())
					}
				}
			}
		})
		
		/*
		 *    /getpref?key=dictname&uuid=1234455&workdir=playground
		 */
		app.get("/getpref", {
			Context ctx ->
			
			def uuid = ctx.queryParam('uuid')
			def workdir = ctx.queryParam('workdir')
			def key = ctx.queryParam('key')
			
			if (!isAuthenticatedAndHasCurrentWorkdirSession(ctx, workdir, uuid)) {
				ctx.status(404)
			} else {
				ctx.result(X4MLMain.getPref(getSessionUser(ctx), workdir, key))
			}
			
			
		})
		
		/*
		 *    /setpref?key=dictname&value=bla&uuid=1234455&workdir=playground
		 */
		app.get("/setpref", {
			Context ctx ->
			def uuid = ctx.queryParam('uuid')
			def workdir = ctx.queryParam('workdir')
			if (!isAuthenticatedAndHasCurrentWorkdirSession(ctx, workdir, uuid)) {
				ctx.status(404)
				return
			}			

			def key = ctx.queryParam('key')
			def value = ctx.queryParam('value')
			
			X4MLMain.setPref(getSessionUser(ctx), workdir, key, value)
			ctx.result('ok')
		})
		
		
		
		app.get("/workdirs", {
			ctx ->
			def username = getSessionUser(ctx)
			//println("workdirs called with $username")
			ctx.json(JsonOutput.toJson(X4MLMain.getAllWorkdirs(username)))
		})
		
		
		app.get("/userfiles", {
			ctx ->
			def username = getSessionUser(ctx)
			if (!username) {
				ctx.status(404)
				return
			}
			ctx.json(X4MLMain.getAllUserFiles(username))
		})
		
		app.post("/uploadresources/{workdir}", {
			ctx ->
			def username = getSessionUser(ctx)
			if (!username) {
				ctx.status(404)
				return
			}
			def workdir = ctx.pathParam('workdir')
			// 'files' corresponds to field-name="files" in <q-uploader> 
			ctx.uploadedFiles("files").each {
				uploadedFile ->
				FileUtil.streamToFile(
					uploadedFile.content(),
					X4MLMain.getWorkdirResourcesFolder(username, workdir).getAbsolutePath() + 
						File.separator + uploadedFile.filename()
				)
			}
		})

		
		/*
		 * 		/dictionary?uuid=1234455&workdir=playground
		 */
		app.get("/dictionary", {
			Context ctx ->
			
			
			def uuid = ctx.queryParam('uuid')
			def workdir = ctx.queryParam('workdir')
			if (!isAuthenticatedAndHasCurrentWorkdirSession(ctx, workdir, uuid)) {
				ctx.status(404)
				return
			}

			def username = getSessionUser(ctx)
			
			def htmlTemplateFileName = X4MLMain.getProcsObject(username, workdir).lastFetchedOtherFileName
			if (!htmlTemplateFileName || !(htmlTemplateFileName.endsWith('.html'))) {
				ctx.html('<h1>An error has occurred.</h1>')
			}

			def allXMLFiles = X4MLMain.listWorkdirFiles(username, workdir).grep{it.endsWith('.xml')}
			
			Map<String, String> file2Lemma = [:]
			
			def lemmaXPath = X4MLMain.getPref(username, workdir, 'lemmaxpath')
			
			if (lemmaXPath) {

				allXMLFiles.each{
					String filename ->
					def xmlContent = X4MLMain.getFileContentInWorkdir(filename, username, workdir)
					def lemmaQueryResultTuple = UpdateManager.xpathProcessor.processCore(lemmaXPath, xmlContent, false)
					def filenameWithoutSuffix = filename[0..-5]
					file2Lemma[filenameWithoutSuffix] = lemmaQueryResultTuple.getV2() ?
						"[$filenameWithoutSuffix]" :		// XPath-Resultat leer oder Fehler
						lemmaQueryResultTuple.getV1()
					
						
				}
	
			} else {
				allXMLFiles.each{
					String filename ->
					def filenameWithoutSuffix = filename[0..-5]
					file2Lemma[filenameWithoutSuffix] = "[$filenameWithoutSuffix]"
					
						
				}
			}
			
			def dictName = X4MLMain.getPref(username, workdir, 'dictname') ?: '[Dictionary without Title]'
			
			
			//String workdirEscapedAsPathSegment = UrlEscapers.urlFragmentEscaper().escape(workdir)
			
			ctx.html("""<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${dictName}</title>
    <link rel="stylesheet" href="/assets/bulma.min.css">
    <style>
      .lemma {cursor: pointer;}
      .lemma:hover {background-color: #DDD;}
      .current {color: blue;}
    </style>
  </head>
  <body>
  <section class="has-background-info-light" style="height:15vh;display:flex;justify-content:center;align-items:center;">

      <h4 class="is-size-4">
        ${dictName}
      </h4>

  </section>


<section style="height:85vh;">

<div class="columns" style="height:100%;">
  <div class="column is-3 m-5" style="overflow:auto;">
	<ul style="height:100%;">
		${file2Lemma.keySet().sort{it.toLowerCase(Locale.ROOT)}.collect{"<li data-lemma='${it}' class='is-size-4 lemma' onclick='highlightLemma(\"${it}\")'>${file2Lemma[it]}</li>"}.join('\n')}
	</ul>
  </div>
  <div class="column m-5" style="overflow:auto;">
    <div class="container" style="height:100%;">
	<iframe id="iframe" style="border:none;width:100%;height:100%;"></iframe>
    </div>
  </div>
</div>

</section>
<script>
   function highlightLemma(fname) {

	  // was:  + "?uuid=$uuid"
      document.getElementById("iframe").src="/entry/" + encodeURIComponent("$workdir") + "/" + encodeURIComponent(fname);

      document.querySelectorAll('.lemma').forEach(lem => {
        if (lem.dataset.lemma === fname) {lem.classList.add("current")} else {lem.classList.remove("current")}
     });
      
   }
</script>
  </body>
</html>""")
			
		})
		
		
		// /init mit payload workdir=…&uuid=…
		app.post('/init', {
			ctx ->
			def username = getSessionUser(ctx)
			if (!username) {
				ctx.status('404')
				return
			}
			def requestData = new JsonSlurper().parseText(ctx.body())
			def workdir = requestData.workdir
			def uuid = requestData.uuid
			def infos4Browser = X4MLMain.startNewSession(username, workdir, uuid)
			ctx.json(JsonOutput.toJson(infos4Browser))
		})
		
		// zur Payload gehören auch keys: workdir, uuid
		app.post('/update', {
			ctx ->
			def username = getSessionUser(ctx)
			def requestData = new JsonSlurper().parseText(ctx.body())
			def workdir = requestData.workdir
			def uuid = requestData.uuid
			if (!isAuthenticatedAndHasCurrentWorkdirSession(ctx, workdir, uuid)) {
				ctx.status(404)
				return
			}
			ctx.json(JsonOutput.toJson(
				UpdateManager.update(requestData, username, workdir)
			))
		})
		
		
//		// query params uuid, workdir
//		app.post('/renderHTML', {
//			ctx ->
//			
//			def uuid = ctx.queryParam('uuid')
//			def workdir = ctx.queryParam('workdir')
//			if (!isAuthenticatedAndHasCurrentWorkdirSession(ctx, workdir, uuid)) {
//				ctx.status(404)
//				return
//			}
//			def username = getSessionUser(ctx)
//			def html = X4MLMain.getProcsObject(username, workdir).mostRecentRealHTML
//			ctx.html(html)
//			 
//		})
		
		/**
		 * text files only
		 * ?resource=true: in resources dir
		 */
		app.get('/workdirfile/{workdir}/{fname}', {
			Context ctx ->
			
			def username = getSessionUser(ctx)
			if (!username) {
				ctx.status(404)
				return
			}
			def workdir = ctx.pathParam('workdir')
			def fname = ctx.pathParam('fname')
			
			boolean isResource = (ctx.queryParam('resource') == 'true')
			ctx.result(X4MLMain.getFileContentInWorkdir(fname, username, workdir, isResource)) 
			
		})
		
		
		/**
		 * xmlfname: XML file name !!!without '.xml' suffix part!!!
		 * for dictionary and HTML template rendering!
		 * 
		 */
		app.get('/entry/{workdir}/{xmlfname}', {
			Context ctx ->
			
			
			
			def workdir = ctx.pathParam('workdir')
			
			/*
			def uuid = ctx.queryParam('uuid')
			if (!isAuthenticatedAndHasCurrentWorkdirSession(ctx, workdir, uuid)) {
				ctx.status(404)
				return
			}
			*/
			
			def username = getSessionUser(ctx)
			if (!username) {
				ctx.status(404)
				return
			}
			
			def xmlfname = ctx.pathParam('xmlfname') + '.xml'
			def xmlContent = X4MLMain.getFileContentInWorkdir(xmlfname, username, workdir)
			def htmlTemplateFileName = X4MLMain.getProcsObject(username, workdir).lastFetchedOtherFileName
			if (!xmlContent || !htmlTemplateFileName || !(htmlTemplateFileName.endsWith('.html'))) {
				ctx.html('<h1>An error has occurred.</h1>')
			}
			def htmlTemplateContent = X4MLMain.getFileContentInWorkdir(htmlTemplateFileName, username, workdir)
			def html = UpdateManager.htmlProcessor.processCore(htmlTemplateContent, xmlContent)
			ctx.html(html)
			
			
		})
		
		// query params uuid, workdir
		app.get('/validateHTML', {
			ctx ->
			//def requestData = ctx.body() // reiner String
			
			def uuid = ctx.queryParam('uuid')
			def workdir = ctx.queryParam('workdir')
			if (!isAuthenticatedAndHasCurrentWorkdirSession(ctx, workdir, uuid)) {
				ctx.status(404)
				return
			}

			def username = getSessionUser(ctx)

			try {
				def post = new URL("https://html5.validator.nu/").openConnection() as HttpURLConnection
				def message = X4MLMain.getProcsObject(username, workdir).mostRecentRealHTML
				post.setRequestMethod('POST')
				post.setDoOutput(true)
				post.setRequestProperty("Content-Type", "text/html; charset=utf-8")
				post.getOutputStream().write(message.getBytes("UTF-8"));
				def postRC = post.getResponseCode();
				if (postRC.equals(200)) {
					def html = post.getInputStream().getText()
					ctx.html(html)
				} else {
					ctx.html('SERVICEERROR: The online service for validating HTML5 responded with an error message.')
				}
			} catch (e) {
				ctx.html('The online service for validating HTML5 was unreachable.\nPlease check your Internet connection.')
			}
			
			 
		})
		
		
		
		
	
	}
	
	
	private void fetchAssets(Context ctx, String pathPrefix, String fname) {
		def suffixpos = fname.lastIndexOf('.')
		def suffix = fname[(suffixpos+1)..-1]
		def mimeAndBinary = X4MLMain.suffixToMIMEAndBinary[suffix]
		ctx.header('Content-Type', "${mimeAndBinary[0]}; charset=utf-8")
		if (!mimeAndBinary[1]) {
			// Text
			ctx.result(X4MLWebApp.class.getResourceAsStream("public/$pathPrefix/" + fname).getText('UTF-8'))
		} else {
			ctx.result(X4MLWebApp.class.getResourceAsStream("public/$pathPrefix/" + fname))
		}
	}
	
	private String getSessionUser(Context ctx) {
		ctx.sessionAttribute(SESSIONUSERNAMEATTRIBUTEKEY)
	}
	
	private boolean isAuthenticatedAndHasCurrentWorkdirSession(Context ctx, String workdir, String uuid) {
		String user = getSessionUser(ctx)
		if (!user) {
			return false
		} 
		X4MLMain.checkIsCurrentWorkdirSession(user, workdir, uuid)
	}
	
	
	
	
}
