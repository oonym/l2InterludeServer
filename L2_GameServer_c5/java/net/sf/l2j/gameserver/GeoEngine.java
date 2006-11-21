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
package net.sf.l2j.gameserver;

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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author -Nemesiss-
 */
public class GeoEngine extends GeoData
{
	private static Logger _log = Logger.getLogger(GeoData.class.getName());
	private static GeoEngine _instance;
	
	private final static int WORLD_X = -131072;
    private final static int WORLD_Y = -262144;
    private final static int E = 1;
    private final static int W = 2;
    private final static int S = 4;
    private final static int N = 8;
	private static Map<Short, MappedByteBuffer> Geodata = new FastMap<Short, MappedByteBuffer>();
	private static Map<Short, IntBuffer> Geodata_index = new FastMap<Short, IntBuffer>();

	public static GeoEngine getInstance()
    {
        if(_instance == null)
            _instance = new GeoEngine();
        return _instance;
    }
    public GeoEngine()
    {
        NinitGeodata();            
    }
	
    //Public Methods
    /**
     * @param x
     * @param y
     * @return Geo Block Type
     */
    public short getType  (int x, int y)         
    {
        return NgetType((x - WORLD_X) >> 4, (y - WORLD_Y) >> 4);        
    }
    /**
     * @param x
     * @param y
     * @param z
     * @return Nearles Z
     */
    public short getHeight(int x, int y, int z)
    {
        return NgetHeight((x - WORLD_X) >> 4,(y - WORLD_Y) >> 4,z);        
    }
    /**
     * @param cha
     * @param target
     * @return True if cha can see target (LOS)
     */
    public boolean canSeeTarget(L2Object cha, L2Object target)
    {
        return canSeeTarget(cha.getX(),cha.getY(),cha.getZ(),target.getX(),target.getY(),target.getZ());
    }    
    /**
     * @param cha
     * @param target
     * @return True if cha can see target (LOS) and send usful info to PC
     */
    public boolean canSeeTargetDebug(L2PcInstance gm, L2Object target)
    {
        return canSeeDebug(gm,(gm.getX() - WORLD_X) >> 4,(gm.getY() - WORLD_Y) >> 4,gm.getZ(),(target.getX() - WORLD_X) >> 4,(target.getY() - WORLD_Y) >> 4,target.getZ());
    }
    /**
     * @param x
     * @param y
     * @param z
     * @return Geo NSWE (0-15)
     */
    public short getNSWE(int x, int y, int z)  
    {
        return NgetNSWE((x - WORLD_X) >> 4,(y - WORLD_Y) >> 4,z);
    }    
    /**
     * @param x
     * @param y
     * @param z
     * @param tx
     * @param ty
     * @param tz
     * @return Last Location (x,y,z) where player can walk - just befor wall
     */
    public Location moveCheck(int x, int y, int z, int tx, int ty, int tz)
    {
    	Location destiny = new Location(tx,ty,tz);
        return MoveCheck(destiny,(x - WORLD_X) >> 4,(y - WORLD_Y) >> 4,z,(tx - WORLD_X) >> 4,(ty - WORLD_Y) >> 4,tz);
    }
    
