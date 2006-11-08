package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2ControlTowerInstance extends L2NpcInstance {

    private List<L2Spawn> _Guards; 
	
	public L2ControlTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    public boolean isAttackable()
    {
        // Attackable during siege by attacker only
        return (getCastle() != null
                && getCastle().getCastleId() > 0
                && getCastle().getSiege().getIsInProgress());
    }

    public boolean isAutoAttackable(L2Character attacker) 
	{
		// Attackable during siege by attacker only
		return (attacker != null 
		        && attacker instanceof L2PcInstance 
		        && getCastle() != null
		        && getCastle().getCastleId() > 0
		        && getCastle().getSiege().getIsInProgress()
		        && getCastle().getSiege().checkIsAttacker(((L2PcInstance)attacker).getClan()));
	}
	
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
            player.setTarget(this);
            
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);
            
            StatusUpdate su = new StatusUpdate(getObjectId());
            su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
            su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
            player.sendPacket(su);
            
            player.sendPacket(new ValidateLocation(this));
		}
		else
		{
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);
            
            if (
                    isAutoAttackable(player) &&                 // Object is attackable
                    Math.abs(player.getZ() - getZ()) < 100      // Less then max height difference
                )
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
            else 
                player.sendPacket(new ActionFailed());
		}
	}

    public void onDeath()
    {
        if (getCastle().getSiege().getIsInProgress())
        {
            getCastle().getSiege().killedCT(this);

            if (getGuards() != null && getGuards().size() > 0)
            {
                for (L2Spawn spawn: getGuards())
                {
                    if (spawn == null) continue;
                    spawn.stopRespawn();
                    //spawn.getLastSpawn().doDie(spawn.getLastSpawn());
                }
            }
        }
    }
    
    public void registerGuard(L2Spawn guard)
    {
        getGuards().add(guard);
    }
    
    public final List<L2Spawn> getGuards()
    {
        if (_Guards == null) _Guards = new FastList<L2Spawn>();
        return _Guards;
    }
}
