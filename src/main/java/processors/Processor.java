package processors;

public interface Processor {
	
	// processingData ist das procs-Objekt; ggf. standardmäßig null, wo nicht benötigt
	public String process(String content, String xmlContent, Object processingData);

}
