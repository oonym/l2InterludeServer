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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.CharNameTable;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CharCreateFail;
import net.sf.l2j.gameserver.serverpackets.CharCreateOk;
import net.sf.l2j.gameserver.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:30 $
 */
@SuppressWarnings("unused")
public class CharacterCreate extends ClientBasePacket
{
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	private static Logger _log = Logger.getLogger(CharacterCreate.class.getName());
	
	// cSdddddddddddd
	private final String _name;
    private final int _race;
	private final int _sex;
	private final int _classId;
	private final int _int;
	private final int _str;
	private final int _con;
	private final int _men;
	private final int _dex;
	private final int _wit;
	private final int _hairStyle;
	private final int _hairColor;
	private final int _face;
	
	public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }
	
	/**
	 * @param decrypt
	 */
	public CharacterCreate(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		
		_name      = readS();
		_race      = readD();
		_sex       = readD();
		_classId   = readD();
		_int       = readD();
		_str       = readD();
		_con       = readD();
		_men       = readD();
		_dex       = readD();
		_wit       = readD();
		_hairStyle = readD();
		_hairColor = readD();
		_face      = readD();
	}

	void runImpl()
	{
        if (CharNameTable.getInstance().accountCharNumber(getClient().getLoginName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
        {
            if (Config.DEBUG)
                _log.fine("Max number of characters reached. Creation failed.");
            CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
            sendPacket(ccf);
            return;
        }
        else if (CharNameTable.getInstance().doesCharNameExist(_name))
		{
			if (Config.DEBUG)
				_log.fine("charname: "+ _name + " already exists. creation failed.");
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
			sendPacket(ccf);
			return;
		}
		else if ((_name.length() < 3) || (_name.length() > 16) || !isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG) 
				_log.fine("charname: " + _name + " is invalid. creation failed.");
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
			sendPacket(ccf);
			return;
		}

		if (Config.DEBUG)
			_log.fine("charname: " + _name + " classId: " + _classId);
		
		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId, _sex!=0);
		if(template == null || template.classBaseLevel > 1) 
		{
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED);
			sendPacket(ccf);
			return;
		}
		
		int objectId = IdFactory.getInstance().getNextId();
		L2PcInstance newChar = L2PcInstance.create(objectId, template, getClient().getLoginName(),
				_name, _hairStyle, _hairColor, _face);
		//newChar.setName(_name);
		//newChar.setHairStyle(_hairStyle);
		//newChar.setHairColor(_hairColor);
		//newChar.setFace(_face);
		newChar.setCurrentHp(template.baseHpMax);
		newChar.setCurrentCp(template.baseCpMax);
		newChar.setCurrentMp(template.baseMpMax);
		//newChar.setMaxLoad(template.baseLoad);
		
		// send acknowledgement
		CharCreateOk cco = new CharCreateOk();
		sendPacket(cco);

		initNewChar(getClient(), newChar);
	}
	
	private boolean isAlphaNumeric(String text)
	{
		boolean result = true;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			if (!Character.isLetterOrDigit(chars[i]))
			{
				result = false;
				break;
			}
		}
		return result;
	}
	
    private boolean isValidName(String text)
    {
            boolean result = true;
            String test = text;
            Pattern pattern;
            try
            {
                pattern = Pattern.compile(Config.CNAME_TEMPLATE);
            }
            catch (PatternSyntaxException e) // case of illegal pattern
            {
            	_log.warning("ERROR : Character name pattern of config is wrong!");
                pattern = Pattern.compile(".*");
            }
            Matcher regexp = pattern.matcher(test);
            if (!regexp.matches())
            {
                    result = false;
            }
            return result;
    }
	
	private void initNewChar(ClientThread client, L2PcInstance newChar)
	{   
		if (Config.DEBUG) _log.fine("Character init start");
		L2World.getInstance().storeObject(newChar);
		
		L2PcTemplate template = newChar.getTemplate();
		
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		
		newChar.setXYZInvisible(template.spawnX, template.spawnY, template.spawnZ);
		newChar.setTitle("");
		
//		if (newChar.isMale())
//		{
//			newChar.setMovementMultiplier(template.getMUnk1());
//			newChar.setAttackSpeedMultiplier(template.getMUnk2());
//			newChar.setCollisionRadius(template.getMColR());
//			newChar.setCollisionHeight(template.getMColH());
//		}
//		else
//		{
//			newChar.setMovementMultiplier(template.getFUnk1());
//			newChar.setAttackSpeedMultiplier(template.getFUnk2());
//			newChar.setCollisionRadius(template.getFColR());
//			newChar.setCollisionHeight(template.getFColH());
//		}

		L2ShortCut shortcut;
		//add attack shortcut
		shortcut = new L2ShortCut(0,0,3,2,-1,1);
		newChar.registerShortCut(shortcut);
		//add take shortcut
		shortcut = new L2ShortCut(3,0,3,5,-1,1);
		newChar.registerShortCut(shortcut);
		//add sit shortcut
		shortcut = new L2ShortCut(10,0,3,0,-1,1);
		newChar.registerShortCut(shortcut);
		
		ItemTable itemTable = ItemTable.getInstance();
		L2Item[] items = template.getItems();
		for (int i = 0; i < items.length; i++)
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", items[i].getItemId(), 1, newChar, null);
			if (item.getItemId()==5588){
			    //add tutbook shortcut
			    shortcut = new L2ShortCut(11,0,1,item.getObjectId(),-1,1);
			    newChar.registerShortCut(shortcut);
			}
			if (item.isEquipable()){
			  if (newChar.getActiveWeaponItem() == null || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
			    newChar.getInventory().equipItemAndRecord(item);
			}
		}
		
		L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for (int i = 0; i < startSkills.length; i++)
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(startSkills[i].getId(), startSkills[i].getLevel()), true);
			if (startSkills[i].getId()==1001 || startSkills[i].getId()==1177){
			    shortcut = new L2ShortCut(1,0,2,startSkills[i].getId(),1,1);
			    newChar.registerShortCut(shortcut);
			}
			if (startSkills[i].getId()==1216){
			    shortcut = new L2ShortCut(10,0,2,startSkills[i].getId(),1,1);
			    newChar.registerShortCut(shortcut);
			}
			if (Config.DEBUG) 
				_log.fine("adding starter skill:" + startSkills[i].getId()+ " / "+ startSkills[i].getLevel());
		}
		
		ClientThread.saveCharToDisk(newChar);
		newChar.deleteMe(); // release the world of this character and it's inventory
		
		// send char list
		
		CharSelectInfo cl =	new CharSelectInfo(client.getLoginName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
        client.setCharSelection(cl.getCharInfo());
        if (Config.DEBUG) _log.fine("Character init end");
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}
