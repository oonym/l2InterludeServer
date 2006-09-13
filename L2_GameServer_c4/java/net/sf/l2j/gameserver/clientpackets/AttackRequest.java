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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class AttackRequest extends ClientBasePacket
{
	// cddddc
	private final int _objectId;
	@SuppressWarnings("unused")
    private final int _originX;
	@SuppressWarnings("unused")
    private final int _originY;
	@SuppressWarnings("unused")
    private final int _originZ;
	@SuppressWarnings("unused")
    private final int _attackId;

	private static final String _C__0A_ATTACKREQUEST = "[C] 0A AttackRequest";
	/**
	 * @param decrypt
	 */
	public AttackRequest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		
		_objectId  = readD();
		_originX  = readD();
		_originY  = readD();
		_originZ  = readD();
		_attackId  = readC(); 	 // 0 for simple click   1 for shift-click
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2Object target = L2World.getInstance().findObject(_objectId);
		if (activeChar == null || target == null)
		    return;

		if ((target != null) && (activeChar.getTarget() != target))
		{
			target.onAction(activeChar);
		}
		else
		{
			if ((target != null) 
					&& (target.getObjectId() != activeChar.getObjectId())
					&& activeChar.getPrivateStoreType() ==0 
					&& activeChar.getActiveRequester() ==null)
			{
				//_log.config("Starting ForcedAttack");
				target.onForcedAttack(activeChar);
				//_log.config("Ending ForcedAttack");				
			} 
			else
			{
				activeChar.sendPacket(new ActionFailed());
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__0A_ATTACKREQUEST;
	}
}
