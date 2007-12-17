/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.L2NpcWalkerAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.model.L2Character;

import java.util.Map;

/**
 * This class manages some npcs can walk in the city. <br>
 * It inherits all methods from L2NpcInstance. <br><br>
 *
 * @original author Rayan RPG for L2Emu Project
 * @since 819
 */
public class L2NpcWalkerInstance extends L2NpcInstance
{
	/**
	 * Constructor of L2NpcWalkerInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 */
	public L2NpcWalkerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setAI(new L2NpcWalkerAI(new L2NpcWalkerAIAccessor()));
	}

	/**
	 * AI can't be deattached, npc must move always with the same AI instance.
	 * @param newAI AI to set for this L2NpcWalkerInstance
	 */
	public void setAI(L2CharacterAI newAI)
	{
		if(_ai == null)
			super.setAI(newAI);
	}

	public void onSpawn()
	{
		
		((L2NpcWalkerAI) getAI()).setHomeX(getX());
		((L2NpcWalkerAI) getAI()).setHomeY(getY());
		((L2NpcWalkerAI) getAI()).setHomeZ(getZ());
	}

	/**
	 * Sends a chat to all _knowObjects
	 * @param chat message to say
	 */
	public void broadcastChat(String chat)
	{
		Map<Integer, L2PcInstance> _knownPlayers = getKnownList().getKnownPlayers();

		if(_knownPlayers == null)
		{
			if(Config.DEVELOPER)
				_log.info("broadcastChat _players == null");
			return;
		}

		//we send message to known players only!
		if(_knownPlayers.size() > 0)
		{
			CreatureSay cs = new CreatureSay(getObjectId(), 0, getName(), chat);

			//we interact and list players here
			for(L2PcInstance players : _knownPlayers.values())

				//finally send packet :D
				players.sendPacket(cs);
		}
	}

	/**
	 * NPCs are immortal
	 * @param i ignore it
	 * @param attacker  ignore it
	 * @param awake  ignore it
	 */
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{}

	/**
	 * NPCs are immortal
	 * @param killer ignore it
	 * @return false
	 */
	public boolean doDie(L2Character killer)
	{
		return false;
	}

	public L2CharacterAI getAI()
	{
		return  super.getAI();
	}

	protected class L2NpcWalkerAIAccessor extends L2Character.AIAccessor
	{
		/**
		 * AI can't be deattached.
		 */
		public void detachAI()
		{}
	}
}
