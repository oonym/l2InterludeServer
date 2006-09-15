/*
 * $Header: /cvsroot/l2j/L2_Gameserver/java/net/sf/l2j/gameserver/model/L2StaticObjectInstance.java,v 1.3.2.2.2.2 2005/02/04 13:05:27 maximas Exp $
 *
 * 
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


import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.knownlist.NullKnownList;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;

/**
 * GODSON ROX!
 */
public class L2StaticObjectInstance extends L2Object
{
    private static Logger _log = Logger.getLogger(L2StaticObjectInstance.class.getName());
    
    /** The interaction distance of the L2StaticObjectInstance */
    public static final int INTERACTION_DISTANCE = 150;

    private int _staticObjectId;
    private int _type = -1;         // 0 - signs, 1 - throne    
    
    /**
     * @return Returns the StaticObjectId.
     */
    public int getStaticObjectId()
    {
        return _staticObjectId;
    }
    /**
     * @param doorId The doorId to set.
     */
    public void setStaticObjectId(int StaticObjectId)
    {
        _staticObjectId = StaticObjectId;
    }
    /**
     */
    public L2StaticObjectInstance(int objectId)
    {
        super(objectId);
        setKnownList(new NullKnownList(new L2StaticObjectInstance[] {this}));
    }
    
    public int getType()
    {
        return _type;
    }
    
    public void setType(int type)
    {
        _type = type;
    }
    
    /**
     * this is called when a player interacts with this NPC
     * @param player
     */
    public void onAction(L2PcInstance player)
    {
        if(_type < 0)
            _log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: "+getStaticObjectId());

        player.setTarget(this);
        MyTargetSelected my = new MyTargetSelected(getObjectId(), 2);
        player.sendPacket(my);
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Object#isAttackable()
     */
    @Override
    @SuppressWarnings("unused")
    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
}
