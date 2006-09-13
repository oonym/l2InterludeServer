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
package net.sf.l2j.gameserver.skills;



/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ConditionLogicOr extends Condition {

	private static Condition[] emptyConditions = new Condition[0]; 
	Condition[] _conditions = emptyConditions;
	
	void add(Condition condition)
	{
		if (condition == null)
			return;
		if (getListener() != null)
			condition.setListener(this);
		final int len = _conditions.length; 
		final Condition[] tmp = new Condition[len+1];
		System.arraycopy(_conditions, 0, tmp, 0, len);
		tmp[len] = condition;
		_conditions = tmp;
	}
	
	void setListener(ConditionListener listener)
	{
		if (listener != null) {
			for (Condition c : _conditions)
				c.setListener(this);
		} else {
			for (Condition c : _conditions)
				c.setListener(null);
		}
		super.setListener(listener);
	}
	
	public boolean testImpl(Env env) {
		for (Condition c : _conditions) {
			if (c.test(env))
				return true;
		}
		return false;
	}

}
