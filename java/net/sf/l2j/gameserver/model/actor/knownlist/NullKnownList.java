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
package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;

public class NullKnownList extends ObjectKnownList
{

	/**
	 * @param activeObject
	 */
	public NullKnownList(L2Object activeObject)
	{
		super(activeObject);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList#addKnownObject(net.sf.l2j.gameserver.model.L2Object, net.sf.l2j.gameserver.model.L2Character)
	 */
	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList#addKnownObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	@Override
	public boolean addKnownObject(L2Object object)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList#getActiveObject()
	 */
	@Override
	public L2Object getActiveObject()
	{
		return super.getActiveObject();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList#getDistanceToForgetObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList#getDistanceToWatchObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList#removeAllKnownObjects()
	 *
	 * no-op
	 */
	@Override
	public void removeAllKnownObjects()
	{ }

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList#removeKnownObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		return false;
	}
}