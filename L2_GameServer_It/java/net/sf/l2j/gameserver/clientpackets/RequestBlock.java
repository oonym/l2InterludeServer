package net.sf.l2j.gameserver.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public final class RequestBlock extends L2GameClientPacket
{
   private static final String _C__A0_REQUESTBLOCK = "[C] A0 RequestBlock";
   private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
   
   private final static int BLOCK = 0;
   private final static int UNBLOCK = 1;
   private final static int BLOCKLIST = 2;
   private final static int ALLBLOCK = 3;
   private final static int ALLUNBLOCK = 4;
   
   private String _name;
   private Integer _type;
   private L2PcInstance _target;
   
   protected void readImpl()
   {
       _type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

       if( _type == BLOCK || _type == UNBLOCK )
       {
           _name = readS();
           _target = L2World.getInstance().getPlayer(_name);
       }
   }
   
   protected void runImpl()
   {
       L2PcInstance activeChar = getClient().getActiveChar();
       
       if (activeChar == null)
    	    return;
       
       switch (_type)
       {
           case BLOCK:
           case UNBLOCK:
               if (_target == null)
               {
                   // Incorrect player name.
                   activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_REGISTER_TO_IGNORE_LIST));
                   return;
               }
               
               if (_target.isGM())
               {
                   // Cannot block a GM character.
                   activeChar.sendPacket(new SystemMessage(827));
                   return;
               }

               if (_type == BLOCK)
                   BlockList.addToBlockList(activeChar, _target);
               else
                   BlockList.removeFromBlockList(activeChar, _target);
               break;
           case BLOCKLIST:
               BlockList.sendListToOwner(activeChar);
               break;
           case ALLBLOCK:
               BlockList.setBlockAll(activeChar, true);
               break;
           case ALLUNBLOCK:
               BlockList.setBlockAll(activeChar, false);
               break;
           default:
               _log.info("Unknown 0x0a block type: " + _type);
       }
   }
   
   public String getType()
   {
       return _C__A0_REQUESTBLOCK;
   }
}