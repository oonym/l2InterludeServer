/*
 *@autor AlterEgo - tnx to Demonia
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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all chest. 
 */
public final class L2ChestInstance extends L2Attackable
{
	private volatile boolean _isBox;
	private volatile boolean _isOpen;
	
	public L2ChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_isBox = (Rnd.get(100)<Config.RATE_BOX_SPAWN);
		_isOpen = false;
	}

	public boolean isAutoAttackable(L2Character attacker)
	{
		return true;
	}

	public boolean isAttackable()
	{
		return true;
	}

	public void doDie(L2Character killer, boolean haveToDrop)
	{
		if (haveToDrop)
		{
			super.doDie(killer);
		}
		else
		{
			DecayTaskManager.getInstance().addDecayTask(this);
			// Set target to null and cancel Attack or Cast
			setTarget(null);
			// Stop movement
			stopMove(null);
			// Stop HP/MP/CP Regeneration task
			getStatus().stopHpMpRegeneration();
			// Stop all active skills effects in progress on the chest
			stopAllEffects();
			// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			broadcastStatusUpdate();
			// Notify L2Character AI
			getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
			getAttackByList().clear();
		}
	}

	/*
	 public String getHtmlPath(int npcId, int val)
	 {
	 String pom = "";

	 if (val == 0) pom = "" + npcId;
	 else pom = npcId + "-" + val;

	 return "data/html/treasure_chests/" + pom + ".htm";
	 }
	 */
	public void doDie(L2Character killer)
	{
		killer.setTarget(null);
		setCurrentHpMp(0.0,0.0);
		doDie(killer, true);
	}

	public boolean isAggressive()
	{
		return false;
	}

	public void OnSpawn()
	{
		super.OnSpawn();
		_isBox = (Rnd.get(100) < Config.RATE_BOX_SPAWN );
		_isOpen = false;
	}

	public boolean isBox() {
		return _isBox;
	}
	
	public boolean isOpen() {
		return _isOpen;
	}
	public void setOpen() {
		_isOpen = true;
	}
	public void dropReward(L2Character player)
	{
		super.doItemDrop(player);
	}

	//cast - trap chest
	public void chestTrap(L2Character player)
	{
		int trapSkillId = 0;
		int rnd = Rnd.get(120);

		if (getTemplate().level >= 61)
		{
			if (rnd >= 90) trapSkillId = 4139;//explosion
			else if (rnd >= 50) trapSkillId = 4118;//area paralysys 
			else if (rnd >= 20) trapSkillId = 1167;//poison cloud
			else trapSkillId = 223;//sting
		}
		else if (getTemplate().level >= 41)
		{
			if (rnd >= 90) trapSkillId = 4139;//explosion
			else if (rnd >= 60) trapSkillId = 96;//bleed 
			else if (rnd >= 20) trapSkillId = 1167;//poison cloud
			else trapSkillId = 4118;//area paralysys
		}
		else if (getTemplate().level >= 21)
		{
			if (rnd >= 80) trapSkillId = 4139;//explosion
			else if (rnd >= 50) trapSkillId = 96;//bleed 
			else if (rnd >= 20) trapSkillId = 1167;//poison cloud
			else trapSkillId = 129;//poison
		}
		else
		{
			if (rnd >= 80) trapSkillId = 4139;//explosion
			else if (rnd >= 50) trapSkillId = 96;//bleed 
			else trapSkillId = 129;//poison
		}

		player.sendPacket(SystemMessage.sendString("There was a trap!"));
		handleCast(player, trapSkillId);
	}
/*
	public void onAction(L2PcInstance player)
	{
		// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);

		player.setTarget(this);
		if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
		{
			// Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
			player.sendPacket(new ActionFailed());
		}
		else
		{
			if (isDead())
			{
				player.sendMessage("The chest is empty.");
				player.setTarget(null);
				return;
			}
		}
	}
*/
	//<--
	//cast casse
	//<--
	private boolean handleCast(L2Character player, int skillId)
	{
		int skillLevel = 1;

		if (getTemplate().level > 20 && getTemplate().level <= 40) skillLevel = 3;
		else if (getTemplate().level > 40 && getTemplate().level <= 60) skillLevel = 5;
		else if (getTemplate().level > 60) skillLevel = 6;

		if (player.isDead() 
			|| !player.isVisible()
			|| !player.isInsideRadius(this, getDistanceToWatchObject(player), false, false))
			return false;

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

		if (player.getEffect(skill) == null)
		{
			skill.getEffects(this, player);
			broadcastPacket(new MagicSkillUser(this, player, skill.getId(), skillLevel,
												skill.getSkillTime(), 0));
			return true;
		}
		return false;
	}
}