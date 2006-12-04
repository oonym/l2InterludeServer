package net.sf.l2j.gameserver.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;

/**
 * Service class for manor
 * 2be rewritten ;)
 */

public class L2Manor
{
    private static Logger _log = Logger.getLogger(L2Manor.class.getName());
    private static final L2Manor _instance = new L2Manor();

    private static Map<Integer, SeedData> _seeds;
    private static boolean initialized = false;
    
    private L2Manor()
    {
        _seeds = new FastMap<Integer,SeedData>();
        parseData();
        
        //Seeds data, moved to datapack...
/*
        _seeds.put(5016,new SeedData(10,5073)); // Seed: Dark Coda, Type1: stem,100 Type2: braided hemp,500 Manufactured: Gludio,Oren
        _seeds.put(5017,new SeedData(13,5068)); // Seed: Red Coda,  Type1: Type2: Manufactured:
        _seeds.put(5018,new SeedData(16,5065)); // Seed: Chilly Coda,  Type1: Type2: Manufactured:
        _seeds.put(5019,new SeedData(19,5067)); // Seed: Blue Coda,  Type1: Type2: Manufactured:
        _seeds.put(5020,new SeedData(22,5069)); // Seed: Golden Coda,  Type1: Type2: Manufactured:
        _seeds.put(5021,new SeedData(25,5071)); // Seed: Lute Coda,  Type1: Type2: Manufactured:
        _seeds.put(5022,new SeedData(28,5070)); // Seed: Desert Coda,  Type1: Type2: Manufactured:
        _seeds.put(5028,new SeedData(31,5078)); // Seed: Red Cobol,  Type1: Type2: Manufactured:
        _seeds.put(5034,new SeedData(34,5075)); // Seed: Chilly Cobol,  Type1: Type2: Manufactured:
        _seeds.put(5023,new SeedData(37,5077)); // Seed: Blue Cobol,  Type1: Type2: Manufactured:
        _seeds.put(5036,new SeedData(40,5082)); // Seed: Thorn Cobol,  Type1: Type2: Manufactured:
        _seeds.put(5037,new SeedData(43,5079)); // Seed: Golden Cobol,  Type1: Type2: Manufactured:
        _seeds.put(5038,new SeedData(46,5084)); // Seed: Great Cobol,  Type1: Type2: Manufactured:
        _seeds.put(5056,new SeedData(49,5088)); // Seed: Red Codran,  Type1: Type2: Manufactured:
        _seeds.put(5049,new SeedData(50,5091)); // Seed: Sea Codran,  Type1: Type2: Manufactured:
        _seeds.put(5057,new SeedData(52,5085)); // Seed: Chilly Codran,  Type1: Type2: Manufactured:
        _seeds.put(5058,new SeedData(55,5087)); // Seed: Blue Codran,  Type1: Type2: Manufactured:
        _seeds.put(5059,new SeedData(58,5092)); // Seed: Twin Codran,  Type1: Type2: Manufactured:
        _seeds.put(5060,new SeedData(61,5094)); // Seed: Great Codran,  Type1: Type2: Manufactured:
        _seeds.put(5061,new SeedData(64,5090)); // Seed: Desert Codran,  Type1: Type2: Manufactured:
*/ 
    }
    
    public static L2Manor getInstance()
    {
        return _instance;
    }
    
    public int getSeedLevel(int seedId)
    {
        SeedData seed = _seeds.get(seedId);
        
        if(seed != null)
            return seed.level;
        return -1;
    }
    
    public short getCropType(int seedId)
    {
        SeedData seed = _seeds.get(seedId);

        if(seed != null)
            return seed.crop;
        return -1;
    }
    
    public synchronized int getRewardItem(int cropId,int type)
    {
        for(SeedData seed : _seeds.values())
        {
            if(seed.crop == cropId)
            {
                if(type == 1)
                {
                    return seed.type1;
                }
                else if(type == 2)
                {
                    return seed.type2;
                }
                else if(type == 0)
                {
                    return 0;
                }
            }
        }
        
        return -1;        
    }
    
