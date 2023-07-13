package processors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document


class HTMLMaker implements Processor {
	
	
	public String processCore(String content, String xmlContent) {
		def xquery =  '``[' +
		escapeQuotes(
			replaceMoustaches(
				repeatMakeXQueryFor(
					replaceMoustaches(content)))) +
	']``'
	
		return BaseXInstance.doQuery(
			"xquery:eval('$xquery', map {'': parse-xml(``[${xmlContent}]``)})",
			'No results.',
			'An error has occurred:'
		).getV1()
	}
		
	@Override
	public String process(String content, String xmlContent, Object processingData) {
		
		processingData.mostRecentHTML = content
		
		
		def realHTML = processCore(content, xmlContent)
			
		processingData.mostRecentRealHTML = realHTML
		
		return realHTML
			
	}
	
	public String mostRecentHTMLPrettyPrinted(processingData) {
		if (processingData.mostRecentHTML) {
			Document doc = Jsoup.parse(processingData.mostRecentHTML)
			return doc.toString()
		} else {
			return null
		}
		
		
	}
	
	private String replaceMoustaches(String text) {
		text
			.replaceAll(/\{\{/, '`{')
			.replaceAll(/\}\}/, '}`')
	}
	
	private String escapeQuotes(String text) {

		text.replace("'", "''")

	}
	
	private String makeXQueryFor(String text) {

		
		/**
		 * whitespace between pseudo-commentaries is now \s, was [^\S\n] = 'whitespace without \n'
		 * advantage: no hassle with additional whitespace around separators
		 * disadv.: HTML is ugly
		 */

		
		def newtext1 = text.replaceAll(
			/(?s)[^\S\n]*<!--\s*repeat\s+for\s+all\s+\$(\p{L}+)\s+in\s+([^>]+?)\s+separated\s+by\s+'([^']+?)'\s*-->\s*(.*?)\s*<!--\s*end\s+repeat\s+for\s+all\s+\$\1\s*-->[^\S\n]*/,
			'`{for \\$$1 at \\$$1positionsuffixX4ML in $2 return ``[$4{{if (\\$$1positionsuffixX4ML lt count($2)) then \'<span>$3</span>\'}}]``}`'
		)
		def newtext2 = newtext1.replaceAll(
			/(?s)[^\S\n]*<!--\s*repeat\s+for\s+all\s+\$(\p{L}+)\s+in\s+(.+?)\s*-->\s*(.*?)\s*<!--\s*end\s+repeat\s+for\s+all\s+\$\1\s*-->[^\S\n]*/,
			'`{for \\$$1 in $2 return ``[$3]``}`'
		)

				
		return newtext2

	}
	
	private String repeatMakeXQueryFor(String text) {

		def newtext = makeXQueryFor(text)
		if (newtext != text) {
			return repeatMakeXQueryFor(newtext)
		} else {
			return newtext
		}
	}

	
}
