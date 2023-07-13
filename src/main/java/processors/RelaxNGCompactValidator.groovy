package processors

class RelaxNGCompactValidator implements Processor {
	
	@Override
	public String process(String rngContent, String xmlContent, Object processingData = null) {

		return BaseXInstance.doQuery(
			"validate:rng-info(``[${xmlContent}]``, ``[${rngContent}]``, true())",
			'XML is valid relative to this RelaxNG.',
			'An unexpected error has occurred (*.rnc file).'
		).getV1()
			
				
	}

}
