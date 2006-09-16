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

import java.util.List;

import javolution.util.FastList;

public class ExEnchantSkillList extends ServerBasePacket
{
    private static final String _S__FE_17_EXENCHANTSKILLLIST = "[S] FE:17 ExEnchantSkillList";
    private List<Skill> _skills;

    class Skill
    {
        public int id;
        public int nextLevel;
        public int data2;
        public int data3;

        Skill(int pId, int pNextLevel, int pData2, int pData3)
        {
            this.id = pId;
            this.nextLevel = pNextLevel;
            this.data2 = pData2;
            this.data3 = pData3;
        }
    }

    public void addSkill(int id, int level, int data2, int data3)
    {
        _skills.add(new Skill(id, level, data2, data3));
    }

    public ExEnchantSkillList()
    {
        _skills = new FastList<Skill>();
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
     */
    @Override
    void runImpl()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
     */
    @Override
    void writeImpl()
    {
        writeC(0xfe);
        writeH(0x17);

        writeD(_skills.size());
        for (Skill sk : _skills)
        {
            writeD(sk.id);
            writeD(sk.nextLevel);
            writeD(sk.data2);
            writeQ(sk.data3); //Q? XP
        }

    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _S__FE_17_EXENCHANTSKILLLIST;
    }

}