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
   private int _seedId;
   private int _canProduce;
   
   public SeedProduction(int id,int amount)
   {
       _seedId = id;
       _canProduce = amount;
   }
   
   public int getSeedId(){return _seedId;}
   public int getCanProduce(){return _canProduce;}
}
