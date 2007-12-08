/* This program is free software; you can redistribute it and/or modify
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

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2EnchantSkillLearn;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.AquireSkillList;
import net.sf.l2j.gameserver.serverpackets.ExEnchantSkillList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.util.logging.Logger;

public class L2FolkInstance extends L2NpcInstance
{
	protected static Logger _log = Logger.getLogger(L2FolkInstance.class.getName());
	private final ClassId[] _classesToTeach;

	public L2FolkInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_classesToTeach = template.getTeachInfo();
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setLastFolkNPC(this);
		super.onAction(player);
	}

	/**
	 * this displays SkillList to the player.
	 * @param player
	 */
	public void showSkillList(L2PcInstance player, ClassId classId)
	{
		if (Config.DEBUG)
			_log.fine("SkillList activated on: "+getObjectId());

		int npcId = getTemplate().npcId;

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"+npcId+", Your classId:"+player.getClassId().getId()+"<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(classId))
        {
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Usual);
		int counts = 0;

		for (L2SkillLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
				continue;

			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
		    int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);

		    if (minlevel > 0)
		    {
		        SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
		        sm.addNumber(minlevel);
		        player.sendPacket(sm);
		    }
		    else
		    {
		        SystemMessage sm = new SystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
		        player.sendPacket(sm);
		    }
		}
		else
		{
            player.sendPacket(asl);
		}

		player.sendPacket(new ActionFailed());
	}

	/**
     * this displays EnchantSkillList to the player.
     * @param player
     */
    public void showEnchantSkillList(L2PcInstance player, ClassId classId)
    {
        if (Config.DEBUG)
            _log.fine("EnchantSkillList activated on: "+getObjectId());
        int npcId = getTemplate().npcId;

        if (_classesToTeach == null)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"+npcId+", Your classId:"+player.getClassId().getId()+"<br>");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }

        if (!getTemplate().canTeach(classId))
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }
        if(player.getClassId().getId() < 88)
        {
        	NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("You must have 3rd class change quest completed.");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);

            return;
        }

        L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
        ExEnchantSkillList esl = new ExEnchantSkillList();
        int counts = 0;

        for (L2EnchantSkillLearn s: skills)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
            if (sk == null) continue;
            counts++;
            esl.addSkill(s.getId(), s.getLevel(), s.getSpCost(), s.getExp());
        }
        if (counts == 0)
        {
            player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            int level = player.getLevel();

            if (level < 74)
            {
                SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
                sm.addNumber(level);
                player.sendPacket(sm);
            }
            else
            {
                TextBuilder sb = new TextBuilder();
                sb.append("<html><body>");
                sb.append("You've learned all skills for your class.<br>");
                sb.append("</body></html>");
                html.setHtml(sb.toString());
                player.sendPacket(html);
            }
        }
        else
        {
            player.sendPacket(esl);
        }

        player.sendPacket(new ActionFailed());
    }

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("SkillList"))
		{
			if (Config.ALT_GAME_SKILL_LEARN)
			{
				String id = command.substring(9).trim();

				if (id.length() != 0)
                {
					player.setSkillLearningClassId(ClassId.values()[Integer.parseInt(id)]);
					showSkillList(player, ClassId.values()[Integer.parseInt(id)]);
				}
                else
                {
					boolean own_class = false;

					if (_classesToTeach != null)
                    {
						for (ClassId cid : _classesToTeach)
                        {
							if (cid.equalsOrChildOf(player.getClassId()))
                            {
								own_class = true;
								break;
							}
						}
					}

					String text = "<html><body><center>Skill learning:</center><br>";

					if (!own_class)
                    {
						String mages = player.getClassId().isMage() ? "fighters" : "mages";
						text +=
							"Skills of your class are the easiest to learn.<br>"+
							"Skills of another class are harder.<br>"+
							"Skills for another race are even more hard to learn.<br>"+
							"You can also learn skills of "+mages+", and they are"+
							" the hardest to learn!<br>"+
							"<br>";
					}

					// make a list of classes
					if (_classesToTeach != null)
                    {
						int count = 0;
						ClassId classCheck = player.getClassId();

						while ((count == 0) && (classCheck != null))
						{
						    for (ClassId cid : _classesToTeach)
						    {
						        if (cid.level() != classCheck.level())
						            continue;

						        if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
						            continue;

						        text += "<a action=\"bypass -h npc_%objectId%_SkillList "+cid.getId()+"\">Learn "+cid+"'s class Skills</a><br>\n";
						        count++;
						    }
						    classCheck = classCheck.getParent();
						}
						classCheck = null;
                    }
                    else
                    {
                        text += "No Skills.<br>";
                    }

					text +=
						"</body></html>";

					insertObjectIdAndShowChatWindow(player, text);
					player.sendPacket( new ActionFailed() );
				}
			}
            else
            {
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player, player.getClassId());
			}
		}
		else if (command.startsWith("EnchantSkillList"))
        {
            showEnchantSkillList(player, player.getClassId());
        }
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class

			super.onBypassFeedback(player, command);
		}
	}
}
