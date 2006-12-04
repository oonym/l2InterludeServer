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
    short cropId;
    int canBuy;
    int rewardType;
    
    public CropProcure(short id, int amount, int type)
    {
        cropId = id;
        canBuy = amount;
        rewardType = type;
    }
    
    public int getReward(){return rewardType;}
    public short getId(){return cropId;}
    public int getAmount(){return canBuy;}
}
