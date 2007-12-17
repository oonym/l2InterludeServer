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

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all RaidBoss.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2BossInstance extends L2MonsterInstance
{
	//protected static Logger _log = Logger.getLogger(L2BossInstance.class.getName());
	private boolean _teleportedToNest;

    private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

    /**
     * Constructor for L2BossInstance. This represent all grandbosses:
     * <ul>
     * <li>12001    Queen Ant</li>
     * <li>12169    Orfen</li>
     * <li>12211    Antharas</li>
     * <li>12372    Baium</li>
     * <li>12374    Zaken</li>
     * <li>12899    Valakas</li>
     * <li>12052    Core</li>
     * </ul>
     * <br>
     * <b>For now it's nothing more than a L2Monster but there'll be a scripting<br>
     * engine for AI soon and we could add special behaviour for those boss</b><br>
     * <br>
     * @param objectId ID of the instance
     * @param template L2NpcTemplate of the instance
     */
	public L2BossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    @Override
	protected int getMaintenanceInterval() { return BOSS_MAINTENANCE_INTERVAL; }

    /**
     * Used by Orfen to set 'teleported' flag, when hp goes to <50%
     * @param flag
     */
    public void setTeleported(boolean flag)
    {
        _teleportedToNest = flag;
    }

    public boolean getTeleported()
    {
        return _teleportedToNest;
    }

    @Override
	public void onSpawn()
    {
    	super.onSpawn();
    }

    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR>
     *
     */
    @Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {
        switch (getTemplate().npcId)
        {
            case 29014: // Orfen
                if ((getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
                {
                    clearAggroList();
                    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    teleToLocation(43577,15985,-4396, false);
                    setTeleported(true);
                }
                break;
            default:
        }

        super.reduceCurrentHp(damage, attacker, awake);
    }

    @Override
	public boolean isRaid()
    {
        return true;
    }
}
