/**
 * 
 */
package net.sf.l2j.gameserver.model;

/**
 * @author zabbix
 *
 */
public class SeedProduction
{
   int seedId;
   int canProduce;
   
   public SeedProduction(int id,int amount)
   {
       seedId = id;
       canProduce = amount;
   }
   
   public int getSeedId(){return seedId;}
   public int getCanProduce(){return canProduce;}
}
