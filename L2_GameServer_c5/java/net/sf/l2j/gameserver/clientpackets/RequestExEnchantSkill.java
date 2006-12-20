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
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2EnchantSkillLearn;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ShortCutRegister;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

/**
 * Format chdd
 * c: (id) 0xD0
 * h: (subid) 0x06
 * d: skill id
 * d: skill lvl
 * @author -Wooden-
 *
 */
public class RequestExEnchantSkill extends ClientBasePacket
{
	private static final String _C__D0_07_REQUESTEXENCHANTSKILL = "[C] D0:07 RequestExEnchantSkill";
	private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
	@SuppressWarnings("unused")
	private int _skillID;
	@SuppressWarnings("unused")
	private int _skillLvl;

	/**
	 * @param buf
	 * @param client
	 */
	public RequestExEnchantSkill(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_skillID = readD();
		_skillLvl = readD(); 
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{

		L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;
        
        L2FolkInstance trainer = player.getLastFolkNPC();
        if (trainer == null) return;
        
        int npcid = trainer.getNpcId();
        
        if ((trainer == null || !player.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !player.isGM())
            return;
        
        if (player.getSkillLevel(_skillID) >= _skillLvl) {
            // already knows the skill with this level
            return;
        }
        
        L2Skill skill = SkillTable.getInstance().getInfo(_skillID, _skillLvl);
        
        int counts = 0;
        int _requiredSp = 10000000;
        int _requiredExp = 100000;
        int _rate = 0;
        //int _lvl = 0;
        //int _skillID = 0;
        int _baseLvl = 1;
        
        L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
            
        for (L2EnchantSkillLearn s : skills)
        {
        	L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
        	if (sk == null || sk != skill || !sk.getCanLearn(player.getClassId())
        			|| !sk.canTeachBy(npcid)) continue;
        	if(player.getClassId().getId() < 88) 
        		continue; // requires to have 3rd class quest completed
        	counts++;
        	_requiredSp = s.getSpCost();
        	_requiredExp = s.getExp();
        	_rate = Formulas.getInstance().calculateEnchantSkillSuccessRate(s.getLevel(), player.getLevel());
        	// get skill level. it will be reverted to if enchant fails
        	_baseLvl = s.getBaseLevel();
        }
            
        if (counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
        {
        	player.sendMessage("You are trying to learn skill that u can't..");
        	Util.handleIllegalPlayerAction(player, "Player " + player.getName()
        			+ " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
        	return;
        }
            
        if (player.getSp() >= _requiredSp && player.getExp() >= _requiredExp)
        {       
        	if (Config.SP_BOOK_NEEDED && (_skillLvl == 101 || _skillLvl == 141)) // only first lvl requires book 
        	{
        		int spbId = 6622;
                  
        		L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);
                       
        		if (spb == null)
        		{
        			// Haven't spellbook
        			player.sendPacket(new SystemMessage(SystemMessage.ITEM_MISSING_TO_LEARN_SKILL));
        			return;
        		}
                        
        		// ok
        		player.destroyItem("Consume", spb, trainer, true);
        	}
        }
        else
        {
        	SystemMessage sm = new SystemMessage(SystemMessage.NOT_ENOUGH_SP_TO_LEARN_SKILL);
        	player.sendPacket(sm);
        	return;
        }
        if (Rnd.get(100) <= _rate)
        {
        	player.addSkill(skill, true);
            
        	if (Config.DEBUG) 
        		_log.fine("Learned skill " + _skillID + " for " + _requiredSp + " SP.");
            
        	player.setSp(player.getSp() - _requiredSp);
        	player.setExp(player.getExp() - _requiredExp);
        	player.updateStats();
            
        	StatusUpdate su = new StatusUpdate(player.getObjectId());
        	su.addAttribute(StatusUpdate.SP, player.getSp());
        	player.sendPacket(su);
            
        	SystemMessage sm = new SystemMessage(SystemMessage.LEARNED_SKILL_S1);
        	sm.addSkillName(_skillID);
        	player.sendPacket(sm);
            
        }
        else
        {
        	player.sendMessage("failed to enchant "+skill.getName());
        	if (skill.getLevel() > 100)
        	{
        		_skillLvl = _baseLvl;
        		player.addSkill(SkillTable.getInstance().getInfo(_skillID, _skillLvl), true);
        	}
        }
        trainer.showEnchantSkillList(player, player.getClassId());
            
        // update all the shortcuts to this skill
        L2ShortCut[] allShortCuts = player.getAllShortCuts();
            
        for (L2ShortCut sc : allShortCuts)          
        {               
        	if (sc.getId() == _skillID && sc.getType() == L2ShortCut.TYPE_SKILL)
        	{
        		L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLvl, 1);
        		player.sendPacket(new ShortCutRegister(newsc));                 
        		player.registerShortCut(newsc);                 
        	}
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_07_REQUESTEXENCHANTSKILL;
	}
	
}