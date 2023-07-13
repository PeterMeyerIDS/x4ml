package processors

import groovy.xml.XmlSlurper

class XMLParser implements Processor {
	
	
	
	@Override
	public String process(String content, String xmlContent, Object processingData) {
		
		//println("processingData: $processingData")
		//println("processingData.mostRecentXMLString: ${processingData.mostRecentXMLString}")
		
		processingData.mostRecentXMLString = content
		
		String message = 'XML is well-formed.'
		
		
		
		try {
			processingData.mostRecentXML = new XmlSlurper(false, true, true).parseText(content)
			processingData.mostRecentNotWellFormed = false
		} catch(e) {
			message = 'XML is not well-formed.\n\n' + 
				e
					.toString()
					.replace('org.xml.sax.SAXParseException;', '')
					.trim()
			processingData.mostRecentXML = null
			processingData.mostRecentNotWellFormed = true
		}

		
		
		return message
		
	}
	
	public String mostRecentXMLPrettyPrinted(processingData) {
		
		if (processingData.mostRecentXML) {
			// prettify if XML was parseable at all!
			return groovy.xml.XmlUtil.serialize(processingData.mostRecentXML)
				.replaceAll(/(<\?xml[^>]*?>)/, '$1\n')
		} else {
			return processingData.mostRecentXMLString
		}
		
	}
	
	
}
