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
package net.sf.l2j.gameserver.model.base;

/**
 * 
 *
 */
public class Experience
{
    public final static long LEVEL[]=
    {
        0,  // level 0
        1,        
        69,       
        364,      
        1169,     
        2885 ,    
        6039  ,   
        11288  ,  
        19424   , 
        31379    ,
        48230    ,  //level 10
        71203    ,
        101678   ,  
        141194   ,
        191455   ,
        254331   ,
        331868   ,
        426289   ,
        540001   ,
        675597   ,
        835863   ,  //level 20
        1023785  ,
        1242547  ,
        1495544  ,
        1786380  ,
        2118877  ,
        2497078  ,
        2925251  ,
        3407898  ,
        3949755  ,
        4555797  ,  //level 30
        5231247  ,
        5981577  ,
        6812514  ,
        7730045  ,
        8740423  ,
        9850167  ,
        11066073 ,
        12395216 ,
        13844952 ,
        15422930 ,  //level 40
        17137088 ,
        18995666 ,
        21007204 ,
        23180551 ,
        25524869 ,
        28049636 ,
        30764655 ,
        33680053 ,
        36806290 ,
        40154163 ,  //level 50
        45525134 ,
        51262491 ,
        57383989 ,
        63907912 ,
        70853090 ,
        80700832 ,
        91162655 ,
        102265882,
        114038596,
        126509653,  //level 60
        146308200,
        167244337,
        189364894,
        212717908,
        237352644,
        271975263,
        308443198,
        346827154,
        387199547,
        429634523,  //level 70
        474207979,
        532694979,
        606322775,
        696381369,
        804225364,  //level 75
        931275364,
        1151264834,
        1511257834,
        2099305233 //this is here just to allow further exp gaining after level 78
        //1788937098, //level 80
        //1999999999
    };
    
	/**
	 * This is the first UNREACHABLE level.<BR>
	 *   ex: If you want a max at 78 & 100%, you have to put 79.<BR><BR>
	 */
	public final static byte MAX_LEVEL = 79;
	
	public final static byte MIN_NEWBIE_LEVEL = 6;
	public final static byte MAX_NEWBIE_LEVEL = 25;
}
