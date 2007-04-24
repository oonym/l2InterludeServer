package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SiegeFlagInstance extends L2NpcInstance
{
    private L2PcInstance _Player;
    private Siege _Siege;
    
	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

        _Player = player;
        _Siege = SiegeManager.getInstance().getSiege(_Player.getX(), _Player.getY());
        if (_Player.getClan() == null || _Siege == null)
        {
            this.deleteMe();
        }
        else
        {
            L2SiegeClan sc = _Siege.getAttackerClan(_Player.getClan());
            if (sc == null)
                this.deleteMe();
            else
                sc.addFlag(this);
        }
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
		        && getCastle().getSiege().getIsInProgress());
	}
	
    public void doDie(L2Character killer)
    {
        L2SiegeClan sc = _Siege.getAttackerClan(_Player.getClan());
        if (sc != null)
        	sc.removeFlag(this);
        
        super.doDie(killer);
    }

    public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	public void onAction(L2PcInstance player)
	{
        if (player == null)
            return;
        
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
}
