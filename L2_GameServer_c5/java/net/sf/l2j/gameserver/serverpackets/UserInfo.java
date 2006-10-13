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
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

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

 * @version $Revision: 1.14.2.4.2.12 $ $Date: 2005/04/11 10:05:55 $
 */
public class UserInfo extends ServerBasePacket
{
    private static final String _S__04_USERINFO = "[S] 04 UserInfo";
    private L2PcInstance _cha;
    private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd,
            _flyWalkSpd;
    private float moveMultiplier;

    /**
     * @param _characters
     */
    public UserInfo(L2PcInstance cha)
    {
        _cha = cha;
    }

    final void runImpl()
    {
        moveMultiplier = _cha.getMovementSpeedMultiplier();
        _runSpd = (int) (_cha.getRunSpeed() / moveMultiplier);
        _walkSpd = (int) (_cha.getWalkSpeed() / moveMultiplier);
        _swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
        _swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
    }

    final void writeImpl()
    {
        writeC(0x04);

        writeD(_cha.getX());
        writeD(_cha.getY());
        writeD(_cha.getZ());
        writeD(_cha.getHeading());
        writeD(_cha.getObjectId());
        writeS(_cha.getName());
        writeD(_cha.getRace().ordinal());
        writeD(_cha.getSex());

        if (_cha.getClassIndex() == 0) writeD(_cha.getClassId().getId());
        else writeD(_cha.getBaseClass());

        writeD(_cha.getLevel());
        writeQ(_cha.getExp());
        writeD(_cha.getSTR());
        writeD(_cha.getDEX());
        writeD(_cha.getCON());
        writeD(_cha.getINT());
        writeD(_cha.getWIT());
        writeD(_cha.getMEN());
        writeD(_cha.getMaxHp());
        writeD((int) _cha.getCurrentHp());
        writeD(_cha.getMaxMp());
        writeD((int) _cha.getCurrentMp());
        writeD(_cha.getSp());
        writeD(_cha.getCurrentLoad());
        writeD(_cha.getMaxLoad());

        writeD(0x28); // unknown

        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));

        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));

        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
        writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));

        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));

        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));

        writeD(_cha.getPAtk(null));
        writeD(_cha.getPAtkSpd());
        writeD(_cha.getPDef(null));
        writeD(_cha.getEvasionRate(null));
        writeD(_cha.getAccuracy());
        writeD(_cha.getCriticalHit(null, null));
        writeD(_cha.getMAtk(null, null));

        writeD(_cha.getMAtkSpd());
        writeD(_cha.getPAtkSpd());

        writeD(_cha.getMDef(null, null));

        writeD(_cha.getPvpFlag()); // 0-non-pvp  1-pvp = violett name
        writeD(_cha.getKarma());

        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_swimRunSpd); // swimspeed
        writeD(_swimWalkSpd); // swimspeed
        writeD(_flRunSpd);
        writeD(_flWalkSpd);
        writeD(_flyRunSpd);
        writeD(_flyWalkSpd);
        writeF(moveMultiplier);
        writeF(_cha.getAttackSpeedMultiplier());

        L2Summon pet = _cha.getPet();
        if (_cha.getMountType() != 0 && pet != null)
        {
            writeF(pet.getTemplate().collisionRadius);
            writeF(pet.getTemplate().collisionHeight);
        }
        else
        {
            writeF(_cha.getBaseTemplate().collisionRadius);
            writeF(_cha.getBaseTemplate().collisionHeight);
        }

        writeD(_cha.getHairStyle());
        writeD(_cha.getHairColor());
        writeD(_cha.getFace());
        writeD((_cha.getAccessLevel() > 0) ? 1 : 0); // builder level 

        String title = _cha.getTitle();
        if (_cha.getInvisible() == 1 && _cha.isGM()) title = "Invisible";
        if (_cha.getPoly().isMorphed())
            title += " - " + NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).name;
        writeS(title);

        writeD(_cha.getClanId());
        writeD(_cha.getClanCrestId());
        writeD(_cha.getAllyId());
        writeD(_cha.getAllyCrestId()); // ally crest id
        writeD(_cha.isClanLeader() ? 0x60 : 0); // siege-flags  0x40 - leader rights  0x20 - ??
        writeC(_cha.getMountType()); // mount type
        writeC(_cha.getPrivateStoreType());
        writeC(_cha.hasDwarvenCraft() ? 1 : 0);
        writeD(_cha.getPkKills());
        writeD(_cha.getPvpKills());

        writeH(_cha.getCubics().size());
        for (int id : _cha.getCubics().keySet())
            writeH(id);

        writeC(0x00); //1-find party members

        writeD(_cha.getAbnormalEffect());
        writeC(0x00);

        writeD(_cha.getClanPrivileges());

        writeH(_cha.getRecomLeft()); //c2  recommendations remaining
        writeH(_cha.getRecomHave()); //c2  recommendations received
        writeD(0x00);
        writeH(_cha.GetInventoryLimit());

        writeD(_cha.getClassId().getId());
        writeD(0x00); // special effects? circles around player...
        
        writeD(_cha.getMaxCp());
        writeD((int) _cha.getCurrentCp());
        writeC(_cha.isMounted() ? 0 : _cha.getEnchantEffect());

        if(_cha.getTeam()==1)
        	writeC(0x01); //team circle around feet 1= Blue, 2 = red
        else if(_cha.getTeam()==2)
        	writeC(0x02); //team circle around feet 1= Blue, 2 = red
        else
        	writeC(0x00); //team circle around feet 1= Blue, 2 = red

        writeD(_cha.getClanCrestLargeId());
        writeC((_cha.isHero() || (_cha.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); //0x01: symbol on char menu ctrl+I  
        writeC((_cha.isHero() || (_cha.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); //0x01: Hero Aura

        writeC(_cha.isFishing() ? 1 : 0); //Fishing Mode
        writeD(_cha.GetFishx()); //fishing x  
        writeD(_cha.GetFishy()); //fishing y
        writeD(_cha.GetFishz()); //fishing z
        writeD(_cha.getNameColor());
        
		//new c5 
       	writeC(_cha.isRunning() ? 0x01 : 0x00); //changes the Speed display on Status Window 
        
        writeD(0x00); // ??
        
        writeD(0x00); // ??
        
        writeD(0x00); //changes the text above CP on Status Window
        writeD(0x00); // ??
        
        writeD(_cha.getTitleColor());
        
        writeD(0x00); // ??

    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__04_USERINFO;
    }
}
