/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.pathfinding.geonodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.Node;
import net.sf.l2j.gameserver.pathfinding.PathFinding;

/**
 *
 * @author -Nemesiss-
 */
public class GeoPathFinding extends PathFinding
{
	private static Logger _log = Logger.getLogger(GeoPathFinding.class.getName());
	private static GeoPathFinding _instance;
	private static Map<Short, ByteBuffer> PathNodes = new FastMap<Short, ByteBuffer>();
	private static Map<Short, IntBuffer> PathNodes_index = new FastMap<Short, IntBuffer>();
	
	public static GeoPathFinding getInstance()
	{
		if (_instance == null)
			_instance = new GeoPathFinding();
		return _instance;
	}
	
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#PathNodesExist(short)
	 */
	@Override
	public boolean PathNodesExist(short regionoffset)
	{
		return PathNodes_index.containsKey(regionoffset);
	}
	
	//	TODO! [Nemesiss]
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#FindPath(int, int, short, int, int, short)
	 */
	@Override
	public AbstractNodeLoc[] FindPath(int gx, int gy, short z, int gtx, int gtz, short tz)
	{
		return null;
	}
	
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#ReadNeighbors(short, short)
	 */
	@Override
	public Node[] ReadNeighbors(Node node, short idx)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	//Private

	private GeoPathFinding()
	{
		LineNumberReader lnr = null;
		try
		{
			_log.info("PathFinding Engine: - Loading Path Nodes...");			
			File Data = new File("./data/pathnode/pn_index.txt");
			if (!Data.exists())
				return;
			
			lnr = new LineNumberReader(new BufferedReader(new FileReader(Data)));	
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Failed to Load pn_index File.");	
		}
		String line;
		try
		{
			while ((line = lnr.readLine()) != null) {
				if (line.trim().length() == 0)
					continue;
				StringTokenizer st = new StringTokenizer(line, "_");
				byte rx = Byte.parseByte(st.nextToken());
				byte ry = Byte.parseByte(st.nextToken());
				LoadPathNodeFile(rx,ry);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Failed to Read pn_index File.");
		}
	}
	
	private void LoadPathNodeFile(byte rx,byte ry)
	{
		String fname = "./data/pathnode/"+rx+"_"+ry+".pn";
		short regionoffset = (short)((rx << 5) + ry);
		_log.info("PathFinding Engine: - Loading: "+fname+" -> region offset: "+regionoffset+"X: "+rx+" Y: "+ry);		
		File Pn = new File(fname);
		int node = 0,size, index = 0;
		try {
	        // Create a read-only memory-mapped file
	        FileChannel roChannel = new RandomAccessFile(Pn, "r").getChannel();
			size = (int)roChannel.size();
			MappedByteBuffer nodes;
			if (Config.FORCE_GEODATA) //Force O/S to Loads this buffer's content into physical memory.
				//it is not guarantee, because the underlying operating system may have paged out some of the buffer's data
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			else
				nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			nodes.order(ByteOrder.LITTLE_ENDIAN);	

			// Indexing pathnode files, so we will know where each block starts
			IntBuffer indexs = IntBuffer.allocate(65536);
			
			while(node < 65536)
			{
				byte layer = nodes.get(index);
		        indexs.put(node, index);
				node++;
				index += layer*10+1;
			}
			PathNodes_index.put(regionoffset, indexs);
			PathNodes.put(regionoffset, nodes);
		} catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("Failed to Load PathNode File: "+fname+"\n");
	    }
		
	}
}
