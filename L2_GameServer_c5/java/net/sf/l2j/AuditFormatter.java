/**
 * 
 */
package net.sf.l2j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class AuditFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");

	public String format(LogRecord record)
	{
        TextBuilder output = new TextBuilder();
		output.append('[');
		output.append(dateFmt.format(new Date(record.getMillis())));
		output.append(']');
		output.append(' ');
		output.append(record.getMessage());
		for (Object p : record.getParameters())
		{
			if (p == null) continue;
			output.append(',');
			output.append(' ');
			output.append(p.toString());
		}
		output.append(CRLF);

		return output.toString();
	}
}
