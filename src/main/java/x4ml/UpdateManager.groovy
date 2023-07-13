package x4ml

import processors.DTDValidator
import processors.HTMLMaker
import processors.Processor
import processors.RelaxNGCompactValidator
import processors.RelaxNGValidator
import processors.XMLParser
import processors.XQueryExecutor

class UpdateManager {
	
	private static Map<String, Processor> suffixToProcessor = [
		'xml': new XMLParser(),
		'dtd': new DTDValidator(),
		'xpath': new XQueryExecutor(),
		'html': new HTMLMaker(),
		'rng': new RelaxNGValidator(),
		'rnc': new RelaxNGCompactValidator()
	]
	
	public static final XMLParser xmlProcessor = suffixToProcessor['xml']
	public static final HTMLMaker htmlProcessor = suffixToProcessor['html']
	public static final XQueryExecutor xpathProcessor = suffixToProcessor['xpath']
	
	
	public static final Set knownSuffixes = suffixToProcessor.keySet()
	public static final Set knownResourcesSuffixes = X4MLMain.suffixToMIMEAndBinary.keySet()
	
	

	
	/**
	 *
	 * xmlContent ist das XML, auf das der Inhalt angewendet wird; bei XML selber immer als null zu setzen
	 *
	 */
	private static String processFileContent(String content, String fname, String xmlContent, procsObject, boolean processAllXMLs = false) {
		
		if (!content ||
			!fname ||
			(!xmlContent && !fname.endsWith('.xml'))) {
			return ''
		} else if (!processAllXMLs && xmlContent && procsObject.isMostRecentNotWellFormed) {
			return '(No output possible: XML is not well-formed.)'
		} else {
			def suffix = fname[(1+fname.lastIndexOf('.'))..-1]
			suffixToProcessor[suffix].process(content, xmlContent, procsObject)
		}
		
	}
	
