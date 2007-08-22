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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * 0000: 04 03 15 00 00 77 ff 00 00 80 f1 ff ff 00 00 00    .....w..........
 * 0010: 00 2a 89 00 4c 43 00 61 00 6c 00 61 00 64 00 6f    .*..LC.a.l.a.d.o
 * 0020: 00 6e 00 00 00 01 00 00 00 00 00 00 00 19 00 00    .n..............
 * 0030: 00 0d 00 00 00 ee 81 02 00 15 00 00 00 18 00 00    ................
 * 0040: 00 19 00 00 00 25 00 00 00 17 00 00 00 28 00 00    .....%.......(..
 * 0050: 00 14 01 00 00 14 01 00 00 02 01 00 00 02 01 00    ................
 * 0060: 00 fa 09 00 00 81 06 00 00 26 34 00 00 2e 00 00    .........&4.....
 * 0070: 00 00 00 00 00 db 9f a1 41 93 26 64 41 de c8 31    ........A.&dA..1
 * 0080: 41 ca 73 c0 41 d5 22 d0 41 83 bd 41 41 81 56 10    A.s.A.".A..AA.V.
 * 0090: 41 00 00 00 00 27 7d 30 41 69 aa e0 40 b4 fb d3    A....'}0Ai..@...
 * 00a0: 41 91 f9 63 41 00 00 00 00 81 56 10 41 00 00 00    A..cA.....V.A...
 * 00b0: 00 71 00 00 00 71 00 00 00 76 00 00 00 74 00 00    .q...q...v...t..
 * 00c0: 00 74 00 00 00 2a 00 00 00 e8 02 00 00 00 00 00    .t...*..........
 * 00d0: 00 5f 04 00 00 ac 01 00 00 cf 01 00 00 62 04 00    ._...........b..
 * 00e0: 00 00 00 00 00 e8 02 00 00 0b 00 00 00 52 01 00    .............R..
 * 00f0: 00 4d 00 00 00 2a 00 00 00 2f 00 00 00 29 00 00    .M...*.../...)..
 * 0100: 00 12 00 00 00 82 01 00 00 52 01 00 00 53 00 00    .........R...S..
 * 0110: 00 00 00 00 00 00 00 00 00 7a 00 00 00 55 00 00    .........z...U..
 * 0120: 00 32 00 00 00 32 00 00 00 00 00 00 00 00 00 00    .2...2..........
 * 0130: 00 00 00 00 00 00 00 00 00 a4 70 3d 0a d7 a3 f0    ..........p=....
 * 0140: 3f 64 5d dc 46 03 78 f3 3f 00 00 00 00 00 00 1e    ?d].F.x.?.......
 * 0150: 40 00 00 00 00 00 00 38 40 02 00 00 00 01 00 00    @......8@.......
 * 0160: 00 00 00 00 00 00 00 00 00 00 00 c1 0c 00 00 01    ................
 * 0170: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00    ................
 * 0180: 00 00 00 00                                        ....
 *
 *
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccdd (h)
 * dddddSddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd  ffffddddSdddddcccddh (h) c dc hhdh
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc hhdh ddddc c dcc cddd d (from 654)
 * but it actually reads
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc *dddddddd* hhdh ddddc dcc cddd d
 * 																					*...*: here i am not sure at least it looks like it reads that much data (32 bytes), not sure about the format inside because it is not read thanks to the ususal parsing function
 * 
 * dddddSddddQddddddddddddddddddddddddddddddddddddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddddddddffffddddSdddddcccddh [h] c dc d hhdh ddddc c dcc cddd d c dd d d

 * @version $Revision: 1.14.2.4.2.12 $ $Date: 2005/04/11 10:05:55 $
 */
public class UserInfo extends L2GameServerPacket
{
    private static final String _S__04_USERINFO = "[S] 04 UserInfo";
    private L2PcInstance _activeChar;
    private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd,
            _flyWalkSpd, _relation;
    private float _moveMultiplier;

    /**
     * @param _characters
     */
    public UserInfo(L2PcInstance character)
    {
        _activeChar = character;
        
        _moveMultiplier = _activeChar.getMovementSpeedMultiplier();
        _runSpd = (int) (_activeChar.getRunSpeed() / _moveMultiplier);
        _walkSpd = (int) (_activeChar.getWalkSpeed() / _moveMultiplier);
        _swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
        _swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
        _relation = _activeChar.isClanLeader() ? 0x40 : 0;
        if (_activeChar.getSiegeState() == 1) _relation |= 0x180;
        if (_activeChar.getSiegeState() == 2) _relation |= 0x80;
    }

