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
package net.sf.l2j.gameserver.model.actor.instance;

import javolution.lang.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.ClassLevel;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2ClassMasterInstance extends L2FolkInstance
{
	//private static Logger _log = Logger.getLogger(L2ClassMasterInstance.class.getName());
	private static int[] _secondClassIds = {2,3,5,6,9,8,12,13,14,16,17,20,21,23,24,27,
	                                        28,30,33,34,36,37,40,41,43,46,48,51,52,55,57};
	
	/**
	 * @param template
	 */
	public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void onAction(L2PcInstance player)
	{
		if (getObjectId() != player.getTargetId())
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			
            if (Config.DEBUG) 
                _log.fine("ClassMaster selected:"+getObjectId());
            
			player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
            if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            {
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				return;
			}
            
			if (Config.DEBUG) 
                _log.fine("ClassMaster activated");
            
			ClassId classId = player.getClassId();

			int jobLevel = 0;
			int level = player.getLevel();
            ClassLevel lvl = PlayerClass.values()[classId.getId()].getLevel();  
            switch (lvl)
			{
                case First:
                    jobLevel = 1;
                    break;
                case Second:
                    jobLevel = 2;
                    break;
                default:
                    jobLevel = 3;
			}
			
			if (!Config.ALLOW_CLASS_MASTERS) 
				jobLevel = 3;

            if (((level >= 20 && jobLevel == 1 ) ||
				(level >= 40 && jobLevel == 2 )) && Config.ALLOW_CLASS_MASTERS)
			{				
				showChatWindow(player, classId.getId());
			}
			else if (level >= 76 && Config.ALLOW_CLASS_MASTERS && classId.getId() < 88)
			{			
				for (int i = 0; i < _secondClassIds.length; i++)
				{
					if (classId.getId() == _secondClassIds[i])
					{
                        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                        TextBuilder sb = new TextBuilder();
                        sb.append("<html><body<table width=200>");
                        sb.append("<tr><td><center>"+CharTemplateTable.getClassNameById(classId.getId())+" Class Master:</center></td></tr>");
                        sb.append("<tr><td><br></td></tr>");
                        sb.append("<tr><td><a action=\"bypass -h npc_"+getObjectId()+"_change_class "+(88+i)+"\">Advance to "+CharTemplateTable.getClassNameById(88+i)+"</a></td></tr>");
                        sb.append("<tr><td><br></td></tr>");
                        sb.append("<tr><td><a action=\"bypass -h npc_"+getObjectId()+"_upgrade_hatchling\">Upgrade Hatchling to Strider</a></td></tr>");
                        sb.append("</table></body></html>");
                        html.setHtml(sb.toString());
                        player.sendPacket(html);
                        return;
					}
				}
			}
			else 
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				TextBuilder sb = new TextBuilder();
				sb.append("<html><head><body>");
				switch (jobLevel)
				{
					case 1:
						sb.append("Come back here when you reached level 20 to change your class.<br>");
						break;
					case 2:
						sb.append("Come back here when you reached level 40 to change your class.<br>");
						break;
					case 3:
						sb.append("There is no class changes for you any more.<br>");
						break;
				}
                
				for (Quest q : Quest.findAllEvents())
					sb.append("Event: <a action=\"bypass -h Quest "+q.getName()+"\">"+q.getDescr()+"</a><br>");

                sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
				
			player.sendPacket(new ActionFailed());
		}
	}
	
	public String getHtmlPath(@SuppressWarnings("unused") int npcId, int val)
	{
		return "data/html/classmaster/" + val + ".htm";
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if(command.startsWith("change_class"))
		{
            int val = Integer.parseInt(command.substring(13));
            
            // Exploit prevention 
            ClassId classId = player.getClassId();
            int level = player.getLevel();
            int jobLevel = 0;
            int newJobLevel = 0;
            
            ClassLevel lvlnow = PlayerClass.values()[classId.getId()].getLevel();  
            switch (lvlnow)
            {
            	case First:
            		jobLevel = 1;
            		break;
            	case Second:
            		jobLevel = 2;
            		break;
            	case Third:
            		jobLevel = 3;
            		break;
            	default:
            		jobLevel = 4;
            }

            if(jobLevel == 4) return; // no more job changes

            ClassLevel lvlnext = PlayerClass.values()[val].getLevel();  
            switch (lvlnext)
            {
            	case First:
            		newJobLevel = 1;
            		break;
            	case Second:
            		newJobLevel = 2;
            		break;
            	case Third:
            		newJobLevel = 3;
            		break;
            	default:
            		newJobLevel = 4;
            }

            // prevents changing between same level jobs
            if(newJobLevel != jobLevel + 1) return;

            if (level < 20 && newJobLevel > 1) return;
            if (level < 40 && newJobLevel > 2) return;
            if (level < 75 && newJobLevel > 3) return;
            // -- prevention ends
            
            
            changeClass(player, val);
            
            if(val >= 88)
            	player.sendPacket(new SystemMessage(1606)); // system sound 3rd occupation
            else
            	player.sendPacket(new SystemMessage(1308));    // system sound for 1st and 2nd occupation

            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><head><body>");
            sb.append("You have now become a <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getClassId().getId()) + "</font>.");
            sb.append("</body></html>");
                        
            html.setHtml(sb.toString());
            player.sendPacket(html);
       }
       else 
       {
           super.onBypassFeedback(player, command);
       }
 }
	
	private void changeClass(L2PcInstance player, int val)
	{
		if (Config.DEBUG) _log.fine("Changing class to ClassId:"+val);
        player.setClassId(val);
        
        if (player.isSubClassActive())
            player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
        else
            player.setBaseClass(player.getActiveClass());
        
		player.broadcastUserInfo();
	}
}
