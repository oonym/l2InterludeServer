package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author Layane
 *
 */
public class L2CabaleBufferInstance extends L2NpcInstance
{
    ScheduledFuture aiTask;
    
    private class CabalaAI implements Runnable
    {
        L2CabaleBufferInstance _caster;
        
        protected CabalaAI(L2CabaleBufferInstance caster) 
        {
            _caster = caster;
        }
        
        public void run()
        {
            boolean isBuffAWinner = false;
            boolean isBuffALoser = false;
            
            final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
            int losingCabal = SevenSigns.CABAL_NULL;
            
            if (winningCabal == SevenSigns.CABAL_DAWN)
                losingCabal = SevenSigns.CABAL_DUSK;
            else if (winningCabal == SevenSigns.CABAL_DUSK)
                losingCabal = SevenSigns.CABAL_DAWN;
            
            /**
             * For each known player in range, cast either the positive or negative buff.
             * <BR>
             * The stats affected depend on the player type, either a fighter or a mystic.
             * <BR><BR>
             * Curse of Destruction (Loser)<BR> 
             *  - Fighters: -25% Accuracy, -25% Effect Resistance<BR>
             *  - Mystics: -25% Casting Speed, -25% Effect Resistance<BR>
             * <BR><BR>
             * Blessing of Prophecy (Winner)
             *  - Fighters: +25% Max Load, +25% Effect Resistance<BR>
             *  - Mystics: +25% Magic Cancel Resist, +25% Effect Resistance<BR>
             */
            for (L2PcInstance player : getKnownList().getKnownPlayers().values())
            {
                final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
                
                if (playerCabal == winningCabal && playerCabal != SevenSigns.CABAL_NULL && _caster.getNpcId() == SevenSigns.ORATOR_NPC_ID)
                {
                    if (!player.isMageClass()) 
                    {
                        if (handleCast(player, 4364)) 
                        {
                            isBuffAWinner = true;
                            continue;
                        }
                    }
                    else 
                    {
                        if (handleCast(player, 4365)) 
                        {
                            isBuffAWinner = true;
                            continue;
                        }
                    }
                }
                else if (playerCabal == losingCabal && playerCabal != SevenSigns.CABAL_NULL && _caster.getNpcId() == SevenSigns.PREACHER_NPC_ID)
                {
                    if (!player.isMageClass()) 
                    {
                        if (handleCast(player, 4361)) 
                        {
                            isBuffALoser = true;
                            continue;
                        }
                    }
                    else 
                    {
                        if (handleCast(player, 4362)) 
                        {
                            isBuffALoser = true;
                            continue;
                        }
                    }
                }
                
                if (isBuffAWinner && isBuffALoser)
                    break;
            }
        }
        
        private boolean handleCast(L2PcInstance player, int skillId)
        {
        	int skillLevel = 1;
        	
        	if (player.getLevel() > 40)
        		skillLevel = 2;
        	
        	if (player.isDead() || !player.isVisible() || !isInsideRadius(player, getDistanceToWatchObject(player), false, false))
        		return false;

        	L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
        	
            if (player.getEffect(skill) == null)
            {
            	skill.getEffects(_caster, player);
            	
           		broadcastPacket(new MagicSkillUser(_caster, player, skill.getId(), skillLevel, skill.getSkillTime(), 0));
                
           		SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
           		sm.addSkillName(skillId);
           		player.sendPacket(sm);
                    
           		return true;
            }

            return false;
        }
    }

    
    public L2CabaleBufferInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
        
        if (aiTask != null) 
        	aiTask.cancel(true);
        
        aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CabalaAI(this), 3000, 3000);
    }
    
    public void deleteMe()
    {
        if (aiTask != null)
        {
            aiTask.cancel(true);
            aiTask = null;
        }
        
        super.deleteMe();
    }
    
    public int getDistanceToWatchObject(L2Object object)
    {
        return 900;
    }
    
    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
}
