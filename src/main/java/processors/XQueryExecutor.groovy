package processors

import org.basex.core.Context
import org.basex.core.cmd.Add
import org.basex.core.cmd.Close
import org.basex.core.cmd.CreateDB
import org.basex.core.cmd.DropDB
import org.basex.core.cmd.Open
import org.basex.core.cmd.XQuery
import org.basex.core.MainOptions


/**
 * 
 * Prozessor für XPath/XQuery-Pane rechts
 *
 */
class XQueryExecutor implements Processor {
	
	public final noresultsString = 'No results.' 
	public final errorString = 'An error has occurred:'

	@Override
	public String process(String query, String xmlContent, Object processingData = null) {
		return processCore(query, xmlContent).getV1()
	}
	
	/**
	 * 
	 * return serialized XQuery XML results (without persistent DB)
	 */
	public Tuple2 processCore(String query, String xmlContent, boolean showResultNumber = true) {
		
		// adaptive: auch Attribute werden serialisiert
		String extendedQuery = showResultNumber ? 
			"for \$x4mlquResX at \$x4mlquResXCnt in ($query) return 'result ' || \$x4mlquResXCnt || ':&#10;-----------&#10;' || serialize(\$x4mlquResX, map {'method': 'adaptive'}) || '&#10;'" :
			"for \$x4mlquResX in ($query) return serialize(\$x4mlquResX, map {'method': 'adaptive'})"
		
		return BaseXInstance.doQuery(
			//"validate:dtd-info('${xmlContent.replaceAll("'", "''")}', '${dtdContent.replaceAll("'", "''")}')",
			// TODO namespaces einfügen nach ``[   – z.B.  declare namespace m="http://www.w3.org/1998/Math/MathML";
			"""xquery:eval(``[${extendedQuery}]``, map {'': parse-xml(``[${xmlContent}]``)})""",
			noresultsString,
			errorString
			)
	}
	
	
	/**
	 * 
	 * make ad hoc in-memory DB
	 */
	public String processQueryAgainstDB(String query, List<String> xmls, boolean jsonSeparation) {
		
		String randomDBName = 'x4mlmainmembasexdb' + UUID.randomUUID().toString()
		
		Context mainmemBasexContext = new Context(false)
		
		mainmemBasexContext.options.set(MainOptions.MAINMEM, true)
		
		new CreateDB(randomDBName).execute(mainmemBasexContext)
		xmls.eachWithIndex {
			xml, idx ->
			new Add("/x4ml/${idx}.xml", xml).execute(mainmemBasexContext)
		}
		new Open(randomDBName).execute(mainmemBasexContext)
		
		String separatorUUID = UUID.randomUUID().toString()
		
		String extendedQuery = jsonSeparation ? "for \$x4mlquResX in ($query) return '$separatorUUID' || serialize(\$x4mlquResX, map {'method': 'adaptive'})" :
				 "for \$x4mlquResX at \$x4mlquResXCnt in ($query) return 'result ' || \$x4mlquResXCnt || ':&#10;-----------&#10;' || serialize(\$x4mlquResX, map {'method': 'adaptive'}) || '&#10;'"
		
		String result = new XQuery(extendedQuery).execute(mainmemBasexContext)
		
		if (jsonSeparation) {
			if (!result) {
				result = '[]'
			} else {
				result = result.substring(separatorUUID.length()).replace('`', '&#96;')
				def results = result.split(separatorUUID)
				result = '[\n' + results.collect{'   `' + it.replaceAll(/\n$/, '') + '`'}.join(',\n') + '\n]'
			}
		}
		
		new Close().execute(mainmemBasexContext)
		
		new DropDB(randomDBName).execute(mainmemBasexContext)
		mainmemBasexContext.close()
		
		result
	}
	
}
