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
package net.sf.l2j.gameserver.skills.funcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.conditions.Condition;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class FuncTemplate {

	public Condition _attachCond;
	public Condition _applayCond;
	public final Class<?> _func;
	public final Constructor _constructor;
	public final Stats _stat;
	public final int _order;
	public final Lambda _lambda;
	
	public FuncTemplate(Condition attachCond, Condition applayCond, String func, Stats stat, int order, Lambda lambda)
	{
		_attachCond = attachCond;
		_applayCond = applayCond;
		_stat = stat;
		_order = order;
		_lambda = lambda;
		try {
			_func = Class.forName("net.sf.l2j.gameserver.skills.funcs.Func"+func);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		try {
			_constructor = _func.getConstructor(
				new Class[]{
						Stats.class, // stats to update
						Integer.TYPE, // order of execution
						Object.class, // owner
						Lambda.class // value for function
				});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Func getFunc(Env env, Object owner)
	{
		if (_attachCond != null && !_attachCond.test(env))
			return null;
		try {
			Func f = (Func)_constructor.newInstance(_stat, _order, owner, _lambda);
			if (_applayCond != null)
				f.setCondition(_applayCond);
			return f;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	
	}
}
