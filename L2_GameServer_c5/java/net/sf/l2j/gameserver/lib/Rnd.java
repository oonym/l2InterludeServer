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
package net.sf.l2j.gameserver.lib;

import net.sf.l2j.util.MTRandom;

/**$
 * 
 * @author Balancer
 *
 */
public class Rnd
{
    private static MTRandom _rnd = new MTRandom();
	
	public static final float get()	// get random number from 0 to 1
	{
		return _rnd.nextFloat();
	}

	public static final int get(int n) // get random number from 0 to n-1
	{
		return (int)Math.floor(_rnd.nextDouble()*n);
	}

	public static final int get(int min, int max)  // get random number from min to max (not max-1 !)
	{
        return min + (int)Math.floor(_rnd.nextDouble()*(max - min + 1));
	}
	public static final int nextInt(int n)  
	{
		return (int)Math.floor(_rnd.nextDouble()*n);
	}
	public static final double nextDouble()  
	{
		return _rnd.nextDouble();
	}
	public static final double nextGaussian()  
	{
		return _rnd.nextGaussian();
	}
	public static final boolean nextBoolean()  
	{
		return _rnd.nextBoolean();
	}
}
