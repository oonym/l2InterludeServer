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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2Effect;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class EffectTemplate
{
    static Logger _log = Logger.getLogger(EffectTemplate.class.getName());

	private final Class _func;
	private final Constructor _constructor;

	public final Condition _attachCond;
	public final Condition _applayCond;
	public final Lambda _lambda;
	public final int _counter;
	public final int _period; // in seconds
	public final short _abnormalEffect;
	public FuncTemplate[] _funcTemplates;
	
	public final String _stackType;
	public final float _stackOrder;
	
	public EffectTemplate(Condition attachCond, Condition applayCond,
			String func, Lambda lambda, int counter, int period, 
			short abnormalEffect, String stackType, float stackOrder)
	{
		_attachCond = attachCond;
		_applayCond = applayCond;
		_lambda = lambda;
		_counter = counter;
		_period = period;
		_abnormalEffect = abnormalEffect;
		_stackType = stackType;
		_stackOrder = stackOrder;
		try {
			_func = Class.forName("net.sf.l2j.gameserver.skills.Effect"+func);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		try {
			_constructor = _func.getConstructor(Env.class, EffectTemplate.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	public L2Effect getEffect(Env env)
	{
		if (_attachCond != null && !_attachCond.test(env))
			return null;
		try {
			L2Effect effect = (L2Effect)_constructor.newInstance(env, this);
			//if (_applayCond != null)
			//	effect.setCondition(_applayCond);
			return effect;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
            _log.warning("Error creating new instance of Class "+_func+" Exception was:");
			e.getTargetException().printStackTrace();
			return null;
		}
	
	}

    public void attach(FuncTemplate f)
    {
    	if (_funcTemplates == null)
    	{
    		_funcTemplates = new FuncTemplate[]{f};
    	}
    	else
    	{
    		int len = _funcTemplates.length;
    		FuncTemplate[] tmp = new FuncTemplate[len+1];
    		System.arraycopy(_funcTemplates, 0, tmp, 0, len);
    		tmp[len] = f;
    		_funcTemplates = tmp;
    	}
    }

}
