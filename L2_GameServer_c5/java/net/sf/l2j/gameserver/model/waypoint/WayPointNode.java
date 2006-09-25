/*
 * $Header: WayPointNode.java, 20/07/2005 19:49:29 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 20/07/2005 19:49:29 $
 * $Revision: 1 $
 * $Log: WayPointNode.java,v $
 * Revision 1  20/07/2005 19:49:29  luisantonioa
 * Added copyright notice
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
package net.sf.l2j.gameserver.model.waypoint;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.util.Point3D;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class WayPointNode extends L2Object
{
    private int _id;
    private String _title, _type;
    private static String NORMAL = "Node", SELECTED = "Selected", LINKED = "Linked";
    public static int LINE_ID = 5560;
    private static String LINE_TYPE = "item";
    private Map<WayPointNode, List<WayPointNode>> linkLists;

    /**
     * @param objectId
     */
    public WayPointNode(int objectId)
    {
        super(objectId);
        linkLists = Collections.synchronizedMap(new WeakHashMap<WayPointNode, List<WayPointNode>>());
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Object#isAutoAttackable(net.sf.l2j.gameserver.model.L2Character)
     */
    public boolean isAutoAttackable(@SuppressWarnings("unused")
    L2Character attacker)
    {
        return false;
    }

    public static WayPointNode spawn(String type, int id, int x, int y, int z)
    {
        WayPointNode newNode = new WayPointNode(IdFactory.getInstance().getNextId());
        newNode.getPoly().setPolyInfo(type, id + "");
        newNode.spawnMe(x, y, z);
        return newNode;
    }

    public static WayPointNode spawn(boolean isItemId, int id, L2PcInstance player)
    {
        return spawn(isItemId ? "item" : "npc", id, player.getX(), player.getY(), player.getZ());
    }

    public static WayPointNode spawn(boolean isItemId, int id, Point3D point)
    {
        return spawn(isItemId ? "item" : "npc", id, point.x, point.y, point.z);
    }

    public static WayPointNode spawn(Point3D point)
    {
        return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, point.x, point.y, point.z);
    }

    public static WayPointNode spawn(L2PcInstance player)
    {
        return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, player.getX(), player.getY(),
                     player.getZ());
    }

    public void onAction(L2PcInstance player)
    {
        if (player.getTarget() != this)
        {
            player.setTarget(this);
            MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
            player.sendPacket(my);
        }
    }

    public void setNormalInfo(String type, int id, String title)
    {
        this._type = type;
        changeID(id, title);
    }

    public void setNormalInfo(String type, int id)
    {
        this._type = type;
        changeID(id);
    }

    private void changeID(int id)
    {
        this._id = id;
        toggleVisible();
        toggleVisible();
    }

    private void changeID(int id, String title)
    {
        setName(title);
        setTitle(title);
        changeID(id);
    }

    public void setLinked()
    {
        changeID(Config.LINKED_NODE_ID, LINKED);
    }

    public void setNormal()
    {
        changeID(Config.NEW_NODE_ID, NORMAL);
    }

    public void setSelected()
    {
        changeID(Config.SELECTED_NODE_ID, SELECTED);
    }

    public boolean isMarker()
    {
        return true;
    }

    public final String getTitle()
    {
        return _title;
    }

    public final void setTitle(String title)
    {
        this._title = title;
    }

    public int getId()
    {
        return _id;
    }

    public String getType()
    {
        return _type;
    }

    public void setType(String type)
    {
        this._type = type;
    }

    /**
     * @param target
     * @param selectedNode
     */
    public static void drawLine(WayPointNode nodeA, WayPointNode nodeB)
    {
        int x1 = nodeA.getX(), y1 = nodeA.getY(), z1 = nodeA.getZ();
        int x2 = nodeB.getX(), y2 = nodeB.getY(), z2 = nodeB.getZ();
        int modX = x1 - x2 > 0 ? -1 : 1;
        int modY = y1 - y2 > 0 ? -1 : 1;
        int modZ = z1 - z2 > 0 ? -1 : 1;

        int diffX = x1 - x2;
        int diffY = y1 - y2;
        int diffZ = z1 - z2;

        int distance = (int) Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);

        int steps = distance / 40;

        List<WayPointNode> lineNodes = new FastList<WayPointNode>();

        for (int i = 0; i < steps; i++)
        {
            x1 = x1 + (modX * diffX / steps);
            y1 = y1 + (modY * diffY / steps);
            z1 = z1 + (modZ * diffZ / steps);

            lineNodes.add(WayPointNode.spawn(LINE_TYPE, LINE_ID, x1, y1, z1));
        }

        nodeA.addLineInfo(nodeB, lineNodes);
        nodeB.addLineInfo(nodeA, lineNodes);
    }

    public void addLineInfo(WayPointNode node, List<WayPointNode> line)
    {
        linkLists.put(node, line);
    }

    /**
     * @param target
     * @param selectedNode 
     */
    public static void eraseLine(WayPointNode target, WayPointNode selectedNode)
    {
        List<WayPointNode> lineNodes = target.getLineInfo(selectedNode);
        if (lineNodes == null) return;
        for (WayPointNode node : lineNodes)
        {
            node.decayMe();
        }
        target.eraseLine(selectedNode);
        selectedNode.eraseLine(target);
    }

    /**
     * @param target
     */
    public void eraseLine(WayPointNode target)
    {
        linkLists.remove(target);
    }

    /**
     * @param selectedNode
     * @return
     */
    private List<WayPointNode> getLineInfo(WayPointNode selectedNode)
    {
        return linkLists.get(selectedNode);
    }

    public static void setLineId(int line_id)
    {
        LINE_ID = line_id;
    }

    public List<WayPointNode> getLineNodes()
    {
        List<WayPointNode> list = new FastList<WayPointNode>();

        for (List<WayPointNode> points : linkLists.values())
        {
            list.addAll(points);
        }

        return list;
    }

}
