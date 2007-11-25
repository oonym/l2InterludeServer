/*
 * $Header: PacketHistory.java, 27/11/2005 01:57:03 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 27/11/2005 01:57:03 $
 * $Revision: 1 $
 * $Log: PacketHistory.java,v $
 * Revision 1  27/11/2005 01:57:03  luisantonioa
 * Added copyright notice
 *
 *
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
package net.sf.l2j.gameserver;

import java.util.Date;
import java.util.Map;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

class PacketHistory
{
    protected Map<Class, Long> _info;
    protected long _timeStamp;

    protected static final XMLFormat<PacketHistory> PACKET_HISTORY_XML = new XMLFormat<PacketHistory>(PacketHistory.class)
    {
        /**
		 * @see javolution.xml.XMLFormat#read(javolution.xml.XMLFormat.InputElement, java.lang.Object)
		 */
		@Override
		public void read(InputElement xml, PacketHistory packetHistory) throws XMLStreamException
		{
			// TODO Auto-generated method stub
			packetHistory._timeStamp = xml.getAttribute("time-stamp", 0);
			packetHistory._info = xml.<Map<Class, Long>> get("info");
		}

		/**
		 * @see javolution.xml.XMLFormat#write(java.lang.Object, javolution.xml.XMLFormat.OutputElement)
		 */
		@Override
		public void write(PacketHistory packetHistory, OutputElement xml) throws XMLStreamException
		{
			// TODO Auto-generated method stub
			xml.setAttribute("time-stamp", new Date(packetHistory._timeStamp).toString());

			for (Class cls : packetHistory._info.keySet())
				xml.setAttribute(cls.getSimpleName(), packetHistory._info.get(cls));
		}

//		public void format(PacketHistory packetHistory, XmlElement xml)
//        {
//            xml.setAttribute("time-stamp", new Date(packetHistory.timeStamp).toString());
//
//            for (Class cls : packetHistory.info.keySet())
//            {
//                xml.setAttribute(cls.getSimpleName(), packetHistory.info.get(cls));
//            }
//        }
//
//        public PacketHistory parse(XmlElement xml)
//        {
//            PacketHistory packetHistory = new PacketHistory();
//            packetHistory.timeStamp     = xml.getAttribute("time-stamp", (long) 0);
//            packetHistory.info          = xml.<Map<Class, Long>> get("info");
//            return packetHistory;
//        }
//
//        public String defaultName()
//        {
//            return "packet-history";
//        }
    };
}