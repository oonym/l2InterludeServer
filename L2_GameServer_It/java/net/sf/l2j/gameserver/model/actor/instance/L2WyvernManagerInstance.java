package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{

    public L2WyvernManagerInstance (int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("RideWyvern"))
        {
        	if(player.getPet() == null) 
        	{   
        		if(player.isMounted())
        		{
        			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
        			sm.addString("Already Have a Pet or Mounted.");
        			player.sendPacket(sm);
        			return;
        		}
        		else
        		{
        			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
        			sm.addString("Summon your Strider.");
        			player.sendPacket(sm);
        			return;
        		}
        	}            
        	else if ((player.getPet().getNpcId()==12526) || (player.getPet().getNpcId()==12527) || (player.getPet().getNpcId()==12528))
            {
        		if (player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= 10)
        		{
        			if (player.getPet().getLevel() < 55)
        			{
        				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                		sm.addString("Your Strider don't reach the required level.");
                		player.sendPacket(sm);
                		return;                
        			}
        			else
        			{
        				if(!player.disarmWeapons()) return;
        				player.getPet().unSummon(player);
        				player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
        				Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
        				player.sendPacket(mount);
        				player.broadcastPacket(mount);
        				player.setMountType(mount.getMountType());
        				player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
        				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                		sm.addString("The Wyvern has been summoned successfully!");
                		player.sendPacket(sm);
                		return;
        			}
        		}
        		else
        		{
        			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
            		sm.addString("You need 10 Crystals: B Grade.");
            		player.sendPacket(sm);
            		return;
        		}
            }
        	else
        	{
        		SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
        		sm.addString("Unsummon your pet.");
        		player.sendPacket(sm);
        		return;
        	}
        }
    }
    public void onAction(L2PcInstance player)
    {
    	player.sendPacket(new ActionFailed());
    	player.setTarget(this);
    	player.sendPacket(new MyTargetSelected(getObjectId(), -15));
    	if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
    		showMessageWindow(player);
    }
    private void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket( new ActionFailed() );
        String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
        
        int condition = validateCondition(player);
        if (condition > COND_ALL_FALSE)
        {
            if (condition == COND_OWNER)                                     // Clan owns castle
                filename = "data/html/wyvernmanager/wyvernmanager.htm";      // Owner message window
        }
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    } 
}
