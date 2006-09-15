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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.serverpackets.CharTemplates;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class NewCharacter extends ClientBasePacket
{
	private static final String _C__0E_NEWCHARACTER = "[C] 0E NewCharacter";
	private static Logger _log = Logger.getLogger(NewCharacter.class.getName());

	/**
	 * packet type id 0x0e
	 * format:		c
	 * @param rawPacket
	 */
	public NewCharacter(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		// packet contains no data so just create answer
	}

	void runImpl()
	{
		if (Config.DEBUG) _log.fine("CreateNewChar");
		
		CharTemplates ct = new CharTemplates();
		
		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(0, false);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.fighter, false);	// human fighter
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.mage, false);	// human mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter, false);	// elf fighter
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage, false);	// elf mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter, false);	// dark elf fighter
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkMage, false);	// dark elf mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter, false);	// orc fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcMage, false);	// orc mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter, false);	// dwarf fighter
		ct.addChar(template);

		sendPacket(ct);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__0E_NEWCHARACTER;
	}
}
