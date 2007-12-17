/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.script;

import java.util.Hashtable;

import net.sf.l2j.gameserver.script.faenor.FaenorInterface;



/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ScriptEngine
{
    protected EngineInterface _utils = new FaenorInterface();
    public static final Hashtable<String, ParserFactory> parserFactories = new Hashtable<String, ParserFactory>();

    protected static Parser createParser(String name)
        throws ParserNotCreatedException
    {
        ParserFactory s = parserFactories.get(name);
        if(s == null) // shape not found
        {
            try
            {
                Class.forName("net.sf.l2j.gameserver.script."+name);
                // By now the static block with no function would
                // have been executed if the shape was found.
                // the shape is expected to have put its factory
                // in the hashtable.

                s = parserFactories.get(name);
                if(s == null) // if the shape factory is not there even now
                {
                    throw (new ParserNotCreatedException());
                }
            }
            catch(ClassNotFoundException e)
            {
                // We'll throw an exception to indicate that
                // the shape could not be created
                throw(new ParserNotCreatedException());
            }
        }
        return(s.create());
    }
}