	/*
	 * In case a class contains more than one static synchronized method, only one thread
	 * can execute inside any of these methods at the same time.
	 *
	 * status is a Groovy map corresponding to a JSON object sent by the browser
	 * this is invoked via POST whenever content of editor panes changes OR new files are selected
	 *     OR file contents are to be changed server-side, e.g. pretty-printing
	 * leads to saving files AND executing parsers/transformers/validators
	 *
	 * keys and values
	 * 		
	 * 		command: update|fetch|create|delete|prettify|(empty)
	 * 		???timestamp: of request, browserside: Date.now() oder window.performance.now()
	 * 		xmlFile: name 'xxx.xml' for currently edited XML file, null otherwise
	 * 		xmlContent: String content of file xmlFile if there is one, null otherwise
	 * 		otherFile: name 'xxx.dtd' / .rnc / .xquery / .html / .xsl for second file, null otherwise
	 * 		otherContent: String content of file otherFile if there is one, null otherwise
	 *
	 *
	 * returns map:
	 * 		
	 *      ???timestamp: repetition of request
	 * 		files: list of strings for file names in current working dir
	 * 		xmlContent: of xmlFile, or null
	 * 		otherContent: of second file, or null
	 * 		xmlMsg: XML parser message
	 * 		otherMsg: DTD/RelaxNG parser errors/validation errors;
	 * 			XPath or XQuery responses or errors;
	 * 			HTML for .html with template pseudocode;
	 * 			XSL transform
	 *
	 */
	private static update(status, String username, String workdir) {
		
		def procsObject = X4MLMain.getProcsObject(username, workdir)
		
		//println("procsObject: $procsObject  ; class is ${procsObject.getClass()}")
		
		
		switch (status.command) {
			case ~/^fetch.*$/:
			case 'prettifyxml':
			case 'prettifyhtml':
				def xmlfname = status.xmlFile
				def xmlContent = X4MLMain.getFileContentInWorkdir(xmlfname, username, workdir)
				if (status.command == 'prettifyxml') {
					xmlContent = xmlProcessor.mostRecentXMLPrettyPrinted(
						procsObject
					)
				}
				String otherfname = status.otherFile
				X4MLMain.getProcsObject(username, workdir).lastFetchedOtherFileName = otherfname
				
				def otherContent = X4MLMain.getFileContentInWorkdir(otherfname, username, workdir)
				if (status.command == 'prettifyhtml') {
					otherContent = htmlProcessor.mostRecentHTMLPrettyPrinted(procsObject)
				}
				def xmlMsg = processFileContent(xmlContent, xmlfname, null, procsObject)
				def otherMsg = processFileContent(otherContent, otherfname, xmlContent, procsObject)
				
				return [files: X4MLMain.listWorkdirFiles(username, workdir),
					xmlContent: xmlContent,
					xmlMsg: xmlMsg,
					otherContent: otherContent,
					otherMsg: otherMsg]
			case 'update':
				def xmlfname = status.xmlFile
				def xmlContent = status.xmlContent
				def xmlMsg = ''
				def otherMsg = ''
				if (xmlfname) {
					X4MLMain.setFileContentInWorkdir(xmlfname, xmlContent, username, workdir) 
					xmlMsg = processFileContent(xmlContent, xmlfname, null, procsObject)
				}
				def otherfname = status.otherFile
				def otherContent = status.otherContent
				if (otherfname) {
					otherMsg = processFileContent(otherContent, otherfname, xmlContent, procsObject)
					X4MLMain.setFileContentInWorkdir(otherfname, otherContent, username, workdir)
				}
				return [files: X4MLMain.listWorkdirFiles(username, workdir),
					xmlMsg: xmlMsg,
					otherMsg: otherMsg]
			case 'allxmlxqdb':
				def otherfname = status.otherFile
				def otherContent = status.otherContent
				def otherMsg = queryXmlDB(otherContent, username, workdir)
				return [files: X4MLMain.listWorkdirFiles(username, workdir),
					otherMsg: otherMsg]
			case 'allxml':
				def otherMsg = ''
				def otherfname = status.otherFile
				def otherContent = status.otherContent
				if (otherfname) {
					otherMsg = X4MLMain.listWorkdirFiles(username, workdir)
						.grep{it.endsWith('.xml')}
						.collect {
							def xmlContent = X4MLMain.getFileContentInWorkdir(it, username, workdir)
							def msg = processFileContent(otherContent, otherfname, xmlContent, procsObject, true)
							return """\n=====================
$it
=====================
$msg\n\n"""}
						.join('\n')
					
				}
				return [files: X4MLMain.listWorkdirFiles(username, workdir),
					otherMsg: otherMsg]
			case ~/^create.*$/:
				def newFileName = status.command[6..-1].trim()
				def error = X4MLMain.createFileInWorkdir(newFileName, username, workdir)
				if (error) {
					return [error: error]
				} else {
					if (newFileName.endsWith('.xml')) {
						def xmlfilecontent = '<?xml version="1.0" encoding="UTF-8" ?>\n<entry>This is a very simple sample XML document.</entry>'
						X4MLMain.getFileInWorkdir(newFileName, username, workdir).setText(xmlfilecontent, 'UTF-8')
						def xmlMsg = processFileContent(xmlfilecontent, newFileName, null, procsObject)
						def otherMsg = processFileContent(status.otherContent, status.otherFile, xmlfilecontent, procsObject)
						return [files: X4MLMain.listWorkdirFiles(username, workdir),
							xmlContent: xmlfilecontent,
							xmlMsg: xmlMsg,
							otherMsg: otherMsg]
					} else {
						def otherfilecontent
						
						if (newFileName.endsWith('.html')) {
							otherfilecontent = '''<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>your title here</title>
  </head>
  <body>
    <p>This is a very minimal sample HTML document.</p>
  </body>
</html>'''
						} else if (newFileName.endsWith('.xpath')) {
							otherfilecontent = '/put/your/own/xpath/expression/here\n\n(:\nOnly one XPath query per file!\nIf you have multiple expressions,\n"store" them in this comment.\nYou may then copy whichever expression you want to use\nto the place  above the comment.\n:)\n'
						} else if (newFileName.endsWith('.dtd')) {
							otherfilecontent = '<!-- this is a minimal DTD -->\n<!ELEMENT someelement ANY>'
						} else {
							otherfilecontent = ''
						}
						
						
						X4MLMain.getFileInWorkdir(newFileName, username, workdir).setText(otherfilecontent, 'UTF-8')
						X4MLMain.getProcsObject(username, workdir).lastFetchedOtherFileName = newFileName
						def otherMsg = processFileContent(otherfilecontent, newFileName, status.xmlContent, procsObject)
						def xmlMsg = processFileContent(status.xmlContent, status.xmlFile, null, procsObject)
						return [files: X4MLMain.listWorkdirFiles(username, workdir),
							otherContent: otherfilecontent,
							xmlMsg: xmlMsg,
							otherMsg: otherMsg]

					}
				}
				
			default:
			return [error: 'An unexpected programming error occurred: INVALID_COMMAND']
		}
		
		
	}

	private static String queryXmlDB(String query, String username, String workdir, boolean jsonSeparation = false) {
		List<String>  xmls = [] // XML, but only well-formed
		for (String fname : X4MLMain.listWorkdirFiles(username, workdir).grep{it.endsWith('.xml')}) {
			def xml = null
			def fcontent = X4MLMain.getFileContentInWorkdir(fname, username, workdir)
			try {
				xml = new XmlSlurper().parseText(fcontent)
			} catch (e) {
				//e.printStackTrace()
			}
			if (xml) {
				xmls << fcontent
			}
		}
		xpathProcessor.processQueryAgainstDB(query, xmls, jsonSeparation)
	}
	
	
}
