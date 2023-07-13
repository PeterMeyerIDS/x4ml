package processors


import org.basex.core.BaseXException
import org.basex.core.Context
import org.basex.core.cmd.XQuery
import org.basex.query.QueryException



/**
 * 
 * BaseX als bloßes Tool für Validierungsoperationen etc.,
 * die KEINEN Datenbankzugriff erfordern
 *
 */
class BaseXInstance {
	
	private static Context context = new Context(false) // false: no file used for context info!
	
	/*
	 * returns tuple (queryResultMessage, boolean emptyOrError)
	 */
	public static Tuple2 doQuery(String query, String emptyQueryResultProxyText, String exceptionText, String introLine=null) {
		
		String message = introLine ? "$introLine\n${'=' * (introLine.length())}\n\n" : ''
		boolean emptyOrError = false
		
		try {
			def baseXQueryResult = new XQuery(query).execute(context)
			message += (baseXQueryResult ?: emptyQueryResultProxyText)
			if (!baseXQueryResult) {
				emptyOrError = true
			}
		} catch (BaseXException e) {
			//e.printStackTrace()
			message += (
				exceptionText + 
				'\n' + 
				e.toString().replaceAll(/(?s)org\.basex\.core\.BaseXException:.*?\] /, ''))   //e.getCause().getMessage()
			emptyOrError = true
		}
		
		new Tuple2(message, emptyOrError)
	
		
	}
	
	
}
