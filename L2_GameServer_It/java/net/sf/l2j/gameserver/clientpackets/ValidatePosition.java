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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.Universe;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.serverpackets.PartyMemberPosition;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.serverpackets.ValidateLocationInVehicle;
import net.sf.l2j.util.Point3D;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends L2GameClientPacket
{
    private static Logger _log = Logger.getLogger(ValidatePosition.class.getName());
    private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";
    
    /** urgent messages, execute immediatly */
    public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }
    
    private int _x;
    private int _y;
    private int _z;
    private int _heading;
    @SuppressWarnings("unused")
    private int _data;
    
    
    protected void readImpl()
    {
        _x  = readD();
        _y  = readD();
        _z  = readD();
        _heading  = readD();
        _data  = readD();
    }
    
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.isTeleporting()) return;
        
        if (Config.COORD_SYNCHRONIZE > 0)
        {
            activeChar.setClientX(_x);
            activeChar.setClientY(_y);
            activeChar.setClientZ(_z);
            activeChar.setClientHeading(_heading);
            int realX = activeChar.getX();
            int realY = activeChar.getY();
            int realZ = activeChar.getZ();
            
            double dx = _x - realX;
            double dy = _y - realY;
            double diffSq = (dx*dx + dy*dy);

            if (Config.DEVELOPER && false) 
                {
//                int dxs = (_x - activeChar._lastClientPosition.x); 
//                int dys = (_y - activeChar._lastClientPosition.y);
//                int dist = (int)Math.sqrt(dxs*dxs + dys*dys);
//                int heading = dist > 0 ? (int)(Math.atan2(-dys/dist, -dxs/dist) * 10430.378350470452724949566316381) + 32768 : 0;
                System.out.println("Client X:" + _x + ", Y:" + _y + ", Z:" + _z + ", H:" + _heading/* + "(" + heading + ")"*/ + ", Dist:" + activeChar.getLastClientDistance(_x, _y, _z));
                System.out.println("Server X:" + realX + ", Y:" + realY + ", Z:" + realZ + ", H:" + activeChar.getHeading() + ", Dist:" + activeChar.getLastServerDistance(realX, realY, realZ));
                }

            if (diffSq > 0)
            {
                if ((Config.COORD_SYNCHRONIZE & 1) == 1
                    && (!activeChar.isMoving() // character is not moving, take coordinates from client
                    || !activeChar.validateMovementHeading(_heading))) // Heading changed on client = possible obstacle
                {
                    if (Config.DEVELOPER) System.out.println(activeChar.getName() + ": Synchronizing position Client --> Server" + (activeChar.isMoving()?" (collision)":" (stay sync)"));
                    activeChar.setXYZ(_x, _y, _z);
                    activeChar.setHeading(_heading);
                }
                else if ((Config.COORD_SYNCHRONIZE & 2) == 2 
                        && diffSq > 10000) // more than can be considered to be result of latency
                {
                    if (Config.DEVELOPER) System.out.println(activeChar.getName() + ": Synchronizing position Server --> Client");
                    if (activeChar.isInBoat())
                    {
                        sendPacket(new ValidateLocationInVehicle(activeChar));
                    }
                    else
                    {
                    	activeChar.sendPacket(new ValidateLocation(activeChar));
                    }
                }
            }
            activeChar.setLastClientPosition(_x, _y, _z);
            activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());
        }
        else if (Config.COORD_SYNCHRONIZE == -1)
        {
            activeChar.setClientX(_x);
            activeChar.setClientY(_y);
            activeChar.setClientZ(_z);
            activeChar.setClientHeading(_heading);
            int realX = activeChar.getX();
            int realY = activeChar.getY();
            int realZ = activeChar.getZ();
            
            if (Point3D.distanceSquared(activeChar.getPosition().getWorldPosition(), new Point3D(_x, _y, _z)) < 250000)
                activeChar.setXYZ(activeChar.getX(),activeChar.getY(),_z);
            int realHeading = activeChar.getHeading();
        
            //activeChar.setHeading(_heading);
            
            //TODO: do we need to validate?
            /*double dx = (_x - realX); 
             double dy = (_y - realY); 
             double dist = Math.sqrt(dx*dx + dy*dy);
             if ((dist < 500)&&(dist > 2)) //check it wasnt teleportation, and char isn't there yet
             activeChar.sendPacket(new CharMoveToLocation(activeChar));*/
            
            if (Config.DEBUG) {
                _log.fine("client pos: "+ _x + " "+ _y + " "+ _z +" head "+ _heading);
                _log.fine("server pos: "+ realX + " "+realY+ " "+realZ +" head "+realHeading);
            }
            
            if (Config.ACTIVATE_POSITION_RECORDER && !activeChar.isFlying() && Universe.getInstance().shouldLog(activeChar.getObjectId()))
                Universe.getInstance().registerHeight(realX, realY, _z);
            
            if (Config.DEVELOPER)
            {
                double dx = _x - realX;
                double dy = _y - realY;
                double diff2 = (dx*dx + dy*dy);
                if (diff2 > 1000000) {
                    if (Config.DEBUG) _log.fine("client/server dist diff "+ (int)Math.sqrt(diff2));
                    if (activeChar.isInBoat())
                    {
                        sendPacket(new ValidateLocationInVehicle(activeChar));
                    }
                    else
                    {
                    	activeChar.sendPacket(new ValidateLocation(activeChar));
                    }
                }
            }
            //trigger a KnownList update
            ThreadPoolManager.getInstance().executeTask( new KnownListAsynchronousUpdateTask(activeChar));
//          // check for objects that are now out of range
//          activeChar.updateKnownCounter += 1;
//          if (activeChar.updateKnownCounter >3)
//          {
//          int delete = 0;
//          Iterator<L2Object> known = activeChar.iterateKnownObjects();
//          ArrayList<L2Object> toBeDeleted = new ArrayList<L2Object>();
//          
//          while (known.hasNext())
//          {
//          L2Object obj = known.next();
//          if (distance(activeChar, obj) > 4000*4000)
//          {
//          toBeDeleted.add(obj);
//          delete++;
//          }
//          }
//          
//          if (delete >0)
//          {
//          for (int i = 0; i < toBeDeleted.size(); i++)
//          {
//          L2Object obj = toBeDeleted.get(i);
//          activeChar.removeKnownObject(obj);
//          obj.removeKnownObject(activeChar);
//          
//          }
//          if (Config.DEBUG) _log.fine("deleted " +delete+" objects");
//          }
//          
//          
//          // check for new objects that are now in range
//          int newObjects = 0;
//          L2Object[] visible = L2World.getInstance().getVisibleObjects(activeChar, 3000);
//          for (int i = 0; i < visible.length; i++)
//          {
//          if (! activeChar.knownsObject(visible[i]))
//          {
//          activeChar.addKnownObject(visible[i]);
//          visible[i].addKnownObject(activeChar);
//          newObjects++;
//          }
//          }
//          
//          if (newObjects >0)
//          {
//          if (Config.DEBUG) _log.fine("added " + newObjects + " new objects");
//          }
//          activeChar.updateKnownCounter = 0;  
//          }
        }
		if(activeChar.getParty() != null)
			activeChar.getParty().broadcastToPartyMembers(activeChar,new PartyMemberPosition(activeChar));
		
		if (Config.ALLOW_WATER)
			activeChar.checkWaterState();
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__48_VALIDATEPOSITION;
    }
    
    public boolean Equal(ValidatePosition pos)
    {
        return _x == pos._x && _y == pos._y && _z == pos._z && _heading == pos._heading;
    }
}