    // syncronize to avoid exceptions while changing seeds data
    public synchronized int getRewardAmount(int cropId,int type)
    {
        for(SeedData seed : _seeds.values())
        {
            if(seed.crop == cropId)
            {
                if(type == 1)
                {
                    return seed.type1amount;
                }
                else if(type == 2)
                {
                    return seed.type2amount;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Return all crops which can be purchased by given castle
     * @param castleId
     * @return
     */
    public List<Short> getCropForCastle(int castleId)
    {
        List<Short> crops =  new FastList<Short>();
        
        for(SeedData seed : _seeds.values())
        {
            switch(castleId)
            {
                case 1:     // Gludio Castle
                    if(seed.GL == 1)
                        crops.add(seed.crop);
                    break;
                case 2:     // Dion castle
                    if(seed.DI == 1)
                        crops.add(seed.crop);
                    break;
                case 3:     // Giran
                    if(seed.GI == 1)
                        crops.add(seed.crop);
                    break;
                case 4:     // Oren
                    if(seed.OR == 1)
                        crops.add(seed.crop);
                    break;
                case 5:     // Aden Castle
                    if(seed.AD == 1)
                        crops.add(seed.crop);
                    break;
                default:
                    _log.warning("[L2Manor::getCropForCastle] invalid castle index? "+castleId);
            }
        }
        
        return crops;
    }
    
    private class SeedData
    {
        public SeedData(int pLevel,short pCrop)
        {
            this.level = pLevel;
            this.crop = pCrop;
        }
        
        public void setData(int pId, int t1, int t2, int t1a, int t2a, int gl, int di, int gi, int or, int ad, int in)
        {
            this.id = pId;
            type1 = t1;
            type2 = t2;
            type1amount = t1a;
            type2amount = t2a;
            GL = gl;
            DI = di;
            GI = gi;
            OR = or;
            AD = ad;
            IN = in;
        }
        
        public int id;
        public int level;          // seed level
        public short crop;           // crop type
        public int type1;
        public int type2;
        public int type1amount;
        public int type2amount;
        public int GL;             // Gludio Castle
        public int DI;             // Dion Castle
        public int GI;             // Giran Castle
        public int OR;             // Oren Castle
        public int AD;             // Aden Castle
        public int IN;             // Innadril Castle
    }
    
    
    private void parseData() {
        LineNumberReader lnr = null;
        try
        {
            File seedData = new File(Config.DATAPACK_ROOT, "data/seeds.csv");
            lnr = new LineNumberReader(new BufferedReader(new FileReader(seedData)));

            String line = null;
            while ((line = lnr.readLine()) != null)
            {
                if (line.trim().length() == 0 || line.startsWith("#"))
                {
                    continue;
                }
                SeedData seed = parseList(line);
                _seeds.put(seed.id, seed);             
            }

            initialized = true;
            _log.config("L2Manor: Loaded " + _seeds.size() + " seeds");
        }
        catch (FileNotFoundException e)
        {
            initialized = false;
            _log.warning("seeds.csv is missing in data folder");
        }
        catch (Exception e)
        {
            initialized = false;
            _log.warning("error while loading seeds: " + e);
        }
        finally
        {
            try
            {
                lnr.close();
            }
            catch (Exception e1)
            {
            }
        }
    }

    private SeedData parseList(String line)
    {
        
        StringTokenizer st = new StringTokenizer(line, ";");

        int seedId = Integer.parseInt(st.nextToken());  // seed id
        int level = Integer.parseInt(st.nextToken());   // seed level
        short cropId = Short.parseShort(st.nextToken());  // crop id
        int type1R = Integer.parseInt(st.nextToken());  // type I reward
        int type1A = Integer.parseInt(st.nextToken());  // type I reward amount
        int type2R = Integer.parseInt(st.nextToken());  // type II reward
        int type2A = Integer.parseInt(st.nextToken());  // type II reward amount
        int GL = Integer.parseInt(st.nextToken());      // can be produced/sold in Gludio castle
        int DI = Integer.parseInt(st.nextToken());      // can be produced/sold in Dion castle
        int GI = Integer.parseInt(st.nextToken());      // can be produced/sold in Giran castle
        int OR = Integer.parseInt(st.nextToken());      // can be produced/sold in Oren castle
        int AD = Integer.parseInt(st.nextToken());      // can be produced/sold in Aden castle
        int IN = Integer.parseInt(st.nextToken());      // can be produced/sold in Innadril castle

        SeedData seed = new SeedData(level,cropId);
        seed.setData(seedId,type1R,type2R,type1A,type2A,GL,DI,GI,OR,AD,IN);
        
        return seed;
    }
    
    public boolean isInitialized()
    {
        return initialized;
    }
}
