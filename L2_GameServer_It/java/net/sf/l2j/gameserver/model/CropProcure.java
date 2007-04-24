/**
 * 
 */
package net.sf.l2j.gameserver.model;

/**
 * @author zabbix
 *
 */
public class CropProcure
{
    int cropId;
    int canBuy;
    int rewardType;
    
    public CropProcure(int id, int amount, int type)
    {
        cropId = id;
        canBuy = amount;
        rewardType = type;
    }
    
    public int getReward(){return rewardType;}
    public int getId(){return cropId;}
    public int getAmount(){return canBuy;}
}
