package net.sf.l2j.gameserver.model.actor.position;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Point3D;

public class ObjectPosition
{
    private static final Logger _log = Logger.getLogger(ObjectPosition.class.getName());

    // =========================================================
    // Data Field
    private L2Object[] _ActiveObject;           // Use array as a dirty trick to keep object as byref instead of byval
    private int _Heading    = 0;
    private Point3D _WorldPosition;
    private L2WorldRegion _WorldRegion;         // Object localization : Used for items/chars that are seen in the world
    
    // =========================================================
    // Constructor
    public ObjectPosition(L2Object[] activeObject)
    {
        _ActiveObject = activeObject;
        setWorldRegion(L2World.getInstance().getRegion(getWorldPosition()));
    }
    
    // =========================================================
    // Method - Public
    /**
     * Set the x,y,z position of the L2Object and if necessary modify its _worldRegion.<BR><BR>
     *
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> _worldRegion != null</li><BR><BR>
     * 
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Update position during and after movement, or after teleport </li><BR>
     */
    public final void setXYZ(int x, int y, int z)
    {
        if (Config.ASSERT) assert getWorldRegion() != null;
        
        setWorldPosition(x, y ,z);
        
        try
        {
            if (L2World.getInstance().getRegion(getWorldPosition()) != getWorldRegion())
                updateWorldRegion();
        }
        catch (Exception e)
        {
            _log.warning("Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
            if (getActiveObject() instanceof L2Character)
                getActiveObject().decayMe();
            else if (getActiveObject() instanceof L2PcInstance)
            {
                //((L2PcInstance)obj).deleteMe();
                ((L2PcInstance)getActiveObject()).teleToLocation(0,0,0);
                ((L2PcInstance)getActiveObject()).sendMessage("Error with your coords, Please ask a GM for help!");
            }
        }
    }

    /**
     * Set the x,y,z position of the L2Object and make it invisible.<BR><BR>
     * 
     * <B><U> Concept</U> :</B><BR><BR>
     * A L2Object is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null <BR><BR>
     * 
     * <B><U> Assert </U> :</B><BR><BR>
     * <li> _worldregion==null <I>(L2Object is invisible)</I></li><BR><BR>
     *  
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Create a Door</li>
     * <li> Restore L2PcInstance</li><BR>
     */
    public final void setXYZInvisible(int x, int y, int z)
    {
        if (Config.ASSERT) assert getWorldRegion() == null;
        if (x > L2World.MAP_MAX_X) x = L2World.MAP_MAX_X - 5000;
        if (x < L2World.MAP_MIN_X) x = L2World.MAP_MIN_X + 5000;
        if (y > L2World.MAP_MAX_Y) y = L2World.MAP_MAX_Y - 5000;
        if (y < L2World.MAP_MIN_Y) y = L2World.MAP_MIN_Y + 5000;

        setWorldPosition(x, y ,z);
        getActiveObject().setIsVisible(false);
    }

    /**
     * checks if current object changed its region, if so, update referencies
     */
    public void updateWorldRegion() 
    {
        if (!getActiveObject().isVisible()) return;

        L2WorldRegion newRegion = L2World.getInstance().getRegion(getWorldPosition());
        if (newRegion != getWorldRegion())
        {
            getWorldRegion().removeVisibleObject(getActiveObject());

            setWorldRegion(newRegion);

            // Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
            getWorldRegion().addVisibleObject(getActiveObject());
        }
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2Object getActiveObject()
    {
        if (_ActiveObject == null || _ActiveObject.length <= 0) return null;
        return _ActiveObject[0];
    }
    
    public final int getHeading() { return _Heading; }
    public final void setHeading(int value) { _Heading = value; }

    /** Return the x position of the L2Object. */
    public final int getX() { return getWorldPosition().getX(); }
    public final void setX(int value) { getWorldPosition().setX(value); }
    
    /** Return the y position of the L2Object. */
    public final int getY() { return getWorldPosition().getY(); }
    public final void setY(int value) { getWorldPosition().setY(value); }
    
    /** Return the z position of the L2Object. */
    public final int getZ() { return getWorldPosition().getZ(); }
    public final void setZ(int value) { getWorldPosition().setZ(value); }

    public final Point3D getWorldPosition()
    {
        if (_WorldPosition == null) _WorldPosition = new Point3D(0, 0, 0);
        return _WorldPosition;
    }
    public final void setWorldPosition(int x, int y, int z)
    {
        getWorldPosition().setXYZ(x,y,z);
        if (getActiveObject() != null && getActiveObject() instanceof L2PcInstance)
            ((L2PcInstance)getActiveObject()).revalidateZone();
    }
    public final void setWorldPosition(Point3D newPosition) { setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ()); }
    
    public final L2WorldRegion getWorldRegion() { return _WorldRegion; }
    public final void setWorldRegion(L2WorldRegion value) { _WorldRegion = value; }
}
