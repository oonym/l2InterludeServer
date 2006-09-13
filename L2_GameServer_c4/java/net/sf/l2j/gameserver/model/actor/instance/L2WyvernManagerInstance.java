package net.sf.l2j.gameserver.model.actor.instance;

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
        int condition = validateCondition(player);
        if (condition <= Cond_All_False)
            return;
        if (condition == Cond_Busy_Because_Of_Siege)
            return;

        if (command.startsWith("RideWyvern"))
        {
            if(player.isMounted() || player.getPet() != null)
            {
                SystemMessage sm = new SystemMessage(614);
                sm.addString("Already Have a Pet or Mounted.");
                player.sendPacket(sm);
                return;
            }
            
            // Wyvern requires 100B crystal for ride...
            if(player.getInventory().getItemByItemId(1460) != null &&
                    player.getInventory().getItemByItemId(1460).getCount() >= 100)
            {
                player.getInventory().destroyItemByItemId("Wyvern", 1460, 100, player, player.getTarget());
                Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
                player.sendPacket(mount);
                player.broadcastPacket(mount);
                player.setMountType(mount.getMountType());
            }
            else
            {
                player.sendMessage("You need 100 B Crystals to ride Wyvern");
            }
        }
    }
    
    /*
         Micht : 06/06/17 : unused
    private void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket( new ActionFailed() );
        String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
        
        int condition = validateCondition(player);
        if (condition > Cond_All_False)
        {
            if (condition == Cond_Owner)                                     // Clan owns castle
                filename = "data/html/wyvernmanager/wyvernmanager.htm";      // Owner message window
        }
        
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    } 
    */
}