    // Private Methods
    private boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
    {
        return canSee((x - WORLD_X) >> 4,(y - WORLD_Y) >> 4,z,(tx - WORLD_X) >> 4,(ty - WORLD_Y) >> 4,tz);
    }
    private static boolean canSee(double x, double y, double z, int tx, int ty, int tz)
    {
        final double dx = (tx - x);
        final double dy = (ty - y);
        final double dz = (tz - z);
        final double distance = Math.sqrt(dx*dx + dy*dy);
        if (distance > 300)
        {
            //Avoid too long check
            return false;
        }
        final double plus_x = dx/distance;
        final double plus_y = dy/distance;
        final double plus_z = dz/distance;
        int new_x = (int)x;
        int new_y = (int)y;
        int last_x;
        int last_y;                
        while (new_x != tx || new_y != ty)
        {
            last_x = new_x;
            last_y = new_y;
            x += plus_x;
            y += plus_y;
            new_x = (int)Math.round(x);
            new_y = (int)Math.round(y);
            if (last_x != new_x || last_y != new_y)
            {                
                z += plus_z;
                if (!NcanMoveNext(last_x,last_y,(int)z,new_x,new_y,tz))
                {
                    return false;
                }                
            }
        }
        return true;
    }
    private static boolean canSeeDebug(L2PcInstance gm, double x, double y, double z, int tx, int ty, int tz)
    {
        final double dx = (tx - x);
        final double dy = (ty - y);
        final double dz = (tz - z);
        final double distance = Math.sqrt(dx*dx + dy*dy);
        if (distance > 300)
        {
            gm.sendMessage("dist > 300");
            return false;
        }
        final double plus_x = dx/distance;
        final double plus_y = dy/distance;
        final double plus_z = dz/distance;
        int new_x = (int)x;
        int new_y = (int)y;
        int last_x;
        int last_y;
        int heading = (int) (Math.atan2(-plus_y, -plus_x) * 10430.378350470452724949566316381);
        heading += 32768;
        int count = 0;
        gm.sendMessage("Los: from X: "+x+ "Y: "+y+ "--->> X: "+tx+" Y: "+ty);
        String angel = "";
        if(8192 < heading && heading < 24576) //S
        {
            angel= "S";
        }
        else if(24576 < heading && heading < 40960) //W
        {
            angel= "W";
        }
        else if(40960 < heading && heading < 57344) //N
        {
            angel= "N";
        }
        else if(57344 < heading || heading < 8192) //E
        {
            angel= "E";
        }
        else if(heading == 8192) //SE
        {
            angel= "SE";
        }
        else if(heading == 24576) //SW
        {
            angel= "SW";
        }
        else if(heading == 40960) //NW
        {
            angel= "NW";
        }
        else if(heading == 57344) //NE
        {
            angel= "NE";
        }
        else
        {
            angel= "Error!";
        }
        gm.sendMessage("Los: Heading: "+heading+ " Angel: "+angel);
        while (new_x != tx || new_y != ty)
        {
            last_x = new_x;
            last_y = new_y;
            x += plus_x;
            y += plus_y;
            new_x = (int)Math.round(x);
            new_y = (int)Math.round(y);
            if (last_x != new_x || last_y != new_y)
            {
                count++;
                z += plus_z;
                if (!NcanMoveNext(last_x,last_y,(int)z,new_x,new_y,tz))
                {
                    return false;
                }
                if (count > distance)
                {
                    gm.sendMessage("Error!!");
                    return false;
                }
            }
        }
        return true;
    }
    private static Location MoveCheck(Location destiny, double x, double y, double z, int tx, int ty, int tz)
    {
        final double dx = (tx - x);
        final double dy = (ty - y);
        final double dz = (tz - z);
        final double distance = Math.sqrt(dx*dx + dy*dy);
        final double plus_x = dx/distance;
        final double plus_y = dy/distance;
        final double plus_z = dz/distance;
        int new_x = (int)x;
        int new_y = (int)y;
        int last_x;
        int last_y;               
        while (new_x != tx || new_y != ty)
        {
            last_x = new_x;
            last_y = new_y;
            x += plus_x;
            y += plus_y;
            new_x = (int)Math.round(x);
            new_y = (int)Math.round(y);
            if (last_x != new_x || last_y != new_y)
            {                
                z += plus_z;
                if (!NcanMoveNext(last_x,last_y,(int)z,new_x,new_y,tz))
                {
                    return new Location((last_x << 4) + WORLD_X,(last_y << 4) + WORLD_Y,(int)z);
                }                
            }            
        }
        return destiny;
    }
	
	
	//GeoEngine
	private static void NinitGeodata()
	{		
		LineNumberReader lnr = null;
		try
		{
			_log.info("Geo Engine: - Loading Geodata...");			
			File Data = new File("./data/geodata/geo_index.txt");
			if (!Data.exists()) return;
			
			lnr = new LineNumberReader(new BufferedReader(new FileReader(Data)));	
		} catch (Exception e) {
			e.printStackTrace();		
			throw new Error("Failed to Load geo_index File.");	
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
				LoadGeodataFile(rx,ry);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Failed to Read geo_index File.");
		}		
	}
	private static void LoadGeodataFile(byte rx, byte ry)
	{
		String fname = "./data/geodata/"+rx+"_"+ry+".l2j";
		short regionoffset = (short)((rx << 5) + ry);
		_log.info("Geo Engine: - Loading: "+fname+" -> region offset: "+regionoffset+"X: "+rx+" Y: "+ry);		
		File Geo = new File(fname);
		int size;		
		int index = 0;		
		int block = 0;			
		int flor = 0;
		try {	    
	        // Create a read-only memory-mapped file
	        FileChannel roChannel = new RandomAccessFile(Geo, "r").getChannel();
			size = (int)roChannel.size();			
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)roChannel.size()).load();								
			geo.order(ByteOrder.LITTLE_ENDIAN);						
			