    protected final void writeImpl()
    {
        writeC(0x04);

        writeD(_activeChar.getX());
        writeD(_activeChar.getY());
        writeD(_activeChar.getZ());
        writeD(_activeChar.getHeading());
        writeD(_activeChar.getObjectId());
        writeS(_activeChar.getName());
        writeD(_activeChar.getRace().ordinal());
        writeD(_activeChar.getAppearance().getSex()? 1 : 0);

        if (_activeChar.getClassIndex() == 0) writeD(_activeChar.getClassId().getId());
        else writeD(_activeChar.getBaseClass());

        writeD(_activeChar.getLevel());
        writeQ(_activeChar.getExp());
        writeD(_activeChar.getSTR());
        writeD(_activeChar.getDEX());
        writeD(_activeChar.getCON());
        writeD(_activeChar.getINT());
        writeD(_activeChar.getWIT());
        writeD(_activeChar.getMEN());
        writeD(_activeChar.getMaxHp());
        writeD((int) _activeChar.getCurrentHp());
        writeD(_activeChar.getMaxMp());
        writeD((int) _activeChar.getCurrentMp());
        writeD(_activeChar.getSp());
        writeD(_activeChar.getCurrentLoad());
        writeD(_activeChar.getMaxLoad());

        writeD(0x28); // unknown

        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_DHAIR));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
        writeD(_activeChar.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
        
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
        writeD(_activeChar.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
        
        // c6 new h's
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeD(_activeChar.getInventory().getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        writeH(0x00);
        // end of c6 new h's
        
        writeD(_activeChar.getPAtk(null));
        writeD(_activeChar.getPAtkSpd());
        writeD(_activeChar.getPDef(null));
        writeD(_activeChar.getEvasionRate(null));
        writeD(_activeChar.getAccuracy());
        writeD(_activeChar.getCriticalHit(null, null));
        writeD(_activeChar.getMAtk(null, null));

        writeD(_activeChar.getMAtkSpd());
        writeD(_activeChar.getPAtkSpd());

        writeD(_activeChar.getMDef(null, null));

        writeD(_activeChar.getPvpFlag()); // 0-non-pvp  1-pvp = violett name
        writeD(_activeChar.getKarma());

        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_swimRunSpd); // swimspeed
        writeD(_swimWalkSpd); // swimspeed
        writeD(_flRunSpd);
        writeD(_flWalkSpd);
        writeD(_flyRunSpd);
        writeD(_flyWalkSpd);
        writeF(_moveMultiplier);
        writeF(_activeChar.getAttackSpeedMultiplier());

        L2Summon pet = _activeChar.getPet();
        if (_activeChar.getMountType() != 0 && pet != null)
        {
            writeF(pet.getTemplate().collisionRadius);
            writeF(pet.getTemplate().collisionHeight);
        }
        else
        {
            writeF(_activeChar.getBaseTemplate().collisionRadius);
            writeF(_activeChar.getBaseTemplate().collisionHeight);
        }

        writeD(_activeChar.getAppearance().getHairStyle());
        writeD(_activeChar.getAppearance().getHairColor());
        writeD(_activeChar.getAppearance().getFace());
        writeD((_activeChar.getAccessLevel() >= Config.GM_ALTG_MIN_LEVEL) ? 1 : 0); // builder level 

        String title = _activeChar.getTitle();
        if (_activeChar.getAppearance().getInvisible() && _activeChar.isGM()) title = "Invisible";
        if (_activeChar.getPoly().isMorphed())
        {
        	L2NpcTemplate polyObj = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
        	if(polyObj != null)
        		title += " - " + polyObj.name;
        }
        writeS(title);

        writeD(_activeChar.getClanId());
        writeD(_activeChar.getClanCrestId());
        writeD(_activeChar.getAllyId());
        writeD(_activeChar.getAllyCrestId()); // ally crest id
        // 0x40 leader rights
        // siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
        writeD(_relation);
        writeC(_activeChar.getMountType()); // mount type
        writeC(_activeChar.getPrivateStoreType());
        writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
        writeD(_activeChar.getPkKills());
        writeD(_activeChar.getPvpKills());

        writeH(_activeChar.getCubics().size());
        for (int id : _activeChar.getCubics().keySet())
            writeH(id);

        writeC(0x00); //1-find party members

        writeD(_activeChar.getAbnormalEffect());
        writeC(0x00);

        writeD(_activeChar.getClanPrivileges());

        writeH(_activeChar.getRecomLeft()); //c2  recommendations remaining
        writeH(_activeChar.getRecomHave()); //c2  recommendations received
        writeD(0x00);
        writeH(_activeChar.GetInventoryLimit());

        writeD(_activeChar.getClassId().getId());
        writeD(0x00); // special effects? circles around player...
        writeD(_activeChar.getMaxCp());
        writeD((int) _activeChar.getCurrentCp());
        writeC(_activeChar.isMounted() ? 0 : _activeChar.getEnchantEffect());

        if(_activeChar.getTeam()==1)
        	writeC(0x01); //team circle around feet 1= Blue, 2 = red
        else if(_activeChar.getTeam()==2)
        	writeC(0x02); //team circle around feet 1= Blue, 2 = red
        else
        	writeC(0x00); //team circle around feet 1= Blue, 2 = red

        writeD(_activeChar.getClanCrestLargeId());
        writeC(_activeChar.isNoble() ? 1 : 0); //0x01: symbol on char menu ctrl+I  
        writeC((_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); //0x01: Hero Aura

        writeC(_activeChar.isFishing() ? 1 : 0); //Fishing Mode
        writeD(_activeChar.GetFishx()); //fishing x  
        writeD(_activeChar.GetFishy()); //fishing y
        writeD(_activeChar.GetFishz()); //fishing z
        writeD(_activeChar.getAppearance().getNameColor());
        
		//new c5 
       	writeC(_activeChar.isRunning() ? 0x01 : 0x00); //changes the Speed display on Status Window 
        
        writeD(_activeChar.getPledgeClass()); //changes the text above CP on Status Window
        writeD(0x00); // ??
        
        writeD(_activeChar.getAppearance().getTitleColor());
        
        //writeD(0x00); // ??
        
        if (_activeChar.isCursedWeaponEquiped())
        	writeD(CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquipedId()));
        else
        	writeD(0x00);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__04_USERINFO;
    }
}
