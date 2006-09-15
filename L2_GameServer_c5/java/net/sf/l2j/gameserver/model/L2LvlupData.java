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
package net.sf.l2j.gameserver.model;

/**
 * This class ...
 * 
 * @author NightMarez
 * @version $Revision: 1.2.2.1.2.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2LvlupData
{
    private int _classid;
    private int _classLvl;
    private float _classHpAdd;
    private float _classHpBase;
    private float _classHpModifier;
    private float _classCpAdd;
    private float _classCpBase;
    private float _classCpModifier;
    private float _classMpAdd;
    private float _classMpBase;
    private float _classMpModifier;

    /**
     * @return Returns the _classHpAdd.
     */
    public float get_classHpAdd()
    {
        return _classHpAdd;
    }

    /**
     * @param hpAdd The _classHpAdd to set.
     */
    public void set_classHpAdd(float hpAdd)
    {
        _classHpAdd = hpAdd;
    }

    /**
     * @return Returns the _classHpBase.
     */
    public float get_classHpBase()
    {
        return _classHpBase;
    }

    /**
     * @param hpBase The _classHpBase to set.
     */
    public void set_classHpBase(float hpBase)
    {
        _classHpBase = hpBase;
    }

    /**
     * @return Returns the _classHpModifier.
     */
    public float get_classHpModifier()
    {
        return _classHpModifier;
    }

    /**
     * @param hpModifier The _classHpModifier to set.
     */
    public void set_classHpModifier(float hpModifier)
    {
        _classHpModifier = hpModifier;
    }

    /**
     * @return Returns the _classCpAdd.
     */
    public float get_classCpAdd()
    {
        return _classCpAdd;
    }

    /**
     * @param hpAdd The _classCpAdd to set.
     */
    public void set_classCpAdd(float cpAdd)
    {
        _classCpAdd = cpAdd;
    }

    /**
     * @return Returns the _classCpBase.
     */
    public float get_classCpBase()
    {
        return _classCpBase;
    }

    /**
     * @param hpBase The _classCpBase to set.
     */
    public void set_classCpBase(float cpBase)
    {
        _classCpBase = cpBase;
    }

    /**
     * @return Returns the _classCpModifier.
     */
    public float get_classCpModifier()
    {
        return _classCpModifier;
    }

    /**
     * @param cpModifier The _classCpModifier to set.
     */
    public void set_classCpModifier(float cpModifier)
    {
        _classCpModifier = cpModifier;
    }

    /**
     * @return Returns the _classid.
     */
    public int get_classid()
    {
        return _classid;
    }

    /**
     * @param _classid The _classid to set.
     */
    public void set_classid(int pClassid)
    {
        this._classid = pClassid;
    }

    /**
     * @return Returns the _classLvl.
     */
    public int get_classLvl()
    {
        return _classLvl;
    }

    /**
     * @param lvl The _classLvl to set.
     */
    public void set_classLvl(int lvl)
    {
        _classLvl = lvl;
    }

    /**
     * @return Returns the _classMpAdd.
     */
    public float get_classMpAdd()
    {
        return _classMpAdd;
    }

    /**
     * @param mpAdd The _classMpAdd to set.
     */
    public void set_classMpAdd(float mpAdd)
    {
        _classMpAdd = mpAdd;
    }

    /**
     * @return Returns the _classMpBase.
     */
    public float get_classMpBase()
    {
        return _classMpBase;
    }

    /**
     * @param mpBase The _classMpBase to set.
     */
    public void set_classMpBase(float mpBase)
    {
        _classMpBase = mpBase;
    }

    /**
     * @return Returns the _classMpModifier.
     */
    public float get_classMpModifier()
    {
        return _classMpModifier;
    }

    /**
     * @param mpModifier The _classMpModifier to set.
     */
    public void set_classMpModifier(float mpModifier)
    {
        _classMpModifier = mpModifier;
    }
}
