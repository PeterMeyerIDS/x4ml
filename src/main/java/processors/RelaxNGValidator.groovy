package processors

class RelaxNGValidator implements Processor {
	
	@Override
	public String process(String rngContent, String xmlContent, Object processingData = null) {

		return BaseXInstance.doQuery(
			"validate:rng-info(``[${xmlContent}]``, ``[${rngContent}]``)",
			'XML is valid relative to this RelaxNG.',
			'An unexpected error has occurred (*.rng file).'
		).getV1()
			
				
	}

}