			if (size > 196608)
			{                
				// Indexing geo files, so we will know where each block starts
				IntBuffer indexs = IntBuffer.allocate(65536);
				while(block < 65536)
			    {	        
					byte type = geo.get(index);
			        indexs.put(block,index);
					block++;
					index++;								
			        if(type == 0)
			        {			
			        	index += 2; // 1x short					
			        }
			        else if(type == 1)
			        {
			        	index += 128; // 64 x short					
			        }       
			        else
			        {					
			            int b;            
			            for(b=0;b<64;b++)
			            {												
			                byte layers = geo.get(index);						
			                index += (layers << 1) + 1;						
			                if (layers > flor)
			                     flor = layers;               
			            }            
			        }		        				
			    }
				Geodata_index.put(regionoffset, indexs);
			}		 
			Geodata.put(regionoffset,geo);
			
			_log.info("Geo Engine: - Max Layers: "+flor+" Size: "+size+" Loaded: "+index);					
	    } catch (Exception e) 
		{
			e.printStackTrace();			
			_log.warning("Failed to Load GeoFile at block: "+block+"\n");
	    }		
	}
	
	//Geodata Methods
	private static short getRegionOffset(int x, int y)
	{
	    int rx = x >> 11; // =/(256 * 8)
	    int ry = y >> 11;
	    return (short)(((rx+16) << 5) + (ry+10));
	}

	private  static int getBlock(int pos)
	{
	    return (pos >> 3) % 256;	    
	}
	
	private static int getCell(int pos)
	{
	    return pos % 8;	    
	}
	
	//Geodata Functions
	private static short NgetType(int x, int y)
	{   
	    short region = getRegionOffset(x,y);
		int blockX = getBlock(x);
		int blockY = getBlock(y);		
		int index = 0;
		if(Geodata_index.get(region) == null) index = ((blockX << 8) + blockY)*3;		
		else index = Geodata_index.get(region).get((blockX << 8) + blockY);
		if(Geodata.get(region) == null)
		{
			_log.warning("Geo Region - Region Offset: "+region+" dosnt exist!!");
			return 0;
		}
		return Geodata.get(region).get(index);      
	}
	private static short NgetHeight(int x, int y, int z)
	{    
	    short region = getRegionOffset(x,y);
	    int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX = 0;		
		int cellY = 0;		
		int index = 0;
		if(Geodata_index.get(region) == null) index = ((blockX << 8) + blockY)*3;		
		else index = Geodata_index.get(region).get(((blockX << 8))+(blockY));
		ByteBuffer geo = Geodata.get(region);
		if(geo == null)
		{
			_log.warning("Geo Region - Region Offset: "+region+" dosnt exist!!");
			return 0;
		}		
		byte type = geo.get(index);
		index++;
	    if(type == 0)
	    {
	        return (short)(geo.getShort(index)&0x0fff0);
	    }
	    else if(type == 1)
	    {
	    	cellX = getCell(x);
			cellY = getCell(y);
	        index += ((cellX << 3) + cellY) << 1;
	        short height = geo.getShort(index);
			height = (short)(height&0x0fff0);
			height = (short)(height >> 1);
			return height;
	    }
	    else
	    {
	    	cellX = getCell(x);
			cellY = getCell(y);
	        int offset = (cellX << 3) + cellY;
	        while(offset > 0)
	        {
	            byte lc = geo.get(index);		                
	            index += (lc << 1) + 1;
	            offset--;
	        }
	        byte layers = geo.get(index);
	        index++;
	        short height=-1;
			if(layers <= 0 || layers > 60)
			{
				_log.warning("Geo Engine: - invalid layers count: "+layers+" at: "+x+" "+y);				
	            return height;
			}
	        short temph = -30000;
	        while(layers > 0)
	        {	            
	            height = geo.getShort(index);
	            height = (short)(height&0x0fff0);
				height = (short)(height >> 1); //height / 2				
	            if ((z-temph)*(z-temph) > (z-height)*(z-height))
	                temph = height;            
	            layers--;
	            index += 2;
	        }	        
		 return temph;
	    }
	}
	private static boolean NcanMoveNext(int x, int y, int z, int tx, int ty, int tz)
	{
	    short region = getRegionOffset(x,y);
	    int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX = 0;		
		int cellY = 0;		
	    short NSWE = -1;	    
	    
		int index = 0;
		if(Geodata_index.get(region) == null) index = ((blockX << 8) + blockY)*3;		
		else index = Geodata_index.get(region).get(((blockX << 8))+(blockY));
		ByteBuffer geo = Geodata.get(region);
		if(geo == null)
		{
			_log.warning("Geo Region - Region Offset: "+region+" dosnt exist!!");
			return false;
		}
		byte type = geo.get(index);
		index++;
	    if(type == 0)
	    {
	        return true;
	    }
	    else if(type == 1)
	    {
	    	cellX = getCell(x);
			cellY = getCell(y);
	        index += ((cellX << 3) + cellY) << 1;
	        short height = geo.getShort(index);
			NSWE = (short)(height&0x0F);		
	    }
	    else
	    {		 
	    	cellX = getCell(x);
			cellY = getCell(y);
	        int offset = (cellX << 3) + cellY;
	        while(offset > 0)
	        {
	            byte lc = geo.get(index);		                 
	            index += (lc << 1) + 1;
	            offset--;
	        }
	        byte layers = geo.get(index);
	        index++;
	        short height=-1;		 
	        if(layers <= 0 || layers > 60)		 
	        {		     
                _log.warning("Geo Engine: - invalid layers count: "+layers+" at: "+x+" "+y);
	            return false;		 
	        }		
	        short tempz = -30000;
	        short tempz2 = -30000;
	        while(layers > 0)
	        {	            
	            height = geo.getShort(index);
	            height = (short)(height&0x0fff0);
				height = (short)(height >> 1); //height / 2

	            if ((z-tempz)*(z-tempz) > (z-height)*(z-height))
	            {
	                tempz = height;
	                NSWE = geo.getShort(index);
	                NSWE = (short)(NSWE&0x0F);                           
	            }
	            if ((tz-tempz2)*(tz-tempz2) > (tz-height)*(tz-height))
	            {
	                tempz2 = height;                                          
	            }                            
	            layers--;
	            index += 2;
	        }
	        if(tempz != tempz2)		 
	        {
	        	return false;		 
	        }	 
	    }                
	    //Check NSWE
	    if(NSWE == 15)
	       return true;
	    if(tx > x)//E
	    {
	    	if ((NSWE & E) == 0)
	            return false;
	    }
	    else if (tx < x)//W
	    {
	    	if ((NSWE & W) == 0)
	            return false;
	    }
	    if (ty > y)//S
	    {
	    	if ((NSWE & S) == 0)
	            return false;
	    }
	    else if (ty < y)//N
	    {
	    	if ((NSWE & N) == 0)
	            return false;
	    }
	    return true;	
	}
	private short NgetNSWE(int x, int y, int z)
	{
		short region = getRegionOffset(x,y);
	    int blockX = getBlock(x);
		int blockY = getBlock(y);
		int cellX = 0;		
		int cellY = 0;
	    short NSWE = -1;	    

		int index = 0;
		if(Geodata_index.get(region) == null) index = ((blockX << 8) + blockY)*3;		
		else index = Geodata_index.get(region).get(((blockX << 8))+(blockY));
		ByteBuffer geo = Geodata.get(region);
		if(geo == null)
		{
			_log.warning("Geo Region - Region Offset: "+region+" dosnt exist!!");
			return 0;
		}
		byte type = geo.get(index);
		index++;
	    if(type == 0)
	    {
	        return 15;
	    }
	    else if(type == 1)
	    {
	    	cellX = getCell(x);
			cellY = getCell(y);
	        index += ((cellX << 3) + cellY) << 1;
	        short height = geo.getShort(index);
			NSWE = (short)(height&0x0F);		
	    }
	    else
	    {		 
	    	cellX = getCell(x);
			cellY = getCell(y);
	        int offset = (cellX << 3) + cellY;
	        while(offset > 0)
	        {
	            short lc = geo.getShort(index);		                 
	            index += (lc << 1) + 1;
	            offset--;
	        }
	        byte layers = geo.get(index);
	        index++;
	        short height=-1;		 
	        if(layers <= 0 || layers > 60)		 
	        {		     
	        	_log.warning("Geo Engine: - invalid layers count: "+layers+" at: "+x+" "+y);
	            return 0;		 
	        }		
	        short tempz = -30000;	        
	        while(layers > 0)
	        {	            
	            height = geo.getShort(index);
	            height = (short)(height&0x0fff0);
				height = (short)(height >> 1); //height / 2	                      

	            if ((z-tempz)*(z-tempz) > (z-height)*(z-height))
	            {
	                tempz = height;
	                NSWE = geo.get(index);
	                NSWE = (short)(NSWE&0x0F);                           
	            }	                                       
	            layers--;
	            index += 2;
	        }	        	 
	    }
	    return NSWE;
	}	
}
