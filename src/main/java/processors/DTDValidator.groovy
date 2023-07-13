package processors

class DTDValidator implements Processor {
	
	@Override
	public String process(String dtdContent, String xmlContent, Object processingData = null) {

		return BaseXInstance.doQuery(
			"validate:dtd-info(``[${xmlContent}]``, ``[${dtdContent}]``)",
			'XML is valid.',
			'An unexpected error has occurred.'
		).getV1()
			
				
	}

}
