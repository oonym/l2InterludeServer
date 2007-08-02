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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

public class Seed implements IItemHandler
{
    private static int[] ITEM_IDS = { // generic seeds (lo-grade)
                                      5016,5017,5018,5019,5020,5021,5022,5023,5024,
                                      5025,5026,5027,5028,5029,5030,5031,5032,5033,
                                      5034,5035,5036,5037,5038,5039,5040,5041,5042,
                                      5043,5044,5045,5046,5047,5048,5049,5050,5051,
                                      5052,5053,5054,5055,5056,5057,5058,5059,5060,
                                      5061,5221,5222,5223,5224,5225,5226,5227,

                                      // alternative seeds(hi-grade)
                                      5650,5651,5652,5653,5654,5655,5656,5657,5658,
                                      5659,5660,5661,5662,5663,5664,5665,5666,5667,
                                      5668,5669,5670,5671,5672,5673,5674,5675,5676,
                                      5678,5678,5679,5680,5681,5682,5683,5684,5685,
                                      5686,5687,5688,5689,5690,5691,5692,5693,5694,
                                      5695,5696,5697,5698,5699,5700,5701,5702
                                      };
    
    private int _seedId;
    private L2MonsterInstance _target;
    private L2PcInstance _activeChar;
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        if (!(playable instanceof L2PcInstance))
            return;

        _activeChar = (L2PcInstance)playable;
        
        if(!(_activeChar.getTarget() instanceof L2MonsterInstance))
        {
        	_activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
            return;
        }

         _target = (L2MonsterInstance)_activeChar.getTarget();
        
        if(_target != null && !_target.isDead() && !_target.isSeeded())
        {
            _seedId = item.getItemId();
                        
            if(areaValid(MapRegionTable.getInstance().getClosestTownNumber(_activeChar)))
            {
                //TODO: valid action animId for seed planting
                MagicSkillUser MSU = new MagicSkillUser(_activeChar,_target,2023,1,2,2);
                _activeChar.sendPacket(MSU);
                _activeChar.broadcastPacket(MSU);

                if(calcSuccess())
                {
                    _target.setSeeded(item.getItemId(),_activeChar.getLevel());
                    _activeChar.sendPacket(new SystemMessage(SystemMessageId.SEED_SUCCESSFULLY_SOWN));
                }
                else
                {
                    _activeChar.sendPacket(new SystemMessage(SystemMessageId.SEED_NOT_SOWN));
                }

                // remove seed from inv & attack target
                _activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
                _target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, _activeChar);
                _activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _target);
            }
            else
            {
                _activeChar.sendPacket(new SystemMessage(SystemMessageId.SEED_CANNOT_BE_SOWN_HERE));
            }
        }
    }
    
    public int[] getItemIds()
    {
        return ITEM_IDS;
    }

    private boolean areaValid(int area)
    {
        
        switch(area)
        {
            case 5: // gludio
                return ((_seedId >= 5016 && _seedId <= 5023) || (_seedId >= 5650 && _seedId <= 5657));
            case 7: // dion
                return ((_seedId >= 5024 && _seedId <= 5032) || (_seedId >= 5658 && _seedId <= 5666));
            case 8: // giran
                return ((_seedId >= 5033 && _seedId <= 5041) || (_seedId >= 5667 && _seedId <= 5675));
            case 9: // oren
                return ((_seedId >= 5042 && _seedId <= 5052) || (_seedId >= 5676 && _seedId <= 5686));
            case 10: // aden
                return ((_seedId >= 5053 && _seedId <= 5061) || (_seedId >= 5687 && _seedId <= 5695));
            case 13: // heine
                return ((_seedId >= 5221 && _seedId <= 5227) || (_seedId >= 5696 && _seedId <= 5702));
            default:
                return false;
        }
        
    }
    
    private boolean calcSuccess()
    {
        int basicSuccess = 90;//Config.MANOR_BASIC_SUCCESS;
        int levelSeed = 0;
	    levelSeed = L2Manor.getInstance().getSeedLevel(_seedId);
        int levelPlayer = _activeChar.getLevel();
        int levelTarget = _target.getLevel();

        int diffPlayerTarget = (levelPlayer - levelTarget);
        if(diffPlayerTarget < 0)
            diffPlayerTarget = -diffPlayerTarget;
        
        int diffSeedTarget = (levelSeed - levelTarget);
        if(diffSeedTarget < 0)
            diffSeedTarget = -diffSeedTarget;
        
        // apply penalty, target <=> player levels
        // 15% penalty for each level
        if(diffPlayerTarget > 5)//Config.MANOR_DIFF_PLAYER_TARGET)
        {
            basicSuccess -= (diffPlayerTarget - 75);//Config.MANOR_DIFF_PLAYER_TARGET)*Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
        }
        
        // apply penalty, seed <=> target levels
        // 15% penalty for each level
        if(diffSeedTarget > 5)//Config.MANOR_DIFF_SEED_TARGET)
        {
            basicSuccess -= (diffSeedTarget - 75);//Config.MANOR_DIFF_SEED_TARGET)*Config.MANOR_DIFF_SEED_TARGET_PENALTY;
        }
        
        // success rate cant be less than 1%
        if(basicSuccess < 1)
            basicSuccess = 1;
        
        int rate = Rnd.nextInt(99);
        
        if(rate < basicSuccess)
            return true;
        return false;
    }
}